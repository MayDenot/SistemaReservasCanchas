package org.example.microservicereservation.repository;

import org.example.microservicereservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

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

  // Encontrar reservas por usuario
  List<Reservation> findByUserId(Long userId);

  // Encontrar reservas por cancha
  List<Reservation> findByCourtId(Long courtId);

  // Encontrar reservas futuras
  List<Reservation> findByStartTimeAfter(LocalDateTime dateTime);

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
}
