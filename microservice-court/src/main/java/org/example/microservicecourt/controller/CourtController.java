package org.example.microservicecourt.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.microservicecourt.service.CourtService;
import org.example.microservicecourt.service.dto.request.CourtRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/courts")
@RequiredArgsConstructor
public class CourtController {
  private final CourtService courtService;

  @GetMapping()
  public ResponseEntity<?> findAll() {
    try {
      return ResponseEntity.ok(courtService.findAll());
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> findById(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(courtService.findById(id));
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping()
  public ResponseEntity<?> save(@PathVariable Long id, @RequestBody CourtRequestDTO request) {
    try {
      return ResponseEntity.ok(courtService.save(id, request));
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CourtRequestDTO request) {
    try {
      return ResponseEntity.ok(courtService.update(id, request));
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(courtService.delete(id));
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/club/{clubId}")
  public ResponseEntity<?> findByClubId(@PathVariable Long clubId) {
    try {
      return ResponseEntity.ok(courtService.findByClubId(clubId));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/club/{clubId}/active")
  public ResponseEntity<?> findByClubIdAndIsActiveTrue(@PathVariable Long clubId) {
    try {
      return ResponseEntity.ok(courtService.findByClubIdAndIsActiveTrue(clubId));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/exists")
  public ResponseEntity<?> existsByNameAndClubId(@PathVariable String name, @PathVariable Long clubId) {
    try {
      return ResponseEntity.ok(courtService.existsByNameAndClubId(name, clubId));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/exists-excluding")
  public ResponseEntity<?> existsByNameAndIdNotAndClubId(@PathVariable String name, @PathVariable Long id, @RequestParam Long clubId) {
    try {
      return ResponseEntity.ok(courtService.existsByNameAndIdNotAndClubId(name, id, clubId));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{courtId}/available")
  public ResponseEntity<?> isCourtAvailable(@PathVariable Long courtId,
                           @RequestParam LocalDateTime startTime,
                           @RequestParam LocalDateTime endTime) {
    try {
      return ResponseEntity.ok(courtService.isCourtAvailable(courtId, startTime, endTime));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/club/{clubId}")
  public ResponseEntity<?> getCourtsByClub(@PathVariable Long clubId) {
    try {
      return ResponseEntity.ok(courtService.getCourtsByClub(clubId));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{id}/exists")
  public ResponseEntity<?> courtExists(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(courtService.existsById(id));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }
}
