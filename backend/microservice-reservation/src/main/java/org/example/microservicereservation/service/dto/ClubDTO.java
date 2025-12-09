package org.example.microservicereservation.service.dto;

import lombok.*;

import java.time.LocalTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ClubDTO {
  private Long id;
  private String name;
  private String address;
  private String phone;
  private LocalTime openingTime;
  private LocalTime closingTime;
  private Boolean isActive;
}
