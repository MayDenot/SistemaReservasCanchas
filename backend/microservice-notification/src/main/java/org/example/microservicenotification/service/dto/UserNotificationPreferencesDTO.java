package org.example.microservicenotification.service.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserNotificationPreferencesDTO {
  private boolean emailEnabled;
  private boolean smsEnabled;
  private boolean pushEnabled;
  private boolean whatsappEnabled;
  private boolean reservationNotifications;
  private boolean paymentNotifications;
  private boolean promotionalNotifications;
  private boolean systemNotifications;
}
