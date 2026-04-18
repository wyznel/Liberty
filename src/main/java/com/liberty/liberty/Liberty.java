package com.liberty.liberty;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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

    private final VBox historyVbox = new VBox();

    private final ArrayList<VBox> chatStore = new ArrayList<>();
    private final ArrayList<Button> chatButtons = new ArrayList<>();

    private ScrollPane outerAgentResponseArea_SCROLLPANE;
    private VBox activeChat;

    private int chatIndex = 0;

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

        outerAgentResponseArea_SCROLLPANE = new ScrollPane();
        outerAgentResponseArea_SCROLLPANE.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        outerAgentResponseArea_SCROLLPANE.getStyleClass().add("scroll-pane-agent-response");
        outerAgentResponseArea_SCROLLPANE.setFitToWidth(true);
        outerAgentResponseArea_SCROLLPANE.setFitToHeight(false);

        VBox agentResponseParent = getNewChatArea(ollamaChatService);
        agentResponseParent.getChildren().add(agentResponseLabel);

        TextArea userInput = getUserInput();

        Button sendButton = new Button("Send");
        sendButton.disableProperty().bind(OllamaBootstrap.isOllamaReady.not());
        sendButton.getStyleClass().add("send-button");

        sendButton.setOnAction(_ -> {
            if (userInput.getText().isEmpty()) return;

            Label newResponseLabel = getNewAgentResponseLabelAndStartNewTyper(agentTyper.getAgentResponseLabel());
            ollamaChatService.getTyper().setLabel(newResponseLabel);
            activeChat.getChildren().add(newResponseLabel);

            if(userInput.getText().startsWith("/")){
                handleCommand(userInput.getText(), agentTyper, ollamaChatService, activeChat);
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

        root.setCenter(outerAgentResponseArea_SCROLLPANE);
        root.setLeft(leftPanel(ollamaChatService));
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
        });
        loadModelTask.setOnFailed(_ -> {
            agentTyper.stopLoadingAnimation();
            agentResponseLabel.setText("Failed to load model. Please restart the application.");
        });
        new Thread(loadModelTask).start();
    }

    private VBox leftPanel(OllamaChatService ollamaChatService){
        VBox leftPanel = new VBox();
        leftPanel.setSpacing(10);
        leftPanel.setPadding(new Insets(0,10,0, -5));
        leftPanel.setAlignment(Pos.TOP_LEFT);

        Button newChat = new Button(" + New Chat");
        Button settings = new Button(" ⚙ Settings");
        Button showHistory = new Button(" ⏱︎ History");

        newChat.disableProperty().bind(OllamaBootstrap.isOllamaReady.not());
        settings.disableProperty().bind(OllamaBootstrap.isOllamaReady.not());
        showHistory.disableProperty().bind(OllamaBootstrap.isOllamaReady.not());

        newChat.setOnAction(e -> {
            VBox newChatArea = getNewChatArea(ollamaChatService);
            Label newResponseLabel = getNewAgentResponseLabelAndStartNewTyper(null);
            ollamaChatService.getTyper().setLabel(newResponseLabel);
            newChatArea.getChildren().add(newResponseLabel);
        });

        settings.setOnAction(e -> {});

        historyVbox.getStyleClass().add(".base");
        leftPanel.getChildren().addAll(newChat, settings, showHistory, historyVbox);
        leftPanel.getStyleClass().add(".base");
        return leftPanel;
    }

    private TextArea getUserInput(){
        TextArea userInput = new TextArea();
        userInput.setWrapText(true);
        userInput.setPromptText("Enter your message here...");
        userInput.getStyleClass().add("user-input-text-area");
        userInput.setPrefHeight(20);
        userInput.setMaxHeight(30);
        userInput.disableProperty().bind(OllamaBootstrap.isOllamaReady.not());
        HBox.setHgrow(userInput, Priority.ALWAYS);
        return userInput;
    }

    private void handleCommand(String userInputText, SmoothTyper agentResponseTyper, OllamaChatService ollamaChatService, VBox agentResponseParent){
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
            case "/clear" -> agentResponseParent.getChildren().clear();
            case "/help" -> agentResponseTyper.append("""
                    Available commands:
                    /save <filename> - Save the current conversation history to a file
                    /load  <filename> - Load a conversation history from a file
                    /exit - Exit the application
                    /clear - Clear the chat window
                    /help - Show this help message""");
            case "/save" -> {
                //Save the current conversation history to a file.
                if (ollamaChatService.saveConversationHistory(parts[1], true)){
                    File file = new File(OllamaChatService.CONVERSATION_HISTORY_DIRECTORY + "/" + parts[1] + ".json"); // Used to get the file path.
                    agentResponseTyper.append("Conversation history saved to " + file.getAbsolutePath());
                }else{
                    agentResponseTyper.append("Failed to save conversation history. File might already exist.");
                }
            }
            case "/load" -> {
                if(ollamaChatService.loadConversation(parts[1], true)){
                    agentResponseTyper.append("Successfully loaded conversation history from: " + new File(OllamaChatService.CONVERSATION_HISTORY_DIRECTORY + "/" + parts[1] + ".json").getAbsolutePath());
                }else{
                    agentResponseTyper.append("Failed to load conversation history, file might not exist!");
                }
            }
            default -> agentResponseTyper.append("Unknown command. Type /help for available commands.");
        }
    }

    public Label getNewAgentResponseLabelAndStartNewTyper(Label prevLabel) {
        if(prevLabel != null){
            prevLabel.setText(prevLabel.getText() + System.lineSeparator() + System.lineSeparator());
        }

        Label agentResponseLabel = new Label();
        agentResponseLabel.setWrapText(true);
        agentResponseLabel.setFocusTraversable(true);

        agentResponseLabel.getStyleClass().add("agent-response-text-area");
        agentResponseLabel.setStyle("-fx-text-fill: white; -fx-background-color: transparent;");

        VBox.setVgrow(agentResponseLabel, Priority.NEVER);

        return agentResponseLabel;
    }

    public VBox getNewChatArea(OllamaChatService ollamaChatService){
        if (activeChat != null) {
            ollamaChatService.saveConversationHistory(activeChat.getId(), false);
            ollamaChatService.clearHistory();
        }

        VBox agentResponseParent = new VBox();
        agentResponseParent.setSpacing(5);
        agentResponseParent.setAlignment(Pos.CENTER_LEFT);
        BorderPane.setMargin(agentResponseParent, new Insets(10));
        chatStore.add(agentResponseParent);
        int newChatID = chatIndex;
        String chatId = Integer.toString(newChatID);

        Button switchToChatButton = new Button("Chat " + chatIndex);
        switchToChatButton.setId(chatId);
        agentResponseParent.setId(chatId);
        chatIndex++;

        switchToChatButton.setOnAction(_ -> {
            if (activeChat != null && !activeChat.getId().equals(String.valueOf(newChatID))) {
                switchMemory(Integer.parseInt(activeChat.getId()), newChatID, ollamaChatService);
            }
            updateActiveChatStyle(newChatID);

            outerAgentResponseArea_SCROLLPANE.setContent(chatStore.get(newChatID));
            activeChat = chatStore.get(newChatID);
        });

        activeChat = agentResponseParent;
        chatButtons.add(switchToChatButton);
        historyVbox.getChildren().add(switchToChatButton);

        updateActiveChatStyle(newChatID);
        outerAgentResponseArea_SCROLLPANE.setContent(agentResponseParent);
        return agentResponseParent;
    }

    private void updateActiveChatStyle(int newIndex) {
        // Remove style from all buttons and add it to the active one
        for (Button chatButton : chatButtons) {
            chatButton.getStyleClass().remove("new-chat-button-active-chat");
        }
        if (newIndex >= 0 && newIndex < chatButtons.size()) {
            chatButtons.get(newIndex).getStyleClass().add("new-chat-button-active-chat");
        }
    }

    private void switchMemory(int oldChatID, int newChatID,  OllamaChatService ollamaChatService){
        System.out.println(oldChatID + " -> " + newChatID);

        ollamaChatService.saveConversationHistory(String.valueOf(oldChatID), false);
        ollamaChatService.clearHistory();
        boolean loaded = ollamaChatService.loadConversation(String.valueOf(newChatID), false);
        if (!loaded) {
            System.out.println("No history found for Chat " + newChatID + ", starting fresh.");
        }
    }

    private static void stopApp(OllamaChatService ollamaChatService){
        try{
            ollamaChatService.getTyper().append("Goodbye! See you next time!");
            OllamaBootstrap.stopOllamaModel(ollamaChatService.getModel());
            ollamaChatService.clearRuntimeHistoryDir();

            System.exit(0);
        }catch (Exception e){
            System.err.println("Failed to stop model: " + e.getMessage());
            System.exit(1);
        }
    }

}