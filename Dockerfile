# Stage 1: Build Stage
FROM gradle:8.12.1-jdk17 AS build
WORKDIR /app

# Copy the Gradle wrapper and source code.  This is optimized for Gradle Wrapper.
COPY gradle ./gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY src ./src

# Use the Gradle wrapper to build the application.  This ensures the correct Gradle version is used.
RUN ./gradlew bootJar

# Stage 2: Runtime Stage
FROM openjdk:17-jdk-slim AS runtime
WORKDIR /app

# Install Python and dependencies
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

# Copy the Python scripts from the build stage.  Adjust this path if necessary!
COPY --from=build /app/build/resources/main/scripts/ ./scripts/

# Set permissions for Python scripts
RUN chmod +x /app/scripts/*.py

# Install Python dependencies (if any)
RUN if [ -f /app/scripts/requirements.txt ]; then pip3 install -r /app/scripts/requirements.txt; fi

# Copy the Spring Boot application JAR from the build stage.
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the Spring Boot application
CMD ["java", "-jar", "app.jar"]
