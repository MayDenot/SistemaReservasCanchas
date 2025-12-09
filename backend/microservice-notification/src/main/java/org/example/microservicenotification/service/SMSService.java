package org.example.microservicenotification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SMSService {
  public boolean sendSMS(String phone, String message) {
    try {
      // Implementar envío de SMS con Twilio, etc.
      log.info("Enviando SMS a: {}, Mensaje: {}", phone, message);
      // Simulación
      Thread.sleep(100);
      return true;
    } catch (Exception e) {
      log.error("Error enviando SMS: {}", e.getMessage());
      return false;
    }
  }
}
