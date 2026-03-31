package org.example.microserviceuser.controller;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.example.microserviceuser.entity.User;
import org.example.microserviceuser.mapper.UserMapper;
import org.example.microserviceuser.service.UserService;
import org.example.microserviceuser.service.dto.request.UserRequestDTO;
import org.example.microserviceuser.service.dto.response.UserResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @GetMapping()
  public ResponseEntity<?> findAll() {
    try {
      return ResponseEntity.ok(userService.findAll());
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> findById(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(userService.findById(id));
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping()
  public ResponseEntity<?> save(@RequestBody UserRequestDTO req) {
    try {
      return ResponseEntity.ok(userService.save(req));
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UserRequestDTO req) {
    try {
      return ResponseEntity.ok(userService.update(id, req));
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable Long id) {
    try {
      userService.delete(id);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{id}/exists")
  public ResponseEntity<?> userExists(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(userService.existsById(id));
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{id}/basic")
  public ResponseEntity<?> getUserBasic(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(userService.getUserBasicById(id));
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/email/{email}")
  public ResponseEntity<?> findByEmail(@PathVariable String email) {
    log.info("=== ENDPOINT: /api/users/email/{} ===", email);

    try {
      // Decodificar el email del path variable
      String decodedEmail = URLDecoder.decode(email, StandardCharsets.UTF_8.name());
      log.info("Email decodificado del path: '{}'", decodedEmail);

      User user = userService.findByEmail(decodedEmail);

      log.info("✅ Usuario encontrado - ID: {}, Email: '{}'",
              user.getId(), user.getEmail());

      UserResponseDTO response = UserMapper.toResponse(user);
      log.info("Response DTO: {}", response);

      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
      log.error("Error decodificando email: {}", email, e);
      return ResponseEntity.badRequest().build();
    } catch (RuntimeException e) {
      log.error("Usuario no encontrado: '{}' - Error: {}", email, e.getMessage());
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error inesperado en findByEmail: ", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/by-email/{email}")
  public ResponseEntity<?> getUserIdByEmail(@PathVariable String email) {
    try {
      User user = userService.findByEmail(email);
      return ResponseEntity.ok(user.getId());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @GetMapping("/first-club-admin")
  public ResponseEntity<?> getFirstClubAdmin() {
    log.info("Buscando primer usuario CLUB_ADMIN");

    try {
      User clubAdmin = userService.findFirstClubAdmin();
      return ResponseEntity.ok(UserMapper.toResponse(clubAdmin));
    } catch (RuntimeException e) {
      log.error("No se encontró ningún CLUB_ADMIN: {}", e.getMessage());
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error buscando CLUB_ADMIN: ", e);
      return ResponseEntity.internalServerError().build();
    }
  }
}
