package org.example.microserviceclub.feignClient;

import org.example.common.dto.ReservationStatsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@FeignClient(name = "reservation-service", url = "http://reservation-service:8080")
public interface ReservationClient {
  @GetMapping("/api/reservations/club/{clubId}/stats")
  ReservationStatsDTO getReservationStatsByClub(
          @PathVariable("clubId") Long clubId,
          @RequestParam(value = "timeRange", defaultValue = "month") String timeRange);

  @GetMapping("/api/reservations/club/{clubId}/count")
  Long getReservationCountByClub(@PathVariable("clubId") Long clubId);

  @GetMapping("/api/reservations/courts/{courtId}/availability")
  Boolean isCourtAvailable(
          @PathVariable("courtId") Long courtId,
          @RequestParam("dateTime") LocalDateTime dateTime,
          @RequestParam("durationHours") int durationHours
  );
}
