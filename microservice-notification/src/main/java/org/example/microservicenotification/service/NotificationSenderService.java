// NotificationSenderService.java
package org.example.microservicenotification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.microservicenotification.entity.Notification;
import org.example.microservicenotification.entity.NotificationChannel;
import org.example.microservicenotification.entity.NotificationStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSenderService {

  private final EmailService emailService;
  private final SMSService smsService;
  private final PushNotificationService pushNotificationService;

  @Async
  @Transactional
  public void sendNotificationAsync(Notification notification) {
    try {
      sendNotification(notification);
    } catch (Exception e) {
      log.error("Error al enviar notificación {}: {}", notification.getId(), e.getMessage());

      // Actualizar estado a fallado
      notification.setStatus(NotificationStatus.FAILED);
      notification.setErrorMessage(e.getMessage());
      notification.setRetryCount(notification.getRetryCount() + 1);
    }
  }

  @Transactional
  public void sendNotification(Notification notification) {
    log.info("Enviando notificación {} por canal: {}", notification.getId(), notification.getChannel());

    boolean success = false;
    String errorMessage = null;

    try {
      switch (notification.getChannel()) {
        case EMAIL:
          success = emailService.sendEmail(
                  notification.getRecipientEmail(),
                  notification.getTitle(),
                  notification.getMessage()
          );
          break;

        case SMS:
          success = smsService.sendSMS(
                  notification.getRecipientPhone(),
                  notification.getMessage()
          );
          break;

        case PUSH:
          success = pushNotificationService.sendPush(
                  notification.getUserId(),
                  notification.getTitle(),
                  notification.getMessage()
          );
          break;

        case IN_APP:
          success = true; // Para notificaciones in-app, siempre éxito (se guarda en BD)
          break;

        default:
          throw new IllegalArgumentException("Canal no soportado: " + notification.getChannel());
      }

      if (success) {
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
        log.info("Notificación {} enviada exitosamente", notification.getId());
      } else {
        notification.setStatus(NotificationStatus.FAILED);
        errorMessage = "Error en el servicio de envío";
        log.error("Error al enviar notificación {}", notification.getId());
      }

    } catch (Exception e) {
      notification.setStatus(NotificationStatus.FAILED);
      errorMessage = e.getMessage();
      log.error("Excepción al enviar notificación {}: {}", notification.getId(), e.getMessage(), e);
    }

    notification.setErrorMessage(errorMessage);
  }
}