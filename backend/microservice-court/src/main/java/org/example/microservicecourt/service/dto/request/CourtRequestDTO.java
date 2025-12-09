package org.example.microservicecourt.service.dto.request;

import lombok.*;
import org.example.microservicecourt.entity.CourtType;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CourtRequestDTO {
  private Long clubId;
  private String name;
  private CourtType type;
  private BigDecimal pricePerHour;
  private Boolean isActive;
}
