package com.liberty.liberty;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Liberty extends Application {

    private final Map<TextArea, SmoothTyper> TYPERS = new HashMap<>();

    @Override
    public void start(Stage stage) {

        TextArea agentResponseTextArea = new TextArea();

        SmoothTyper agentResponseTyper = new SmoothTyper(agentResponseTextArea);
        TYPERS.put(agentResponseTextArea, agentResponseTyper);
        agentResponseTyper.startTyper();

        OllamaChatService ollamaChatService = new OllamaChatService(agentResponseTyper);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root-pane");

        agentResponseTextArea.setWrapText(true);
        agentResponseTextArea.setEditable(false);
        agentResponseTextArea.getStyleClass().add("agent-response-text-area");
        agentResponseTextArea.setStyle("-fx-text-fill: white; -fx-background-color: transparent");
        agentResponseTyper.showLoadingAnimation(ollamaChatService.getModel());
        agentResponseTextArea.textProperty().addListener((observable, oldValue, newValue) -> agentResponseTextArea.setScrollTop(Double.MAX_VALUE));
        BorderPane.setMargin(agentResponseTextArea, new Insets(10));

        TextArea userInput = new TextArea();
        userInput.setWrapText(true);
        userInput.setPromptText("Enter your message here...");
        userInput.getStyleClass().add("user-input-text-area");
        userInput.setPrefHeight(20);
        userInput.setMaxHeight(30);
        HBox.setHgrow(userInput, Priority.ALWAYS);

        Button sendButton = new Button("Send");
        sendButton.setDisable(true);
        sendButton.getStyleClass().add("send-button");

        sendButton.setOnAction(_ -> {

            if (userInput.getText().isEmpty()) return;
            if(userInput.getText().startsWith("/")){
                handleCommand(userInput.getText(), agentResponseTyper, ollamaChatService);
            }else{
                ollamaChatService.getAgentResponse(userInput.getText());
            }
            userInput.clear();
        });
        userInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                sendButton.fire();
            }
        });

        HBox inputBox = new HBox(10, userInput, sendButton);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(15, 0, 0, 0));

        root.setCenter(agentResponseTextArea);
        root.setBottom(inputBox);

        Scene scene = new Scene(root, 600, 700);

        scene.getStylesheets().add(Objects.requireNonNull(Liberty.class.getResource("style.css")).toString());

        stage.setMinWidth(600);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.setTitle("Liberty Agent");
        stage.show();

        //Load model in the background.
        Task<Void> loadModelTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                OllamaBootstrap.ensureOllamaReady(ollamaChatService.getModel());
                return null;
            }
        };
        loadModelTask.setOnSucceeded(_ -> {
            agentResponseTyper.stopLoadingAnimation();
            agentResponseTextArea.setText("Model loaded successfully, begin chatting!\n\nType /help for available commands.");
            userInput.setDisable(false);
            sendButton.setDisable(false);
        });
        loadModelTask.setOnFailed(_ -> {
            agentResponseTyper.stopLoadingAnimation();
            agentResponseTextArea.setText("Failed to load model. Please restart the application.");
        });
        new Thread(loadModelTask).start();
    }

    private void handleCommand(String userInputText, SmoothTyper agentResponseTyper, OllamaChatService ollamaChatService){
        String[] parts = userInputText.split(" ");

        switch (parts[0].toLowerCase()){
            case "/exit" -> {
                agentResponseTyper.append("Goodbye! See you next time!\n\n");
                System.exit(0);
            }
            case "/clear" -> agentResponseTyper.getAgentResponseTextArea().setText("");
            case "/help" -> {
                agentResponseTyper.append("Available commands:\n" +
                        "/save <filename> - Save the current conversation history to a file\n" +
                        "/load  <filename> - Load a conversation history from a file\n" +
                        "/exit - Exit the application\n" +
                        "/clear - Clear the chat window\n" +
                        "/help - Show this help message\n\n");
            }
            case "/save" -> {
                //Save the current conversation history to a file.
                if (ollamaChatService.saveConversationHistory(parts[1])){
                    File file = new File(parts[1] + ".json");
                    agentResponseTyper.append("Conversation history saved to " + file.getAbsolutePath() + "\n\n");
                }else{
                    agentResponseTyper.append("Failed to save conversation history. File might already exist.\n\n");
                }
            }
            case "/load" -> {
                if(ollamaChatService.loadPreviousConversation(parts[1])){
                    agentResponseTyper.append("Successfully loaded conversation history from: " + new File(parts[1] + ".json").getAbsolutePath() + "\n\n");
                }else{
                    agentResponseTyper.append("Failed to load conversation history, file might not exist!");
                }
            }
            default -> agentResponseTyper.append("Unknown command. Type /help for available commands.\n\n");
        }

    }

}