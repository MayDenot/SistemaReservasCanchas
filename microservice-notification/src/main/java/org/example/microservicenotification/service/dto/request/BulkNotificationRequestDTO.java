package org.example.microservicenotification.service.dto.request;

import lombok.*;
import org.example.microservicenotification.entity.NotificationType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class BulkNotificationRequestDTO {
  private List<Long> userIds;
  private NotificationType type;
  private String title;
  private String message;
}
