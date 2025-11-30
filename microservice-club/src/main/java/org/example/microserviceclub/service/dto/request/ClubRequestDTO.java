package org.example.microserviceclub.service.dto.request;

import lombok.*;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ClubRequestDTO {
  private String name;
  private String address;
  private String phone;
  private LocalTime openingTime;
  private LocalTime closingTime;
  private Long adminId;
}
