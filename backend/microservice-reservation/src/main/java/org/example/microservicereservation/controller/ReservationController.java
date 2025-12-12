package org.example.microservicereservation.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.microservicereservation.service.ReservationService;
import org.example.microservicereservation.service.dto.ReservationConflictDTO;
import org.example.microservicereservation.service.dto.request.ReservationRequestDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
  private final ReservationService reservationService;

  @GetMapping
  public ResponseEntity<?> findAll() {
    try {
      return ResponseEntity.ok(reservationService.findAll());
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/my-reservations")
  public ResponseEntity<?> getMyReservations(
          @RequestHeader("X-User-Email") String userEmail
  ) {
    try {
      return ResponseEntity.ok(reservationService.findByUserEmail(userEmail));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> findById(@PathVariable("id") Long id) {
    try {
      return ResponseEntity.ok(reservationService.findById(id));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping()
  public ResponseEntity<?> save(@RequestBody ReservationRequestDTO request) {
    try {
      return ResponseEntity.ok(reservationService.save(request));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(@PathVariable("id") Long id, @RequestBody ReservationRequestDTO request) {
    try {
      return ResponseEntity.ok(reservationService.update(id, request));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable("id") Long id) {
    try {
      reservationService.delete(id);
      return ResponseEntity.ok().build();
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{id}/exists")
  public ResponseEntity<Boolean> existsById(@PathVariable("id") Long id) {
    return ResponseEntity.ok(reservationService.existsById(id));
  }

  @PatchMapping("/{id}/payment-status")
  public ResponseEntity<Void> updatePaymentStatus(
          @PathVariable("id") Long id,
          @RequestBody Map<String, String> request) {
    String status = request.get("paymentStatus");
    reservationService.updatePaymentStatus(id, status);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{id}/pending-amount")
  public ResponseEntity<BigDecimal> getPendingAmount(@PathVariable("id") Long id) {
    return ResponseEntity.ok(reservationService.getPendingAmount(id));
  }

  @PostMapping("/{id}/apply-payment")
  public ResponseEntity<Void> applyPayment(
          @PathVariable("id") Long id,
          @RequestBody Map<String, Object> request) {
    BigDecimal amount = new BigDecimal(request.get("amount").toString());
    String method = (String) request.get("paymentMethod");
    String transactionId = (String) request.get("transactionId");
    String notes = (String) request.get("notes");

    reservationService.applyPayment(id, amount, method, transactionId, notes);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/conflicts")
  public ResponseEntity<?> hasReservationConflict(
          @RequestParam("courtId") Long courtId,
          @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
          @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
    try {
      boolean hasConflict = reservationService.hasReservationConflict(courtId, startTime, endTime);
      return ResponseEntity.ok(hasConflict);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(Map.of("error", "Error interno del servidor"));
    }
  }

  @GetMapping("/conflicts/details")
  public ResponseEntity<?> getConflictingReservations(
          @RequestParam("courtId") Long courtId,
          @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
          @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
    try {
      List<ReservationConflictDTO> conflicts =
              reservationService.getConflictingReservations(courtId, startTime, endTime);
      return ResponseEntity.ok(conflicts);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(Map.of("error", "Error interno del servidor"));
    }
  }
}
