package org.example.microservicereservation.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InternalServiceAuthenticationFilter extends OncePerRequestFilter {

  @Value("${service.internal.token}")
  private String expectedInternalToken;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
          throws ServletException, IOException {

    // Verificar si es una llamada de servicio interno
    String internalToken = request.getHeader("X-Internal-Service-Token");
    String serviceName = request.getHeader("X-Service-Name");

    if (internalToken != null && internalToken.equals(expectedInternalToken)) {
      // Autenticar como servicio interno
      UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(
                      serviceName != null ? serviceName : "internal-service",
                      null,
                      List.of(new SimpleGrantedAuthority("ROLE_SERVICE"))
              );

      SecurityContextHolder.getContext().setAuthentication(authentication);
      System.out.println("✅ Autenticado como servicio interno: " + serviceName);
    }

    filterChain.doFilter(request, response);
  }
}