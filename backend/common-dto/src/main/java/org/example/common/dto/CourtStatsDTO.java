package org.example.common.dto;

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
public class CourtStatsDTO {
  private Long totalCourts;
  private Long activeCourts;
  private Long inactiveCourts;
  private Long maintenanceCourts;

  private List<CourtTypeCount> courtTypes;

  private List<PopularCourt> popularCourts;

  private BigDecimal totalRevenue;
  private BigDecimal averageRevenuePerCourt;
  private BigDecimal highestPricedCourt;
  private BigDecimal lowestPricedCourt;

  // Métodos helper
  public Double getActivePercentage() {
    if (totalCourts == null || totalCourts == 0) {
      return 0.0;
    }
    return (activeCourts != null ? activeCourts.doubleValue() : 0.0) / totalCourts * 100;
  }

  public Double getOccupancyRate() {
    // Tasa de ocupación estimada (necesitarías datos de reservas)
    // Por ahora devolvemos un valor dummy
    return 65.5; // 65.5% de ocupación promedio
  }

  // Clase interna para cancha popular
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PopularCourt {
    private Long courtId;
    private String courtName;
    private String courtType;
    private Long reservationCount; // Número de reservas
    private BigDecimal totalRevenue; // Ingresos generados
    private Double occupancyRate; // Tasa de ocupación
    private BigDecimal averageRating; // Calificación promedio (si aplica)

    public String getDisplayName() {
      return courtName + " (" + courtType + ")";
    }
  }
}