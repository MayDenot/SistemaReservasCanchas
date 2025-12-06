# ===== STAGE 1: BUILD - COMPILAR TODO EL PROYECTO =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copiar el pom.xml padre
COPY pom.xml .

# Crear las carpetas de los módulos (Maven necesita que existan)
RUN mkdir -p common-dto api-gateway microservice-club microservice-court \
             microservice-notification microservice-payment microservice-reservation microservice-user

# Copiar todos los pom.xml primero
COPY common-dto/pom.xml ./common-dto/
COPY api-gateway/pom.xml ./api-gateway/
COPY microservice-club/pom.xml ./microservice-club/
COPY microservice-court/pom.xml ./microservice-court/
COPY microservice-notification/pom.xml ./microservice-notification/
COPY microservice-payment/pom.xml ./microservice-payment/
COPY microservice-reservation/pom.xml ./microservice-reservation/
COPY microservice-user/pom.xml ./microservice-user/

# Copiar TODO el código fuente
COPY common-dto/src ./common-dto/src
COPY api-gateway/src ./api-gateway/src
COPY microservice-club/src ./microservice-club/src
COPY microservice-court/src ./microservice-court/src
COPY microservice-notification/src ./microservice-notification/src
COPY microservice-payment/src ./microservice-payment/src
COPY microservice-reservation/src ./microservice-reservation/src
COPY microservice-user/src ./microservice-user/src

# Compilar todo de una vez (Maven respeta el orden de módulos en el pom.xml)
RUN mvn clean install -DskipTests -B

# ===== STAGE 2: API GATEWAY =====
FROM eclipse-temurin:21-jre AS api-gateway
WORKDIR /app
COPY --from=build /build/api-gateway/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]

# ===== STAGE 3: CLUB SERVICE =====
FROM eclipse-temurin:21-jre AS club-service
WORKDIR /app
COPY --from=build /build/microservice-club/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]

# ===== STAGE 4: COURT SERVICE =====
FROM eclipse-temurin:21-jre AS court-service
WORKDIR /app
COPY --from=build /build/microservice-court/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]

# ===== STAGE 5: NOTIFICATION SERVICE =====
FROM eclipse-temurin:21-jre AS notification-service
WORKDIR /app
COPY --from=build /build/microservice-notification/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]

# ===== STAGE 6: PAYMENT SERVICE =====
FROM eclipse-temurin:21-jre AS payment-service
WORKDIR /app
COPY --from=build /build/microservice-payment/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]

# ===== STAGE 7: RESERVATION SERVICE =====
FROM eclipse-temurin:21-jre AS reservation-service
WORKDIR /app
COPY --from=build /build/microservice-reservation/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]

# ===== STAGE 8: USER SERVICE =====
FROM eclipse-temurin:21-jre AS user-service
WORKDIR /app
COPY --from=build /build/microservice-user/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]