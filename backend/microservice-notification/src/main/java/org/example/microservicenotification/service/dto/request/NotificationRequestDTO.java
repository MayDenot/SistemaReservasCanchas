package org.example.microservicenotification.service.dto.request;

import lombok.*;
import org.example.microservicenotification.entity.NotificationChannel;
import org.example.microservicenotification.entity.NotificationStatus;
import org.example.microservicenotification.entity.NotificationType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class NotificationRequestDTO {
  private Long userId;
  private Long reservationId;
  private NotificationType type;
  private String title;
  private String message;
  private NotificationChannel channel;
  private NotificationStatus status;
  private String recipientEmail;
  private String recipientPhone;
  private LocalDateTime sentAt;
  private LocalDateTime createdAt;
  private Integer retryCount;
  private String errorMessage;
}
