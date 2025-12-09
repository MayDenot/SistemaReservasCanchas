package org.example.microserviceuser.service.dto.request;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserProfileRequestDTO {
  private String name;
  private String phone;
  private String currentPassword;
  private String newPassword;
}
