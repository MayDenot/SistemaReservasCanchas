package org.example.microservicecourt.feignClient;

import org.example.common.dto.ClubResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@FeignClient(name = "microservice-club", url = "http://club-service:8080")
public interface ClubClient {
  @GetMapping("/api/clubs/{id}")
  ClubResponseDTO getClubById(@PathVariable("id") Long id);

  @GetMapping("/api/clubs/{id}/exists")
  Boolean clubExists(@PathVariable("id") Long id);

  @GetMapping("/api/clubs/{id}/is-open")
  Boolean isClubOpen(@PathVariable("id") Long id,
                     @RequestParam("dateTime") LocalDateTime dateTime);
}
