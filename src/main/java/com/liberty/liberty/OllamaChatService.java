package com.liberty.liberty;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class OllamaChatService {

    private final String OLLAMA_URL = "http://127.0.0.1:11434/api/chat";
//    private final String MODEL = "qwen3.5:4b";
//    private final String MODEL = "gemma4:e4b";
    private final String MODEL = "qwen2.5-coder:7b-instruct";
    private final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final File conversationHistoryDirectory = new File("conversation_history");
    private final List<Map<String, Object>> conversationHistory = new ArrayList<>();

    {
        conversationHistoryDirectory.mkdirs();
        conversationHistory.add(Map.of(
                "role", "system",
                "content", "Do not use any Emoji's in your responses, be direct, no fluff no waffle."
        ));
    }
    private final SmoothTyper agentResponseTyper;


    public OllamaChatService(SmoothTyper agentResponseTyper) {
        this.agentResponseTyper = agentResponseTyper;
    }

    public void getAgentResponse(String newMessage) {
        if (newMessage == null || newMessage.isBlank()) {
            return;
        }

        new Thread(() -> {
            try {
                conversationHistory.add(Map.of(
                        "role", "user",
                        "content", newMessage
                ));

                Map<String, Object> bodyMap = Map.of(
                        "model", MODEL,
                        "messages", conversationHistory,
                        "stream", true
                );

                String body = MAPPER.writeValueAsString(bodyMap);

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(OLLAMA_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<Stream<String>> response = httpClient.send(req, HttpResponse.BodyHandlers.ofLines());

                StringBuilder fullResponse = new StringBuilder();

                response.body().forEach(chunk -> {
                    if (chunk == null || chunk.isBlank()) {
                        return;
                    }

                    try {
                        ResponseDTO resp = MAPPER.readValue(chunk, ResponseDTO.class);
                        MessageDTO message = resp.getMessage();

                        if(message != null){
                            String thinking = message.getThinking();
                            String content = message.getContent();

                            if (thinking != null && !thinking.isBlank()) {
                                agentResponseTyper.showLoadingAnimation("-> Model Thinking");
                            }else{
                                agentResponseTyper.stopLoadingAnimation();
                            }
                            if (content != null && !content.isBlank()) {
                                Platform.runLater(() -> agentResponseTyper.append(content));
                                fullResponse.append(content);
                            }
                        }

                        if (resp.isDone()) {
                            conversationHistory.add(Map.of(
                                    "role", "assistant",
                                    "content", fullResponse.toString()
                            ));
                            agentResponseTyper.append("\n\n");
                            System.out.println(conversationHistory);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() ->
                        agentResponseTyper  .append("[Request Failed] " + e.getMessage() + "\n\n")
                );
            }
        }).start();
    }

    public boolean saveConversationHistory(String filename){
        try{
            File path = new File(conversationHistoryDirectory + "//" +filename+".json");
            if(path.createNewFile()){
                MAPPER.writeValue(path, conversationHistory);
                System.out.println("Conversation history saved to " + path.getPath());
                return true;
            }else{
                //File already exists.
                return false;
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public boolean loadPreviousConversation(String filename){
        try{
            File previousConversation = new File(conversationHistoryDirectory + "//" + filename + ".json");
            if(previousConversation.exists() && previousConversation.isFile()){
                conversationHistory.addAll(MAPPER.readValue(previousConversation, List.class));
                return true;
            }else{
                return false;
            }
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getModel() {
        return MODEL;
    }




}
