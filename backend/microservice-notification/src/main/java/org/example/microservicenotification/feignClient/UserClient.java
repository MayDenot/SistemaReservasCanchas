package org.example.microservicenotification.feignClient;

import org.example.microservicenotification.service.dto.UserBasicInfoDTO;
import org.example.microservicenotification.service.dto.UserNotificationPreferencesDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "microservice-user", url = "http://localhost:8080")
public interface UserClient {
  @GetMapping("/api/users/{id}/exists")
  boolean userExists(@PathVariable("id") Long id);

  @GetMapping("/api/users/{id}/basic")
  UserBasicInfoDTO getUserBasicInfo(@PathVariable("id") Long id);

  @GetMapping("/api/users/{id}/email")
  String getUserEmail(@PathVariable("id") Long id);

  @GetMapping("/api/users/{id}/phone")
  String getUserPhone(@PathVariable("id") Long id);

  @GetMapping("/api/users/{id}/notification-preferences")
  UserNotificationPreferencesDTO getNotificationPreferences(@PathVariable("id") Long id);
}
