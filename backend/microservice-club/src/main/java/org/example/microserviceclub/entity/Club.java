package org.example.microserviceclub.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "clubs")
public class Club {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false)
  private String name;
  @Column(nullable = false)
  private String address;
  @Column
  private String phone;
  @Column(name = "opening_time", nullable = false)
  private LocalTime openingTime;
  @Column(name = "closing_time", nullable = false)
  private LocalTime closingTime;
  @Column(name = "admin_id", nullable = false)
  private Long adminId;
}
