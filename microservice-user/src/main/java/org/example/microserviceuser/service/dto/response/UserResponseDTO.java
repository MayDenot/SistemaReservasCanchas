package org.example.microserviceuser.service.dto.response;

import lombok.*;
import org.example.microserviceuser.entity.UserRole;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserResponseDTO {
  private Long id;
  private String email;
  private UserRole userRole;
  private String name;
  private String phone;
  private LocalDateTime createdAt;
}
