package org.example.microservicereservation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

  @Column(name = "user_email", nullable = false)
  private String userEmail;

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
  @Column(name = "payment_status", nullable = false)
  private ReservationPaymentStatus paymentStatus = ReservationPaymentStatus.PENDING;

  // Campos para manejo de pagos (opcional)
  @Column(name = "total_amount", precision = 10, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "paid_amount", precision = 10, scale = 2)
  private BigDecimal paidAmount = BigDecimal.ZERO;

  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  // Método para calcular monto pendiente
  public BigDecimal getPendingAmount() {
    if (totalAmount == null) {
      return BigDecimal.ZERO;
    }
    return totalAmount.subtract(paidAmount != null ? paidAmount : BigDecimal.ZERO);
  }
}