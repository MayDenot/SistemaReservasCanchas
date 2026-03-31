package org.example.microserviceclub.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubStatsDTO {
  private Long totalCourts;
  private Long activeCourts;
  private Long inactiveCourts;

  private Long totalReservations;
  private Long activeReservations;
  private Long pendingReservations;
  private Long cancelledReservations;

  private BigDecimal totalRevenue;
  private BigDecimal monthlyRevenue;
  private BigDecimal weeklyRevenue;

  private Long memberCount;

  // Información del club
  private String clubName;
  private String address;
  private String openingHours;
}