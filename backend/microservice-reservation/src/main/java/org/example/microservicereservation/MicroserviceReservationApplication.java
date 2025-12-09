package org.example.microservicereservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MicroserviceReservationApplication {

  public static void main(String[] args) {
    SpringApplication.run(MicroserviceReservationApplication.class, args);
  }

}
