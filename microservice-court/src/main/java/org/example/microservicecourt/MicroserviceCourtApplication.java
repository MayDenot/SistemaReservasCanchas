package org.example.microservicecourt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MicroserviceCourtApplication {

  public static void main(String[] args) {
    SpringApplication.run(MicroserviceCourtApplication.class, args);
  }

}
