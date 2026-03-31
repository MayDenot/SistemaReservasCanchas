package org.example.microservicereservation.service.dto.request;

import lombok.*;
import org.example.microservicereservation.entity.ReservationPaymentStatus;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdatePaymentStatusRequest {
  private ReservationPaymentStatus paymentStatus;
  private String paymentMethod;
  private String transactionId;
  private BigDecimal paidAmount;
}
