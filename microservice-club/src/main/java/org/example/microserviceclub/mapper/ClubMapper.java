package org.example.microserviceclub.mapper;

import org.example.microserviceclub.entity.Club;
import org.example.microserviceclub.service.dto.UserBasicInfoDTO;
import org.example.microserviceclub.service.dto.request.ClubRequestDTO;
import org.example.microserviceclub.service.dto.response.ClubResponseDTO;
import org.example.microserviceclub.service.dto.response.ClubWithAdminResponseDTO;

public class ClubMapper {
  public static Club toEntity(ClubRequestDTO dto) {
    return Club.builder()
            .name(dto.getName())
            .address(dto.getAddress())
            .phone(dto.getPhone())
            .openingTime(dto.getOpeningTime())
            .closingTime(dto.getClosingTime())
            .adminId(dto.getAdminId())
            .build();
  }

  public static ClubResponseDTO toResponse(Club club) {
    return ClubResponseDTO.builder()
            .id(club.getId())
            .name(club.getName())
            .address(club.getAddress())
            .phone(club.getPhone())
            .openingTime(club.getOpeningTime())
            .closingTime(club.getClosingTime())
            .adminId(club.getAdminId())
            .build();
  }

  public static ClubWithAdminResponseDTO toResponseWithUser(Club club, UserBasicInfoDTO user) {
    return ClubWithAdminResponseDTO.builder()
            .id(club.getId())
            .name(club.getName())
            .address(club.getAddress())
            .phone(club.getPhone())
            .openingTime(club.getOpeningTime())
            .closingTime(club.getClosingTime())
            .userId(user.getId())
            .build();
  }
}
