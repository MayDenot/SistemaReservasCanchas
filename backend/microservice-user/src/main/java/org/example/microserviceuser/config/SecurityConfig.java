package org.example.microserviceuser.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
          throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    log.info("=== CONFIGURANDO SEGURIDAD USER-SERVICE ===");
    log.info("Endpoints públicos configurados:");
    log.info("  - OPTIONS: /**");
    log.info("  - Auth: /api/auth/**");
    log.info("  - Health: /actuator/health, /api/actuator/health");
    log.info("  - User endpoints públicos:");
    log.info("    - GET /api/users/email/**");
    log.info("    - GET /api/users/by-email/**");
    log.info("    - GET /api/users/first-club-admin");
    log.info("    - GET /api/users/{id}/exists");
    log.info("    - GET /api/users/{id}/basic");

    http
            // Habilitar CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                    // ⚠️ CRÍTICO: Permitir OPTIONS (preflight CORS)
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // Health checks
                    .requestMatchers("/actuator/health", "/api/actuator/health").permitAll()

                    // Auth público
                    .requestMatchers("/api/auth/**").permitAll()

                    // 🔥 ENDPOINTS PARA MICROSERVICIOS - SIN AUTENTICACIÓN
                    .requestMatchers(HttpMethod.GET, "/api/users/email/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/by-email/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/first-club-admin").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/{id}/exists").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/{id}/basic").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/simple/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/debug/**").permitAll()

                    // Profile protegido
                    .requestMatchers("/api/users/profile/**").authenticated()

                    // El resto requiere autenticación
                    .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

    log.info("✅ Configuración de seguridad completada");
    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:3000",
            "http://localhost:8080",
            "http://localhost:8081",
            "http://club-service:8080",    // Para llamadas desde club-service
            "http://court-service:8080",   // Para llamadas desde court-service
            "http://reservation-service:8080", // Para llamadas desde reservation-service
            "http://api-gateway:8080"      // Para llamadas desde api-gateway
    ));

    configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
    ));

    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}