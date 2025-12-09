package org.example.microservicepayment.service.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PaymentValidationResponse {
  private boolean canProcess;
  private String message;
  private BigDecimal amountDue;
  private BigDecimal minimumPayment;
  private boolean requiresDeposit;
}
