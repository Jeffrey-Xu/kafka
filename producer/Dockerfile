# Simplified Dockerfile for Spring Boot Kafka Producer
# Uses pre-built JAR from CI/CD pipeline

FROM openjdk:17-jdk-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r kafka && useradd -r -g kafka kafka

# Set working directory
WORKDIR /app

# Copy the pre-built JAR file from CI/CD build
COPY target/spring-kafka-producer-*.jar app.jar

# Change ownership to non-root user
RUN chown -R kafka:kafka /app

# Switch to non-root user
USER kafka

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM options for containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
