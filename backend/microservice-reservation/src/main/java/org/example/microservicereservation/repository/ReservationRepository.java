package org.example.microservicereservation.repository;

import org.example.microservicereservation.entity.Reservation;
import org.example.microservicereservation.entity.ReservationPaymentStatus;
import org.example.microservicereservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
  // Verificar si una cancha está disponible en un horario
  @Query("SELECT COUNT(r) = 0 FROM Reservation r " +
          "WHERE r.courtId = :courtId " +
          "AND r.status NOT IN ('CANCELLED', 'REJECTED') " +
          "AND ((r.startTime < :endTime AND r.endTime > :startTime))")
  boolean isCourtAvailable(@Param("courtId") Long courtId,
                           @Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime);

  @Query("SELECT r FROM Reservation r WHERE r.userEmail = :userEmail")
  List<Reservation> findByUserEmail(@Param("userEmail") String userEmail);

  @Query("SELECT r FROM Reservation r WHERE r.clubId = :clubId " +
          "AND (:status IS NULL OR r.status = :status) " +
          "AND (:startDateTime IS NULL OR r.startTime >= :startDateTime) " +
          "AND (:endDateTime IS NULL OR r.startTime <= :endDateTime) " +
          "AND (:courtId IS NULL OR r.courtId = :courtId) " +
          "ORDER BY r.startTime DESC")
  List<Reservation> findByClubId(@Param("clubId") Long clubId,
                                 @Param("status") String status,
                                 @Param("startDateTime") LocalDateTime startDateTime,
                                 @Param("endDateTime") LocalDateTime endDateTime,
                                 @Param("courtId") Long courtId);

  @Query("SELECT r FROM Reservation r WHERE r.clubId = :clubId " +
          "ORDER BY r.createdAt DESC")
  List<Reservation> findRecentByClubId(@Param("clubId") Long clubId,
                                       @RequestParam(defaultValue = "10") int limit);

  @Query("SELECT COALESCE(SUM(r.totalAmount), 0.0) FROM Reservation r " +
          "WHERE r.clubId = :clubId " +
          "AND r.startTime >= :startDate " +
          "AND r.paymentStatus = 'CONFIRMED' " +
          "AND r.status = 'CONFIRMED'")
  Double calculateRevenueByClub(@Param("clubId") Long clubId,
                                @Param("startDate") LocalDateTime startDate);

  // SOLO devuelve courtId y count, el nombre se obtiene vía Feign Client
  @Query("SELECT r.courtId, COUNT(r) " +
          "FROM Reservation r " +
          "WHERE r.clubId = :clubId " +
          "AND r.startTime >= :startDate " +
          "AND r.status IN ('CONFIRMED', 'PENDING') " +
          "GROUP BY r.courtId " +
          "ORDER BY COUNT(r) DESC")
  List<Object[]> findMostPopularCourtByClub(@Param("clubId") Long clubId,
                                            @Param("startDate") LocalDateTime startDate);

  @Query("SELECT EXTRACT(HOUR FROM r.startTime) as hour, COUNT(r) " +
          "FROM Reservation r " +
          "WHERE r.clubId = :clubId " +
          "AND r.startTime >= :startDate " +
          "AND r.status IN ('CONFIRMED', 'PENDING') " +
          "GROUP BY EXTRACT(HOUR FROM r.startTime) " +
          "ORDER BY COUNT(r) DESC")
  List<Object[]> findPeakHoursByClub(@Param("clubId") Long clubId,
                                     @Param("startDate") LocalDateTime startDate);

  // Método para obtener recuentos por hora
  @Query("SELECT EXTRACT(HOUR FROM r.startTime), COUNT(r) " +
          "FROM Reservation r " +
          "WHERE r.clubId = :clubId AND r.startTime > :startDate " +
          "GROUP BY EXTRACT(HOUR FROM r.startTime) " +
          "ORDER BY COUNT(r) DESC")
  List<Object[]> findReservationCountsByHour(@Param("clubId") Long clubId,
                                             @Param("startDate") LocalDateTime startDate);

  // Método para obtener recuentos por cancha
  @Query("SELECT r.courtId, COUNT(r) " +
          "FROM Reservation r " +
          "WHERE r.clubId = :clubId AND r.startTime > :startDate " +
          "GROUP BY r.courtId " +
          "ORDER BY COUNT(r) DESC")
  List<Object[]> findCourtReservationCountsByClub(@Param("clubId") Long clubId,
                                                  @Param("startDate") LocalDateTime startDate);

  // Métodos de conteo
  Long countByClubIdAndStartTimeAfter(Long clubId, LocalDateTime startDate);

  Long countByClubIdAndStatusAndStartTimeAfter(Long clubId, ReservationStatus status, LocalDateTime startDate);

  List<Reservation> findByClubIdAndStatusAndPaymentStatusAndStartTimeAfter(
          Long clubId, ReservationStatus status, ReservationPaymentStatus paymentStatus, LocalDateTime startDate);

  // Método para verificar conflictos de reserva
  @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
          "FROM Reservation r " +
          "WHERE r.courtId = :courtId " +
          "AND r.status IN ('CONFIRMED', 'PENDING') " +
          "AND ((:startTime < r.endTime AND :endTime > r.startTime))")
  boolean hasReservationConflict(@Param("courtId") Long courtId,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime);

  @Query("SELECT r FROM Reservation r " +
          "WHERE r.courtId = :courtId " +
          "AND r.status IN ('CONFIRMED', 'PENDING') " +
          "AND ((:startTime < r.endTime AND :endTime > r.startTime)) " +
          "ORDER BY r.startTime ASC")
  List<Reservation> findConflictingReservations(@Param("courtId") Long courtId,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

  @Query("SELECT r FROM Reservation r WHERE " +
          "r.courtId = :courtId AND " +
          "r.status = 'ACTIVE' AND " +
          "((r.startTime < :endDateTime AND r.endTime > :startDateTime) OR " +
          "(r.startTime = :startDateTime))")
  List<Reservation> findOverlappingReservations(
          @Param("courtId") Long courtId,
          @Param("startDateTime") LocalDateTime startDateTime,
          @Param("endDateTime") LocalDateTime endDateTime
  );

  // Métodos adicionales útiles
  List<Reservation> findByCourtId(Long courtId);

  List<Reservation> findByCourtIdAndStartTimeBetween(Long courtId, LocalDateTime start, LocalDateTime end);

  List<Reservation> findByStatus(ReservationStatus status);

  List<Reservation> findByUserId(Long userId);

  List<Reservation> findByClubIdAndStatus(Long clubId, ReservationStatus status);

  @Query("SELECT r FROM Reservation r WHERE r.clubId = :clubId " +
          "AND r.startTime BETWEEN :startDate AND :endDate")
  List<Reservation> findByClubIdAndDateRange(@Param("clubId") Long clubId,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
}
