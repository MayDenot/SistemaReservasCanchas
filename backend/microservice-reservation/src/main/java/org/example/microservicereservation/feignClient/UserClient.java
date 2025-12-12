package org.example.microservicereservation.feignClient;

import org.example.common.dto.UserBasicInfoDTO;
import org.example.microservicereservation.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "http://user-service:8080",
configuration = FeignConfig.class)
public interface UserClient {

  @GetMapping("/api/users/{id}/exists")
  boolean userExists(
          @PathVariable("id") Long id);

  @GetMapping("/api/users/{id}/basic")
  UserBasicInfoDTO getUserBasic(
          @PathVariable("id") Long id);
}