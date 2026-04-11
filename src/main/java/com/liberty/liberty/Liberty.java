package com.liberty.liberty;

import io.github.ollama4j.Ollama;
import io.github.ollama4j.exceptions.OllamaException;
import io.github.ollama4j.models.chat.*;
import io.github.ollama4j.models.generate.OllamaGenerateRequest;
import io.github.ollama4j.models.generate.OllamaGenerateTokenHandler;
import io.github.ollama4j.models.response.OllamaResult;
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
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class Liberty extends Application {

    private final Map<TextArea, SmoothTyper> TYPERS = new HashMap<>();

    private final VBox VBOX_agentResponseArea = new VBox();

    @Override
    public void start(Stage stage) throws OllamaException {
        init(stage);
    }

    private void init(Stage stage){
        TextArea agentResponseTextArea = getNewAgentResponseTextAreaAndStartNewTyper();
        SmoothTyper agentTyper = new SmoothTyper(agentResponseTextArea);
        agentTyper.startTyper();
        TYPERS.put(agentResponseTextArea, agentTyper);

        OllamaChatService ollamaChatService = new OllamaChatService(agentTyper);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root-pane");

        VBOX_agentResponseArea.setSpacing(5);
        VBOX_agentResponseArea.setAlignment(Pos.CENTER_LEFT);
        VBOX_agentResponseArea.getChildren().add(agentResponseTextArea);

        BorderPane.setMargin(VBOX_agentResponseArea, new Insets(10));
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

            TextArea test1 = getNewAgentResponseTextAreaAndStartNewTyper();
            ollamaChatService.getTyper().setTextArea(test1);
            VBOX_agentResponseArea.getChildren().add(test1);

            if(userInput.getText().startsWith("/")){
                handleCommand(userInput.getText(), agentTyper, ollamaChatService);
            }else{
                ollamaChatService.chat(userInput.getText());
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

        ScrollPane outerAgentResponseArea_SCROLLPANE = new ScrollPane();
        outerAgentResponseArea_SCROLLPANE.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        outerAgentResponseArea_SCROLLPANE.getStyleClass().add("scroll-pane-agent-response");

        outerAgentResponseArea_SCROLLPANE.setContent(VBOX_agentResponseArea);
        outerAgentResponseArea_SCROLLPANE.setFitToWidth(true);
        outerAgentResponseArea_SCROLLPANE.setFitToHeight(true);

        root.setCenter(outerAgentResponseArea_SCROLLPANE);
        root.setBottom(inputBox);

        Scene scene = new Scene(root, 600, 700);

        scene.getStylesheets().add(Objects.requireNonNull(Liberty.class.getResource("style.css")).toString());

        stage.setMinWidth(600);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.setTitle("Liberty Agent");
        stage.show();

        //Load model in the background.
        agentTyper.showLoadingAnimation(ollamaChatService.getModel());
        Task<Void> loadModelTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                OllamaBootstrap.ensureOllamaReady(ollamaChatService.getModel());
                return null;
            }
        };
        loadModelTask.setOnSucceeded(_ -> {
            agentTyper.stopLoadingAnimation();
            agentTyper.append("Model loaded successfully, begin chatting!\n\nType /help for available commands.\n\n");
            userInput.setDisable(false);
            sendButton.setDisable(false);
        });
        loadModelTask.setOnFailed(_ -> {
            agentTyper.stopLoadingAnimation();
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

    public VBox getVBOX_agentResponseArea() {
        return VBOX_agentResponseArea;
    }

    public TextArea getNewAgentResponseTextAreaAndStartNewTyper(){
        TextArea agentResponseTextArea = new TextArea();
        agentResponseTextArea.setWrapText(true);
        agentResponseTextArea.setMinHeight(20);
        agentResponseTextArea.setPrefHeight(100);
        agentResponseTextArea.setEditable(false);
        agentResponseTextArea.getStyleClass().add("agent-response-text-area");
        agentResponseTextArea.setStyle("-fx-text-fill: white; -fx-background-color: transparent");

        Runnable dynamicHeightResize = () -> {
          Text helper = new Text(agentResponseTextArea.getText());
          helper.setFont(agentResponseTextArea.getFont());
          helper.setWrappingWidth(agentResponseTextArea.getWidth() - 20);
          double textHeight = helper.getLayoutBounds().getHeight();

          agentResponseTextArea.setPrefHeight(textHeight + 25);
        };
        agentResponseTextArea.textProperty().addListener((obs, oldHeight, newHeight) -> {dynamicHeightResize.run();});

        return agentResponseTextArea;
    }

}