# Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and configuration
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Fix permissions
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build application
RUN ./mvnw package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder /app/target/core-fit-0.0.1-SNAPSHOT.jar .

# Expose port
EXPOSE 8000

# Run the application
ENTRYPOINT ["java", "-jar", "core-fit-0.0.1-SNAPSHOT.jar"]