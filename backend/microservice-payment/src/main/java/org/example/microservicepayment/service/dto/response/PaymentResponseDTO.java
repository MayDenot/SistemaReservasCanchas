package org.example.microservicepayment.service.dto.response;

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
public class PaymentResponseDTO {
  private Long id;
  private Long reservationId;
  private BigDecimal amount;
  private PaymentStatus status;
  private PaymentMethod method;
  private String externalPaymentId; // Para MercadoPago, etc.
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
