package org.example.microserviceclub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MicroserviceClubApplication {

  public static void main(String[] args) {
    SpringApplication.run(MicroserviceClubApplication.class, args);
  }

}
