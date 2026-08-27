# Multi-stage Docker build for Enterprise Flow Engine (EFE)
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
VOLUME /tmp
COPY --from=builder /workspace/target/*.jar /app/efe.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/efe.jar"]
