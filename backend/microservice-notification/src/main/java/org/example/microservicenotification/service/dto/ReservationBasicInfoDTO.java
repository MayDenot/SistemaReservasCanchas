package org.example.microservicenotification.service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ReservationBasicInfoDTO {
  private Long id;
  private Long userId;
  private Long courtId;
  private Long clubId;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String status;
  private String paymentStatus;
  private BigDecimal totalAmount;
  private BigDecimal paidAmount;
  private String courtName;
  private String clubName;
}
