package org.example.microservicenotification.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.microservicenotification.entity.NotificationType;
import org.example.microservicenotification.service.NotificationService;
import org.example.microservicenotification.service.dto.request.BulkNotificationRequestDTO;
import org.example.microservicenotification.service.dto.request.NotificationRequestDTO;
import org.example.microservicenotification.service.dto.request.NotificationUpdateRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<?> findAll() {
    try {
      return ResponseEntity.ok(notificationService.findAll());
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> findById(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(notificationService.findById(id));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping
  public ResponseEntity<?> create(@RequestBody NotificationRequestDTO request) {
    try {
      return ResponseEntity.ok(notificationService.create(request));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @PutMapping("/{id}/status")
  public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                        @RequestBody NotificationUpdateRequestDTO request) {
    try {
      return ResponseEntity.ok(notificationService.updateStatus(id, request));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable Long id) {
    try {
      notificationService.delete(id);
      return ResponseEntity.ok().build();
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<?> findByUserId(@PathVariable Long userId) {
    try {
      return ResponseEntity.ok(notificationService.findByUserId(userId));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/reservation/{reservationId}")
  public ResponseEntity<?> findByReservationId(@PathVariable Long reservationId) {
    try {
      return ResponseEntity.ok(notificationService.findByReservationId(reservationId));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<?> findByStatus(@PathVariable String status) {
    try {
      return ResponseEntity.ok(notificationService.findByStatus(status));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/stats")
  public ResponseEntity<?> getStats() {
    try {
      return ResponseEntity.ok(notificationService.getStats());
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/user/{userId}/stats")
  public ResponseEntity<?> getUserStats(@PathVariable Long userId) {
    try {
      return ResponseEntity.ok(notificationService.getUserStats(userId));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/{id}/retry")
  public ResponseEntity<?> retryNotification(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(notificationService.retryNotification(id));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/reservation/{reservationId}/confirmation")
  public ResponseEntity<?> sendReservationConfirmation(@PathVariable Long reservationId) {
    try {
      return ResponseEntity.ok(notificationService.sendReservationNotification(
              reservationId, NotificationType.RESERVATION_CONFIRMED));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/payment/{paymentId}")
  public ResponseEntity<?> sendPaymentNotification(@PathVariable Long paymentId,
                                                   @RequestParam Long userId,
                                                   @RequestParam boolean success) {
    try {
      return ResponseEntity.ok(notificationService.sendPaymentNotification(userId, paymentId, success));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/user/{userId}")
  public ResponseEntity<?> createForUser(@PathVariable Long userId,
                                         @RequestParam NotificationType type,
                                         @RequestParam String title,
                                         @RequestParam String message) {
    try {
      return ResponseEntity.ok(notificationService.createForUser(userId, type, title, message));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/bulk")
  public ResponseEntity<?> sendBulkNotification(@RequestBody BulkNotificationRequestDTO request) {
    try {
      notificationService.sendBulkNotification(
              request.getUserIds(),
              request.getType(),
              request.getTitle(),
              request.getMessage()
      );
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/user/{userId}/unread")
  public ResponseEntity<?> getUnreadNotifications(@PathVariable Long userId) {
    try {
      return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @PutMapping("/{id}/read")
  public ResponseEntity<?> markAsRead(@PathVariable Long id,
                                      @RequestParam Long userId) {
    try {
      notificationService.markAsRead(id, userId);
      return ResponseEntity.ok().build();
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @PutMapping("/user/{userId}/read-all")
  public ResponseEntity<?> markAllAsRead(@PathVariable Long userId) {
    try {
      notificationService.markAllAsRead(userId);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/welcome/{userId}")
  public ResponseEntity<?> sendWelcomeNotification(@PathVariable Long userId) {
    try {
      notificationService.sendWelcomeNotification(userId);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/reservation/{reservationId}")
  public ResponseEntity<?> sendReservationNotification(@PathVariable Long reservationId,
                                                       @RequestParam NotificationType type) {
    try {
      return ResponseEntity.ok(notificationService.sendReservationNotification(reservationId, type));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }
}