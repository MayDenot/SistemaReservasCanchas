package org.example.microservicepayment.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.microservicepayment.entity.PaymentStatus;
import org.example.microservicepayment.service.PaymentService;
import org.example.microservicepayment.service.dto.request.PaymentRequestDTO;
import org.example.microservicepayment.service.dto.request.PaymentUpdateRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
  private final PaymentService paymentService;

  @GetMapping()
  public ResponseEntity<?> findAll() {
    try {
      return ResponseEntity.ok(paymentService.findAll());
    }  catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> findByid(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(paymentService.findById(id));
    }  catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping()
  public ResponseEntity<?> save(@RequestBody PaymentRequestDTO request) {
    try {
      return ResponseEntity.ok(paymentService.save(request));
    }  catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PaymentUpdateRequestDTO request) {
    try {
      return ResponseEntity.ok(paymentService.update(id, request));
    }  catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable Long id) {
    try {
      Long deletedId = paymentService.delete(id);
      return ResponseEntity.ok(Map.of(
              "message", "Pago eliminado exitosamente",
              "id", deletedId
      ));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/{id}/exists")
  public ResponseEntity<?> existsById(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(paymentService.existsById(id));
    }  catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/reservation/{reservationId}")
  public ResponseEntity<?> findByReservationId(@PathVariable Long reservationId) {
    try {
      return ResponseEntity.ok(paymentService.findByReservationId(reservationId));
    }  catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<?> findByStatus(@RequestBody PaymentStatus status) {
    try {
      return ResponseEntity.ok(paymentService.findByStatus(status));
    }  catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/reservation/{reservationId}/total-paid")
  public ResponseEntity<?> getTotalPaidByReservation(@PathVariable Long reservationId) {
    try {
      BigDecimal totalPaid = paymentService.getTotalPaidByReservation(reservationId);
      return ResponseEntity.ok(Map.of(
              "reservationId", reservationId,
              "totalPaid", totalPaid
      ));
    }  catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/{id}/cancel-by-reason")
  public ResponseEntity<?> cancelPaymentWithReason(@PathVariable Long id,
                                                   @RequestParam(required = false) String reason) {
    try {
      String cancelReason = (reason != null && !reason.isBlank()) ?
              reason : "Cancelado por el usuario";
      return ResponseEntity.ok(paymentService.cancelPayment(id, cancelReason));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (IllegalArgumentException | IllegalStateException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
              .body(Map.of("error", "Error al cancelar el pago"));
    }
  }
}
