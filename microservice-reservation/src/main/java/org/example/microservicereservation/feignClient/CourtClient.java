package org.example.microservicereservation.feignClient;

import org.example.microservicereservation.service.dto.CourtDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "microservice-court", url = "http://localhost:8085")
public interface CourtClient {
  // Obtener cancha por ID
  @GetMapping("/{id}")
  CourtDTO findById(@PathVariable Long id);

  // Verificar si una cancha existe
  @GetMapping("/{id}/exists")
  boolean courtExists(@PathVariable Long id);

  // Obtener canchas por club
  @GetMapping("/club/{clubId}")
  List<CourtDTO> getCourtsByClub(@PathVariable Long clubId);

  // Verificar disponibilidad de una cancha
  @GetMapping("/{courtId}/available")
  boolean isCourtAvailable(@PathVariable Long courtId,
                           @RequestParam LocalDateTime startTime,
                           @RequestParam LocalDateTime endTime);
}
