package org.example.microservicereservation.service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CourtDTO {
  private Long id;
  private String name;
  private String type;
  private BigDecimal pricePerHour;
  private Boolean isActive;
  private Long clubId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
