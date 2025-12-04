package org.example.microserviceclub.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.microserviceclub.service.ClubService;
import org.example.microserviceclub.service.dto.request.ClubRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class ClubController {
  private final ClubService clubService;

  @GetMapping()
  public ResponseEntity<?> findAll() {
    try {
      return ResponseEntity.ok(clubService.findAll());
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> findById(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(clubService.findById(id));
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping()
  public ResponseEntity<?> save(@RequestBody ClubRequestDTO request) {
    try {
      return ResponseEntity.ok(clubService.save(request));
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ClubRequestDTO request) {
    try {
      return ResponseEntity.ok(clubService.update(id, request));
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable Long id) {
    try {
      clubService.delete(id);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/exists")
  public ResponseEntity<?> existsByName(@RequestParam String name) {
    try {
      return ResponseEntity.ok(clubService.existsByName(name));
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{clubId}/with-user")
  public ResponseEntity<?> getClubWithUser(@PathVariable Long clubId) {
    try {
      return ResponseEntity.ok(clubService.getClubWithUser(clubId));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{id}/exists")
  public ResponseEntity<?> clubExists(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(clubService.existsById(id));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{id}/is-open")
  public ResponseEntity<?> isClubOpen(@PathVariable Long id,
                     @RequestParam LocalDateTime dateTime) {
    try {
      return ResponseEntity.ok(clubService.isClubOpenAt(id, dateTime));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }
}