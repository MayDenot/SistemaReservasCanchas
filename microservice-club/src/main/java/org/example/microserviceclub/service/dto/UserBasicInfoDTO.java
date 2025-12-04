package org.example.microserviceclub.service.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBasicInfoDTO {
  private Long id;
  private String username;
  private String email;
  private String firstName;
  private String lastName;
  private String fullName;
}
