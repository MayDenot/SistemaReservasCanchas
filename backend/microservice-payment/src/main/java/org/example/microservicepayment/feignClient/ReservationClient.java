package org.example.microservicepayment.feignClient;

import org.example.microservicepayment.service.dto.ApplyPaymentRequest;
import org.example.microservicepayment.service.dto.request.UpdatePaymentStatusRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@FeignClient(name = "microservice-reservation", url = "http://localhost:8080")
public interface ReservationClient {
  @GetMapping("/api/reservations/{id}/exists")
  Boolean existsById(@PathVariable("id") Long id);

  @PatchMapping("/api/reservations/{id}/payment-status")
  ResponseEntity<Void> updatePaymentStatus(
          @PathVariable("id") Long id,
          @RequestBody UpdatePaymentStatusRequest request);

  @GetMapping("/api/reservations/{id}/pending-amount")
  BigDecimal getPendingAmount(@PathVariable("id") Long id);

  @PostMapping("/api/reservations/{id}/apply-payment")
  ResponseEntity<Void> applyPayment(
          @PathVariable("id") Long id,
          @RequestBody ApplyPaymentRequest request);
}
