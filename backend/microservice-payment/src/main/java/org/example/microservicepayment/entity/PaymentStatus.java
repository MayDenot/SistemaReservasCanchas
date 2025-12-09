package org.example.microservicepayment.entity;

public enum PaymentStatus {
  PENDING,       // Creado pero no procesado
  PROCESSING,    // En proceso de pago
  COMPLETED,     // Pago exitoso
  FAILED,        // Pago fallido
  CANCELLED,     // Cancelado por usuario
  REFUNDED       // Reembolsado
}
