package org.example.common.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ClubWithAdminResponseDTO {
  private Long id;
  private String name;
  private String address;
  private String phone;
  private LocalTime openingTime;
  private LocalTime closingTime;
  private Long userId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public String getOperatingHours() {
    if (openingTime != null && closingTime != null) {
      return openingTime + " - " + closingTime;
    }
    return null;
  }
}
