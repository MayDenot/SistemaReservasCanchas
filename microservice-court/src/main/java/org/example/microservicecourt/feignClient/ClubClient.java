package org.example.microservicecourt.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "microservice-club", url = "http://localhost:8086")
public interface ClubClient {
  @GetMapping("/{id}")
  ClubResponseDTO getClubById(@PathVariable Long id);

  @GetMapping("/{id}/exists")
  boolean existsById(@PathVariable Long id);
}
