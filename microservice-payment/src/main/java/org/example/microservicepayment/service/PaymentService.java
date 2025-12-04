package org.example.microservicepayment.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.microservicepayment.entity.Payment;
import org.example.microservicepayment.entity.PaymentMethod;
import org.example.microservicepayment.entity.PaymentStatus;
import org.example.microservicepayment.feignClient.ReservationClient;
import org.example.microservicepayment.mapper.PaymentMapper;
import org.example.microservicepayment.repository.PaymentRepository;
import org.example.microservicepayment.service.dto.ApplyPaymentRequest;
import org.example.microservicepayment.service.dto.request.PaymentRequestDTO;
import org.example.microservicepayment.service.dto.request.PaymentUpdateRequestDTO;
import org.example.microservicepayment.service.dto.request.UpdatePaymentStatusRequest;
import org.example.microservicepayment.service.dto.response.PaymentResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
  private final PaymentRepository paymentRepository;
  private final ReservationClient reservationClient;

  @Transactional(readOnly = true)
  public List<PaymentResponseDTO> findAll() {
    return this.paymentRepository.findAll()
            .stream()
            .map(PaymentMapper::toResponse)
            .toList();
  }

  @Transactional(readOnly = true)
  public PaymentResponseDTO findById(Long id) {
    return PaymentMapper.toResponse(
            paymentRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado con id: " + id))
    );
  }

  @Transactional
  public PaymentResponseDTO save(PaymentRequestDTO request) {
    validateReservation(request.getReservationId());
    validatePaymentRequest(request);
    checkExistingPaymentForReservation(request.getReservationId());

    Payment payment = PaymentMapper.toEntity(request);
    payment.setStatus(PaymentStatus.PENDING);
    payment.setCreatedAt(LocalDateTime.now());
    payment.setUpdatedAt(LocalDateTime.now());

    if (requiresExternalPaymentId(payment.getMethod())) {
      String externalId = generateExternalPaymentId(payment);
      payment.setExternalPaymentId(externalId);
    }

    Payment savedPayment = paymentRepository.save(payment);

    // Procesar pago externo si es necesario
    processExternalPaymentIfNeeded(savedPayment);

    // Aplicar pago a la reserva
    applyPaymentToReservation(savedPayment.getReservationId(), savedPayment);

    log.info("Pago creado - ID: {}, Reserva: {}, Monto: {}, Método: {}",
            savedPayment.getId(), savedPayment.getReservationId(),
            savedPayment.getAmount(), savedPayment.getMethod());

    return PaymentMapper.toResponse(savedPayment);
  }

  @Transactional
  public PaymentResponseDTO update(Long id, PaymentUpdateRequestDTO dto) {
    if (dto.getStatus() == null && dto.getExternalPaymentId() == null) {
      throw new IllegalArgumentException("Debe proporcionar al menos un campo para actualizar");
    }

    Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado con id: " + id));

    if (!canUpdatePayment(payment)) {
      throw new IllegalStateException(
              String.format("No se puede actualizar un pago con estado: %s", payment.getStatus())
      );
    }

    applyPaymentUpdates(payment, dto);
    payment.setUpdatedAt(LocalDateTime.now());

    Payment updatedPayment = paymentRepository.save(payment);
    executeStateChangeActions(payment, dto.getStatus());
    auditPaymentUpdate(updatedPayment, dto);

    return PaymentMapper.toResponse(updatedPayment);
  }

  @Transactional
  public Long delete(Long id) {
    if (!this.paymentRepository.existsById(id)) {
      throw new EntityNotFoundException("Pago no encontrado con id: " + id);
    }

    Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado con id: " + id));

    if (!canDeletePayment(payment)) {
      throw new IllegalStateException(
              String.format("No se puede eliminar un pago con estado: %s", payment.getStatus())
      );
    }

    this.paymentRepository.deleteById(id);
    log.info("Pago eliminado - ID: {}", id);
    return id;
  }

  @Transactional(readOnly = true)
  public boolean existsById(Long id) {
    return this.paymentRepository.existsById(id);
  }

  @Transactional(readOnly = true)
  public Optional<PaymentResponseDTO> findByReservationId(Long reservationId) {
    return paymentRepository.findByReservationId(reservationId)
            .map(PaymentMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public List<PaymentResponseDTO> findByStatus(PaymentStatus status) {
    return paymentRepository.findByStatus(status)
            .stream()
            .map(PaymentMapper::toResponse)
            .toList();
  }

  @Transactional(readOnly = true)
  public BigDecimal getTotalPaidByReservation(Long reservationId) {
    return paymentRepository.findByReservationIdAndStatus(reservationId, PaymentStatus.COMPLETED)
            .stream()
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Transactional
  public PaymentResponseDTO cancelPayment(Long id, String reason) {
    Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado con id: " + id));

    if (payment.getStatus() == PaymentStatus.COMPLETED) {
      throw new IllegalStateException("No se puede cancelar un pago ya completado. Use reembolso.");
    }

    payment.setStatus(PaymentStatus.CANCELLED);
    payment.setUpdatedAt(LocalDateTime.now());

    Payment cancelledPayment = paymentRepository.save(payment);

    // Actualizar estado en reserva
    updateReservationPaymentStatus(payment.getReservationId(), PaymentStatus.CANCELLED);

    log.info("Pago cancelado - ID: {}, Razón: {}", id, reason);

    return PaymentMapper.toResponse(cancelledPayment);
  }

  private void validateReservation(Long reservationId) {
    if (reservationId == null) {
      throw new IllegalArgumentException("El ID de reserva es obligatorio");
    }

    try {
      boolean reservationExists = reservationClient.existsById(reservationId);
      if (!reservationExists) {
        throw new EntityNotFoundException("Reserva no encontrada con id: " + reservationId);
      }
    } catch (Exception e) {
      log.error("Error al validar la reserva con ID: {}", reservationId, e);
      throw new RuntimeException("Error al validar la reserva: " + e.getMessage());
    }
  }

  private void validatePaymentRequest(PaymentRequestDTO request) {
    if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("El monto debe ser mayor a cero");
    }

    if (request.getMethod() == null) {
      throw new IllegalArgumentException("El método de pago es obligatorio");
    }

    if (request.getAmount().compareTo(new BigDecimal("1000000")) > 0) {
      throw new IllegalArgumentException("El monto excede el límite permitido");
    }
  }

  private void checkExistingPaymentForReservation(Long reservationId) {
    Optional<Payment> existingPayment = paymentRepository.findByReservationId(reservationId);
    if (existingPayment.isPresent()) {
      Payment existing = existingPayment.get();
      if (existing.getStatus() != PaymentStatus.CANCELLED &&
              existing.getStatus() != PaymentStatus.FAILED &&
              existing.getStatus() != PaymentStatus.REFUNDED) {
        throw new RuntimeException(
                String.format("Ya existe un pago %s para la reserva id: %s",
                        existing.getStatus(), reservationId)
        );
      }
    }
  }

  private String generateExternalPaymentId(Payment payment) {
    return String.format("EXT-%s-%d-%d",
            payment.getMethod().toString().substring(0, 3),
            payment.getReservationId(),
            System.currentTimeMillis() % 10000);
  }

  private boolean requiresExternalPaymentId(PaymentMethod method) {
    return method == PaymentMethod.MERCADOPAGO ||
            method == PaymentMethod.STRIPE ||
            method == PaymentMethod.PAYPAL;
  }

  private boolean requiresExternalPaymentProcessing(PaymentMethod method) {
    return method == PaymentMethod.MERCADOPAGO ||
            method == PaymentMethod.STRIPE ||
            method == PaymentMethod.PAYPAL;
  }

  private void processExternalPaymentIfNeeded(Payment payment) {
    if (requiresExternalPaymentProcessing(payment.getMethod())) {
      try {
        log.info("Iniciando procesamiento de pago externo para pago ID: {}", payment.getId());

        // Simular procesamiento
        if (payment.getMethod() == PaymentMethod.MERCADOPAGO) {
          payment.setStatus(PaymentStatus.PROCESSING);
          paymentRepository.save(payment);
        }

      } catch (Exception e) {
        log.error("Error al procesar pago externo para pago ID: {}", payment.getId(), e);
        payment.setStatus(PaymentStatus.FAILED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        throw new RuntimeException("Error al procesar pago externo: " + e.getMessage());
      }
    }
  }

  private void applyPaymentUpdates(Payment payment, PaymentUpdateRequestDTO dto) {
    if (dto.getStatus() != null) {
      validateStatusTransition(payment.getStatus(), dto.getStatus());
      payment.setStatus(dto.getStatus());
    }

    if (dto.getExternalPaymentId() != null && !dto.getExternalPaymentId().trim().isEmpty()) {
      payment.setExternalPaymentId(dto.getExternalPaymentId().trim());
    }

    if (dto.getAmount() != null) {
      if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("El monto debe ser mayor a cero");
      }
      payment.setAmount(dto.getAmount());
    }

    if (dto.getMethod() != null) {
      payment.setMethod(dto.getMethod());
    }
  }

  private boolean canUpdatePayment(Payment payment) {
    return payment.getStatus() != PaymentStatus.COMPLETED &&
            payment.getStatus() != PaymentStatus.CANCELLED &&
            payment.getStatus() != PaymentStatus.REFUNDED;
  }

  private boolean canDeletePayment(Payment payment) {
    return payment.getStatus() == PaymentStatus.PENDING ||
            payment.getStatus() == PaymentStatus.FAILED ||
            payment.getStatus() == PaymentStatus.CANCELLED;
  }

  private void validateStatusTransition(PaymentStatus current, PaymentStatus newStatus) {
    Map<PaymentStatus, List<PaymentStatus>> validTransitions = Map.of(
            PaymentStatus.PENDING, List.of(PaymentStatus.PROCESSING, PaymentStatus.COMPLETED,
                    PaymentStatus.FAILED, PaymentStatus.CANCELLED),
            PaymentStatus.PROCESSING, List.of(PaymentStatus.COMPLETED, PaymentStatus.FAILED,
                    PaymentStatus.CANCELLED),
            PaymentStatus.COMPLETED, List.of(PaymentStatus.REFUNDED),
            PaymentStatus.FAILED, List.of(PaymentStatus.PENDING, PaymentStatus.PROCESSING),
            PaymentStatus.CANCELLED, List.of()
    );

    List<PaymentStatus> allowedTransitions = validTransitions.getOrDefault(current, List.of());
    if (!allowedTransitions.contains(newStatus)) {
      throw new IllegalArgumentException(
              String.format("Transición inválida de estado: %s → %s", current, newStatus)
      );
    }
  }

  private void executeStateChangeActions(Payment payment, PaymentStatus newStatus) {
    if (newStatus == null) return;

    switch (newStatus) {
      case COMPLETED:
        updateReservationPaymentStatus(payment.getReservationId(), PaymentStatus.COMPLETED);
        log.info("Pago completado - ID: {}, Reserva: {}", payment.getId(), payment.getReservationId());
        break;

      case FAILED:
        log.warn("Pago fallido - ID: {}, Reserva: {}", payment.getId(), payment.getReservationId());
        break;

      case CANCELLED:
        updateReservationPaymentStatus(payment.getReservationId(), PaymentStatus.CANCELLED);
        log.info("Pago cancelado - ID: {}, Reserva: {}", payment.getId(), payment.getReservationId());
        break;

      case REFUNDED:
        updateReservationPaymentStatus(payment.getReservationId(), PaymentStatus.REFUNDED);
        log.info("Pago reembolsado - ID: {}, Reserva: {}", payment.getId(), payment.getReservationId());
        break;
    }
  }

  private void updateReservationPaymentStatus(Long reservationId, PaymentStatus paymentStatus) {
    try {
      // Convertir PaymentStatus a String para enviar al otro microservicio
      String statusString = mapPaymentStatusToString(paymentStatus);

      UpdatePaymentStatusRequest request = UpdatePaymentStatusRequest.builder()
              .paymentStatus(statusString)
              .paymentReference("PAYMENT_" + LocalDateTime.now())
              .notes("Estado actualizado desde microservicio de pagos")
              .build();

      reservationClient.updatePaymentStatus(reservationId, request);
      log.debug("Estado de pago actualizado para reserva: {} -> {}", reservationId, statusString);

    } catch (Exception e) {
      log.error("Error al actualizar estado de pago de la reserva: {}", reservationId, e);
    }
  }

  private void applyPaymentToReservation(Long reservationId, Payment payment) {
    try {
      ApplyPaymentRequest request = ApplyPaymentRequest.builder()
              .amount(payment.getAmount())
              .paymentMethod(payment.getMethod().toString())
              .transactionId(payment.getExternalPaymentId() != null ?
                      payment.getExternalPaymentId() :
                      "INTERNAL_" + payment.getId())
              .notes("Pago aplicado desde microservicio de pagos")
              .build();

      reservationClient.applyPayment(reservationId, request);

    } catch (Exception e) {
      log.error("Error al aplicar pago a la reserva {}: {}", reservationId, e.getMessage());
    }
  }

  private String mapPaymentStatusToString(PaymentStatus paymentStatus) {
    return switch (paymentStatus) {
      case COMPLETED -> "PAID";
      case PENDING -> "PENDING";
      case PROCESSING -> "PROCESSING";
      case FAILED -> "FAILED";
      case CANCELLED -> "CANCELLED";
      case REFUNDED -> "REFUNDED";
      default -> "PENDING";
    };
  }

  private void auditPaymentUpdate(Payment payment, PaymentUpdateRequestDTO dto) {
    String auditMessage = String.format(
            "Pago ID %d actualizado. Estado: %s -> %s, Razón: %s, Usuario: %d, Monto: %s",
            payment.getId(),
            payment.getStatus(),
            dto.getStatus(),
            dto.getUpdateReason() != null ? dto.getUpdateReason() : "N/A",
            dto.getUpdatedByUserId() != null ? dto.getUpdatedByUserId() : 0,
            payment.getAmount()
    );

    log.info(auditMessage);
  }
}