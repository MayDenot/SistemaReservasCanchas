package org.example.microservicecourt.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.microservicecourt.service.CourtService;
import org.example.microservicecourt.service.dto.request.CourtRequestDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
  public ResponseEntity<?> findById(@PathVariable("id") Long id) {
    try {
      return ResponseEntity.ok(courtService.findById(id));
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping()
  public ResponseEntity<?> save(@RequestBody CourtRequestDTO request) {
    try {
      System.out.println("=== DEBUG REQUEST ===");
      System.out.println("Request completo: " + request);
      System.out.println("ClubId: " + request.getClubId());
      System.out.println("Name: " + request.getName());
      System.out.println("Type: " + request.getType());
      System.out.println("===================");

      return ResponseEntity.ok(courtService.save(request.getClubId(), request));
    } catch (EntityNotFoundException e) {
      System.err.println("Club no encontrado: " + e.getMessage());
      return ResponseEntity.notFound().build();
    } catch (IllegalArgumentException e) {
      System.err.println("Error de validación: " + e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      System.err.println("Error general: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(@PathVariable("id") Long id, @RequestBody CourtRequestDTO request) {
    try {
      return ResponseEntity.ok(courtService.update(id, request));
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable("id") Long id) {
    try {
      return ResponseEntity.ok(courtService.delete(id));
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/club/{clubId}")
  public ResponseEntity<?> findByClubId(@PathVariable("clubId") Long clubId) {
    try {
      return ResponseEntity.ok(courtService.findByClubId(clubId));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/club/{clubId}/active")
  public ResponseEntity<?> findByClubIdAndIsActiveTrue(@PathVariable("clubId") Long clubId) {
    try {
      return ResponseEntity.ok(courtService.findByClubIdAndIsActiveTrue(clubId));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/exists")
  public ResponseEntity<?> existsByNameAndClubId(@PathVariable("name") String name, @PathVariable("clubId") Long clubId) {
    try {
      return ResponseEntity.ok(courtService.existsByNameAndClubId(name, clubId));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/exists-excluding")
  public ResponseEntity<?> existsByNameAndIdNotAndClubId(@PathVariable("name") String name,
                                                         @PathVariable("id") Long id,
                                                         @RequestParam("clubId") Long clubId) {
    try {
      return ResponseEntity.ok(courtService.existsByNameAndIdNotAndClubId(name, id, clubId));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{courtId}/check-availability")
  public ResponseEntity<?> isCourtAvailable(@PathVariable("courtId") Long courtId,
                                            @RequestParam("startTime") LocalDateTime startTime,
                                            @RequestParam("endTime") LocalDateTime endTime) {
    try {
      return ResponseEntity.ok(courtService.isCourtAvailable(courtId, startTime, endTime));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{courtId}/available")
  public ResponseEntity<?> getAvailableSlots(
          @PathVariable("courtId") Long courtId,
          @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    try {
      List<String> availableSlots = courtService.getAvailableTimeSlots(courtId, date);
      return ResponseEntity.ok(availableSlots);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.badRequest().body(e.getMessage());
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
