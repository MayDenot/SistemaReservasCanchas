package org.example.microservicepayment.service.dto.request;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdatePaymentStatusRequest {
  private String paymentStatus;
  private String paymentReference;
  private String notes;
}
