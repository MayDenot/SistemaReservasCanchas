package org.example.common.dto;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CourtResponseDTO {
  private Long id;
  private Long clubId;
  private String name;
  private CourtType type;
  private BigDecimal pricePerHour;
  private Boolean isActive;
  private String clubName;
  private String description;
}

