package org.example.microserviceuser.service.dto.request;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserLoginRequestDTO {
  private String email;
  private String password;
}
