package org.example.microservicecourt.repository;

import org.example.microservicecourt.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
  boolean existsByNameAndIdNotAndClubId(String name, Long id, Long clubId);
  List<Court> findByClubId(Long clubId);
  List<Court> findByClubIdAndIsActiveTrue(Long clubId);
  boolean existsByNameAndClubId(String name, Long clubId);
  List<Court> findByIdIn(List<Long> ids);

  // Contar canchas por club
  @Query("SELECT COUNT(c) FROM Court c WHERE c.clubId = :clubId")
  Long countByClubId(@Param("clubId") Long clubId);

  // Contar canchas activas por club
  @Query("SELECT COUNT(c) FROM Court c WHERE c.clubId = :clubId AND c.isActive = true")
  Long countByClubIdAndIsActiveTrue(@Param("clubId") Long clubId);

  // Verificar si existe cancha con mismo nombre en el mismo club
  @Query("SELECT COUNT(c) > 0 FROM Court c WHERE c.clubId = :clubId AND LOWER(c.name) = LOWER(:name)")
  boolean existsByClubIdAndName(@Param("clubId") Long clubId, @Param("name") String name);
}
