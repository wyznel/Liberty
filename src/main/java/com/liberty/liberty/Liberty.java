package com.liberty.liberty;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.util.*;

public class Liberty extends Application {

    private final VBox VBOX_agentResponseArea = new VBox();

    @Override
    public void start(Stage stage) {
        init(stage);
    }

    private void init(Stage stage){
        Label agentResponseLabel = getNewAgentResponseLabelAndStartNewTyper(null);
        SmoothTyper agentTyper = new SmoothTyper(agentResponseLabel);
        agentTyper.startTyper();

        OllamaChatService ollamaChatService = new OllamaChatService(agentTyper);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root-pane");

        VBOX_agentResponseArea.setSpacing(5);
        VBOX_agentResponseArea.setAlignment(Pos.CENTER_LEFT);
        VBOX_agentResponseArea.getChildren().add(agentResponseLabel);

        BorderPane.setMargin(VBOX_agentResponseArea, new Insets(10));
        TextArea userInput = new TextArea();
        userInput.setWrapText(true);
        userInput.setPromptText("Enter your message here...");
        userInput.getStyleClass().add("user-input-text-area");
        userInput.setPrefHeight(20);
        userInput.setMaxHeight(30);
        userInput.setDisable(true);
        HBox.setHgrow(userInput, Priority.ALWAYS);

        Button sendButton = new Button("Send");
        sendButton.setDisable(true);
        sendButton.getStyleClass().add("send-button");

        sendButton.setOnAction(_ -> {
            if (userInput.getText().isEmpty()) return;

//            sendButton.setDisable(true);
//            userInput.setDisable(true);

            Label test1 = getNewAgentResponseLabelAndStartNewTyper(agentTyper.getAgentResponseLabel());
            ollamaChatService.getTyper().setLabel(test1);
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
        outerAgentResponseArea_SCROLLPANE.setFitToHeight(false);

        root.setCenter(outerAgentResponseArea_SCROLLPANE);
        root.setBottom(inputBox);

        Scene scene = new Scene(root, 700, 700);

        scene.getStylesheets().add(Objects.requireNonNull(Liberty.class.getResource("style.css")).toString());

        try{
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("assets/icon.png"))));

            if(Taskbar.isTaskbarSupported()){
                var taskbar = Taskbar.getTaskbar();

                if(taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)){
                    final Toolkit toolkit = Toolkit.getDefaultToolkit();
                    var dockIcon = toolkit.getImage(getClass().getResource("assets/icon.png"));
                    taskbar.setIconImage(dockIcon);
                }
            }

        }catch (Exception e){
            System.err.println("Failed to load icon: " + e.getMessage());
        }

        stage.setOnCloseRequest(e -> {
            e.consume();
            stopApp(ollamaChatService);
        });

        stage.setMinWidth(700);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.setTitle("Liberty Agent");
        stage.show();

        //Load model in the background.
        agentTyper.showLoadingAnimation(ollamaChatService.getModel());
        Task<Void> loadModelTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                OllamaBootstrap.ensureOllamaReady(ollamaChatService);
                return null;
            }
        };

        loadModelTask.setOnSucceeded(_ -> {
            agentTyper.stopLoadingAnimation();
            agentTyper.append("Model loaded successfully, begin chatting!\n\nType /help for available commands.");
            userInput.setDisable(false);
            sendButton.setDisable(false);
        });
        loadModelTask.setOnFailed(_ -> {
            agentTyper.stopLoadingAnimation();
            agentResponseLabel.setText("Failed to load model. Please restart the application.");
        });
        new Thread(loadModelTask).start();
    }

    private void handleCommand(String userInputText, SmoothTyper agentResponseTyper, OllamaChatService ollamaChatService){
        String[] parts = userInputText.split(" ");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        agentResponseTyper.clearShown();

        if(parts[0].equalsIgnoreCase("/help")){
            System.out.println("hello!");
        }

        switch (parts[0].toLowerCase()){
            case "/exit" -> stopApp(ollamaChatService);
            case "/clear" -> VBOX_agentResponseArea.getChildren().clear();
            case "/help" -> agentResponseTyper.append("""
                    Available commands:
                    /save <filename> - Save the current conversation history to a file
                    /load  <filename> - Load a conversation history from a file
                    /exit - Exit the application
                    /clear - Clear the chat window
                    /help - Show this help message""");
            case "/save" -> {
                //Save the current conversation history to a file.
                if (ollamaChatService.saveConversationHistory(parts[1])){
                    File file = new File(OllamaChatService.CONVERSATION_HISTORY_DIRECTORY + "/" + parts[1] + ".json"); // Used to get the file path.
                    agentResponseTyper.append("Conversation history saved to " + file.getAbsolutePath());
                }else{
                    agentResponseTyper.append("Failed to save conversation history. File might already exist.");
                }
            }
            case "/load" -> {
                if(ollamaChatService.loadPreviousConversation(parts[1])){
                    agentResponseTyper.append("Successfully loaded conversation history from: " + new File(OllamaChatService.CONVERSATION_HISTORY_DIRECTORY + "/" + parts[1] + ".json").getAbsolutePath());
                }else{
                    agentResponseTyper.append("Failed to load conversation history, file might not exist!");
                }
            }
            default -> agentResponseTyper.append("Unknown command. Type /help for available commands.");
        }
    }

    public VBox getVBOX_agentResponseArea() {
        return VBOX_agentResponseArea;
    }

    public Label getNewAgentResponseLabelAndStartNewTyper(Label prevLabel) {
        if(prevLabel != null){
            prevLabel.setText(prevLabel.getText() + "\n\n");
        }

        Label agentResponseLabel = new Label();
        agentResponseLabel.setWrapText(true);
        agentResponseLabel.setFocusTraversable(true);

        agentResponseLabel.getStyleClass().add("agent-response-text-area");
        agentResponseLabel.setStyle("-fx-text-fill: white; -fx-background-color: transparent;");

        VBox.setVgrow(agentResponseLabel, Priority.NEVER);

        return agentResponseLabel;
    }

    private static void stopApp(OllamaChatService ollamaChatService){
        try{
            ollamaChatService.getTyper().append("Goodbye! See you next time!");
            OllamaBootstrap.stopOllamaModel(ollamaChatService.getModel());
            System.exit(0);
        }catch (Exception e){
            System.err.println("Failed to stop model: " + e.getMessage());
            System.exit(1);
        }
    }

}