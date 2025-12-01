package org.example.microserviceclub.service;

import lombok.RequiredArgsConstructor;
import org.example.microserviceclub.entity.Club;
import org.example.microserviceclub.mapper.ClubMapper;
import org.example.microserviceclub.repository.ClubRepository;
import org.example.microserviceclub.service.dto.request.ClubRequestDTO;
import org.example.microserviceclub.service.dto.response.ClubResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubService {
  private final ClubRepository clubRepository;

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
}
