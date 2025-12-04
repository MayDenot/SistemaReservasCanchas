package org.example.microservicenotification.service.dto.request;

import lombok.*;
import org.example.microservicenotification.entity.NotificationStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class NotificationUpdateRequestDTO {
  private NotificationStatus status;
  private String errorMessage;
  private Integer retryCount;
}
