package com.resonance.music.resonancemusic.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class YouTubeController {

    // Search Endpoint
    @PostMapping("/search")
    public ResponseEntity<?> searchYouTube(@RequestBody Map<String, String> payload) {
        String query = payload.get("query");
        try {
            // Run the Python script
            ProcessBuilder processBuilder = new ProcessBuilder(
                "python",
                "src/main/resources/scripts/yt_search.py", // Update path here
                query
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read the script output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            process.waitFor();

            // Return the response
            return ResponseEntity.ok(output.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // Stream Endpoint
    @PostMapping("/stream")
    public ResponseEntity<?> getAudioStream(@RequestBody Map<String, String> payload) {
        String videoUrl = payload.get("url");
        try {
            // Run the Python script
            ProcessBuilder processBuilder = new ProcessBuilder(
                "python",
                "src/main/resources/scripts/yt_stream.py", // Update path here
                videoUrl
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read the script output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            process.waitFor();

            // Return the response
            return ResponseEntity.ok(output.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // Health Endpoint
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Backend is alive and running!");
    }
}

