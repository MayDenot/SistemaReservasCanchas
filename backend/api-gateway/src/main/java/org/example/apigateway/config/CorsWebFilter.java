package org.example.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsWebFilter {

  private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
          "http://localhost:5173",
          "http://localhost:3000",
          "http://127.0.0.1:5173",
          "http://127.0.0.1:3000"
  );

  private static final List<String> ALLOWED_METHODS = Arrays.asList(
          "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
  );

  private static final List<String> ALLOWED_HEADERS = Arrays.asList(
          "Authorization",
          "Content-Type",
          "Accept",
          "Origin",
          "X-Requested-With"
  );

  private static final List<String> EXPOSED_HEADERS = Arrays.asList(
          "Authorization",
          "Content-Type",
          "Content-Disposition",
          "X-User-Email",
          "X-User-Role"
  );

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public WebFilter corsFilter() {
    return (ServerWebExchange ctx, WebFilterChain chain) -> {
      ServerHttpRequest request = ctx.getRequest();
      ServerHttpResponse response = ctx.getResponse();
      HttpHeaders headers = response.getHeaders();

      String origin = request.getHeaders().getOrigin();

      if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, String.join(", ", ALLOWED_METHODS));


        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, String.join(", ", ALLOWED_HEADERS));

        headers.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, String.join(", ", EXPOSED_HEADERS));

        headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
      }

      if (request.getMethod() == HttpMethod.OPTIONS) {
        response.setStatusCode(HttpStatus.OK);
        return Mono.empty();
      }

      return chain.filter(ctx);
    };
  }
}
