package org.example.microservicereservation.service.dto.request;

import lombok.*;
import org.example.microservicereservation.entity.ReservationPaymentStatus;
import org.example.microservicereservation.entity.ReservationStatus;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReservationRequestDTO {
  private Long userId;
  private Long courtId;
  private Long clubId;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private ReservationStatus status;
  private ReservationPaymentStatus paymentStatus;
  private LocalDateTime createdAt;
}
