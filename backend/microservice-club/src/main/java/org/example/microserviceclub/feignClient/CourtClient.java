package org.example.microserviceclub.feignClient;

import org.example.common.dto.CourtResponseDTO;
import org.example.common.dto.CourtStatsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "court-service", url = "http://court-service:8080")
public interface CourtClient {
  @GetMapping("/api/courts/club/{clubId}")
  List<CourtResponseDTO> findByClubId(@PathVariable("clubId") Long clubId);

  @GetMapping("/api/courts/club/{clubId}/stats")
  CourtStatsDTO getCourtStatsByClub(@PathVariable("clubId") Long clubId);

  @GetMapping("/api/courts/club/{clubId}/count")
  Long getCourtCountByClub(@PathVariable("clubId") Long clubId);
}
