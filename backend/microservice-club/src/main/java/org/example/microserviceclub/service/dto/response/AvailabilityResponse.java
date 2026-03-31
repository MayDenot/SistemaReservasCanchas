package org.example.microserviceclub.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {
  private boolean available;
  private String reason;
  private List<AvailableCourt> availableCourts;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AvailableCourt {
    private Long courtId;
    private String courtName;
    private String courtType;
    private BigDecimal pricePerHour;
  }
}