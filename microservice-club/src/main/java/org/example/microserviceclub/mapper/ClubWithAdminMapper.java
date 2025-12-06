package org.example.microserviceclub.mapper;

import org.example.microserviceclub.entity.Club;
import org.example.common.dto.ClubWithAdminRequestDTO;
import org.example.common.dto.ClubWithAdminResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ClubWithAdminMapper {
  public static Club toEntity(ClubWithAdminRequestDTO dto) {
    return Club.builder()
            .name(dto.getName())
            .address(dto.getAddress())
            .phone(dto.getPhone())
            .openingTime(dto.getOpeningTime())
            .closingTime(dto.getClosingTime())
            .adminId(dto.getAdminId())
            .build();
  }

  public static ClubWithAdminResponseDTO toResponse(Club club, Long userId) {
    return ClubWithAdminResponseDTO.builder()
            .id(club.getId())
            .name(club.getName())
            .address(club.getAddress())
            .phone(club.getPhone())
            .openingTime(club.getOpeningTime())
            .closingTime(club.getClosingTime())
            .userId(userId)
            .build();
  }
}
