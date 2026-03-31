package org.example.microserviceclub.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.ClubResponseDTO;
import org.example.common.dto.UserBasicInfoDTO;
import org.example.microserviceclub.feignClient.UserClient;
import org.example.microserviceclub.service.ClubService;
import org.example.common.dto.ClubRequestDTO;
import org.example.microserviceclub.service.ClubSettingsDTO;
import org.example.microserviceclub.service.SpecialHoursDTO;
import org.example.microserviceclub.service.dto.ClubStatsDTO;
import org.example.microserviceclub.service.dto.request.UpdateHoursRequest;
import org.example.microserviceclub.service.dto.response.AvailabilityResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/clubs")
public class ClubController {
  private final ClubService clubService;
  private final UserClient userClient;

  public ClubController(ClubService clubService, UserClient userClient) {
    this.clubService = clubService;
    this.userClient = userClient;
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
  public ResponseEntity<?> save(
          @RequestBody ClubRequestDTO request,
          @RequestHeader(value = "X-User-Email", required = false) String userEmail) {

    log.info("=== CREACIÓN DE CLUB - INICIO ===");
    log.info("Email del usuario: {}", userEmail);

    try {
      Long userId = null;
      String userSource = null; // Para tracking

      if (userEmail != null && !userEmail.trim().isEmpty()) {
        try {
          log.info("🔍 ESTRATEGIA 1: Buscando usuario por email: '{}'", userEmail);

          UserBasicInfoDTO userResponse = userClient.findByEmail(userEmail.trim());

          if (userResponse != null && userResponse.getId() != null) {
            userId = userResponse.getId();
            userSource = "email_provided";
            log.info("✅ Usuario encontrado por email - ID: {}, Email: {}",
                    userId, userResponse.getEmail());
          }

        } catch (Exception e) {
          log.warn("⚠️  Estrategia 1 falló: {}", e.getMessage());
          // Continuamos con otras estrategias
        }
      }

      if (userId == null) {
        String errorMessage = """
                    ❌ No se pudo determinar un administrador válido para el club.
                    
                    Posibles soluciones:
                    1. Asegúrate de que el usuario '{}' exista en el sistema
                    2. Verifica que el usuario tenga rol CLUB_ADMIN o SUPER_ADMIN
                    3. Contacta al administrador para que te asigne permisos
                    
                    Error detallado: El sistema no encontró ningún usuario con permisos 
                    para administrar un club.
                    """.formatted(userEmail != null ? userEmail : "N/A");

        log.error(errorMessage);
        return ResponseEntity.badRequest().body(errorMessage);
      }

      log.info("🎯 Creando club con adminId: {} (fuente: {})", userId, userSource);
      request.setAdminId(userId);

      ClubResponseDTO response = clubService.save(request);

      log.info("✅ CLUB CREADO EXITOSAMENTE");
      log.info("   - Club ID: {}", response.getId());
      log.info("   - Nombre: {}", response.getName());
      log.info("   - Admin ID: {}", userId);
      log.info("   - Fuente del admin: {}", userSource);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("❌ ERROR CRÍTICO creando club: ", e);

      return ResponseEntity.badRequest().body(
              String.format("""
                    ❌ Error creando club: %s
                    
                    Por favor, verifica:
                    1. Que el servicio de usuarios esté disponible
                    2. Que exista al menos un usuario con rol CLUB_ADMIN
                    3. Que los datos del club sean válidos
                    
                    Contacta al administrador si el problema persiste.
                    """, e.getMessage())
      );
    }
  }

  /**
   * Endpoint para verificar el estado del sistema
   */
  @GetMapping("/system-status")
  public ResponseEntity<?> getSystemStatus() {
    log.info("=== VERIFICACIÓN DE ESTADO DEL SISTEMA ===");

    try {
      // Verificar conexión con user-service
      UserBasicInfoDTO firstAdmin = userClient.getFirstClubAdmin();

      return ResponseEntity.ok().body(
              String.format("""
                    ✅ Sistema operativo correctamente
                    
                    Detalles:
                    - User-service: CONECTADO
                    - Administrador disponible: %s (ID: %d)
                    - Rol: %s
                    - Club-service: LISTO
                    
                    El sistema está listo para crear clubs.
                    """,
                      firstAdmin.getEmail(),
                      firstAdmin.getId(),
                      firstAdmin.getUserRole())
      );

    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(
              String.format("""
                    ❌ Problemas detectados en el sistema
                    
                    Error: %s
                    
                    Verifica que:
                    1. El servicio de usuarios esté ejecutándose
                    2. Exista al menos un usuario CLUB_ADMIN
                    3. La red entre servicios esté funcionando
                    """, e.getMessage())
      );
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

  @GetMapping("/{id}/stats")
  public ResponseEntity<?> getClubStats(@PathVariable Long id) {
    try {
      ClubStatsDTO stats = clubService.getClubStats(id);
      return ResponseEntity.ok(stats);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // GET /clubs/admin/{adminId}
  @GetMapping("/admin/{adminId}")
  public ResponseEntity<?> getClubsByAdmin(@PathVariable Long adminId) {
    try {
      List<ClubResponseDTO> clubs = clubService.getClubsByAdmin(adminId);
      return ResponseEntity.ok(clubs);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // GET /clubs/my-club
  @GetMapping("/my-club")
  public ResponseEntity<?> getMyClub(@RequestParam Long userId) {
    try {
      ClubResponseDTO club = clubService.getMyClub(userId);
      return ResponseEntity.ok(club);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // PATCH /clubs/{id}/hours
  @PatchMapping("/{id}/hours")
  public ResponseEntity<?> updateClubHours(
          @PathVariable Long id,
          @RequestBody UpdateHoursRequest request) {
    try {
      ClubResponseDTO club = clubService.updateClubHours(
              id, request.getOpeningTime(), request.getClosingTime());
      return ResponseEntity.ok(club);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // GET /clubs/{id}/special-hours
  @GetMapping("/{id}/special-hours")
  public ResponseEntity<?> getSpecialHours(@PathVariable Long id) {
    try {
      List<SpecialHoursDTO> specialHours = clubService.getSpecialHours(id);
      return ResponseEntity.ok(specialHours);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // POST /clubs/{id}/special-hours
  @PostMapping("/{id}/special-hours")
  public ResponseEntity<?> addSpecialHours(
          @PathVariable Long id,
          @RequestBody SpecialHoursDTO specialHoursDTO) {
    try {
      SpecialHoursDTO savedHours = clubService.addSpecialHours(id, specialHoursDTO);
      return ResponseEntity.ok(savedHours);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // GET /clubs/{id}/availability
  @GetMapping("/{id}/availability")
  public ResponseEntity<?> checkAvailability(
          @PathVariable Long id,
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
          @RequestParam(defaultValue = "1") int durationHours) {
    try {
      AvailabilityResponse response = clubService.checkAvailability(id, dateTime, durationHours);
      return ResponseEntity.ok(response);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // GET /clubs/{id}/settings
  @GetMapping("/{id}/settings")
  public ResponseEntity<ClubSettingsDTO> getClubSettings(@PathVariable Long id) {
    try {
      ClubSettingsDTO settings = clubService.getClubSettings(id);
      return ResponseEntity.ok(settings);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // PUT /clubs/{id}/settings
  @PutMapping("/{id}/settings")
  public ResponseEntity<ClubSettingsDTO> updateClubSettings(
          @PathVariable Long id,
          @RequestBody ClubSettingsDTO settings) {
    try {
      ClubSettingsDTO updatedSettings = clubService.updateClubSettings(id, settings);
      return ResponseEntity.ok(updatedSettings);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // Método para extraer userId del token
  private Long extractUserIdFromToken(String token) {
    try {
      // Decodificar el token JWT
      String[] parts = token.split("\\.");
      if (parts.length < 2) {
        throw new RuntimeException("Token inválido");
      }

      String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
      ObjectMapper mapper = new ObjectMapper();
      JsonNode jsonNode = mapper.readTree(payload);

      // Buscar el userId en el token
      if (jsonNode.has("userId")) {
        return jsonNode.get("userId").asLong();
      } else if (jsonNode.has("sub")) {
        // Si usas email como subject, necesitas buscar el ID en la BD
        String email = jsonNode.get("sub").asText();
        // Llamar a user-service para obtener el ID
        return userClient.findByEmail(email).getId();
      }

      throw new RuntimeException("No se pudo extraer userId del token");
    } catch (Exception e) {
      throw new RuntimeException("Error procesando token: " + e.getMessage());
    }
  }
}