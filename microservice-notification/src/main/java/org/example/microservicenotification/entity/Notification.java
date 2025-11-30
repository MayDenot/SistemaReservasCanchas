package org.example.microservicenotification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(name = "notifications")
public class Notification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "user_id", nullable = false)
  private Long userId;
  @Column(name = "reservation_id")
  private Long reservationId;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;
  @Column(nullable = false)
  private String title;
  @Column(columnDefinition = "TEXT")
  private String message;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationChannel channel;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationStatus status = NotificationStatus.PENDING;
  @Column(name = "recipient_email")
  private String recipientEmail;
  @Column(name = "recipient_phone")
  private String recipientPhone;
  @Column(name = "sent_at")
  private LocalDateTime sentAt;
  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();
  @Column(name = "retry_count")
  private Integer retryCount;
  @Column(name = "error_message")
  private String errorMessage;
}
