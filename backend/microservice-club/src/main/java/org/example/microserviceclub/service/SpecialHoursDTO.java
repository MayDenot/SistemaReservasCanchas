package org.example.microserviceclub.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialHoursDTO {
  private Long id;
  private LocalDate date;
  private String openingTime;
  private String closingTime;
  private String reason;
  private Boolean isClosed;
  private LocalDate createdAt;
}