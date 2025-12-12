package org.example.microserviceclub.controller;

import jakarta.persistence.EntityNotFoundException;
import org.example.microserviceclub.service.ClubService;
import org.example.common.dto.ClubRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/clubs")
public class ClubController {
  private final ClubService clubService;

  public ClubController(ClubService clubService) {
    this.clubService = clubService;
  }

  @GetMapping()
  public ResponseEntity<?> findAll() {
    try {
      return ResponseEntity.ok(clubService.findAll());
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> findById(@PathVariable("id") Long id) {
    try {
      return ResponseEntity.ok(clubService.findById(id));
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping()
  public ResponseEntity<?> save(@RequestBody ClubRequestDTO request) {
    try {
      System.out.println("=== DEBUG REQUEST ===");
      System.out.println("Request: " + request);
      System.out.println("AdminId: " + request.getAdminId());
      System.out.println("===================");
      return ResponseEntity.ok(clubService.save(request));
    } catch (Exception e) {
      e.printStackTrace(); // Agregá esto también para ver el stack trace completo
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(@PathVariable("id") Long id, @RequestBody ClubRequestDTO request) {
    try {
      return ResponseEntity.ok(clubService.update(id, request));
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable("id") Long id) {
    try {
      clubService.delete(id);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/exists")
  public ResponseEntity<?> existsByName(@RequestParam("name") String name) {
    try {
      return ResponseEntity.ok(clubService.existsByName(name));
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{clubId}/with-user")
  public ResponseEntity<?> getClubWithUser(@PathVariable("clubId") Long clubId) {
    try {
      return ResponseEntity.ok(clubService.getClubWithUser(clubId));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{id}/exists")
  public ResponseEntity<Boolean> clubExists(@PathVariable("id") Long id) {
    try {
      System.out.println("=== CHECKING CLUB EXISTS ===");
      System.out.println("Club ID recibido: " + id);

      boolean exists = clubService.existsById(id);

      System.out.println("Exists: " + exists);
      System.out.println("===========================");

      return ResponseEntity.ok(exists);
    } catch (Exception e) {
      System.err.println("Error verificando club: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.ok(false);
    }
  }

  @GetMapping("/{id}/is-open")
  public ResponseEntity<?> isClubOpen(@PathVariable("id") Long id,
                     @RequestParam("dateTime") LocalDateTime dateTime) {
    try {
      return ResponseEntity.ok(clubService.isClubOpenAt(id, dateTime));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }
}