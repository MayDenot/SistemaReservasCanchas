package org.example.microserviceclub.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.microserviceclub.entity.Club;
import org.example.microserviceclub.feignClient.UserClient;
import org.example.microserviceclub.mapper.ClubMapper;
import org.example.microserviceclub.repository.ClubRepository;
import org.example.microserviceclub.service.dto.UserBasicInfoDTO;
import org.example.microserviceclub.service.dto.request.ClubRequestDTO;
import org.example.microserviceclub.service.dto.response.ClubResponseDTO;
import org.example.microserviceclub.service.dto.response.ClubWithAdminResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubService {
  private final ClubRepository clubRepository;
  private final UserClient userClient;

  @Transactional(readOnly = true)
  public List<ClubResponseDTO> findAll() {
    return this.clubRepository.findAll()
            .stream()
            .map(ClubMapper::toResponse)
            .toList();
  }

  @Transactional(readOnly = true)
  public ClubResponseDTO findById(Long id) {
    return ClubMapper.toResponse(this.clubRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Club no encontrado con id: " + id)));
  }

  @Transactional
  public ClubResponseDTO update(Long id, ClubRequestDTO request) {
    Club club = this.clubRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Club no encontrado con id: " + id));


    club.setName(request.getName());
    club.setAddress(request.getAddress());
    club.setPhone(request.getPhone());
    club.setOpeningTime(request.getOpeningTime());
    club.setClosingTime(request.getClosingTime());

    return ClubMapper.toResponse(this.clubRepository.save(club));
  }

  @Transactional
  public ClubResponseDTO save(ClubRequestDTO request) {
    if (this.clubRepository.existsByName(request.getName())) {
      throw new RuntimeException("Ya existe un club con el nombre: " + request.getName());
    }

    Club club = new Club();
    club.setName(request.getName());
    club.setAddress(request.getAddress());
    club.setPhone(request.getPhone());
    club.setOpeningTime(request.getOpeningTime());
    club.setClosingTime(request.getClosingTime());

    Club savedClub = this.clubRepository.save(club);
    return ClubMapper.toResponse(savedClub);
  }

  @Transactional
  public Long delete(Long id) {
    if (!this.clubRepository.existsById(id)) {
      throw new RuntimeException("Club no encontrado con id: " + id);
    }
    this.clubRepository.deleteById(id);
    return id;
  }

  @Transactional
  public boolean existsByName(String name) {
    return this.clubRepository.existsByName(name);
  }

  @Transactional(readOnly = true)
  public ClubWithAdminResponseDTO getClubWithUser(Long clubId) {
    Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new RuntimeException("Club no encontrado"));

    UserBasicInfoDTO user = userClient.getUserBasic(club.getAdminId());

    return ClubMapper.toResponseWithUser(club, user);
  }

  @Transactional(readOnly = true)
  public boolean isClubOpenAt(Long clubId, LocalDateTime dateTime) {
    Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new EntityNotFoundException("Club no encontrado"));

    LocalTime timeToCheck = dateTime.toLocalTime();
    LocalTime openingTime = club.getOpeningTime();
    LocalTime closingTime = club.getClosingTime();

    // Verificar si la hora está dentro del horario de apertura
    return !timeToCheck.isBefore(openingTime) && !timeToCheck.isAfter(closingTime);
  }

  @Transactional(readOnly = true)
  public boolean existsById(Long id) {
    return clubRepository.existsById(id);
  }
}
