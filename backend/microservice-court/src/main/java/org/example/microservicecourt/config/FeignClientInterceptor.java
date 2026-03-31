package org.example.microservicecourt.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

  @Value("${service.internal.token}")
  private String internalToken;

  @Override
  public void apply(RequestTemplate template) {
    ServletRequestAttributes attributes = (ServletRequestAttributes)
            RequestContextHolder.getRequestAttributes();

    if (attributes != null) {
      // Si hay contexto HTTP (llamada desde usuario), usar su token
      HttpServletRequest request = attributes.getRequest();
      String authHeader = request.getHeader("Authorization");

      if (authHeader != null && !authHeader.isEmpty()) {
        template.header("Authorization", authHeader);

        String userEmail = request.getHeader("X-User-Email");
        if (userEmail != null) {
          template.header("X-User-Email", userEmail);
        }

        String userRole = request.getHeader("X-User-Role");
        if (userRole != null) {
          template.header("X-User-Role", userRole);
        }
        return;
      }
    }

    // Si NO hay contexto HTTP (llamada interna), usar token de servicio
    template.header("X-Internal-Service-Token", internalToken);
    template.header("X-Service-Name", "court-service");
  }
}