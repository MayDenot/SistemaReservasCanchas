package org.example.microservicereservation.service.dto.request;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RejectReservationRequest {
  private String reason;
}
