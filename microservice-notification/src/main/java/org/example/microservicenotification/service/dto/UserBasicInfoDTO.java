package org.example.microservicenotification.service.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserBasicInfoDTO {
  private Long id;
  private String email;
  private String phone;
  private String firstName;
  private String lastName;
  private boolean active;
}
