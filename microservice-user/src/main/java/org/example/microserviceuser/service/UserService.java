package org.example.microserviceuser.service;

import lombok.RequiredArgsConstructor;
import org.example.microserviceuser.entity.User;
import org.example.microserviceuser.mapper.UserMapper;
import org.example.microserviceuser.repository.UserRepository;
import org.example.microserviceuser.service.dto.request.UserRequestDTO;
import org.example.microserviceuser.service.dto.response.UserResponseDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("El email ya está registrado: " + request.getEmail());
    }

    User user = UserMapper.toEntity(request);

    user.setPassword(passwordEncoder.encode(request.getPassword()));

    User savedUser = userRepository.save(user);

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
    return userRepository.findByEmail(email).orElse(null);
  }

  @Transactional(readOnly = true)
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }
}
