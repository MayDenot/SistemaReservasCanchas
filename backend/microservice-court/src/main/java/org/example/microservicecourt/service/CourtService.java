package org.example.microservicecourt.service;

import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.*;
import org.example.microservicecourt.entity.Court;
import org.example.microservicecourt.feignClient.ClubClient;
import org.example.microservicecourt.feignClient.ReservationClient;
import org.example.microservicecourt.mapper.CourtMapper;
import org.example.microservicecourt.repository.CourtRepository;
import org.example.microservicecourt.service.dto.CourtAvailabilityDTO;
import org.example.microservicecourt.service.dto.ReservationConflictDTO;
import org.example.microservicecourt.service.dto.request.CourtRequestDTO;
import org.example.microservicecourt.service.dto.request.UpdateCourtStatusRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.ServiceUnavailableException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourtService {
  private final CourtRepository courtRepository;
  private final ClubClient clubClient;
  private final ReservationClient reservationClient;
  private final CourtMapper courtMapper;

  @Transactional(readOnly = true)
  public List<CourtResponseDTO> findAll() {
    return this.courtRepository.findAll()
            .stream()
            .map(courtMapper::toResponse) // Usar método de instancia
            .toList();
  }

  @Transactional(readOnly = true)
  public CourtResponseDTO findById(Long id) {
    Court court = this.courtRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cancha no encontrado con id: " + id));
    return courtMapper.toResponse(court); // Usar método de instancia
  }

  @Transactional
  public CourtResponseDTO update(Long id, CourtRequestDTO request) {
    Court court = courtRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Cancha no encontrada con id: " + id));

    // Validación de nombre único en el mismo club
    if (courtRepository.existsByNameAndIdNotAndClubId(
            request.getName(), id, court.getClubId())) {
      throw new IllegalArgumentException("Ya existe otra cancha con el nombre: " + request.getName() + " en este club");
    }

    // Usar el método updateEntity del mapper
    court = courtMapper.updateEntity(court, request);

    Court updatedCourt = courtRepository.save(court);
    return courtMapper.toResponse(updatedCourt);
  }

  @Transactional
  public CourtResponseDTO save(Long clubId, CourtRequestDTO courtRequest) {
    try {
      boolean clubExists = clubClient.clubExists(clubId);
      if (!clubExists) {
        throw new EntityNotFoundException("Club no encontrado con id: " + clubId);
      }
    } catch (FeignException.NotFound e) {
      throw new EntityNotFoundException("Club no encontrado con id: " + clubId);
    }

    if (courtRepository.existsByNameAndClubId(courtRequest.getName(), clubId)) {
      throw new IllegalArgumentException("Ya existe una cancha con el nombre: " + courtRequest.getName() + " en este club");
    }

    // Usar el método del mapper
    Court court = courtMapper.toEntity(courtRequest);
    court.setClubId(clubId);

    Court savedCourt = courtRepository.save(court);
    return courtMapper.toResponse(savedCourt);
  }

  @Transactional
  public Long delete(Long id) {
    if (!this.courtRepository.existsById(id)) {
      throw new RuntimeException("Cancha no encontrado con id: " + id);
    }
    this.courtRepository.deleteById(id);
    return id;
  }

  @Transactional(readOnly = true)
  public boolean existsByNameAndClubId(String name, Long clubId) {
    return this.courtRepository.existsByNameAndClubId(name, clubId);
  }

  @Transactional(readOnly = true)
  public boolean existsByNameAndIdNotAndClubId(String name, Long id, Long clubId) {
    return this.courtRepository.existsByNameAndIdNotAndClubId(name, id, clubId);
  }

  @Transactional(readOnly = true)
  public List<CourtResponseDTO> findByClubId(Long clubId) {
    return this.courtRepository.findByClubId(clubId)
            .stream()
            .map(courtMapper::toResponse) // Usar método de instancia
            .toList();
  }

  @Transactional(readOnly = true)
  public List<CourtResponseDTO> findByClubIdAndIsActiveTrue(Long clubId) {
    return this.courtRepository.findByClubIdAndIsActiveTrue(clubId)
            .stream()
            .map(courtMapper::toResponse) // Usar método de instancia
            .toList();
  }

  @Transactional(readOnly = true)
  public boolean existsById(Long id) {
    return this.courtRepository.existsById(id);
  }

  @Transactional(readOnly = true)
  public boolean isCourtAvailable(Long courtId, LocalDateTime startTime, LocalDateTime endTime) throws ServiceUnavailableException {
    validateAvailabilityParameters(startTime, endTime);

    Court court = courtRepository.findById(courtId)
            .orElseThrow(() -> new EntityNotFoundException("Cancha no encontrada con id: " + courtId));

    if (!court.getIsActive()) {
      return false;
    }

    boolean isClubOpen = isClubOpenAtTime(court.getClubId(), startTime, endTime);
    if (!isClubOpen) {
      return false;
    }

    try {
      boolean hasRemoteConflicts = reservationClient.hasReservationConflict(
              courtId, startTime, endTime);

      if (hasRemoteConflicts) {
        return false;
      }
    } catch (Exception e) {
      throw new ServiceUnavailableException("No se puede verificar disponibilidad en este momento");
    }

    return true;
  }

  @Transactional(readOnly = true)
  public List<String> getAvailableTimeSlots(Long courtId, LocalDate date) {
    Court court = courtRepository.findById(courtId)
            .orElseThrow(() -> new EntityNotFoundException("Cancha no encontrada con id: " + courtId));

    if (!court.getIsActive()) {
      return List.of();
    }

    List<String> availableSlots = new ArrayList<>();
    LocalDateTime currentSlot = date.atTime(8, 0);
    LocalDateTime endOfDay = date.atTime(22, 0);

    while (currentSlot.isBefore(endOfDay)) {
      LocalDateTime slotEnd = currentSlot.plusMinutes(30);

      // Verificar que no sea en el pasado
      if (currentSlot.isBefore(LocalDateTime.now())) {
        currentSlot = currentSlot.plusMinutes(30);
        continue;
      }

      try {
        // Ahora debería funcionar con el token de servicio
        boolean isAvailable = isCourtAvailable(courtId, currentSlot, slotEnd);

        if (isAvailable) {
          String timeSlot = currentSlot.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
          availableSlots.add(timeSlot);
        }
      } catch (Exception e) {
        System.err.println("Error verificando slot " + currentSlot + ": " + e.getMessage());
      }

      currentSlot = currentSlot.plusMinutes(30);
    }

    return availableSlots;
  }

  @Transactional(readOnly = true)
  public List<CourtResponseDTO> getCourtsByIds(List<Long> ids) {
    List<Court> courts = courtRepository.findByIdIn(ids);

    return courts.stream()
            .map(courtMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public CourtAvailabilityDTO checkCourtAvailability(Long courtId, LocalDateTime startTime, LocalDateTime endTime) {
    validateAvailabilityParameters(startTime, endTime);

    Court court = courtRepository.findById(courtId)
            .orElseThrow(() -> new EntityNotFoundException("Cancha no encontrada"));

    CourtAvailabilityDTO availability = CourtAvailabilityDTO.builder()
            .courtId(courtId)
            .courtName(court.getName())
            .startTime(startTime)
            .endTime(endTime)
            .courtActive(court.getIsActive())
            .build();

    if (!court.getIsActive()) {
      availability.setAvailable(false);
      availability.setReason("La cancha no está activa");
      return availability;
    }

    if (!isClubOpenAtTime(court.getClubId(), startTime, endTime)) {
      availability.setAvailable(false);
      availability.setReason("El club está cerrado en ese horario");
      return availability;
    }

    boolean hasConflicts;
    try {
      hasConflicts = reservationClient.hasReservationConflict(courtId, startTime, endTime);
    } catch (Exception e) {
      hasConflicts = true;
    }

    if (hasConflicts) {
      availability.setAvailable(false);
      availability.setReason("La cancha ya está reservada en ese horario");

      availability.setConflictingReservations(getConflictingReservations(courtId, startTime, endTime));
    } else {
      availability.setAvailable(true);
      availability.setReason("Disponible");

      BigDecimal price = calculateReservationPrice(court, startTime, endTime);
      availability.setPrice(price);
      availability.setDurationHours(getDurationInHours(startTime, endTime));
    }

    return availability;
  }

  @Transactional(readOnly = true)
  public CourtStatsDTO getCourtStatsByClub(Long clubId) {
    CourtStatsDTO stats = new CourtStatsDTO();

    // 1. Conteo total de canchas
    Long totalCourts = courtRepository.countByClubId(clubId);
    stats.setTotalCourts(totalCourts != null ? totalCourts : 0L);

    // 2. Canchas activas
    Long activeCourts = courtRepository.countByClubIdAndIsActiveTrue(clubId);
    stats.setActiveCourts(activeCourts != null ? activeCourts : 0L);

    // 3. Canchas inactivas
    Long inactiveCourts = totalCourts - activeCourts;
    stats.setInactiveCourts(inactiveCourts > 0 ? inactiveCourts : 0L);

    // 4. Conteo por tipo
    List<CourtTypeCount> courtTypes = getCourtTypesByClub(clubId);
    stats.setCourtTypes(courtTypes);

    // 5. Canchas más populares (necesitarías datos de reservas)
    // Por ahora devolvemos las canchas con sus datos básicos
    List<CourtStatsDTO.PopularCourt> popularCourts = getPopularCourtsByClub(clubId);
    stats.setPopularCourts(popularCourts);

    return stats;
  }

  @Transactional(readOnly = true)
  public Long getCourtCountByClub(Long clubId) {
    Long count = courtRepository.countByClubId(clubId);
    return count != null ? count : 0L;
  }

  @Transactional
  public CourtResponseDTO updateCourtStatus(Long courtId, UpdateCourtStatusRequest request) {
    log.info("Actualizando estado de la cancha: {}", courtId);

    Court court = courtRepository.findById(courtId)
            .orElseThrow(() -> new RuntimeException("Cancha no encontrada con id: " + courtId));

    // Actualizar estado
    court.setIsActive(request.getIsActive());

    // Si se desactiva, guardar motivo de mantenimiento
    if (request.getMaintenanceNotes() != null) {
      // Podrías tener un campo 'maintenanceNotes' en la entidad
      // court.setMaintenanceNotes(request.getMaintenanceNotes());
      log.info("Notas de mantenimiento para cancha {}: {}", courtId, request.getMaintenanceNotes());
    }

    court.setUpdatedAt(LocalDateTime.now());

    Court updatedCourt = courtRepository.save(court);
    return courtMapper.toResponse(updatedCourt);
  }

  // Métodos auxiliares
  private List<CourtTypeCount> getCourtTypesByClub(Long clubId) {
    List<Court> courts = courtRepository.findByClubId(clubId);

    Map<CourtType, Long> typeCounts = courts.stream()
            .collect(Collectors.groupingBy(Court::getType, Collectors.counting()));

    return typeCounts.entrySet().stream()
            .map(entry -> new CourtTypeCount(entry.getKey().name(), entry.getValue()))
            .collect(Collectors.toList());
  }

  private List<CourtStatsDTO.PopularCourt> getPopularCourtsByClub(Long clubId) {
    List<Court> courts = courtRepository.findByClubId(clubId);

    // Por ahora, devolvemos todas las canchas ordenadas por precio (como ejemplo)
    // En una implementación real, esto vendría del microservicio de reservas
    return courts.stream()
            .sorted(Comparator.comparing(Court::getPricePerHour).reversed())
            .limit(5) // Top 5
            .map(court -> CourtStatsDTO.PopularCourt.builder()
                    .courtId(court.getId())
                    .courtName(court.getName())
                    .reservationCount(0L) // Por ahora 0, se llenaría con datos reales
                    .totalRevenue(BigDecimal.ZERO) // Por ahora 0
                    .build())
            .collect(Collectors.toList());
  }

  private void validateAvailabilityParameters(LocalDateTime startTime, LocalDateTime endTime) {
    if (startTime == null || endTime == null) {
      throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
    }

    if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
      throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
    }

    if (startTime.isBefore(LocalDateTime.now())) {
      throw new IllegalArgumentException("No se puede verificar disponibilidad en el pasado");
    }

    long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
    if (minutes < 30) {
      throw new IllegalArgumentException("La reserva debe ser de al menos 30 minutos");
    }

    if (minutes > 240) {
      throw new IllegalArgumentException("La reserva no puede exceder las 4 horas");
    }
  }

  private boolean isClubOpenAtTime(Long clubId, LocalDateTime startTime, LocalDateTime endTime) {
    try {
      boolean clubOpenAtStart = clubClient.isClubOpen(clubId, startTime);
      boolean clubOpenAtEnd = clubClient.isClubOpen(clubId, endTime);

      return clubOpenAtStart && clubOpenAtEnd;
    } catch (Exception e) {
      return true;
    }
  }

  private BigDecimal calculateReservationPrice(Court court, LocalDateTime startTime, LocalDateTime endTime) {
    if (court.getPricePerHour() == null || court.getPricePerHour().compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    BigDecimal hours = BigDecimal.valueOf(getDurationInHours(startTime, endTime));

    // Multiplicación exacta con BigDecimal
    return court.getPricePerHour().multiply(hours)
            .setScale(2, RoundingMode.HALF_UP); // 2 decimales, redondeo estándar
  }

  private double getDurationInHours(LocalDateTime startTime, LocalDateTime endTime) {
    java.time.Duration duration = java.time.Duration.between(startTime, endTime);
    return duration.toMinutes() / 60.0;
  }

  private List<ReservationConflictDTO> getConflictingReservations(Long courtId, LocalDateTime startTime, LocalDateTime endTime) {
    try {
      return reservationClient.getConflictingReservations(courtId, startTime, endTime);
    } catch (Exception e) {
      return List.of();
    }
  }
}