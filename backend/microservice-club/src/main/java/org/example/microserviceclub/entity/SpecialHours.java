package org.example.microserviceclub.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "special_hours")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpecialHours {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "club_id", nullable = false)
  private Club club;

  @Column(name = "date", nullable = false)
  private LocalDate date;

  @Column(name = "opening_time")
  private LocalTime openingTime;

  @Column(name = "closing_time")
  private LocalTime closingTime;

  @Column(name = "reason")
  private String reason;

  @Column(name = "is_closed", nullable = false)
  private Boolean isClosed = false;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}