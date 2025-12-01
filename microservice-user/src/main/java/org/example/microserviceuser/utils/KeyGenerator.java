package org.example.microserviceuser.utils;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Scanner;

public class KeyGenerator {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    System.out.println("🔐 GENERADOR DE CLAVES JWT SEGURAS");
    System.out.println("===================================");

    System.out.print("¿Qué algoritmo usar? (1=HS256, 2=HS384, 3=HS512): ");
    int choice = scanner.nextInt();

    String algorithm;
    int minLength;

    switch (choice) {
      case 1:
        algorithm = "HS256";
        minLength = 32;
        break;
      case 2:
        algorithm = "HS384";
        minLength = 48;
        break;
      case 3:
      default:
        algorithm = "HS512";
        minLength = 64;
        break;
    }

    // Generar clave
    SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.valueOf(algorithm));
    String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());

    System.out.println("\n✅ CLAVE GENERADA EXITOSAMENTE:");
    System.out.println("Algoritmo: " + algorithm);
    System.out.println("Longitud: " + base64Key.length() + " caracteres");
    System.out.println("Clave: " + base64Key);

    // Verificación de seguridad
    if (base64Key.length() >= minLength) {
      System.out.println("🔒 NIVEL DE SEGURIDAD: ALTO");
    } else {
      System.out.println("⚠️  ADVERTENCIA: Clave más corta de lo recomendado");
    }

    scanner.close();
  }
}