package org.example.microservicenotification.service.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class NotificationStatsDTO {
  private Long total;
  private Long sent;
  private Long pending;
  private Long failed;
  private Long delivered;
}
