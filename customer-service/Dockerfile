# Multi-stage build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy maven files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Make mvnw executable
RUN chmod +x ./mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar from builder
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/customer-service/actuator/health || exit 1

# Environment variables
ENV SPRING_PROFILES_ACTIVE=docker
ENV MONGO_HOST=mongodb
ENV MONGO_PORT=27017
ENV MONGO_DATABASE=customer_db
ENV MONGO_USERNAME=root
ENV MONGO_PASSWORD=root

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
