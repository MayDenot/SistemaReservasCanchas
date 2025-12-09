package org.example.microservicecourt.service.dto.response;

import lombok.*;
import org.example.microservicecourt.entity.CourtType;

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
}
