package org.example.microservicecourt.feignClient;

import org.example.microservicecourt.config.FeignConfig;
import org.example.microservicecourt.service.dto.ReservationConflictDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "microservice-reservation", url = "http://reservation-service:8080"
  ,configuration = FeignConfig.class)
public interface ReservationClient {
  @GetMapping("/conflicts")
  boolean hasReservationConflict(@RequestParam("courtId") Long courtId,
                                 @RequestParam("startTime") LocalDateTime startTime,
                                 @RequestParam("endTime") LocalDateTime endTime);

  @GetMapping("/conflicts/details")
  List<ReservationConflictDTO> getConflictingReservations(@RequestParam("courtId") Long courtId,
                                                          @RequestParam("startTime") LocalDateTime startTime,
                                                          @RequestParam("endTime") LocalDateTime endTime);
}
