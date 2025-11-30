package org.example.microserviceuser.mapper;

import org.example.microserviceuser.entity.User;
import org.example.microserviceuser.service.dto.request.UserRequestDTO;
import org.example.microserviceuser.service.dto.response.UserResponseDTO;

public class UserMapper {
  public static User toEntity(UserRequestDTO dto) {
    return User.builder()
            .email(dto.getEmail())
            .password(dto.getPassword())
            .userRole(dto.getUserRole())
            .name(dto.getName())
            .phone(dto.getPhone())
            .createdAt(dto.getCreatedAt())
            .build();
  }

  public static UserResponseDTO toResponse(User user) {
    return UserResponseDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .userRole(user.getUserRole())
            .name(user.getName())
            .phone(user.getPhone())
            .createdAt(user.getCreatedAt())
            .build();
  }
}
