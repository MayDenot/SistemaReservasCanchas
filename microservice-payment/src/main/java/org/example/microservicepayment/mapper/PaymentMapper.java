package org.example.microservicepayment.mapper;


import org.example.microservicepayment.entity.Payment;
import org.example.microservicepayment.service.dto.request.PaymentRequestDTO;
import org.example.microservicepayment.service.dto.response.PaymentResponseDTO;

public class PaymentMapper {
  public static Payment toEntity(PaymentRequestDTO dto) {
    return Payment.builder()
            .reservationId(dto.getReservationId())
            .amount(dto.getAmount())
            .status(dto.getStatus())
            .method(dto.getMethod())
            .externalPaymentId(dto.getExternalPaymentId())
            .createdAt(dto.getCreatedAt())
            .updatedAt(dto.getUpdatedAt())
            .build();
  }

  public static PaymentResponseDTO toResponse(Payment pay) {
    return PaymentResponseDTO.builder()
            .id(pay.getId())
            .reservationId(pay.getReservationId())
            .amount(pay.getAmount())
            .status(pay.getStatus())
            .method(pay.getMethod())
            .externalPaymentId(pay.getExternalPaymentId())
            .createdAt(pay.getCreatedAt())
            .updatedAt(pay.getUpdatedAt())
            .build();
  }
}
