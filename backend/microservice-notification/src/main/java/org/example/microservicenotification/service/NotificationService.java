package org.example.microservicenotification.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.microservicenotification.entity.Notification;
import org.example.microservicenotification.entity.NotificationChannel;
import org.example.microservicenotification.entity.NotificationStatus;
import org.example.microservicenotification.entity.NotificationType;
import org.example.microservicenotification.feignClient.ReservationClient;
import org.example.microservicenotification.feignClient.UserClient;
import org.example.microservicenotification.mapper.NotificationMapper;
import org.example.microservicenotification.repository.NotificationRepository;
import org.example.microservicenotification.service.dto.ReservationBasicInfoDTO;
import org.example.microservicenotification.service.dto.UserBasicInfoDTO;
import org.example.microservicenotification.service.dto.UserNotificationPreferencesDTO;
import org.example.microservicenotification.service.dto.request.NotificationRequestDTO;
import org.example.microservicenotification.service.dto.request.NotificationUpdateRequestDTO;
import org.example.microservicenotification.service.dto.response.NotificationResponseDTO;
import org.example.microservicenotification.service.dto.NotificationStatsDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationSenderService notificationSenderService;
  private final UserClient userClient;
  private final ReservationClient reservationClient;

  private final Map<Long, LocalDateTime> lastUserNotificationCache = new ConcurrentHashMap<>();
  private static final int MAX_RETRIES = 3;
  private static final int NOTIFICATION_COOLDOWN_MINUTES = 5;

  @Transactional(readOnly = true)
  public List<NotificationResponseDTO> findAll() {
    return notificationRepository.findAll()
            .stream()
            .map(NotificationMapper::toResponse)
            .toList();
  }

  @Transactional(readOnly = true)
  public NotificationResponseDTO findById(Long id) {
    return notificationRepository.findById(id)
            .map(NotificationMapper::toResponse)
            .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada con id: " + id));
  }

  @Transactional
  public NotificationResponseDTO create(NotificationRequestDTO request) {
    validateNotificationRequest(request);

    // Verificar cooldown para evitar spam
    if (isInCooldown(request.getUserId(), request.getType())) {
      log.warn("Notificación en cooldown para usuario: {}, tipo: {}",
              request.getUserId(), request.getType());
      throw new IllegalStateException("Por favor espera antes de enviar otra notificación similar");
    }

    // Si no se proporcionó email/teléfono pero sí userId, obtener del UserClient
    if (request.getUserId() != null) {
      enrichNotificationData(request);
    }

    Notification notification = NotificationMapper.toEntity(request);
    notification.setStatus(NotificationStatus.PENDING);
    notification.setCreatedAt(LocalDateTime.now());

    Notification savedNotification = notificationRepository.save(notification);

    // Verificar preferencias del usuario antes de enviar
    if (shouldSendNotification(savedNotification)) {
      notificationSenderService.sendNotificationAsync(savedNotification);
    } else {
      notification.setStatus(NotificationStatus.CANCELLED);
      notification.setErrorMessage("Cancelada por preferencias del usuario");
      notificationRepository.save(notification);
      log.info("Notificación cancelada por preferencias del usuario: {}", savedNotification.getId());
    }

    // Actualizar caché de cooldown
    lastUserNotificationCache.put(request.getUserId(), LocalDateTime.now());

    log.info("Notificación creada - ID: {}, Usuario: {}, Tipo: {}",
            savedNotification.getId(), savedNotification.getUserId(), savedNotification.getType());

    return NotificationMapper.toResponse(savedNotification);
  }

  @Transactional
  public NotificationResponseDTO createForUser(Long userId, NotificationType type, String title, String message) {
    try {
      // Obtener información del usuario
      UserBasicInfoDTO userInfo = userClient.getUserBasicInfo(userId);
      UserNotificationPreferencesDTO preferences = userClient.getNotificationPreferences(userId);

      // Determinar el mejor canal según preferencias del usuario
      NotificationChannel channel = determineBestChannel(preferences, type);

      // Crear notificación
      NotificationRequestDTO request = NotificationRequestDTO.builder()
              .userId(userId)
              .type(type)
              .title(title)
              .message(message)
              .channel(channel)
              .recipientEmail(userInfo.getEmail())
              .recipientPhone(userInfo.getPhone())
              .build();

      return create(request);

    } catch (Exception e) {
      log.error("Error al crear notificación para usuario {}: {}", userId, e.getMessage());
      throw new RuntimeException("Error al crear notificación: " + e.getMessage());
    }
  }

  @Transactional
  public void sendBulkNotification(List<Long> userIds, NotificationType type, String title, String message) {
    List<NotificationResponseDTO> results = new ArrayList<>();

    for (Long userId : userIds) {
      try {
        NotificationResponseDTO notification = createForUser(userId, type, title, message);
        results.add(notification);
        log.info("Notificación enviada a usuario {}: {}", userId, notification.getId());
      } catch (Exception e) {
        log.error("Error al enviar notificación a usuario {}: {}", userId, e.getMessage());
        // Continuar con el siguiente usuario
      }
    }

    log.info("Notificación masiva completada. Enviadas: {}, Total usuarios: {}",
            results.size(), userIds.size());
  }

  @Transactional
  public NotificationResponseDTO updateStatus(Long id, NotificationUpdateRequestDTO request) {
    Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada con id: " + id));

    if (request.getStatus() != null) {
      notification.setStatus(request.getStatus());

      if (request.getStatus() == NotificationStatus.SENT ||
              request.getStatus() == NotificationStatus.DELIVERED) {
        notification.setSentAt(LocalDateTime.now());
      }
    }

    if (request.getErrorMessage() != null) {
      notification.setErrorMessage(request.getErrorMessage());
    }

    if (request.getRetryCount() != null) {
      notification.setRetryCount(request.getRetryCount());
    }

    notificationRepository.save(notification);

    log.info("Estado de notificación actualizado - ID: {}, Nuevo estado: {}",
            id, request.getStatus());

    return NotificationMapper.toResponse(notification);
  }

  @Transactional
  public void delete(Long id) {
    if (!notificationRepository.existsById(id)) {
      throw new EntityNotFoundException("Notificación no encontrada con id: " + id);
    }
    notificationRepository.deleteById(id);
    log.info("Notificación eliminada - ID: {}", id);
  }

  @Transactional(readOnly = true)
  public List<NotificationResponseDTO> findByUserId(Long userId) {
    // Primero verificar que el usuario existe
    try {
      boolean userExists = userClient.userExists(userId);
      if (!userExists) {
        throw new EntityNotFoundException("Usuario no encontrado con id: " + userId);
      }
    } catch (Exception e) {
      log.warn("No se pudo verificar usuario {}: {}", userId, e.getMessage());
    }

    return notificationRepository.findByUserId(userId)
            .stream()
            .map(NotificationMapper::toResponse)
            .toList();
  }

  @Transactional(readOnly = true)
  public List<NotificationResponseDTO> findByReservationId(Long reservationId) {
    return notificationRepository.findByReservationId(reservationId)
            .stream()
            .map(NotificationMapper::toResponse)
            .toList();
  }

  @Transactional(readOnly = true)
  public List<NotificationResponseDTO> findByStatus(String status) {
    try {
      NotificationStatus notificationStatus = NotificationStatus.valueOf(status.toUpperCase());
      return notificationRepository.findByStatus(notificationStatus)
              .stream()
              .map(NotificationMapper::toResponse)
              .toList();
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Estado de notificación inválido: " + status);
    }
  }

  @Transactional(readOnly = true)
  public NotificationStatsDTO getStats() {
    return NotificationStatsDTO.builder()
            .total(notificationRepository.count())
            .sent(notificationRepository.countByStatus(NotificationStatus.SENT))
            .pending(notificationRepository.countByStatus(NotificationStatus.PENDING))
            .failed(notificationRepository.countByStatus(NotificationStatus.FAILED))
            .delivered(notificationRepository.countByStatus(NotificationStatus.DELIVERED))
            .build();
  }

  @Transactional(readOnly = true)
  public NotificationStatsDTO getUserStats(Long userId) {
    // Verificar que el usuario existe
    try {
      boolean userExists = userClient.userExists(userId);
      if (!userExists) {
        throw new EntityNotFoundException("Usuario no encontrado con id: " + userId);
      }
    } catch (Exception e) {
      log.warn("No se pudo verificar usuario {}: {}", userId, e.getMessage());
    }

    return NotificationStatsDTO.builder()
            .total(notificationRepository.countByUserIdAndStatus(userId, null))
            .sent(notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.SENT))
            .pending(notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.PENDING))
            .failed(notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.FAILED))
            .delivered(notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.DELIVERED))
            .build();
  }

  @Transactional
  public NotificationResponseDTO retryNotification(Long id) {
    Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada con id: " + id));

    if (notification.getStatus() != NotificationStatus.FAILED &&
            notification.getStatus() != NotificationStatus.PENDING) {
      throw new IllegalStateException("Solo se pueden reintentar notificaciones falladas o pendientes");
    }

    if (notification.getRetryCount() >= MAX_RETRIES) {
      throw new IllegalStateException("Máximo de reintentos alcanzado para la notificación: " + id);
    }

    notification.setStatus(NotificationStatus.PENDING);
    notification.setRetryCount(notification.getRetryCount() + 1);
    notificationRepository.save(notification);

    // Reintentar envío
    notificationSenderService.sendNotificationAsync(notification);

    log.info("Reintentando notificación - ID: {}, Intento: {}",
            id, notification.getRetryCount());

    return NotificationMapper.toResponse(notification);
  }

  @Scheduled(fixedDelay = 30000) // Cada 30 segundos
  @Transactional
  public void retryFailedNotifications() {
    LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(5);

    List<Notification> failedNotifications = notificationRepository
            .findNotificationsForRetry(MAX_RETRIES, cutoffTime);

    if (!failedNotifications.isEmpty()) {
      log.info("Reintentando {} notificaciones falladas", failedNotifications.size());

      for (Notification notification : failedNotifications) {
        notification.setStatus(NotificationStatus.PENDING);
        notification.setRetryCount(notification.getRetryCount() + 1);
        notificationRepository.save(notification);

        notificationSenderService.sendNotificationAsync(notification);
      }
    }
  }

  @Transactional
  public NotificationResponseDTO sendReservationConfirmation(Long reservationId) {
    try {
      // 1. Obtener información de la reserva
      ReservationBasicInfoDTO reservationInfo = reservationClient.getReservationBasicInfo(reservationId);

      if (reservationInfo == null) {
        throw new EntityNotFoundException("Reserva no encontrada con id: " + reservationId);
      }

      // 2. Extraer userId del DTO de reserva
      Long userId = reservationInfo.getUserId();

      if (userId == null) {
        throw new IllegalArgumentException("La reserva no tiene un usuario asociado");
      }

      // 3. Verificar que el usuario existe
      boolean userExists = userClient.userExists(userId);
      if (!userExists) {
        throw new EntityNotFoundException("Usuario no encontrado con id: " + userId);
      }

      // 4. Obtener información del usuario
      UserBasicInfoDTO userInfo = userClient.getUserBasicInfo(userId);
      if (userInfo == null) {
        throw new RuntimeException("No se pudo obtener información del usuario: " + userId);
      }

      // 5. Obtener preferencias de notificación del usuario
      UserNotificationPreferencesDTO preferences = userClient.getNotificationPreferences(userId);
      if (preferences == null) {
        preferences = getDefaultPreferences(); // Usar preferencias por defecto
      }

      // 6. Determinar el mejor canal según preferencias
      NotificationChannel bestChannel = determineBestChannel(preferences, NotificationType.RESERVATION_CONFIRMED);

      // 7. Construir mensaje personalizado
      String userName = (userInfo.getFirstName() != null && !userInfo.getFirstName().isBlank()) ?
              userInfo.getFirstName() : "Usuario";

      String message = String.format("¡Hola! Tu reserva #%d ha sido confirmada exitosamente. " +
                      "Detalles: %s - %s",
              userName,
              reservationId,
              reservationInfo.getStartTime() != null ?
                      reservationInfo.getStartTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "",
              reservationInfo.getEndTime() != null ?
                      reservationInfo.getEndTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""
      );

      // 8. Crear notificación
      NotificationRequestDTO request = NotificationRequestDTO.builder()
              .userId(userId)
              .reservationId(reservationId)
              .type(NotificationType.RESERVATION_CONFIRMED)
              .title("Reserva Confirmada #" + reservationId)
              .message(message)
              .channel(bestChannel)
              .recipientEmail(userInfo.getEmail())
              .recipientPhone(userInfo.getPhone())
              .build();

      // 9. Crear y enviar notificación
      NotificationResponseDTO notification = create(request);

      log.info("Notificación de confirmación de reserva enviada - Reserva: {}, Usuario: {}, Canal: {}",
              reservationId, userId, bestChannel);

      return notification;

    } catch (EntityNotFoundException e) {
      log.error("Recurso no encontrado para notificación de reserva {}: {}", reservationId, e.getMessage());
      throw e; // Relanzar para que el controller lo maneje
    } catch (IllegalArgumentException e) {
      log.error("Datos inválidos para notificación de reserva {}: {}", reservationId, e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Error al enviar confirmación de reserva {}: {}", reservationId, e.getMessage(), e);
      throw new RuntimeException("Error al enviar notificación de confirmación: " + e.getMessage());
    }
  }

  @Transactional
  public NotificationResponseDTO sendPaymentNotification(Long userId, Long paymentId, boolean success) {
    try {
      UserBasicInfoDTO userInfo = userClient.getUserBasicInfo(userId);
      UserNotificationPreferencesDTO preferences = userClient.getNotificationPreferences(userId);

      NotificationType type = success ? NotificationType.PAYMENT_SUCCESS : NotificationType.PAYMENT_FAILED;

      NotificationRequestDTO request = NotificationRequestDTO.builder()
              .userId(userId)
              .type(type)
              .title(success ? "Pago Exitoso" : "Pago Fallido")
              .message(success ?
                      String.format("¡Hola! Tu pago #%d se procesó correctamente. ¡Gracias!",
                              userInfo.getFirstName(), paymentId) :
                      String.format("¡Hola! Hubo un problema con tu pago #%d. Por favor, inténtalo nuevamente.",
                              userInfo.getFirstName(), paymentId))
              .channel(determineBestChannel(preferences, type))
              .recipientEmail(userInfo.getEmail())
              .recipientPhone(userInfo.getPhone())
              .build();

      return create(request);

    } catch (Exception e) {
      log.error("Error al enviar notificación de pago para usuario {}: {}", userId, e.getMessage());
      throw new RuntimeException("Error al enviar notificación de pago: " + e.getMessage());
    }
  }

  @Transactional
  public void sendWelcomeNotification(Long userId) {
    try {
      UserBasicInfoDTO userInfo = userClient.getUserBasicInfo(userId);

      NotificationRequestDTO request = NotificationRequestDTO.builder()
              .userId(userId)
              .type(NotificationType.WELCOME_MESSAGE)
              .title("¡Bienvenido a nuestro club!")
              .message(String.format("¡Hola! Te damos la bienvenida a nuestro sistema de reservas. " +
                      "Estamos encantados de tenerte con nosotros.", userInfo.getFirstName()))
              .channel(NotificationChannel.EMAIL) // Welcome por email generalmente
              .recipientEmail(userInfo.getEmail())
              .build();

      create(request);
      log.info("Notificación de bienvenida enviada a usuario: {}", userId);

    } catch (Exception e) {
      log.error("Error al enviar notificación de bienvenida a usuario {}: {}", userId, e.getMessage());
    }
  }

  @Transactional(readOnly = true)
  public List<NotificationResponseDTO> getUnreadNotifications(Long userId) {
    return notificationRepository.findByUserIdAndStatus(userId, NotificationStatus.SENT)
            .stream()
            .filter(notification -> notification.getSentAt() != null &&
                    notification.getStatus() != NotificationStatus.READ)
            .map(NotificationMapper::toResponse)
            .toList();
  }

  @Transactional
  public void markAsRead(Long notificationId, Long userId) {
    Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada"));

    // Verificar que la notificación pertenece al usuario
    if (!notification.getUserId().equals(userId)) {
      throw new IllegalStateException("La notificación no pertenece al usuario");
    }

    notification.setStatus(NotificationStatus.READ);
    notificationRepository.save(notification);

    log.info("Notificación marcada como leída - ID: {}, Usuario: {}", notificationId, userId);
  }

  @Transactional
  public void markAllAsRead(Long userId) {
    List<Notification> unreadNotifications = notificationRepository
            .findByUserIdAndStatus(userId, NotificationStatus.SENT);

    unreadNotifications.forEach(notification -> {
      notification.setStatus(NotificationStatus.READ);
    });

    notificationRepository.saveAll(unreadNotifications);

    log.info("Todas las notificaciones marcadas como leídas para usuario: {}", userId);
  }

  @Transactional
  public NotificationResponseDTO sendReservationNotification(Long reservationId, NotificationType type) {
    try {
      ReservationBasicInfoDTO reservationInfo = reservationClient.getReservationBasicInfo(reservationId);
      Long userId = reservationInfo.getUserId();
      UserBasicInfoDTO userInfo = userClient.getUserBasicInfo(userId);

      // Determinar título y mensaje según el tipo
      String title;
      String message;
      String userName = (userInfo.getFirstName() != null && !userInfo.getFirstName().isBlank()) ?
              userInfo.getFirstName() : "Usuario";

      switch (type) {
        case RESERVATION_CREATED:
          title = "Reserva Creada #" + reservationId;
          message = String.format("¡Hola %s! Tu reserva #%d ha sido creada exitosamente y está pendiente de confirmación.",
                  userName, reservationId);
          break;

        case RESERVATION_CONFIRMED:
          title = "Reserva Confirmada #" + reservationId;
          message = String.format("¡Hola! Tu reserva #%d ha sido confirmada. ¡Prepárate para disfrutar!",
                  userName, reservationId);
          break;

        case RESERVATION_CANCELLED:
          title = "Reserva Cancelada #" + reservationId;
          message = String.format("¡Hola! Tu reserva #%d ha sido cancelada. Si tienes dudas, contáctanos.",
                  userName, reservationId);
          break;

        case RESERVATION_REMINDER:
          title = "Recordatorio de Reserva #" + reservationId;
          message = String.format("¡Hola! Recuerda que tienes una reserva #%d mañana. No olvides asistir.",
                  userName, reservationId);
          break;

        default:
          throw new IllegalArgumentException("Tipo de notificación de reserva no soportado: " + type);
      }

      // Obtener preferencias
      UserNotificationPreferencesDTO preferences = userClient.getNotificationPreferences(userId);
      NotificationChannel channel = determineBestChannel(preferences, type);

      // Crear notificación
      NotificationRequestDTO request = NotificationRequestDTO.builder()
              .userId(userId)
              .reservationId(reservationId)
              .type(type)
              .title(title)
              .message(message)
              .channel(channel)
              .recipientEmail(userInfo.getEmail())
              .recipientPhone(userInfo.getPhone())
              .build();

      return create(request);

    } catch (Exception e) {
      log.error("Error al enviar notificación de reserva {} (tipo: {}): {}",
              reservationId, type, e.getMessage());
      throw new RuntimeException("Error al enviar notificación de reserva: " + e.getMessage());
    }
  }

  // ===== MÉTODOS PRIVADOS =====

  private void validateNotificationRequest(NotificationRequestDTO request) {
    if (request.getUserId() == null && request.getReservationId() == null) {
      throw new IllegalArgumentException("Se requiere userId o reservationId");
    }

    if (request.getType() == null) {
      throw new IllegalArgumentException("El tipo de notificación es requerido");
    }

    if (request.getTitle() == null || request.getTitle().isBlank()) {
      throw new IllegalArgumentException("El título es requerido");
    }

    if (request.getChannel() == null) {
      throw new IllegalArgumentException("El canal de envío es requerido");
    }
  }

  private void enrichNotificationData(NotificationRequestDTO request) {
    try {
      if (request.getUserId() != null) {
        UserBasicInfoDTO userInfo = userClient.getUserBasicInfo(request.getUserId());

        // Si no se proporcionó email pero el usuario tiene, usarlo
        if ((request.getRecipientEmail() == null || request.getRecipientEmail().isBlank())
                && userInfo.getEmail() != null && !userInfo.getEmail().isBlank()) {
          request.setRecipientEmail(userInfo.getEmail());
        }

        // Si no se proporcionó teléfono pero el usuario tiene, usarlo
        if ((request.getRecipientPhone() == null || request.getRecipientPhone().isBlank())
                && userInfo.getPhone() != null && !userInfo.getPhone().isBlank()) {
          request.setRecipientPhone(userInfo.getPhone());
        }
      }
    } catch (Exception e) {
      log.warn("No se pudo enriquecer datos de notificación para usuario {}: {}",
              request.getUserId(), e.getMessage());
    }
  }

  private boolean shouldSendNotification(Notification notification) {
    if (notification.getUserId() == null) {
      return true; // Si no hay userId, enviar de todos modos
    }

    try {
      UserNotificationPreferencesDTO preferences = userClient
              .getNotificationPreferences(notification.getUserId());

      // Verificar si el usuario tiene habilitado este canal
      boolean channelEnabled = switch (notification.getChannel()) {
        case EMAIL -> preferences.isEmailEnabled();
        case SMS -> preferences.isSmsEnabled();
        case PUSH -> preferences.isPushEnabled();
        case WHATSAPP -> preferences.isWhatsappEnabled();
        case IN_APP -> true; // In-app siempre disponible
        default -> false;
      };

      // Verificar si el usuario quiere este tipo de notificación
      boolean typeEnabled = switch (notification.getType()) {
        case RESERVATION_CONFIRMED, RESERVATION_CREATED, RESERVATION_CANCELLED, RESERVATION_REMINDER ->
                preferences.isReservationNotifications();
        case PAYMENT_SUCCESS, PAYMENT_FAILED, PAYMENT_PENDING, PAYMENT_REFUND ->
                preferences.isPaymentNotifications();
        case PROMOTIONAL -> preferences.isPromotionalNotifications();
        case SYSTEM_ALERT, WELCOME_MESSAGE -> preferences.isSystemNotifications();
        default -> true;
      };

      return channelEnabled && typeEnabled;

    } catch (Exception e) {
      log.warn("No se pudieron obtener preferencias para usuario {}: {}",
              notification.getUserId(), e.getMessage());
      return true; // En caso de error, enviar de todos modos
    }
  }

  private NotificationChannel determineBestChannel(UserNotificationPreferencesDTO preferences,
                                                   NotificationType type) {
    // Prioridad de canales según el tipo de notificación
    if (type == NotificationType.RESERVATION_CONFIRMED || type == NotificationType.PAYMENT_SUCCESS) {
      if (preferences.isEmailEnabled()) return NotificationChannel.EMAIL;
      if (preferences.isSmsEnabled()) return NotificationChannel.SMS;
      if (preferences.isWhatsappEnabled()) return NotificationChannel.WHATSAPP;
      if (preferences.isPushEnabled()) return NotificationChannel.PUSH;
    } else if (type == NotificationType.RESERVATION_REMINDER) {
      if (preferences.isSmsEnabled()) return NotificationChannel.SMS;
      if (preferences.isWhatsappEnabled()) return NotificationChannel.WHATSAPP;
      if (preferences.isPushEnabled()) return NotificationChannel.PUSH;
      if (preferences.isEmailEnabled()) return NotificationChannel.EMAIL;
    }

    // Default
    return NotificationChannel.IN_APP; // Siempre disponible
  }

  private boolean isInCooldown(Long userId, NotificationType type) {
    // No aplicar cooldown para notificaciones importantes
    if (type == NotificationType.RESERVATION_CONFIRMED ||
            type == NotificationType.PAYMENT_SUCCESS ||
            type == NotificationType.SYSTEM_ALERT) {
      return false;
    }

    LocalDateTime lastNotification = lastUserNotificationCache.get(userId);
    if (lastNotification == null) {
      return false;
    }

    LocalDateTime cooldownEnd = lastNotification.plusMinutes(NOTIFICATION_COOLDOWN_MINUTES);
    return LocalDateTime.now().isBefore(cooldownEnd);
  }

  // Método auxiliar para preferencias por defecto
  private UserNotificationPreferencesDTO getDefaultPreferences() {
    return UserNotificationPreferencesDTO.builder()
            .emailEnabled(true)
            .smsEnabled(true)
            .pushEnabled(true)
            .whatsappEnabled(false)
            .reservationNotifications(true)
            .paymentNotifications(true)
            .promotionalNotifications(false)
            .systemNotifications(true)
            .build();
  }
}