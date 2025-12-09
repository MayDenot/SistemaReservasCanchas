package org.example.microserviceclub.repository;

import org.example.microserviceclub.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {
  boolean existsByName(String name);
}
