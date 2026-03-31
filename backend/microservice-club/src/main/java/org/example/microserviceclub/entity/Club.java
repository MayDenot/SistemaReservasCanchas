package org.example.microserviceclub.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "clubs")
public class Club {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String address;
  private String phone;

  @Column(name = "admin_id")
  private Long adminId;

  @Column(name = "opening_time")
  private LocalTime openingTime; // HH:mm

  @Column(name = "closing_time")
  private LocalTime closingTime; // HH:mm

  private Boolean isActive;

  @Column(name = "booking_advance_days")
  private Integer bookingAdvanceDays;

  @Column(name = "max_booking_duration")
  private Integer maxBookingDuration;

  @Column(name = "min_booking_duration")
  private Integer minBookingDuration;

  @Column(name = "cancellation_policy_hours")
  private Integer cancellationPolicyHours;

  @Column(name = "require_deposit")
  private Boolean requireDeposit;

  @Column(name = "deposit_percentage")
  private BigDecimal depositPercentage;

  @Column(name = "auto_confirm_reservations")
  private Boolean autoConfirmReservations;

  @Column(name = "notification_email")
  private String notificationEmail;

  @Column(name = "notification_phone")
  private String notificationPhone;

  @Column(name = "maintenance_mode")
  private Boolean maintenanceMode;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "days_off")
  private String daysOff;
}
