package org.example.microservicereservation.service;

import org.springframework.data.domain.PageRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.ReservationStatsDTO;
import org.example.microservicereservation.entity.Reservation;
import org.example.microservicereservation.entity.ReservationPaymentStatus;
import org.example.microservicereservation.entity.ReservationStatus;
import org.example.microservicereservation.feignClient.ClubClient;
import org.example.microservicereservation.feignClient.CourtClient;
import org.example.microservicereservation.feignClient.UserClient;
import org.example.microservicereservation.mapper.ReservationMapper;
import org.example.microservicereservation.repository.ReservationRepository;
import org.example.microservicereservation.service.dto.CourtDTO;
import org.example.microservicereservation.service.dto.ReservationConflictDTO;
import org.example.microservicereservation.service.dto.request.ReservationRequestDTO;
import org.example.microservicereservation.service.dto.response.ReservationResponseDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {
  private final ReservationRepository reservationRepository;
  private final UserClient userClient;
  private final CourtClient courtClient;
  private final ClubClient clubClient;

  @Transactional(readOnly = true)
  public List<ReservationResponseDTO> findAll() {
    return this.reservationRepository.findAll()
            .stream()
            .map(ReservationMapper::toResponse)
            .toList();
  }

  @Transactional(readOnly = true)
  public ReservationResponseDTO findById(Long id) {
    return ReservationMapper.toResponse(this.reservationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Reserva no encontrada con id: " + id)));
  }

  @Transactional
  public ReservationResponseDTO save(ReservationRequestDTO request) {
    validateResourcesExist(request);

    validateCourtAvailability(request);

    validateReservationDates(request);

    Reservation reservation = ReservationMapper.toEntity(request);
    reservation.setStatus(ReservationStatus.PENDING);
    reservation.setPaymentStatus(ReservationPaymentStatus.PENDING);
    reservation.setCreatedAt(LocalDateTime.now());

    Reservation savedReservation = reservationRepository.save(reservation);

    return ReservationMapper.toResponse(savedReservation);
  }

  @Transactional
  public ReservationResponseDTO update(Long id, ReservationRequestDTO request) {
    Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada con id: " + id));

    if (!reservation.getUserId().equals(request.getUserId())) {
      throw new RuntimeException("No se puede cambiar el usuario de una reserva");
    }

    if (!reservation.getCourtId().equals(request.getCourtId()) ||
            !reservation.getStartTime().equals(request.getStartTime())) {
      validateCourtAvailability(request);
    }

    validateReservationDates(request);

    reservation.setStartTime(request.getStartTime());
    reservation.setEndTime(request.getEndTime());
    reservation.setStatus(request.getStatus());
    reservation.setPaymentStatus(request.getPaymentStatus());

    Reservation updatedReservation = reservationRepository.save(reservation);
    return ReservationMapper.toResponse(updatedReservation);
  }

  @Transactional
  public Long delete(Long id) {
    if (!reservationRepository.existsById(id)) {
      throw new RuntimeException("Reserva no encontrada con id: " + id);
    }

    this.reservationRepository.deleteById(id);
    return id;
  }

  @Transactional(readOnly = true)
  public boolean existsById(Long id) {
    return reservationRepository.existsById(id);
  }

  @Transactional(readOnly = true)
  public boolean hasReservationConflict(Long courtId, LocalDateTime startTime, LocalDateTime endTime) {
    return reservationRepository.hasReservationConflict(courtId, startTime, endTime);
  }

  @Transactional(readOnly = true)
  public List<ReservationConflictDTO> getConflictingReservations(Long courtId,
                                                                 LocalDateTime startTime,
                                                                 LocalDateTime endTime) {
    if (startTime == null || endTime == null) {
      throw new IllegalArgumentException("Las fechas son obligatorias");
    }

    if (!endTime.isAfter(startTime)) {
      throw new IllegalArgumentException("La fecha de fin debe ser posterior a la de inicio");
    }

    List<Reservation> conflictingReservations = findConflictingReservations(courtId, startTime, endTime);

    return conflictingReservations.stream()
            .map(this::mapToConflictDTO)
            .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public BigDecimal getPendingAmount(Long id) {
    Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada con id: " + id));

    // Calcular el precio total de la reserva
    CourtDTO court = courtClient.findById(reservation.getCourtId());
    BigDecimal totalAmount = calculateReservationPrice(court, reservation.getStartTime(), reservation.getEndTime());

    // Obtener el total ya pagado
    BigDecimal paidAmount = getTotalPaidByReservation(id);

    // Calcular pendiente
    BigDecimal pendingAmount = totalAmount.subtract(paidAmount);

    // No retornar valores negativos
    return pendingAmount.compareTo(BigDecimal.ZERO) > 0 ? pendingAmount : BigDecimal.ZERO;
  }

  @Transactional(readOnly = true)
  public BigDecimal getTotalPaidByReservation(Long reservationId) {
    // Si tu entidad Reservation tiene paidAmount, usar eso:
    Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada"));

    // Si tienes campo paidAmount, usarlo. Si no, calcular basado en paymentStatus
    if (reservation.getPaidAmount() != null) {
      return reservation.getPaidAmount();
    }

    // Si no hay paidAmount, inferir del estado
    return reservation.getPaymentStatus() == ReservationPaymentStatus.CONFIRMED ?
            calculateReservationPriceForReservation(reservation) : BigDecimal.ZERO;
  }

  public BigDecimal calculateReservationPrice(CourtDTO court, LocalDateTime startTime, LocalDateTime endTime) {
    if (court == null) {
      log.warn("CourtDTO es null");
      return BigDecimal.ZERO;
    }

    if (court.getPricePerHour() == null) {
      log.warn("Court {} no tiene precio por hora definido", court.getId());
      return BigDecimal.ZERO;
    }

    // Validar fechas
    if (startTime == null || endTime == null) {
      throw new IllegalArgumentException("Las fechas no pueden ser null");
    }

    if (!endTime.isAfter(startTime)) {
      throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
    }

    try {
      // Calcular duración en horas con decimales
      Duration duration = Duration.between(startTime, endTime);
      long minutes = duration.toMinutes();

      // Convertir a horas decimales (ej: 90 minutos = 1.5 horas)
      BigDecimal minutesBigDecimal = BigDecimal.valueOf(minutes);
      BigDecimal sixty = new BigDecimal("60");

      // Calcular horas: minutos / 60
      BigDecimal hoursDecimal = minutesBigDecimal.divide(sixty, 2, RoundingMode.HALF_UP);

      // Obtener precio por hora
      BigDecimal pricePerHour = court.getPricePerHour();

      // Calcular precio total
      BigDecimal totalPrice = pricePerHour.multiply(hoursDecimal);

      // Redondear a 2 decimales
      return totalPrice.setScale(2, RoundingMode.HALF_UP);

    } catch (Exception e) {
      log.error("Error al calcular precio para court {}: {}", court.getId(), e.getMessage(), e);
      return BigDecimal.ZERO;
    }
  }

  // Método auxiliar para calcular precio de una reserva existente
  private BigDecimal calculateReservationPriceForReservation(Reservation reservation) {
    CourtDTO court = courtClient.findById(reservation.getCourtId());
    return calculateReservationPrice(court, reservation.getStartTime(), reservation.getEndTime());
  }

  @Transactional
  public void updatePaymentStatus(Long id, String paymentStatus) {
    Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada con id: " + id));

    // Convertir String a ReservationPaymentStatus
    ReservationPaymentStatus newStatus = mapStringToPaymentStatus(paymentStatus);

    // Validar transición de estado
    validatePaymentStatusTransition(reservation.getPaymentStatus(), newStatus);

    // Actualizar estado de pago
    reservation.setPaymentStatus(newStatus);

    // Si el pago se confirma, actualizar paidAmount
    if (newStatus == ReservationPaymentStatus.CONFIRMED) {
      // Calcular y establecer el total pagado
      BigDecimal totalAmount = calculateReservationPriceForReservation(reservation);
      reservation.setPaidAmount(totalAmount);

      // También actualizar estado de reserva a CONFIRMED
      reservation.setStatus(ReservationStatus.CONFIRMED);
    }

    // Si se cancela o falla, resetear paidAmount
    if (newStatus == ReservationPaymentStatus.CANCELLED || newStatus == ReservationPaymentStatus.FAILED) {
      reservation.setPaidAmount(BigDecimal.ZERO);
    }

    reservation.setUpdatedAt(LocalDateTime.now());
    reservationRepository.save(reservation);

    log.info("Estado de pago actualizado para reserva {}: {} -> {}",
            id, reservation.getPaymentStatus(), newStatus);
  }

  @Transactional
  public void applyPayment(Long id, BigDecimal amount, String paymentMethod, String transactionId, String notes) {
    Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada con id: " + id));

    // Validar que la reserva acepte pagos
    if (reservation.getStatus() == ReservationStatus.CANCELLED) {
      throw new IllegalStateException("No se puede aplicar pago a una reserva cancelada");
    }

    // Calcular precio total si no está calculado
    BigDecimal totalAmount = calculateReservationPriceForReservation(reservation);

    // Obtener monto actual pagado
    BigDecimal currentPaidAmount = reservation.getPaidAmount() != null ?
            reservation.getPaidAmount() : BigDecimal.ZERO;
    BigDecimal newPaidAmount = currentPaidAmount.add(amount);

    // Validar que no se pague más del total
    if (newPaidAmount.compareTo(totalAmount) > 0) {
      throw new IllegalArgumentException(
              String.format("El pago excede el monto total. Total: %s, Pagado: %s, Intento: %s",
                      totalAmount, currentPaidAmount, amount)
      );
    }

    // Actualizar monto pagado
    reservation.setPaidAmount(newPaidAmount);
    reservation.setTotalAmount(totalAmount);

    // Actualizar estado de pago basado en el monto
    if (newPaidAmount.compareTo(totalAmount) == 0) {
      // Pago completo
      reservation.setPaymentStatus(ReservationPaymentStatus.CONFIRMED);
      reservation.setStatus(ReservationStatus.CONFIRMED);
    } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
      // Pago parcial - pero tu enum no tiene PARTIAL, usar PENDING
      reservation.setPaymentStatus(ReservationPaymentStatus.PENDING);
    } else {
      // Sin pago
      reservation.setPaymentStatus(ReservationPaymentStatus.PENDING);
    }

    reservation.setUpdatedAt(LocalDateTime.now());
    reservationRepository.save(reservation);

    log.info("Pago aplicado a reserva {} - Monto: {}, Total pagado: {}, Método: {}",
            id, amount, newPaidAmount, paymentMethod);
  }

  @Transactional(readOnly = true)
  public List<ReservationResponseDTO> findByUserEmail(String userEmail) {
    log.info("🔍 [DEBUG] Iniciando findByUserEmail con email: {}", userEmail);

    // 1. Verificar autenticación
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !auth.isAuthenticated()) {
      log.error("❌ [ERROR] Usuario no autenticado");
      throw new SecurityException("Usuario no autenticado");
    }

    String emailFromAuth = auth.getName();
    log.info("🔍 [DEBUG] Email del contexto: {}", emailFromAuth);

    // 2. Validar que solo vea sus propias reservas
    if (!emailFromAuth.equals(userEmail)) {
      log.warn("⚠️ [WARN] Intento de acceso no autorizado. Email solicitado: {}, Email autenticado: {}",
              userEmail, emailFromAuth);
      throw new SecurityException("No tienes permiso para ver reservas de otros usuarios");
    }

    try {
      // 3. Buscar directamente por email (SIN llamar a userClient)
      log.info("🔍 [DEBUG] Buscando reservas por email en BD: {}", userEmail);

      List<Reservation> reservas = reservationRepository.findByUserEmail(userEmail);
      log.info("🔍 [DEBUG] Número de reservas encontradas: {}", reservas.size());

      if (reservas.isEmpty()) {
        log.info("ℹ️ [INFO] No se encontraron reservas para el email: {}", userEmail);
        return Collections.emptyList();
      }

      // 4. Convertir a DTO
      return reservas.stream()
              .map(ReservationMapper::toResponse)
              .collect(Collectors.toList());

    } catch (Exception e) {
      log.error("❌ [ERROR] Excepción en findByUserEmail: {}", e.getMessage(), e);
      throw new RuntimeException("Error al obtener reservas del usuario: " + e.getMessage(), e);
    }
  }

  @Transactional(readOnly = true)
  public List<ReservationResponseDTO> getReservationsByClub(Long clubId, String status,
                                                            LocalDate startDate, LocalDate endDate, Long courtId) {

    LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
    LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

    List<Reservation> reservations = reservationRepository.findByClubId(
            clubId, status, startDateTime, endDateTime, courtId);

    return reservations.stream()
            .map(ReservationMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public ReservationStatsDTO getReservationStats(Long clubId, String timeRange) {
    ReservationStatsDTO stats = new ReservationStatsDTO();

    LocalDateTime startDate = calculateStartDate(timeRange);

    // Total reservas en el período
    Long totalReservations = reservationRepository.countByClubIdAndStartTimeAfter(clubId, startDate);
    stats.setTotalReservations(totalReservations != null ? totalReservations : 0L);

    // Reservas activas (CONFIRMED)
    Long activeReservations = reservationRepository.countByClubIdAndStatusAndStartTimeAfter(
            clubId, ReservationStatus.CONFIRMED, startDate);
    stats.setActiveReservations(activeReservations != null ? activeReservations : 0L);

    // Reservas pendientes
    Long pendingReservations = reservationRepository.countByClubIdAndStatusAndStartTimeAfter(
            clubId, ReservationStatus.PENDING, startDate);
    stats.setPendingReservations(pendingReservations != null ? pendingReservations : 0L);

    // Canceladas
    Long cancelledReservations = reservationRepository.countByClubIdAndStatusAndStartTimeAfter(
            clubId, ReservationStatus.CANCELLED, startDate);
    stats.setCancelledReservations(cancelledReservations != null ? cancelledReservations : 0L);

    // Ingresos
    Double revenue = reservationRepository.calculateRevenueByClub(clubId, startDate);
    stats.setRevenue(revenue != null ? revenue : 0.0);

    // Valor promedio por reserva
    Double averageBookingValue = revenue != null && totalReservations != null && totalReservations > 0
            ? revenue / totalReservations
            : 0.0;
    stats.setAverageBookingValue(BigDecimal.valueOf(averageBookingValue));

    // Cancha más popular - usando Feign Client para obtener nombre
    List<Object[]> popularCourtData = reservationRepository.findMostPopularCourtByClub(clubId, startDate);
    if (popularCourtData != null && !popularCourtData.isEmpty()) {
      Object[] popularCourt = popularCourtData.get(0); // La primera es la más popular
      if (popularCourt.length >= 2) {
        ReservationStatsDTO.PopularCourt popular = new ReservationStatsDTO.PopularCourt();

        // Obtener datos del array
        Long courtId = ((Number) popularCourt[0]).longValue();
        Long count = ((Number) popularCourt[1]).longValue();

        popular.setCourtId(courtId);
        popular.setCount(count);

        // Obtener nombre de la cancha usando Feign Client
        try {
          CourtDTO court = courtClient.findById(courtId);
          if (court != null) {
            popular.setCourtName(court.getName());
          } else {
            popular.setCourtName("Cancha #" + courtId);
          }
        } catch (Exception e) {
          log.warn("Error obteniendo nombre de cancha {}: {}", courtId, e.getMessage());
          popular.setCourtName("Cancha #" + courtId);
        }

        stats.setMostPopularCourt(popular);
      }
    }

    // Horas pico
    List<Object[]> peakHours = reservationRepository.findPeakHoursByClub(clubId, startDate);
    List<ReservationStatsDTO.PeakHour> peakHourList = peakHours.stream()
            .map(hour -> {
              Integer hourValue = ((Number) hour[0]).intValue();
              Long count = ((Number) hour[1]).longValue();
              return new ReservationStatsDTO.PeakHour(hourValue, count);
            })
            .collect(Collectors.toList());
    stats.setPeakHours(peakHourList);

    return stats;
  }

  public List<ReservationResponseDTO> getRecentReservationsByClub(Long clubId, int limit) {
    List<Reservation> reservations = reservationRepository.findRecentByClubId(clubId, PageRequest.of(0, limit));

    // Aplicar límite
    List<Reservation> limitedReservations = reservations.stream()
            .limit(limit)
            .collect(Collectors.toList());

    // Convertir a DTO
    List<ReservationResponseDTO> responseDTOs = limitedReservations.stream()
            .map(ReservationMapper::toResponse)
            .collect(Collectors.toList());

    // Enriquecer con información de canchas usando Feign Client
    return enrichReservationsWithCourtInfo(responseDTOs);
  }

  private List<ReservationResponseDTO> enrichReservationsWithCourtInfo(List<ReservationResponseDTO> reservations) {
    if (reservations.isEmpty()) {
      return reservations;
    }

    // Obtener IDs únicos de canchas
    Set<Long> courtIds = reservations.stream()
            .map(ReservationResponseDTO::getCourtId)
            .collect(Collectors.toSet());

    try {
      // Obtener canchas usando Feign Client
      List<CourtDTO> courts = courtClient.getCourtsByIds(new ArrayList<>(courtIds));
      Map<Long, CourtDTO> courtMap = courts.stream()
              .collect(Collectors.toMap(CourtDTO::getId, court -> court));

      // Enriquecer cada reserva con nombre de cancha
      return reservations.stream()
              .map(reservation -> {
                CourtDTO court = courtMap.get(reservation.getCourtId());
                if (court != null) {
                  reservation.setCourtName(court.getName());
                }
                return reservation;
              })
              .collect(Collectors.toList());

    } catch (Exception e) {
      log.error("Error enriqueciendo reservas con información de canchas: {}", e.getMessage());
      return reservations;
    }
  }

  @Transactional
  public ReservationResponseDTO confirmReservation(Long reservationId, String adminNotes) {
    Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

    reservation.setStatus(ReservationStatus.CONFIRMED);
    if (adminNotes != null) {
      reservation.setAdminNotes(adminNotes);
    }
    reservation.setUpdatedAt(LocalDateTime.now());

    reservation = reservationRepository.save(reservation);

    // Aquí podrías agregar notificaciones al usuario
    // notificationService.sendReservationConfirmed(reservation.getUserId(), reservation);

    return ReservationMapper.toResponse(reservation);
  }

  @Transactional
  public ReservationResponseDTO rejectReservation(Long reservationId, String reason) {
    Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

    reservation.setStatus(ReservationStatus.CANCELLED);
    reservation.setCancellationReason(reason);
    reservation.setUpdatedAt(LocalDateTime.now());

    reservation = reservationRepository.save(reservation);

    // Notificar al usuario y posible reembolso
    // notificationService.sendReservationRejected(reservation.getUserId(), reservation, reason);
    // paymentService.refundIfPaid(reservation);

    return ReservationMapper.toResponse(reservation);
  }

  // Método para mapear String a ReservationPaymentStatus
  private ReservationPaymentStatus mapStringToPaymentStatus(String status) {
    if (status == null) {
      return ReservationPaymentStatus.PENDING;
    }

    String upperStatus = status.toUpperCase();
    return switch (upperStatus) {
      case "PAID", "CONFIRMED" -> ReservationPaymentStatus.CONFIRMED;
      case "PENDING" -> ReservationPaymentStatus.PENDING;
      case "FAILED" -> ReservationPaymentStatus.FAILED;
      case "CANCELLED" -> ReservationPaymentStatus.CANCELLED;
      case "REFUNDED" -> ReservationPaymentStatus.REFUNDED;
      default -> throw new IllegalArgumentException("Estado de pago inválido: " + status);
    };
  }

  // Método para obtener el precio total de la reserva
  @Transactional(readOnly = true)
  public BigDecimal getReservationTotalAmount(Long id) {
    Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada"));

    // Calcular si no está almacenado
    if (reservation.getTotalAmount() != null) {
      return reservation.getTotalAmount();
    }

    return calculateReservationPriceForReservation(reservation);
  }

  @Transactional(readOnly = true)
  public boolean isCourtAvailable(Long courtId, LocalDateTime startDateTime, int durationHours) {
    // Calcular fecha/hora de fin
    LocalDateTime endDateTime = startDateTime.plusHours(durationHours);

    // Buscar reservas solapadas para esta cancha
    List<Reservation> overlappingReservations = reservationRepository
            .findOverlappingReservations(courtId, startDateTime, endDateTime);

    // Verificar si hay reservas activas en ese horario
    boolean isAvailable = overlappingReservations.isEmpty();

    log.debug("Cancha {} disponible de {} a {}: {}",
            courtId, startDateTime, endDateTime, isAvailable);

    return isAvailable;
  }

  private BigDecimal calculateRevenue(Long clubId, LocalDateTime startDate) {
    try {
      // Obtener todas las reservas confirmadas y pagadas
      List<Reservation> paidReservations = reservationRepository
              .findByClubIdAndStatusAndPaymentStatusAndStartTimeAfter(
                      clubId, ReservationStatus.CONFIRMED,
                      ReservationPaymentStatus.CONFIRMED, startDate);

      if (paidReservations.isEmpty()) {
        return BigDecimal.ZERO;
      }

      // Obtener IDs de canchas
      Set<Long> courtIds = paidReservations.stream()
              .map(Reservation::getCourtId)
              .collect(Collectors.toSet());

      // Obtener precios de canchas desde el microservicio de canchas
      Map<Long, BigDecimal> courtPrices = getCourtPrices(new ArrayList<>(courtIds));

      // Calcular ingresos
      BigDecimal totalRevenue = BigDecimal.ZERO;

      for (Reservation reservation : paidReservations) {
        BigDecimal pricePerHour = courtPrices.get(reservation.getCourtId());
        if (pricePerHour != null && reservation.getTotalAmount() != null) {
          totalRevenue = totalRevenue.add(reservation.getTotalAmount());
        }
      }

      return totalRevenue;

    } catch (Exception e) {
      log.error("Error calculando ingresos para club {}: {}", clubId, e.getMessage());
      return BigDecimal.ZERO;
    }
  }

  private Map<Long, BigDecimal> getCourtPrices(List<Long> courtIds) {
    try {
      List<CourtDTO> courts = courtClient.getCourtsByIds(courtIds);
      return courts.stream()
              .collect(Collectors.toMap(
                      CourtDTO::getId,
                      CourtDTO::getPricePerHour
              ));
    } catch (Exception e) {
      log.error("Error obteniendo precios de canchas: {}", e.getMessage());
      return new HashMap<>();
    }
  }

  private ReservationStatsDTO.PopularCourt findMostPopularCourt(Long clubId, LocalDateTime startDate) {
    try {
      // Obtener recuentos por cancha desde el repositorio
      List<Object[]> courtCounts = reservationRepository
              .findCourtReservationCountsByClub(clubId, startDate);

      if (courtCounts.isEmpty()) {
        return null;
      }

      // Obtener el primer resultado (ordenado por count descendente)
      Object[] mostPopular = courtCounts.get(0);
      Long courtId = (Long) mostPopular[0];
      Long count = (Long) mostPopular[1];

      // Obtener nombre de la cancha desde el microservicio
      try {
        CourtDTO court = courtClient.findById(courtId);
        return ReservationStatsDTO.PopularCourt.builder()
                .courtId(courtId)
                .courtName(court.getName())
                .reservationCount(count)
                .build();
      } catch (Exception e) {
        return ReservationStatsDTO.PopularCourt.builder()
                .courtId(courtId)
                .courtName("Cancha #" + courtId)
                .reservationCount(count)
                .build();
      }

    } catch (Exception e) {
      log.error("Error encontrando cancha popular: {}", e.getMessage());
      return null;
    }
  }

  private List<ReservationStatsDTO.PeakHour> findPeakHours(Long clubId, LocalDateTime startDate) {
    try {
      List<Object[]> hourCounts = reservationRepository
              .findReservationCountsByHour(clubId, startDate);

      return hourCounts.stream()
              .map(result -> {
                Integer hour = ((Number) result[0]).intValue();
                Long count = ((Number) result[1]).longValue();
                return ReservationStatsDTO.PeakHour.builder()
                        .hour(hour)
                        .count(count)
                        .build();
              })
              .collect(Collectors.toList());

    } catch (Exception e) {
      log.error("Error obteniendo horas pico: {}", e.getMessage());
      return new ArrayList<>();
    }
  }

  private LocalDateTime calculateStartDate(String timeRange) {
    LocalDateTime now = LocalDateTime.now();

    switch (timeRange.toLowerCase()) {
      case "today":
        return now.toLocalDate().atStartOfDay();
      case "week":
        return now.minusWeeks(1);
      case "month":
        return now.minusMonths(1);
      case "year":
        return now.minusYears(1);
      default:
        return now.minusMonths(1);
    }
  }

  private List<Reservation> findConflictingReservations(Long courtId,
                                                        LocalDateTime startTime,
                                                        LocalDateTime endTime) {
    return reservationRepository.findConflictingReservations(courtId, startTime, endTime);
  }

  private ReservationConflictDTO mapToConflictDTO(Reservation reservation) {
    return ReservationConflictDTO.builder()
            .reservationId(reservation.getId())
            .conflictingStartTime(reservation.getStartTime())
            .conflictingEndTime(reservation.getEndTime())
            .status(reservation.getStatus().name())
            .userId(reservation.getUserId())
            .build();
  }

  private void validateResourcesExist(ReservationRequestDTO request) {
    boolean userExists = userClient.userExists(request.getUserId());
    if (!userExists) {
      throw new EntityNotFoundException("Usuario no encontrado con id: " + request.getUserId());
    }

    CourtDTO court = courtClient.findById(request.getCourtId());
    if (court == null || !court.getClubId().equals(request.getClubId())) {
      throw new RuntimeException("La cancha no existe o no pertenece al club especificado");
    }

    boolean clubExists = clubClient.clubExists(request.getClubId());
    if (!clubExists) {
      throw new EntityNotFoundException("Club no encontrado con id: " + request.getClubId());
    }
  }

  private void validateCourtAvailability(ReservationRequestDTO request) {
    boolean isAvailable = reservationRepository.isCourtAvailable(
            request.getCourtId(),
            request.getStartTime(),
            request.getEndTime()
    );

    if (!isAvailable) {
      throw new RuntimeException("La cancha no está disponible en el horario solicitado");
    }

    CourtDTO court = courtClient.findById(request.getCourtId());
    if (!court.getIsActive()) {
      throw new RuntimeException("La cancha no está activa");
    }
  }

  private void validateReservationDates(ReservationRequestDTO request) {
    if (request.getEndTime().isBefore(request.getStartTime()) ||
            request.getEndTime().isEqual(request.getStartTime())) {
      throw new RuntimeException("La fecha de fin debe ser posterior a la fecha de inicio");
    }

    if (request.getStartTime().isBefore(LocalDateTime.now())) {
      throw new RuntimeException("No se pueden hacer reservas en el pasado");
    }

    Duration duration = Duration.between(request.getStartTime(), request.getEndTime());
    if (duration.toMinutes() < 60) {
      throw new RuntimeException("La reserva debe ser de al menos 60 minutos");
    }

    if (duration.toHours() > 4) {
      throw new RuntimeException("La reserva no puede exceder las 4 horas");
    }
  }

  private void validatePaymentStatusTransition(ReservationPaymentStatus current, ReservationPaymentStatus newStatus) {
    // No se puede cambiar desde estados finales
    if (current == ReservationPaymentStatus.CONFIRMED &&
            (newStatus == ReservationPaymentStatus.PENDING || newStatus == ReservationPaymentStatus.FAILED)) {
      throw new IllegalStateException("No se puede cambiar un pago confirmado a pendiente o fallido");
    }

    if (current == ReservationPaymentStatus.CANCELLED || current == ReservationPaymentStatus.REFUNDED) {
      throw new IllegalStateException("No se puede cambiar el estado de un pago " + current);
    }
  }
}
