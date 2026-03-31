package org.example.microservicecourt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.common.dto.CourtType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(name = "courts")
public class Court {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "club_id", nullable = false)
  private Long clubId;
  @Column(nullable = false)
  private String name;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CourtType type;
  @Column(name = "price_per_hour", nullable = false, precision = 10, scale = 2)
  private BigDecimal pricePerHour;
  @Column(name = "is_active")
  private Boolean isActive;
  @Column(name = "description")
  private String description;
  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();
  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();
}