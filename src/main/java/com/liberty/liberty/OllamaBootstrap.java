package com.liberty.liberty;

import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.generate.OllamaGenerateRequest;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class OllamaBootstrap {
    private static final String BASE_URL = "http://127.0.0.1:11434";
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

    public static BooleanProperty isOllamaReady = new SimpleBooleanProperty(false);

    public static void ensureOllamaReady(OllamaChatService ollamaChatService) throws Exception {

        String modelName = ollamaChatService.getModel();

        if (!isServerUp(ollamaChatService)) {
            new ProcessBuilder("ollama", "serve")
                    .redirectErrorStream(true)
                    .start();

            waitForServer(ollamaChatService);
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
        warmModel(ollamaChatService);
        isOllamaReady.set(true);
    }

    private static boolean isServerUp(OllamaChatService ollamaChatService) {
        try {
            return ollamaChatService.getOllama().ping();
        } catch (Exception e) {
            return false;
        }
    }

    private static void waitForServer(OllamaChatService ollamaChatService) throws Exception {
        int attempts = 0;
        while (attempts < 20) {
            if (isServerUp(ollamaChatService)){return;}
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

    private static void warmModel(OllamaChatService ollamaChatService) throws Exception {
        ollamaChatService.getOllama().generate(
                OllamaGenerateRequest.builder()
                        .withModel(ollamaChatService.getModel())
                        .withPrompt("Ignore this, warming up the model")
                        .build(),
        null);
    }

    public static void stopOllamaModel(String modelName) throws Exception {
        new ProcessBuilder("ollama", "stop", modelName)
                .inheritIO()
                .start();
    }

    public static void setActiveModel(String modelName, OllamaChatService ollamaChatService){
        try{
            stopOllamaModel(modelName);
        }catch (Exception e){
            System.err.println("Failed to stop model: " + e.getMessage());
        }
        ollamaChatService.getBuilder().setModel(modelName);
        OllamaChatRequest newChatRequest = ollamaChatService.getBuilder().withMessage(OllamaChatMessageRole.SYSTEM, String.format("""
            You are Liberty, a local desktop AI assistant.
            You may answer normally or request exactly one tool call.
            
            Available tools:
            1. createNewFile(filename: string)
            2. writeToFile(filename: string, data: string)
            3. readFromFile(filename: string)
            4. extractTextFromPDF(filename: string)
            
            Rules:
            - Be direct, no fluff, no waffle, and DO NOT use emojis.
            - When asked for your name, respond with: %s
            """, modelName)).build();

        ollamaChatService.setRequestModel(newChatRequest);

        try{
            ensureOllamaReady(ollamaChatService);
        }catch (Exception e){
            System.err.println("Failed to warm up model: " + e.getMessage());
        }
    }

}