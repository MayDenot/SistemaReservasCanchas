package org.example.microservicenotification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {
  public boolean sendEmail(String to, String subject, String body) {
    try {
      // Implementar envío de email con JavaMailSender, SendGrid, etc.
      log.info("Enviando email a: {}, Asunto: {}", to, subject);
      // Simulación
      Thread.sleep(100);
      return true;
    } catch (Exception e) {
      log.error("Error enviando email: {}", e.getMessage());
      return false;
    }
  }
}
