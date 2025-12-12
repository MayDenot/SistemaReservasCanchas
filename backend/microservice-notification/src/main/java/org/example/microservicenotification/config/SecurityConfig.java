package org.example.microservicenotification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            // Habilitar CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Deshabilitar CSRF (API REST)
            .csrf(csrf -> csrf.disable())

            // Reglas de autorización
            .authorizeHttpRequests(authz -> authz
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    .requestMatchers("/actuator/health", "/api/actuator/health").permitAll()

                    .requestMatchers(HttpMethod.GET, "/api/notifications/**").permitAll()

                    .requestMatchers(HttpMethod.POST, "/api/notifications/**").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/notifications/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/notifications/**").authenticated()

                    // Cualquier otra request requiere autenticación
                    .anyRequest().authenticated()
            )

            // Sin sesiones (Stateless - JWT en el gateway)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

    return http.build();
  }

  /**
   * Configuración CORS para el microservicio
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Origins permitidos
    configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:3000",
            "http://localhost:8080",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:3000"
    ));

    // Métodos HTTP permitidos
    configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
    ));

    // Headers permitidos
    configuration.setAllowedHeaders(List.of("*"));

    // Headers expuestos
    configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-User-Email",
            "X-User-Role"
    ));

    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
