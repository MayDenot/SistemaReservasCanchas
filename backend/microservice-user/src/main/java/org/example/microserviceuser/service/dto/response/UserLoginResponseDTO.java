package org.example.microserviceuser.service.dto.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserLoginResponseDTO {
  private String token;
  private String tokenType;
  private Long expiresIn;
  private UserResponseDTO user;
}
