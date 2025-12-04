package org.example.microservicepayment.feignClient;

import org.example.microservicepayment.service.dto.ApplyPaymentRequest;
import org.example.microservicepayment.service.dto.request.UpdatePaymentStatusRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@FeignClient(name = "microservice-reservation", url = "http://localhost:8083")
public interface ReservationClient {
  @GetMapping("/api/reservations/{id}/exists")
  Boolean existsById(@PathVariable Long id);

  @PatchMapping("/api/reservations/{id}/payment-status")
  ResponseEntity<Void> updatePaymentStatus(
          @PathVariable Long id,
          @RequestBody UpdatePaymentStatusRequest request);

  @GetMapping("/api/reservations/{id}/pending-amount")
  BigDecimal getPendingAmount(@PathVariable Long id);

  @PostMapping("/api/reservations/{id}/apply-payment")
  ResponseEntity<Void> applyPayment(
          @PathVariable Long id,
          @RequestBody ApplyPaymentRequest request);
}
