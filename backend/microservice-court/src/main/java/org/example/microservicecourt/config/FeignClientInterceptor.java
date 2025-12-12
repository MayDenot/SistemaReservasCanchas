package org.example.microservicecourt.config;

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

      String authHeader = request.getHeader("Authorization");
      if (authHeader != null && !authHeader.isEmpty()) {
        template.header("Authorization", authHeader);
      }

      String userEmail = request.getHeader("X-User-Email");
      if (userEmail != null && !userEmail.isEmpty()) {
        template.header("X-User-Email", userEmail);
      }

      String userRole = request.getHeader("X-User-Role");
      if (userRole != null && !userRole.isEmpty()) {
        template.header("X-User-Role", userRole);
      }
    }
  }
}