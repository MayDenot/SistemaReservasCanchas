package org.example.microserviceclub.repository;

import org.example.microserviceclub.entity.SpecialHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialHoursRepository extends JpaRepository<SpecialHours, Long> {
  List<SpecialHours> findByClubId(Long clubId);
  List<SpecialHours> findByClubIdAndDate(Long clubId, LocalDate date);
  boolean existsByClubIdAndDate(Long clubId, LocalDate date);
}