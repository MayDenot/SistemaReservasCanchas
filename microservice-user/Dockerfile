# Etapa 1: Build
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/microservice-user-*.jar app.jar
CMD ["java", "-jar", "app.jar"]