package org.example.microservicenotification.feignClient;

import org.example.microservicenotification.service.dto.ReservationBasicInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "microservice-reservation", url = "http://localhost:8080")
public interface ReservationClient {

  @GetMapping("/api/reservations/{id}/basic-info")
  ReservationBasicInfoDTO getReservationBasicInfo(@PathVariable("id") Long id);

  @GetMapping("/api/reservations/{id}/exists")
  boolean reservationExists(@PathVariable("id") Long id);

  @GetMapping("/api/reservations/{id}")
  Object getReservationById(@PathVariable("id") Long id);
}
