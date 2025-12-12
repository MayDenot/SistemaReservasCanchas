package org.example.microservicereservation.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
          throws ServletException, IOException {

    // Leer headers del Gateway
    String userEmail = request.getHeader("X-User-Email");
    String userRole = request.getHeader("X-User-Role");

    if (userEmail != null) {
      // Si no hay rol, usar uno por defecto
      if (userRole == null || userRole.isEmpty()) {
        userRole = "ROLE_USER";
      }

      // Crear autenticación
      UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(
                      userEmail,
                      null,
                      List.of(new SimpleGrantedAuthority(userRole))
              );

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }
}