package org.example.microservicereservation.feignClient;

import org.example.microservicereservation.service.dto.ClubDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@FeignClient(name = "microservice-club", url = "http://localhost:8080")
public interface ClubClient {
  @GetMapping("/{id}/exists")
  boolean clubExists(@PathVariable("id") Long id);

  @GetMapping("/{id}")
  ClubDTO findById(@PathVariable("id") Long id);

  @GetMapping("/{id}/is-open")
  boolean isClubOpen(@PathVariable("id") Long id,
                     @RequestParam("dateTime") LocalDateTime dateTime);
}
