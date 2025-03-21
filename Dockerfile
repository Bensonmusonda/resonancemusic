# Stage 1: Build the Spring Boot application (Gradle)
FROM gradle:8.2.1-jdk17 AS builder
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew bootJar -Dorg.gradle.daemon=false

# Stage 2: Build the Python scripts environment and copy the Spring Boot JAR
FROM python:3.9-slim
WORKDIR /app

# Install Java Runtime Environment (JRE)
RUN apt-get update && apt-get install -y openjdk-17-jre-headless && rm -rf /var/lib/apt/lists/*

# Create a non-root user
RUN adduser --disabled-password --gecos '' appuser

# Change ownership of /app to appuser
RUN chown -R appuser:appuser /app

# Switch to the appuser
USER appuser

# Install yt-dlp and other Python dependencies (if any)
COPY src/main/resources/scripts/requirements.txt ./requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# Copy the Spring Boot JAR from the builder stage
COPY --from=builder /app/build/libs/*.jar ./app.jar

# Copy your Python scripts
COPY src/main/resources/scripts/ ./scripts/

# Copy the Spring Boot application properties (if needed)
COPY src/main/resources/application.properties ./application.properties

# Expose the Spring Boot port
EXPOSE 8080

# Run the Spring Boot application and your Python scripts (if needed)
CMD ["sh", "-c", "java -jar app.jar"]