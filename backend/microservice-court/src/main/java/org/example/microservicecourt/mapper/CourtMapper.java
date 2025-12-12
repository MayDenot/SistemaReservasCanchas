package org.example.microservicecourt.mapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.microservicecourt.entity.Court;
import org.example.microservicecourt.feignClient.ClubClient;
import org.example.microservicecourt.service.dto.request.CourtRequestDTO;
import org.example.microservicecourt.service.dto.response.CourtResponseDTO;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourtMapper {

  private final ClubClient clubClient;
  private final Map<Long, String> clubNameCache = new ConcurrentHashMap<>();

  public Court toEntity(CourtRequestDTO dto) {
    if (dto == null) {
      return null;
    }

    return Court.builder()
            .clubId(dto.getClubId())
            .name(dto.getName())
            .type(dto.getType())
            .pricePerHour(dto.getPricePerHour())
            .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
            .build();
  }

  public CourtResponseDTO toResponse(Court court) {
    if (court == null) {
      return null;
    }

    return CourtResponseDTO.builder()
            .id(court.getId())
            .clubId(court.getClubId())
            .name(court.getName())
            .type(court.getType())
            .pricePerHour(court.getPricePerHour())
            .isActive(court.getIsActive())
            .clubName(getClubName(court.getClubId()))
            .build();
  }

  private String getClubName(Long clubId) {
    if (clubId == null) {
      return "Sin club";
    }

    if (clubNameCache.containsKey(clubId)) {
      String cached = clubNameCache.get(clubId);
      log.info("✅ Usando cache para clubId {}: {}", clubId, cached);
      return cached;
    }

    String clubName;

    try {
      var clubResponse = clubClient.getClubById(clubId);
      clubName = clubResponse.getName();
      log.info("✅ Obtenido nombre del club {}: {}", clubId, clubName);

      clubNameCache.put(clubId, clubName);
    } catch (Exception e) {
      log.error("❌ Error obteniendo club {}: {}", clubId, e.getMessage());
      clubName = "Club #" + clubId;
      clubNameCache.put(clubId, clubName);
    }

    return clubName;
  }

  public Court updateEntity(Court court, CourtRequestDTO dto) {
    if (court == null || dto == null) {
      return court;
    }

    if (dto.getClubId() != null) {
      court.setClubId(dto.getClubId());
      // Limpiar cache si cambia el clubId
      clubNameCache.remove(court.getClubId());
    }
    if (dto.getName() != null) {
      court.setName(dto.getName());
    }
    if (dto.getType() != null) {
      court.setType(dto.getType());
    }
    if (dto.getPricePerHour() != null) {
      court.setPricePerHour(dto.getPricePerHour());
    }
    if (dto.getIsActive() != null) {
      court.setIsActive(dto.getIsActive());
    }

    return court;
  }

  @PostConstruct
  public void init() {
    log.info("🧹 Limpiando cache de nombres de clubs al iniciar");
    clubNameCache.clear();
  }
}