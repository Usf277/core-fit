# Dockerfile for Java core app
FROM eclipse-temurin:17-jdk-alpine

# Set workdir
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Fix permissions for Maven wrapper
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Package application
RUN ./mvnw package -DskipTests

# Expose port (change if your app uses a different port)
EXPOSE 8080

# Run the app
CMD ["./mvnw", "spring-boot:run"]
