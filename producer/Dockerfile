# Multi-stage build for Spring Boot Kafka Producer
FROM maven:3.9-openjdk-17-slim AS build

# Set working directory
WORKDIR /app

# Copy parent POM and common module first (for better caching)
COPY pom.xml .
COPY common/pom.xml common/
COPY producer/pom.xml producer/

# Download dependencies (this layer will be cached if POMs don't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY common/src common/src
COPY producer/src producer/src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM openjdk:17-jdk-slim

# Create non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/producer/target/spring-kafka-producer-*.jar app.jar

# Change ownership to spring user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
