package com.liberty.liberty;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Liberty extends Application {

    private final Map<Label, SmoothTyper> TYPERS = new HashMap<>();

    @Override
    public void start(Stage stage) {

        Label agentResponseLabel = new Label();
        SmoothTyper agentResponseTyper = new SmoothTyper(agentResponseLabel);
        TYPERS.put(agentResponseLabel, agentResponseTyper);
        agentResponseTyper.startTyper();

        OllamaChatService ollamaChatService = new OllamaChatService(agentResponseTyper);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");

        agentResponseLabel.setWrapText(true);
        agentResponseLabel.setStyle("-fx-text-fill: #e8e8e8; -fx-font-size: 14px; -fx-font-family: 'JetBrains Mono', 'Consolas', 'Monospaced';");
        agentResponseLabel.setText("Loading model: " + ollamaChatService.getModel() + "...");

        ScrollPane scrollPane = new ScrollPane(agentResponseLabel);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: #2b2b2b; -fx-background-color: #2b2b2b; -fx-border-color: #3c3f41; -fx-border-radius: 5;");
        scrollPane.setPadding(new Insets(10));

        agentResponseLabel.textProperty().addListener((observable, oldValue, newValue) -> scrollPane.setVvalue(1.0));
        agentResponseLabel.setText("Loading model...");
        TextField userInput = new TextField();
        userInput.setDisable(true);
        userInput.setPromptText("Enter your message here...");
        userInput.setStyle("-fx-background-color: #3c3f41; -fx-text-fill: white; -fx-prompt-text-fill: #808080; -fx-background-radius: 20; -fx-padding: 10 15; -fx-border-color: #555555; -fx-border-radius: 20;");

        HBox.setHgrow(userInput, Priority.ALWAYS);

        Button sendButton = new Button("Send");
        sendButton.setDisable(true);
        sendButton.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 20; -fx-padding: 10 20; -fx-cursor: hand;");

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

        root.setCenter(scrollPane);
        root.setBottom(inputBox);

        Scene scene = new Scene(root, 600, 700);
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
            agentResponseLabel.setText("Model loaded successfully, begin chatting!\n\nType /help for available commands.");
            userInput.setDisable(false);
            sendButton.setDisable(false);
        });
        loadModelTask.setOnFailed(_ -> agentResponseLabel.setText("Failed to load model. Please restart the application."));
        new Thread(loadModelTask).start();
    }

    private void handleCommand(String userInputText, SmoothTyper agentResponseTyper, OllamaChatService ollamaChatService){
        String[] parts = userInputText.split(" ");

        switch (parts[0].toLowerCase()){
            case "/exit" -> {
                agentResponseTyper.append("Goodbye! See you next time!\n\n");
                System.exit(0);
            }
            case "/clear" -> agentResponseTyper.getLabel().setText("");
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