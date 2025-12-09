package org.example.microservicepayment.entity;

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
@Table(name = "payments")
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "reservation_id", nullable = false)
  private Long reservationId;
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentMethod method;
  @Column(name = "external_payment_id")
  private String externalPaymentId; // Para MercadoPago, etc.
  @Column(name = "created_at")
  private LocalDateTime createdAt;
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}