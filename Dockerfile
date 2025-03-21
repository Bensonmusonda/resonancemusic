# Stage 1: Build Stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copy the Maven project files
COPY pom.xml .
COPY src ./src

# Build the Spring Boot application
RUN mvn clean package -DskipTests

# Stage 2: Runtime Stage
FROM openjdk:17-jdk-slim AS runtime
WORKDIR /app

# Install Python and dependencies
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

# Copy the Python scripts
COPY --from=build /app/src/main/resources/scripts/ ./scripts/

# Set permissions for Python scripts
RUN chmod +x /app/scripts/*.py

# Install Python dependencies (if any)
RUN if [ -f /app/scripts/requirements.txt ]; then pip3 install -r /app/scripts/requirements.txt; fi

# Copy the Spring Boot application JAR
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the Spring Boot application
CMD ["java", "-jar", "app.jar"]
