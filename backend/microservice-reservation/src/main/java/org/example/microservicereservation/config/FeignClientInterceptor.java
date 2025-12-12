package org.example.microservicereservation.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

  @Override
  public void apply(RequestTemplate template) {
    ServletRequestAttributes attributes = (ServletRequestAttributes)
            RequestContextHolder.getRequestAttributes();

    if (attributes != null) {
      HttpServletRequest request = attributes.getRequest();

      // Copiar TODOS los headers de autenticación y del gateway
      String authHeader = request.getHeader("Authorization");
      if (authHeader != null && !authHeader.isEmpty()) {
        template.header("Authorization", authHeader);
        //log.debug("Copied Authorization header to Feign request: {}", authHeader.substring(0, Math.min(20, authHeader.length())) + "...");
      }

      // Copiar headers del gateway
      String userEmail = request.getHeader("X-User-Email");
      if (userEmail != null && !userEmail.isEmpty()) {
        template.header("X-User-Email", userEmail);
      }

      String userRole = request.getHeader("X-User-Role");
      if (userRole != null && !userRole.isEmpty()) {
        template.header("X-User-Role", userRole);
      }

      // Si no hay Authorization, intentar usar X-User-Email como alternativa
      if (authHeader == null && userEmail != null) {
        //log.warn("No Authorization header found, using X-User-Email for service-to-service auth");
        // Podrías añadir un token de servicio aquí si tienes
      }
    } else {
      //log.warn("No request context available for Feign interceptor");
    }
  }
}