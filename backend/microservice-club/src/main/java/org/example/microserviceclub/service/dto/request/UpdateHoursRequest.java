package org.example.microserviceclub.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHoursRequest {
  private String openingTime;
  private String closingTime;
  private List<String> daysOff;
}
