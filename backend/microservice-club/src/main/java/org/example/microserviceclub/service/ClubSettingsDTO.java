package org.example.microserviceclub.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubSettingsDTO {
  // Horarios
  private String openingTime;
  private String closingTime;

  // Políticas de reserva
  private Integer bookingAdvanceDays;
  private Integer maxBookingDuration; // en horas
  private Integer minBookingDuration; // en horas
  private Integer cancellationPolicyHours;

  // Pagos
  private Boolean requireDeposit;
  private BigDecimal depositPercentage;
  private Boolean autoConfirmReservations;

  // Notificaciones
  private String notificationEmail;
  private String notificationPhone;

  // Configuración general
  private Boolean isActive;
  private Boolean maintenanceMode;
}