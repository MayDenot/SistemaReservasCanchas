package org.example.microserviceclub.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.*;
import org.example.microserviceclub.entity.Club;
import org.example.microserviceclub.entity.SpecialHours;
import org.example.microserviceclub.feignClient.CourtClient;
import org.example.microserviceclub.feignClient.ReservationClient;
import org.example.microserviceclub.feignClient.UserClient;
import org.example.microserviceclub.mapper.ClubMapper;
import org.example.microserviceclub.repository.ClubRepository;
import org.example.microserviceclub.repository.SpecialHoursRepository;
import org.example.microserviceclub.service.dto.ClubStatsDTO;
import org.example.microserviceclub.service.dto.response.AvailabilityResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClubService {
  private final ClubRepository clubRepository;
  private final UserClient userClient;
  private final CourtClient courtClient;
  private final SpecialHoursRepository specialHoursRepository;
  private final ReservationClient reservationClient;

  private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

  public ClubService(ClubRepository clubRepository, UserClient userClient,
                     CourtClient courtClient, ReservationClient reservationClient, SpecialHoursRepository specialHoursRepository) {
    this.clubRepository = clubRepository;
    this.userClient = userClient;
    this.courtClient = courtClient;
    this.reservationClient = reservationClient;
    this.specialHoursRepository = specialHoursRepository;
  }

  @Transactional(readOnly = true)
  public List<ClubResponseDTO> findAll() {
    return this.clubRepository.findAll()
            .stream()
            .map(ClubMapper::toResponse)
            .toList();
  }

  @Transactional(readOnly = true)
  public ClubResponseDTO findById(Long id) {
    return ClubMapper.toResponse(this.clubRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Club no encontrado con id: " + id)));
  }

  @Transactional
  public ClubResponseDTO update(Long id, ClubRequestDTO request) {
    Club club = this.clubRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Club no encontrado con id: " + id));

    club.setName(request.getName());
    club.setAddress(request.getAddress());
    club.setPhone(request.getPhone());
    club.setOpeningTime(request.getOpeningTime());
    club.setClosingTime(request.getClosingTime());

    return ClubMapper.toResponse(this.clubRepository.save(club));
  }

  @Transactional
  public ClubResponseDTO save(ClubRequestDTO request) {
    log.info("Validando creación de club...");

    // Validar que adminId no sea null
    if (request.getAdminId() == null) {
      throw new RuntimeException("El adminId es requerido para crear un club");
    }

    // Validar que el club no exista
    if (clubRepository.existsByName(request.getName())) {
      throw new RuntimeException("Ya existe un club con el nombre: " + request.getName());
    }

    // Verificar que el usuario administrador exista
    try {
      UserBasicInfoDTO admin = userClient.getUserBasic(request.getAdminId());
      log.info("Administrador validado: {} ({})", admin.getFullName(), admin.getEmail());

      // Podrías verificar aquí que el usuario tenga rol CLUB_ADMIN o SUPER_ADMIN
      // si tienes esa información en UserBasicInfoDTO

    } catch (Exception e) {
      throw new RuntimeException(
              String.format("El usuario administrador (ID: %d) no existe o no es válido. Error: %s",
                      request.getAdminId(), e.getMessage())
      );
    }

    // Crear el club
    Club club = ClubMapper.toEntity(request);
    Club savedClub = clubRepository.save(club);

    log.info("Club creado exitosamente - ID: {}, Nombre: {}",
            savedClub.getId(), savedClub.getName());

    return ClubMapper.toResponse(savedClub);
  }

  @Transactional
  public Long delete(Long id) {
    if (!this.clubRepository.existsById(id)) {
      throw new RuntimeException("Club no encontrado con id: " + id);
    }
    this.clubRepository.deleteById(id);
    return id;
  }

  @Transactional
  public boolean existsByName(String name) {
    return this.clubRepository.existsByName(name);
  }

  @Transactional(readOnly = true)
  public ClubWithAdminResponseDTO getClubWithUser(Long clubId) {
    Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new RuntimeException("Club no encontrado"));

    UserBasicInfoDTO user = userClient.getUserBasic(club.getAdminId());

    return ClubMapper.toResponseWithUser(club, user);
  }

  @Transactional(readOnly = true)
  public Boolean isClubOpenAt(Long clubId, LocalDateTime dateTime) {
    Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new EntityNotFoundException("Club no encontrado"));

    LocalTime timeToCheck = dateTime.toLocalTime();
    LocalTime openingTime = club.getOpeningTime();
    LocalTime closingTime = club.getClosingTime();

    // Verificar si la hora está dentro del horario de apertura
    return !timeToCheck.isBefore(openingTime) && !timeToCheck.isAfter(closingTime);
  }

  @Transactional(readOnly = true)
  public Boolean existsById(Long id) {
    return clubRepository.existsById(id);
  }

  // GET /clubs/{id}/stats
  @Transactional(readOnly = true)
  public ClubStatsDTO getClubStats(Long clubId) {
    Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new RuntimeException("Club no encontrado"));

    ClubStatsDTO stats = ClubStatsDTO.builder()
            .clubName(club.getName())
            .address(club.getAddress())
            .openingHours(club.getOpeningTime().toString() + " - " + club.getClosingTime().toString())
            .build();

    try {
      // 1. Obtener estadísticas de canchas
      CourtStatsDTO courtStats = courtClient.getCourtStatsByClub(clubId);
      stats.setTotalCourts(courtStats.getTotalCourts());
      stats.setActiveCourts(courtStats.getActiveCourts());
      stats.setInactiveCourts(courtStats.getInactiveCourts());
    } catch (Exception e) {
      log.error("Error obteniendo estadísticas de canchas: {}", e.getMessage());
      stats.setTotalCourts(0L);
      stats.setActiveCourts(0L);
      stats.setInactiveCourts(0L);
    }

    try {
      // 2. Obtener estadísticas de reservas
      ReservationStatsDTO reservationStats = reservationClient
              .getReservationStatsByClub(clubId, "month");

      stats.setTotalReservations(reservationStats.getTotalReservations());
      stats.setActiveReservations(reservationStats.getActiveReservations());
      stats.setPendingReservations(reservationStats.getPendingReservations());
      stats.setCancelledReservations(reservationStats.getCancelledReservations());
      stats.setTotalRevenue(reservationStats.getTotalRevenue());

      // Calcular ingresos semanales y mensuales
      stats.setMonthlyRevenue(reservationStats.getTotalRevenue());
      stats.setWeeklyRevenue(calculateWeeklyRevenue(reservationStats.getTotalRevenue()));

    } catch (Exception e) {
      log.error("Error obteniendo estadísticas de reservas: {}", e.getMessage());
      stats.setTotalReservations(0L);
      stats.setActiveReservations(0L);
      stats.setPendingReservations(0L);
      stats.setCancelledReservations(0L);
      stats.setTotalRevenue(BigDecimal.ZERO);
      stats.setMonthlyRevenue(BigDecimal.ZERO);
      stats.setWeeklyRevenue(BigDecimal.ZERO);
    }

    try {
      // 3. Obtener conteo de miembros
      Long memberCount = getUserCountByClub(clubId);
      stats.setMemberCount(memberCount);
    } catch (Exception e) {
      log.error("Error obteniendo conteo de miembros: {}", e.getMessage());
      stats.setMemberCount(0L);
    }

    return stats;
  }

  // GET /clubs/admin/{adminId}
  @Transactional(readOnly = true)
  public List<ClubResponseDTO> getClubsByAdmin(Long adminId) {
    List<Club> clubs = clubRepository.findByAdminId(adminId);
    return clubs.stream()
            .map(ClubMapper::toResponse)
            .collect(Collectors.toList());
  }

  // GET /clubs/my-club
  @Transactional(readOnly = true)
  public ClubResponseDTO getMyClub(Long userId) {
    List<Club> clubs = clubRepository.findByAdminId(userId);

    if (clubs.isEmpty()) {
      throw new RuntimeException("No eres administrador de ningún club");
    }

    // Devuelve el primer club del administrador
    return ClubMapper.toResponse(clubs.get(0));
  }

  // PATCH /clubs/{id}/hours
  @Transactional
  public ClubResponseDTO updateClubHours(Long clubId, String openingTime, String closingTime) {
    Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new RuntimeException("Club no encontrado"));

    // Validar formato
    validateTimeFormat(openingTime);
    validateTimeFormat(closingTime);

    // Convertir String a LocalTime
    LocalTime opening = LocalTime.parse(openingTime, timeFormatter);
    LocalTime closing = LocalTime.parse(closingTime, timeFormatter);

    club.setOpeningTime(opening);
    club.setClosingTime(closing);
    club.setUpdatedAt(LocalDateTime.now());

    club = clubRepository.save(club);
    return ClubMapper.toResponse(club);
  }

  // GET /clubs/{id}/availability
  @Transactional(readOnly = true)
  public AvailabilityResponse checkAvailability(Long clubId, LocalDateTime dateTime, Integer durationHours) {
    Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new RuntimeException("Club no encontrado"));

    AvailabilityResponse response = new AvailabilityResponse();

    // 1. Verificar si el club está activo
    if (club.getIsActive() == null || !club.getIsActive()) {
      response.setAvailable(false);
      response.setReason("El club no está activo");
      return response;
    }

    // 2. Verificar si está en modo mantenimiento
    if (club.getMaintenanceMode() != null && club.getMaintenanceMode()) {
      response.setAvailable(false);
      response.setReason("El club está en modo mantenimiento");
      return response;
    }

    // 3. Verificar horario regular
    if (!isClubOpenAt(clubId, dateTime)) {
      response.setAvailable(false);
      response.setReason("El club está cerrado en ese horario");
      return response;
    }

    try {
      // 4. Obtener canchas disponibles del microservicio de canchas
      List<CourtResponseDTO> allCourts = courtClient.findByClubId(clubId);

      // 5. Verificar disponibilidad de cada cancha
      List<AvailabilityResponse.AvailableCourt> availableCourts = new ArrayList<>();

      for (CourtResponseDTO court : allCourts) {
        if (court.getIsActive()) {
          // Verificar si la cancha está disponible en ese horario
          boolean isCourtAvailable = checkCourtAvailability(court.getId(), dateTime, durationHours);

          if (isCourtAvailable) {
            availableCourts.add(AvailabilityResponse.AvailableCourt.builder()
                    .courtId(court.getId())
                    .courtName(court.getName())
                    .courtType(court.getType().name())
                    .pricePerHour(court.getPricePerHour())
                    .build());
          }
        }
      }

      response.setAvailable(!availableCourts.isEmpty());
      response.setAvailableCourts(availableCourts);
      response.setReason(availableCourts.isEmpty() ?
              "No hay canchas disponibles en ese horario" : "Disponible");

    } catch (Exception e) {
      log.error("Error verificando disponibilidad: {}", e.getMessage());
      response.setAvailable(false);
      response.setReason("Error al verificar disponibilidad");
    }

    return response;
  }

  // GET /clubs/{id}/settings
  @Transactional(readOnly = true)
  public ClubSettingsDTO getClubSettings(Long clubId) {
    Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new RuntimeException("Club no encontrado"));

    return convertClubToSettingsDTO(club);
  }

  // PUT /clubs/{id}/settings
  @Transactional
  public ClubSettingsDTO updateClubSettings(Long clubId, ClubSettingsDTO settingsDTO) {
    Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new RuntimeException("Club no encontrado"));

    // Actualizar configuración directamente en la entidad Club
    club.setBookingAdvanceDays(settingsDTO.getBookingAdvanceDays());
    club.setMaxBookingDuration(settingsDTO.getMaxBookingDuration());
    club.setMinBookingDuration(settingsDTO.getMinBookingDuration());
    club.setCancellationPolicyHours(settingsDTO.getCancellationPolicyHours());
    club.setRequireDeposit(settingsDTO.getRequireDeposit());
    club.setDepositPercentage(settingsDTO.getDepositPercentage());
    club.setAutoConfirmReservations(settingsDTO.getAutoConfirmReservations());
    club.setNotificationEmail(settingsDTO.getNotificationEmail());
    club.setNotificationPhone(settingsDTO.getNotificationPhone());
    club.setIsActive(settingsDTO.getIsActive());
    club.setMaintenanceMode(settingsDTO.getMaintenanceMode());
    club.setUpdatedAt(LocalDateTime.now());

    // Actualizar horarios si se proporcionan
    if (settingsDTO.getOpeningTime() != null) {
      club.setOpeningTime(LocalTime.parse(String.valueOf(LocalTime.parse(settingsDTO.getOpeningTime(), timeFormatter))));
    }
    if (settingsDTO.getClosingTime() != null) {
      club.setClosingTime(LocalTime.parse(String.valueOf(LocalTime.parse(settingsDTO.getClosingTime(), timeFormatter))));
    }

    club = clubRepository.save(club);
    return convertClubToSettingsDTO(club);
  }

  @Transactional(readOnly = true)
  public List<SpecialHoursDTO> getSpecialHours(Long clubId) {
    Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new EntityNotFoundException("Club no encontrado"));

    List<SpecialHours> specialHoursList = specialHoursRepository.findByClubId(clubId);

    return specialHoursList.stream()
            .map(this::convertToSpecialHoursDTO)
            .collect(Collectors.toList());
  }

  @Transactional
  public SpecialHoursDTO addSpecialHours(Long clubId, SpecialHoursDTO specialHoursDTO) {
    Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new EntityNotFoundException("Club no encontrado"));

    // Validar que la fecha especial no se solape con otra existente
    List<SpecialHours> existingHours = specialHoursRepository.findByClubIdAndDate(
            clubId, specialHoursDTO.getDate());

    if (!existingHours.isEmpty()) {
      throw new RuntimeException("Ya existe un horario especial para esta fecha");
    }

    SpecialHours specialHours = new SpecialHours();
    specialHours.setClub(club);
    specialHours.setDate(specialHoursDTO.getDate());
    specialHours.setOpeningTime(LocalTime.parse(specialHoursDTO.getOpeningTime(), timeFormatter));
    specialHours.setClosingTime(LocalTime.parse(specialHoursDTO.getClosingTime(), timeFormatter));
    specialHours.setReason(specialHoursDTO.getReason());
    specialHours.setIsClosed(specialHoursDTO.getIsClosed() != null ? specialHoursDTO.getIsClosed() : false);
    specialHours.setCreatedAt(LocalDateTime.now());

    specialHours = specialHoursRepository.save(specialHours);
    return convertToSpecialHoursDTO(specialHours);
  }

  // Métodos auxiliares
  private BigDecimal calculateWeeklyRevenue(BigDecimal monthlyRevenue) {
    // Asumimos 4 semanas por mes
    return monthlyRevenue != null ?
            monthlyRevenue.divide(BigDecimal.valueOf(4), 2, BigDecimal.ROUND_HALF_UP) :
            BigDecimal.ZERO;
  }

  private SpecialHoursDTO convertToSpecialHoursDTO(SpecialHours specialHours) {
    return SpecialHoursDTO.builder()
            .id(specialHours.getId())
            .date(specialHours.getDate())
            .openingTime(specialHours.getOpeningTime().format(timeFormatter))
            .closingTime(specialHours.getClosingTime().format(timeFormatter))
            .reason(specialHours.getReason())
            .isClosed(specialHours.getIsClosed())
            .createdAt(LocalDate.from(specialHours.getCreatedAt()))
            .build();
  }

  private Long getUserCountByClub(Long clubId) {
    try {
      // Esto debería venir del microservicio de usuarios o reservas
      // Por simplicidad, devolvemos un valor dummy
      return 50L;
    } catch (Exception e) {
      log.error("Error obteniendo conteo de usuarios: {}", e.getMessage());
      return 0L;
    }
  }

  private boolean checkCourtAvailability(Long courtId, LocalDateTime dateTime, Integer durationHours) {
    try {
      // Llamar al microservicio de reservas para verificar disponibilidad
      return reservationClient.isCourtAvailable(courtId, dateTime, durationHours);
    } catch (Exception e) {
      log.error("Error verificando disponibilidad de cancha {}: {}", courtId, e.getMessage());
      return false;
    }
  }

  private void validateTimeFormat(String time) {
    try {
      LocalTime.parse(time, timeFormatter);
    } catch (DateTimeParseException e) {
      throw new RuntimeException("Formato de hora inválido. Use HH:mm (ej: 08:00, 14:30)");
    }
  }

  private ClubSettingsDTO convertClubToSettingsDTO(Club club) {
    return ClubSettingsDTO.builder()
            .openingTime(club.getOpeningTime() != null ? club.getOpeningTime().format(DateTimeFormatter.ofPattern(String.valueOf(timeFormatter))) : null)
            .closingTime(club.getClosingTime() != null ? club.getClosingTime().format(DateTimeFormatter.ofPattern(String.valueOf(timeFormatter))) : null)
            .bookingAdvanceDays(club.getBookingAdvanceDays())
            .maxBookingDuration(club.getMaxBookingDuration())
            .minBookingDuration(club.getMinBookingDuration())
            .cancellationPolicyHours(club.getCancellationPolicyHours())
            .requireDeposit(club.getRequireDeposit())
            .depositPercentage(club.getDepositPercentage())
            .autoConfirmReservations(club.getAutoConfirmReservations())
            .notificationEmail(club.getNotificationEmail())
            .notificationPhone(club.getNotificationPhone())
            .isActive(club.getIsActive())
            .maintenanceMode(club.getMaintenanceMode())
            .build();
  }
}