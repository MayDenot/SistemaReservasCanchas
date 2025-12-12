package org.example.microservicereservation.feignClient;

import org.example.microservicereservation.service.dto.CourtDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "microservice-court", url = "http://court-service:8080")
public interface CourtClient {
  // Obtener cancha por ID
  @GetMapping("/{id}")
  CourtDTO findById(@PathVariable("id") Long id);

  // Verificar si una cancha existe
  @GetMapping("/{id}/exists")
  boolean courtExists(@PathVariable("id") Long id);

  // Verificar disponibilidad de una cancha
  @GetMapping("/{courtId}/check-availability")
  boolean isCourtAvailable(@PathVariable("courtId") Long courtId,
                           @RequestParam("startTime") LocalDateTime startTime,
                           @RequestParam("endTime") LocalDateTime endTime);
}
