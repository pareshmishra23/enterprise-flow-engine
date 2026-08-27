# Multi-stage Docker build for the EFE reconciliation-example reference application.
# Builds the full reactor (efe-platform + examples) and packages the reconciliation-example
# Spring Boot app as an independently containerized runnable image: efe-reconciliation:dev
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY pom.xml .
COPY efe-platform ./efe-platform
COPY examples ./examples
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
VOLUME /tmp
COPY --from=builder /workspace/examples/reconciliation-example/target/efe-reconciliation-1.0.0-SNAPSHOT.jar /app/efe.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/efe.jar"]
