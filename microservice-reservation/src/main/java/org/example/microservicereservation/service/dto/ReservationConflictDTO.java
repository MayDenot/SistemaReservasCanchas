package org.example.microservicereservation.service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReservationConflictDTO {
  private Long reservationId;
  private LocalDateTime conflictingStartTime;
  private LocalDateTime conflictingEndTime;
  private String status;
  private Long userId;
}
