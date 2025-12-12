package org.example.microserviceuser.controller;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.example.microserviceuser.entity.User;
import org.example.microserviceuser.mapper.UserMapper;
import org.example.microserviceuser.service.UserService;
import org.example.microserviceuser.service.dto.request.UserRequestDTO;
import org.example.microserviceuser.service.dto.response.UserResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
  public ResponseEntity<UserResponseDTO> findByEmail(
          @PathVariable String email,
          @RequestHeader(value = "Authorization", required = false) String authHeader,
          @RequestHeader(value = "X-User-Email", required = false) String xUserEmail) {

    log.info("Finding user by email: {}", email);
    log.info("Headers - Authorization: {}, X-User-Email: {}",
            authHeader != null ? "Present" : "Missing",
            xUserEmail);

    try {
      User user = userService.findByEmail(email);
      return ResponseEntity.ok(UserMapper.toResponse(user));
    } catch (RuntimeException e) {
      log.error("User not found with email: {}", email);
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error finding user by email: {}", email, e);
      return ResponseEntity.badRequest().build();
    }
  }
}
