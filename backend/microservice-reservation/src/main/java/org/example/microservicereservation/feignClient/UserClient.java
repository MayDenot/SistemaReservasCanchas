package org.example.microservicereservation.feignClient;

import org.example.common.dto.UserBasicInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "microservice-user", url = "http://localhost:8080")
public interface UserClient {
  @GetMapping("/{id}/exists")
  boolean userExists(@PathVariable("id") Long id);

  @GetMapping("/{id}/basic")
  UserBasicInfoDTO getUserBasic(@PathVariable("id") Long id);
}
