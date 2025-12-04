FROM maven:3.9.8-eclipse-temurin-21 AS builder

WORKDIR /app
COPY . .

# SOLUCIÓN: Agregar 'clean install' como goals
RUN mvn clean install -DskipTests -Dspring-boot.repackage.skip=true

# Verificar que se construyeron los JARs
RUN echo "JARs construidos:" && find . -name "*.jar" -path "*/target/*" -type f | head -20

# Copiar JARs para la siguiente etapa
RUN mkdir -p /jars && find . -path "*/target/*.jar" -type f ! -name "*-sources.jar" ! -name "*-javadoc.jar" -exec cp {} /jars/ \;

# Mostrar los JARs copiados
RUN echo "JARs copiados a /jars:" && ls -la /jars/