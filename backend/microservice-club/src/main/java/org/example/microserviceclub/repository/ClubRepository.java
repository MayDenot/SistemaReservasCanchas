package org.example.microserviceclub.repository;

import org.example.microserviceclub.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {
  boolean existsByName(String name);
  List<Club> findByAdminId(Long adminId);
}
