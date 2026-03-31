package org.example.microserviceclub.config;

import feign.codec.ErrorDecoder;

public class CustomErrorDecoder implements ErrorDecoder {
  @Override
  public Exception decode(String methodKey, feign.Response response) {
    System.err.println("Feign Client Error:");
    System.err.println("Method: " + methodKey);
    System.err.println("Status: " + response.status());
    System.err.println("Headers: " + response.headers());

    return new RuntimeException("Feign Client Error - Status: " + response.status());
  }
}
