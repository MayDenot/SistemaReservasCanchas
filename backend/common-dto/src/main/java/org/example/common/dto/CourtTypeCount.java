package org.example.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtTypeCount {
  private String type;
  private Long count;
  private Double percentage;

  // Constructor para facilitar la creación
  public CourtTypeCount(String type, Long count) {
    this.type = type;
    this.count = count;
  }

  // Método para calcular el porcentaje
  public void calculatePercentage(Long totalCourts) {
    if (totalCourts != null && totalCourts > 0 && count != null) {
      this.percentage = (count.doubleValue() / totalCourts.doubleValue()) * 100;
    } else {
      this.percentage = 0.0;
    }
  }
}