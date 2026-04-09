package com.liberty.liberty;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class OllamaBootstrap {
    private static final String BASE_URL = "http://127.0.0.1:11434";
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();


    public static Process ensureOllamaReady(String modelName) throws Exception {
        Process ollamaProcess = null;

        if (!isServerUp()) {
            ollamaProcess = new ProcessBuilder("ollama", "serve")
                    .redirectErrorStream(true)
                    .start();

            waitForServer();
        }

        if (!isModelInstalled(modelName)) {
            Process pullModel = new ProcessBuilder("ollama", "pull", modelName)
                    .inheritIO()
                    .start();

            int exit = pullModel.waitFor();
            if (exit != 0) {
                throw new Exception("Failed to pull model: " + modelName);
            }
        }
        warmModel(modelName);

        return ollamaProcess;
    }

    private static boolean isServerUp() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/version"))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

            HttpResponse<String> resp = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());

            return resp.statusCode() == 200;

        } catch (Exception e) {
            return false;
        }
    }

    private static void waitForServer() throws Exception {
        int attempts = 0;
        while (attempts < 20) {
            if (isServerUp()) return;
            Thread.sleep(500);
            attempts++;
        }
        throw new IllegalStateException("Ollama server did not start in time.");
    }

    private static boolean isModelInstalled(String modelName) {
        try{
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/tags"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();


            HttpResponse<String> resp = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 && resp.body().contains(modelName);
        }catch (Exception e){
            return false;
        }
    }

    private static void warmModel(String modelName) throws Exception {
        Map<String, Object> bodyMap = Map.of(
                "model",    modelName,
                "messages", List.of(
                        Map.of(
                        "role", "user",
                        "content", "hi"
                        )
                ),
                "stream",   false
        );

        String body = new ObjectMapper().writeValueAsString(bodyMap);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/chat"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMinutes(2))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response =
                CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Failed to warm model. HTTP " + response.statusCode() + " Body: " + response.body()
            );
        }
    }
}