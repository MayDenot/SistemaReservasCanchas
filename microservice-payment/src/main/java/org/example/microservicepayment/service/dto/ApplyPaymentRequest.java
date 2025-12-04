package org.example.microservicepayment.service.dto;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ApplyPaymentRequest {
  private BigDecimal amount;
  private String paymentMethod;
  private String transactionId;
  private String notes;
}
