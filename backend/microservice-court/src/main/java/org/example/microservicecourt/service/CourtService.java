package org.example.microservicecourt.service;

import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.microservicecourt.entity.Court;
import org.example.microservicecourt.feignClient.ClubClient;
import org.example.microservicecourt.feignClient.ReservationClient;
import org.example.microservicecourt.mapper.CourtMapper;
import org.example.microservicecourt.repository.CourtRepository;
import org.example.microservicecourt.service.dto.CourtAvailabilityDTO;
import org.example.microservicecourt.service.dto.ReservationConflictDTO;
import org.example.microservicecourt.service.dto.request.CourtRequestDTO;
import org.example.microservicecourt.service.dto.response.CourtResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.ServiceUnavailableException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourtService {
  private final CourtRepository courtRepository;
  private final ClubClient clubClient;
  private final ReservationClient reservationClient;

  @Transactional(readOnly = true)
  public List<CourtResponseDTO> findAll() {
    return this.courtRepository.findAll()
            .stream()
            .map(CourtMapper::toResponse)
            .toList();
  }

  @Transactional(readOnly = true)
  public CourtResponseDTO findById(Long id) {
    return CourtMapper.toResponse(this.courtRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cancha no encontrado con id: " + id)));
  }

  @Transactional
  public CourtResponseDTO update(Long id, CourtRequestDTO request) {
    Court court = courtRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Cancha no encontrada con id: " + id));

    if (courtRepository.existsByNameAndIdNotAndClubId(
            request.getName(), id, court.getClubId())) {
      throw new IllegalArgumentException("Ya existe otra cancha con el nombre: " + request.getName() + " en este club");
    }

    court.setName(request.getName());
    court.setType(request.getType());
    court.setPricePerHour(request.getPricePerHour());
    court.setIsActive(request.getIsActive());

    Court updatedCourt = courtRepository.save(court);
    return CourtMapper.toResponse(updatedCourt);
  }

  @Transactional
  public CourtResponseDTO save(Long clubId, CourtRequestDTO courtRequestDTO) {
    try {
      boolean clubExists = clubClient.clubExists(clubId);
      if (!clubExists) {
        throw new EntityNotFoundException("Club no encontrado con id: " + clubId);
      }
    } catch (FeignException.NotFound e) {
      throw new EntityNotFoundException("Club no encontrado con id: " + clubId);
    }

    if (courtRepository.existsByNameAndClubId(courtRequestDTO.getName(), clubId)) {
      throw new IllegalArgumentException("Ya existe una cancha con el nombre: " + courtRequestDTO.getName() + " en este club");
    }

    Court court = CourtMapper.toEntity(courtRequestDTO);
    court.setClubId(clubId);

    Court savedCourt = courtRepository.save(court);
    return CourtMapper.toResponse(savedCourt);
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
            .map(CourtMapper::toResponse)
            .toList();
  }

  @Transactional(readOnly = true)
  public List<CourtResponseDTO> findByClubIdAndIsActiveTrue(Long clubId) {
    return this.courtRepository.findByClubIdAndIsActiveTrue(clubId)
            .stream()
            .map(CourtMapper::toResponse)
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
  public List<CourtResponseDTO> getCourtsByClub(Long clubId) {
    return this.courtRepository.findByClubId(clubId)
            .stream()
            .map(CourtMapper::toResponse)
            .toList();
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
