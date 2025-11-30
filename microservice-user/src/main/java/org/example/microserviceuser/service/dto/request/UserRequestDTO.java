package org.example.microserviceuser.service.dto.request;

import lombok.*;
import org.example.microserviceuser.entity.UserRole;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserRequestDTO {
  private String email;
  private String password;
  private UserRole userRole;
  private String name;
  private String phone;
  private LocalDateTime createdAt;
}
