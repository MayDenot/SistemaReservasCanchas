package org.example.microserviceuser.mapper;

import org.example.microserviceuser.entity.User;
import org.example.microserviceuser.service.dto.request.UserProfileRequestDTO;
import org.example.microserviceuser.service.dto.request.UserRequestDTO;
import org.example.microserviceuser.service.dto.response.UserLoginResponseDTO;
import org.example.microserviceuser.service.dto.response.UserProfileResponseDTO;
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

  public static UserProfileResponseDTO toProfileResponse(User user) {
    return UserProfileResponseDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .phone(user.getPhone())
            .createdAt(user.getCreatedAt())
            .build();
  }

  public static UserLoginResponseDTO toLoginResponse(User user, String token) {
    UserResponseDTO userResponse = toResponse(user);

    return UserLoginResponseDTO.builder()
            .token(token)
            .tokenType("Bearer")
            .expiresIn(3600L) // 1 hora en segundos
            .user(userResponse)
            .build();
  }

  public static UserLoginResponseDTO toLoginResponse(User user, String token, String tokenType, Long expiresIn) {
    UserResponseDTO userResponse = toResponse(user);

    return UserLoginResponseDTO.builder()
            .token(token)
            .tokenType(tokenType)
            .expiresIn(expiresIn)
            .user(userResponse)
            .build();
  }

  public static boolean hasPasswordChange(UserProfileRequestDTO dto) {
    return dto.getNewPassword() != null && !dto.getNewPassword().trim().isEmpty();
  }
}
