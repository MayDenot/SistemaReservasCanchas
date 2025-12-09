package org.example.microserviceuser.service.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserProfileResponseDTO {
  private Long id;
  private String email;
  private String name;
  private String phone;
  private LocalDateTime createdAt;
  private Boolean isActive;
}
