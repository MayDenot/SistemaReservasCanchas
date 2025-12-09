package org.example.microservicepayment.service.dto.request;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.example.microservicepayment.entity.PaymentMethod;
import org.example.microservicepayment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PaymentRequestDTO {
  private Long reservationId;
  private BigDecimal amount;
  private PaymentStatus status;
  private PaymentMethod method;
  private String externalPaymentId; // Para MercadoPago, etc.
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
