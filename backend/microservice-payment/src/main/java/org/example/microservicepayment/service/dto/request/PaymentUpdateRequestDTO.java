package org.example.microservicepayment.service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.example.microservicepayment.entity.PaymentMethod;
import org.example.microservicepayment.entity.PaymentStatus;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PaymentUpdateRequestDTO {
  @NotNull(message = "El estado del pago es obligatorio")
  private PaymentStatus status;
  // ID del pago en el sistema externo (MercadoPago, Stripe, etc.)
  @Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "El ID externo solo puede contener letras, números, guiones y guiones bajos")
  private String externalPaymentId;
  // Puede ser útil para correcciones de monto (ajustes, descuentos, etc.)
  @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
  private BigDecimal amount;
  // Para cambios de método de pago (menos común, pero posible)
  private PaymentMethod method;
  // Metadata adicional para el pago
  private String metadata;
  // Razón de la actualización (útil para auditoría)
  private String updateReason;
  // Campos para notificaciones/observaciones
  private String notes;
  // Para pagos con tarjeta, podría actualizarse la última actualización del token
  @Pattern(regexp = "^[0-9]{4}$", message = "Los últimos 4 dígitos deben ser numéricos")
  private String lastFourDigits;
  // Referencia interna para conciliación
  private String internalReference;
  private Boolean manuallyApproved;
  // ID del usuario que realiza la actualización (para auditoría)
  private Long updatedByUserId;
}
