package org.example.microserviceuser.controller;

import lombok.RequiredArgsConstructor;
import org.example.microserviceuser.mapper.UserMapper;
import org.example.microserviceuser.security.jwt.TokenProvider;
import org.example.microserviceuser.service.UserService;
import org.example.microserviceuser.service.dto.request.UserLoginRequestDTO;
import org.example.microserviceuser.service.dto.request.UserRequestDTO;
import org.example.microserviceuser.service.dto.response.UserLoginResponseDTO;
import org.example.microserviceuser.service.dto.response.UserResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;
  private final TokenProvider tokenProvider;
  private final AuthenticationManager authenticationManager;

  @PostMapping("/register")
  public ResponseEntity<UserResponseDTO> register(@RequestBody UserRequestDTO request) {
    UserResponseDTO response = userService.save(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/login")
  public ResponseEntity<UserLoginResponseDTO> login(@RequestBody UserLoginRequestDTO request) {
    try {
      // Autenticar con Spring Security
      Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                      request.getEmail(),
                      request.getPassword()
              )
      );

      // Generar token
      String token = tokenProvider.createToken(authentication);

      // Obtener usuario para la respuesta
      org.example.microserviceuser.entity.User user =
              userService.findByEmail(request.getEmail());

      // Crear respuesta
      UserLoginResponseDTO response = UserLoginResponseDTO.builder()
              .token(token)
              .tokenType("Bearer")
              .expiresIn(86400L)
              .user(UserMapper.toResponse(user))
              .build();

      return ResponseEntity.ok(response);
    } catch (BadCredentialsException e) {
      return ResponseEntity.status(401).build();
    }
  }

  @PostMapping("/validate")
  public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
    boolean isValid = tokenProvider.validateToken(token);
    return ResponseEntity.ok(isValid);
  }
}
