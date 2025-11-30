package org.example.microservicereservation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "reservations")
public class Reservation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "user_id", nullable = false)
  private Long userId;
  @Column(name = "court_id", nullable = false)
  private Long courtId;
  @Column(name = "club_id", nullable = false)
  private Long clubId;
  @Column(name = "start_time", nullable = false)
  private LocalDateTime startTime;
  @Column(name = "end_time", nullable = false)
  private LocalDateTime endTime;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReservationStatus status = ReservationStatus.PENDING;
  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status")
  private ReservationPaymentStatus paymentStatus = ReservationPaymentStatus.PENDING;
  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();
}