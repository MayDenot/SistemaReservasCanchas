package org.example.microservicecourt.service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CourtAvailabilityDTO {
  private Long courtId;
  private String courtName;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Boolean available;
  private String reason;
  private Boolean courtActive;
  private BigDecimal price;
  private Double durationHours;
  private List<ReservationConflictDTO> conflictingReservations;
}
