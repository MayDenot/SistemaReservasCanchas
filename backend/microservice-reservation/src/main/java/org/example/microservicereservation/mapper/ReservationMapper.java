package org.example.microservicereservation.mapper;

import org.example.microservicereservation.entity.Reservation;
import org.example.microservicereservation.service.dto.request.ReservationRequestDTO;
import org.example.microservicereservation.service.dto.response.ReservationResponseDTO;

public class ReservationMapper {
  public static Reservation toEntity(ReservationRequestDTO dto) {
    return Reservation.builder()
            .userId(dto.getUserId())
            .courtId(dto.getCourtId())
            .clubId(dto.getClubId())
            .userEmail(dto.getUserEmail())
            .startTime(dto.getStartTime())
            .endTime(dto.getEndTime())
            .status(dto.getStatus())
            .paymentStatus(dto.getPaymentStatus())
            .createdAt(dto.getCreatedAt())
            .build();
  }

  public static ReservationResponseDTO toResponse(Reservation res) {
    return ReservationResponseDTO.builder()
            .id(res.getId())
            .userId(res.getUserId())
            .courtId(res.getCourtId())
            .clubId(res.getClubId())
            .userEmail(res.getUserEmail())
            .startTime(res.getStartTime())
            .endTime(res.getEndTime())
            .status(res.getStatus())
            .paymentStatus(res.getPaymentStatus())
            .createdAt(res.getCreatedAt())
            .build();
  }
}
