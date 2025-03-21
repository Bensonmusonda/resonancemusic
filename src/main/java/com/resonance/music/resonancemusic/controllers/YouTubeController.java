package com.resonance.music.resonancemusic.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api")
public class YouTubeController {

    private static final Logger logger = LoggerFactory.getLogger(YouTubeController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${yt.search.script.path}")
    private String ytSearchScriptPath; //  "scripts/yt_search.py"

    @Value("${yt.stream.script.path}")
    private String ytStreamScriptPath; // "scripts/yt_stream.py"

    // Search Endpoint
    @PostMapping("/search")
    @Async
    public CompletableFuture<ResponseEntity<?>> searchYouTube(@RequestBody Map<String, String> payload) {
        String query = payload.get("query");
        logger.info("Searching YouTube for: {}", query);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Determine the correct script path
                String scriptPath = getScriptPath(ytSearchScriptPath);

                ProcessBuilder processBuilder = new ProcessBuilder("python", scriptPath, query);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                String output = getProcessOutput(process);
                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    logger.error("Search script failed with exit code {}. Output: {}", exitCode, output);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Search failed: " + output);
                }

                // Extract JSON from output
                String jsonString = extractJson(output);
                if (jsonString == null) {
                    logger.error("No valid JSON found in script output: {}", output);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("No valid JSON found in script output");
                }

                // Use TypeReference to handle JSON array
                List<Map<String, Object>> jsonOutput = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {});
                logger.info("Search successful, returning results.");
                return ResponseEntity.ok(jsonOutput);

            } catch (IOException | InterruptedException | IllegalArgumentException e) {
                logger.error("Error during search: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error: " + e.getMessage());
            }
        });
    }

    // Stream Endpoint
    @PostMapping("/stream")
    @Async
    public CompletableFuture<ResponseEntity<?>> getAudioStream(@RequestBody Map<String, String> payload) {
        String videoUrl = payload.get("url");
        logger.info("Streaming audio from: {}", videoUrl);
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Determine the correct script path
                String scriptPath = getScriptPath(ytStreamScriptPath);

                ProcessBuilder processBuilder = new ProcessBuilder("python", scriptPath, videoUrl);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                String output = getProcessOutput(process);
                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    logger.error("Stream script failed with exit code {}. Output: {}", exitCode, output);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Stream failed: " + output);
                }

                // Extract JSON from output
                String jsonString = extractJson(output);
                if (jsonString == null) {
                    logger.error("No valid JSON found in script output: {}", output);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("No valid JSON found in script output");
                }

                // Use TypeReference to handle JSON array
                List<Map<String, Object>> jsonOutput = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {});
                logger.info("Stream was successful.");
                return ResponseEntity.ok(jsonOutput);
            } catch (IOException | InterruptedException | IllegalArgumentException e) {
                logger.error("Error during stream: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error: " + e.getMessage());
            }
        });
    }

    // Health Endpoint
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.info("Health check requested.");
        return ResponseEntity.ok("Backend is alive and running!");
    }

    // Helper method to read process output
    private String getProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
            return output.toString();
        }
    }

    // Helper method to extract JSON from a string
    private String extractJson(String input) {
        // Regular expression to find JSON object (starting with { and ending with })
        Pattern pattern = Pattern.compile("\\{.*\\}");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(0); // Return the first JSON object found
        }
        return null;
    }

    private String getScriptPath(String scriptName) {
        Path scriptPath = Paths.get(scriptName);
        if (Files.exists(scriptPath)) {
            return scriptPath.toString();
        } else {
            return "/app/" + scriptName; // Path for Docker container
        }
    }
}

