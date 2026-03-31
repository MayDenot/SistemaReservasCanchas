package org.example.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationStatsDTO {
  private Long totalReservations;
  private Long activeReservations;        // CONFIRMED
  private Long pendingReservations;       // PENDING
  private Long cancelledReservations;     // CANCELLED

  // CAMBIA Double a BigDecimal
  private BigDecimal totalRevenue;
  private BigDecimal pendingRevenue;      // Pago pendiente
  private BigDecimal averageBookingValue;

  private PopularCourt mostPopularCourt;
  private List<PeakHour> peakHours;

  // Faltaba el campo revenue (Double) que usas en el servicio
  // Si necesitas mantener compatibilidad, añade:
  @Builder.Default
  private Double revenue = 0.0;

  // Métodos helper
  public BigDecimal getCancellationRate() {
    if (totalReservations == null || totalReservations == 0) {
      return BigDecimal.ZERO;
    }
    return BigDecimal.valueOf(cancelledReservations != null ? cancelledReservations : 0L)
            .divide(BigDecimal.valueOf(totalReservations), 2, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
  }

  public BigDecimal getConfirmationRate() {
    if (totalReservations == null || totalReservations == 0) {
      return BigDecimal.ZERO;
    }
    return BigDecimal.valueOf(activeReservations != null ? activeReservations : 0L)
            .divide(BigDecimal.valueOf(totalReservations), 2, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PopularCourt {
    private Long courtId;
    private String courtName;
    private Long reservationCount;  // O usa solo "count" para mantener consistencia

    // Alias para mantener compatibilidad con tu código actual
    public Long getCount() {
      return reservationCount;
    }

    public void setCount(Long count) {
      this.reservationCount = count;
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PeakHour {
    private Integer hour; // 0-23
    private Long count;

    public String getFormattedHour() {
      if (hour == null) return "";
      String start = String.format("%02d:00", hour);
      String end = String.format("%02d:00", (hour + 1) % 24);
      return start + " - " + end;
    }
  }
}