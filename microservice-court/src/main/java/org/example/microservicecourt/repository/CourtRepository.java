package org.example.microservicecourt.repository;

import org.example.microservicecourt.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
  boolean existsByNameAndClubId(String name, Long clubId);
  boolean existsByNameAndIdNotAndClubId(String name, Long id, Long clubId);
  List<Court> findByClubId(Long clubId);
  List<Court> findByClubIdAndIsActiveTrue(Long clubId);
}
