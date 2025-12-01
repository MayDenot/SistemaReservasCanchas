package org.example.microserviceuser;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MicroserviceUserApplication {

  public static void main(String[] args) {
    // Cargar variables del archivo .env
    Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing() // No falla si no existe .env
            .load();

    // Configurar System Properties para que Spring las use
    dotenv.entries().forEach(entry -> {
      System.setProperty(entry.getKey(), entry.getValue());
    });

    SpringApplication.run(MicroserviceUserApplication.class, args);
  }

}
