package org.example.microserviceuser.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.microserviceuser.entity.User;
import org.example.microserviceuser.entity.UserRole;
import org.example.microserviceuser.mapper.UserMapper;
import org.example.microserviceuser.repository.UserRepository;
import org.example.microserviceuser.service.dto.request.UserRequestDTO;
import org.example.microserviceuser.service.dto.response.UserResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  public List<UserResponseDTO> findAll() {
    return this.userRepository.findAll()
            .stream()
            .map(UserMapper::toResponse)
            .toList();
  }

  @Transactional(readOnly = true)
  public UserResponseDTO findById(Long id) {
    return UserMapper.toResponse(this.userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id)));
  }

  @Transactional()
  public UserResponseDTO save(UserRequestDTO request) {
    System.out.println("Request recibido: " + request);
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("El email ya está registrado: " + request.getEmail());
    }

    User user = UserMapper.toEntity(request);
    System.out.println("User entity creada: " + user);

    user.setPassword(passwordEncoder.encode(request.getPassword()));
    User savedUser = userRepository.save(user);
    System.out.println("User guardado: " + savedUser);

    return UserMapper.toResponse(savedUser);
  }


  @Transactional()
  public UserResponseDTO update(Long id, UserRequestDTO req) {
    User usuario = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

    if (!usuario.getEmail().equals(req.getEmail()) &&
            userRepository.existsByEmail(req.getEmail())) {
      throw new RuntimeException("El email ya está en uso: " + req.getEmail());
    }

    usuario.setEmail(req.getEmail());
    usuario.setUserRole(req.getUserRole());
    usuario.setName(req.getName());
    usuario.setPhone(req.getPhone());

    if (req.getPassword() != null && !req.getPassword().trim().isEmpty()) {
      usuario.setPassword(passwordEncoder.encode(req.getPassword()));
    }

    User usuarioActualizado = userRepository.save(usuario);
    return UserMapper.toResponse(usuarioActualizado);
  }

  @Transactional()
  public Long delete(Long id) {
    if (!userRepository.existsById(id)) {
      throw new RuntimeException("Usuario no encontrado con id: " + id);
    }
    userRepository.deleteById(id);
    return id;
  }

  @Transactional(readOnly = true)
  public User findByEmail(String email) {
    log.info("Buscando usuario por email: '{}'", email);

    try {
      // Decodificar URL si viene encoded
      String decodedEmail = URLDecoder.decode(email, StandardCharsets.UTF_8.name());
      log.info("Email decodificado: '{}'", decodedEmail);

      // Limpiar el email
      String cleanEmail = decodedEmail.trim();
      log.info("Email limpio: '{}'", cleanEmail);

      // Intentar métodos en orden:

      // 1. Búsqueda exacta
      Optional<User> user = userRepository.findByEmail(cleanEmail);
      if (user.isPresent()) {
        log.info("✅ Usuario encontrado (método 1 - exacto): {}", user.get().getEmail());
        return user.get();
      }

      // 2. Búsqueda exacta con trim en BD
      user = userRepository.findByEmailExact(cleanEmail);
      if (user.isPresent()) {
        log.info("✅ Usuario encontrado (método 2 - exacto con trim): {}", user.get().getEmail());
        return user.get();
      }

      // 3. Búsqueda case-insensitive
      user = userRepository.findByEmailCaseInsensitive(cleanEmail);
      if (user.isPresent()) {
        log.info("✅ Usuario encontrado (método 3 - case-insensitive): {}", user.get().getEmail());
        return user.get();
      }

      // 4. Búsqueda manual como último recurso
      user = userRepository.findAll().stream()
              .filter(u -> u.getEmail().trim().equalsIgnoreCase(cleanEmail))
              .findFirst();

      if (user.isPresent()) {
        log.info("✅ Usuario encontrado (método 4 - manual): {}", user.get().getEmail());
        return user.get();
      }

      // Log detallado de todos los usuarios para debug
      log.error("=== USUARIOS EN BD ===");
      userRepository.findAll().forEach(u ->
              log.error("  ID: {}, Email: '{}' (len: {})",
                      u.getId(), u.getEmail(), u.getEmail().length()));

      throw new RuntimeException("Usuario no encontrado con email: '" + cleanEmail + "'");

    } catch (Exception e) {
      log.error("Error en findByEmail: ", e);
      throw new RuntimeException("Error buscando usuario: " + e.getMessage());
    }
  }

  @Transactional(readOnly = true)
  public User findFirstClubAdmin() {
    log.info("Buscando primer usuario CLUB_ADMIN");

    List<User> clubAdmins = userRepository.findByUserRole(UserRole.CLUB_ADMIN);

    if (!clubAdmins.isEmpty()) {
      User admin = clubAdmins.get(0);
      log.info("✅ CLUB_ADMIN encontrado: {} (ID: {})", admin.getEmail(), admin.getId());
      return admin;
    }

    // Si no hay CLUB_ADMIN, buscar SUPER_ADMIN
    log.info("No hay CLUB_ADMIN, buscando SUPER_ADMIN...");
    List<User> superAdmins = userRepository.findByUserRole(UserRole.SUPER_ADMIN);

    if (!superAdmins.isEmpty()) {
      User admin = superAdmins.get(0);
      log.info("✅ SUPER_ADMIN encontrado: {} (ID: {})", admin.getEmail(), admin.getId());
      return admin;
    }

    // Si no hay ninguno, lanzar excepción
    throw new RuntimeException("No se encontró ningún usuario administrador (CLUB_ADMIN o SUPER_ADMIN)");
  }

  @Transactional(readOnly = true)
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  @Transactional(readOnly = true)
  public boolean existsById(Long id) {
    return userRepository.existsById(id);
  }

  @Transactional(readOnly = true)
  public UserResponseDTO getUserBasicById(Long id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));

    return UserMapper.toResponse(user);
  }
}
