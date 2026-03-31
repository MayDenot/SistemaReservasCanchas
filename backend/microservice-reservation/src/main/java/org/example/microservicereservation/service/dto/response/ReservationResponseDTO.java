package org.example.microservicereservation.service.dto.response;

import lombok.*;
import org.example.microservicereservation.entity.ReservationPaymentStatus;
import org.example.microservicereservation.entity.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReservationResponseDTO {
  private Long id;
  private Long userId;
  private Long courtId;
  private Long clubId;
  private String userEmail;

  private LocalDateTime startTime;
  private LocalDateTime endTime;

  private ReservationStatus status;
  private ReservationPaymentStatus paymentStatus;

  private LocalDateTime createdAt;

  private BigDecimal totalAmount;
  private BigDecimal paidAmount;
  private BigDecimal pendingAmount;

  private String courtName;
  private String clubName;
  private String userName;
}
