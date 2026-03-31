package org.example.microserviceclub.feignClient;

import org.example.common.dto.UserBasicInfoDTO;
import org.example.microserviceclub.config.FeignClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "microservice-user", url = "http://user-service:8080",
        configuration = FeignClientConfiguration.class)
public interface UserClient {
  @GetMapping("/api/users/{userId}/basic")
  UserBasicInfoDTO getUserBasic(@PathVariable("userId") Long userId);

  @GetMapping("/api/users/{userId}/exists")
  boolean userExists(@PathVariable("userId") Long userId);

  @GetMapping("/api/users/{id}")
  UserBasicInfoDTO getUserById(@PathVariable("id") Long id);

  @GetMapping("/api/users/current")
  UserBasicInfoDTO getCurrentUser();

  @GetMapping("/api/users/by-role")
  List<UserBasicInfoDTO> getUsersByRole(@RequestParam("role") String role);

  @GetMapping("/api/users/email/{email}")
  UserBasicInfoDTO  findByEmail(@PathVariable("email") String email);

  @GetMapping("/api/users/first-club-admin")
  UserBasicInfoDTO getFirstClubAdmin();
}
