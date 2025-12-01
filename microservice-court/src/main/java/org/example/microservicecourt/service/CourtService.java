package org.example.microservicecourt.service;

import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.microservicecourt.entity.Court;
import org.example.microservicecourt.feignClient.ClubClient;
import org.example.microservicecourt.mapper.CourtMapper;
import org.example.microservicecourt.repository.CourtRepository;
import org.example.microservicecourt.service.dto.request.CourtRequestDTO;
import org.example.microservicecourt.service.dto.response.CourtResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourtService {
  private final CourtRepository courtRepository;
  private final ClubClient clubServiceClient;

  @Transactional(readOnly = true)
  public List<CourtResponseDTO> findAll() {
    return this.courtRepository.findAll()
            .stream()
            .map(CourtMapper::toResponse)
            .toList();
  }

  @Transactional(readOnly = true)
  public CourtResponseDTO findById(Long id) {
    return CourtMapper.toResponse(this.courtRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cancha no encontrado con id: " + id)));
  }

  @Transactional
  public CourtResponseDTO update(Long id, CourtRequestDTO request) {
    Court court = courtRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Cancha no encontrada con id: " + id));

    if (courtRepository.existsByNameAndIdNotAndClubId(
            request.getName(), id, court.getClubId())) {
      throw new IllegalArgumentException("Ya existe otra cancha con el nombre: " + request.getName() + " en este club");
    }

    court.setName(request.getName());
    court.setType(request.getType());
    court.setPricePerHour(request.getPricePerHour());
    court.setIsActive(request.getIsActive());

    Court updatedCourt = courtRepository.save(court);
    return CourtMapper.toResponse(updatedCourt);
  }

  @Transactional
  public CourtResponseDTO save(Long clubId, CourtRequestDTO courtRequestDTO) {
    try {
      boolean clubExists = clubServiceClient.existsById(clubId);
      if (!clubExists) {
        throw new EntityNotFoundException("Club no encontrado con id: " + clubId);
      }
    } catch (FeignException.NotFound e) {
      throw new EntityNotFoundException("Club no encontrado con id: " + clubId);
    }

    if (courtRepository.existsByNameAndClubId(courtRequestDTO.getName(), clubId)) {
      throw new IllegalArgumentException("Ya existe una cancha con el nombre: " + courtRequestDTO.getName() + " en este club");
    }

    Court court = CourtMapper.toEntity(courtRequestDTO);
    court.setClubId(clubId);

    Court savedCourt = courtRepository.save(court);
    return CourtMapper.toResponse(savedCourt);
  }

  @Transactional
  public Long delete(Long id) {
    if (!this.courtRepository.existsById(id)) {
      throw new RuntimeException("Cancha no encontrado con id: " + id);
    }
    this.courtRepository.deleteById(id);
    return id;
  }

  @Transactional(readOnly = true)
  public boolean existsByNameAndClubId(String name, Long clubId) {
    return this.courtRepository.existsByNameAndClubId(name, clubId);
  }

  @Transactional(readOnly = true)
  public boolean existsByNameAndIdNotAndClubId(String name, Long id, Long clubId) {
    return this.courtRepository.existsByNameAndIdNotAndClubId(name, id, clubId);
  }

  @Transactional(readOnly = true)
  public List<CourtResponseDTO> findByClubId(Long clubId) {
    return this.courtRepository.findByClubId(clubId)
            .stream()
            .map(CourtMapper::toResponse)
            .toList();
  }

  @Transactional(readOnly = true)
  public List<CourtResponseDTO> findByClubIdAndIsActiveTrue(Long clubId) {
    return this.courtRepository.findByClubIdAndIsActiveTrue(clubId)
            .stream()
            .map(CourtMapper::toResponse)
            .toList();
  }
}
