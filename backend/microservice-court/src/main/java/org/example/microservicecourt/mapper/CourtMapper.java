package org.example.microservicecourt.mapper;

import org.example.microservicecourt.entity.Court;
import org.example.microservicecourt.service.dto.request.CourtRequestDTO;
import org.example.microservicecourt.service.dto.response.CourtResponseDTO;

public class CourtMapper {
  public static Court toEntity(CourtRequestDTO dto) {
    return Court.builder()
            .clubId(dto.getClubId())
            .name(dto.getName())
            .type(dto.getType())
            .pricePerHour(dto.getPricePerHour())
            .isActive(dto.getIsActive())
            .build();
  }

  public static CourtResponseDTO toResponse(Court court) {
    return CourtResponseDTO.builder()
            .id(court.getId())
            .clubId(court.getClubId())
            .name(court.getName())
            .type(court.getType())
            .pricePerHour(court.getPricePerHour())
            .isActive(court.getIsActive())
            .build();
  }
}
