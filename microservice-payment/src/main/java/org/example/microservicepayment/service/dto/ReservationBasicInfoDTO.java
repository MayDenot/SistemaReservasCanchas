package org.example.microservicepayment.service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReservationBasicInfoDTO {
  private Long id;
  private Long userId;
  private Long courtId;
  private Long clubId;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String status;           // "CONFIRMED", "PENDING", etc.
  private String paymentStatus;    // "PENDING", "PAID", "CANCELLED"
  private BigDecimal totalAmount;
  private BigDecimal paidAmount;
  private BigDecimal pendingAmount;
}
