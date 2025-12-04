package org.example.microservicecourt.feignClient;

import org.example.microservicecourt.service.dto.ReservationConflictDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "microservice-reservation", url = "http://localhost:8082")
public interface ReservationClient {
  @GetMapping("/conflicts")
  boolean hasReservationConflict(@RequestParam Long courtId,
                                 @RequestParam LocalDateTime startTime,
                                 @RequestParam LocalDateTime endTime);

  @GetMapping("/conflicts/details")
  List<ReservationConflictDTO> getConflictingReservations(@RequestParam Long courtId,
                                                          @RequestParam LocalDateTime startTime,
                                                          @RequestParam LocalDateTime endTime);
}
