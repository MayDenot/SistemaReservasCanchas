package org.example.microservicenotification.mapper;

import org.example.microservicenotification.entity.Notification;
import org.example.microservicenotification.service.dto.request.NotificationRequestDTO;
import org.example.microservicenotification.service.dto.response.NotificationResponseDTO;

public class NotificationMapper {
  public static Notification toEntity(NotificationRequestDTO dto) {
    return Notification.builder()
            .userId(dto.getUserId())
            .reservationId(dto.getReservationId())
            .type(dto.getType())
            .title(dto.getTitle())
            .message(dto.getMessage())
            .channel(dto.getChannel())
            .status(dto.getStatus())
            .recipientEmail(dto.getRecipientEmail())
            .recipientPhone(dto.getRecipientPhone())
            .sentAt(dto.getSentAt())
            .createdAt(dto.getCreatedAt())
            .retryCount(dto.getRetryCount())
            .errorMessage(dto.getErrorMessage())
            .build();
  }

  public static NotificationResponseDTO toEntity(Notification not) {
    return NotificationResponseDTO.builder()
            .id(not.getId())
            .userId(not.getUserId())
            .reservationId(not.getReservationId())
            .type(not.getType())
            .title(not.getTitle())
            .message(not.getMessage())
            .channel(not.getChannel())
            .status(not.getStatus())
            .recipientEmail(not.getRecipientEmail())
            .recipientPhone(not.getRecipientPhone())
            .sentAt(not.getSentAt())
            .createdAt(not.getCreatedAt())
            .retryCount(not.getRetryCount())
            .errorMessage(not.getErrorMessage())
            .build();
  }
}
