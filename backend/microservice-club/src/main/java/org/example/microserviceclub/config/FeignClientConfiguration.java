package org.example.microserviceclub.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfiguration {

  @Bean
  public Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL; // Para debugging
  }

  @Bean
  public ErrorDecoder errorDecoder() {
    return new CustomErrorDecoder();
  }

  @Bean
  public RequestInterceptor requestInterceptor() {
    return requestTemplate -> {
      // Agregar headers comunes si es necesario
      requestTemplate.header("Accept", "application/json");
      requestTemplate.header("Content-Type", "application/json");
    };
  }
}