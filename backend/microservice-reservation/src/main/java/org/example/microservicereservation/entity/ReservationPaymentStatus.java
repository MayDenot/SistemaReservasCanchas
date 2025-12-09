package org.example.microservicereservation.entity;

public enum ReservationPaymentStatus {
  PENDING,    // Pendiente de pago
  CONFIRMED,  // Pago confirmado (sync con Payment Service)
  FAILED,     // Pago fallido
  CANCELLED,  // Pago cancelado
  REFUNDED    // Reembolsado
}
