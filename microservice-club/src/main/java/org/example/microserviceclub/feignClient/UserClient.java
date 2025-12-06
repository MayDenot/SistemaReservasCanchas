package org.example.microserviceclub.feignClient;

import org.example.common.dto.UserBasicInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "microservice-user", url = "http://localhost:8081")
public interface UserClient {
  @GetMapping("/api/users/{userId}/basic")
  UserBasicInfoDTO getUserBasic(@PathVariable Long userId);

  @GetMapping("/api/users/{userId}/exists")
  boolean userExists(@PathVariable Long userId);
}
