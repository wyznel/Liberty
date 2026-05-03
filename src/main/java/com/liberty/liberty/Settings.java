package com.liberty.liberty;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
public class Settings {

    private static Scene scene;
    private static BorderPane root;
    private static Scene chatScene;

    private static final ArrayList<Button> activeMenuSelectionButtons = new ArrayList<>();
    private static final ArrayList<Label> activeModelLabels = new ArrayList<>();

    public static OllamaChatService ollamaChatService;

    public static Scene getScene(double oldWidth, double oldHeight, Scene mainScene){
        chatScene = mainScene;
        root.setPrefSize(oldWidth, oldHeight);
        return scene;
    }

    public static void init(){
        root = new BorderPane();
        root.getStyleClass().add("base");

        root.setLeft(getLeftPanel());
        scene = new Scene(root);

        scene.getStylesheets().add(Objects.requireNonNull(Settings.class.getResource("settings.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(Settings.class.getResource("main.css")).toExternalForm());
    }

    private static VBox getLeftPanel(){
        VBox leftPanel = new VBox();
        leftPanel.setSpacing(10);
        leftPanel.setPadding(new Insets(10,10,0,10));
        leftPanel.setAlignment(Pos.TOP_LEFT);

        Button backToChatsButton = new Button("← Back");
        Button showModels = new Button("Models");
        Button showAddedFiles = new Button("Files");

        backToChatsButton.setOnAction(_ -> {
            Liberty.mainStage.setScene(chatScene);
        });

        showModels.setOnAction(_ -> {
            updateActiveMenuSelectionButton(showModels);
            root.setCenter(getModelTab());
        });

        showAddedFiles.setOnAction(_ -> {
            updateActiveMenuSelectionButton(showAddedFiles);
        });

        activeMenuSelectionButtons.add(backToChatsButton);
        activeMenuSelectionButtons.add(showModels);
        activeMenuSelectionButtons.add(showAddedFiles);

        updateActiveMenuSelectionButton(showModels);
        leftPanel.getChildren().addAll(backToChatsButton, showModels, showAddedFiles);
        leftPanel.getStyleClass().addAll("base", "left-panel");

        showModels.fire();
        return leftPanel;
    }

    private static VBox getModelTab(){
        VBox modelTab = new VBox();

        Arrays.stream(OllamaChatService.getAvailableModels()).forEach(model -> {
            Label modelLabel = new Label(model);
            modelLabel.getStyleClass().add("model-label");

            modelLabel.setMaxWidth(200);
            Button selectModelButton = new Button("Select");
            HBox container = new HBox(10, modelLabel, selectModelButton);
            container.setAlignment(Pos.CENTER_LEFT);

            HBox.setHgrow(modelLabel, Priority.ALWAYS);
            modelTab.getChildren().add(container);

            selectModelButton.setOnAction(_ -> {
                updateActiveModelLabel(modelLabel);
                OllamaBootstrap.setActiveModel(model, ollamaChatService);
            });
            activeModelLabels.add(modelLabel);
        });

        return modelTab;
    }

    private static void updateActiveMenuSelectionButton(Button activeTab){
        for(Button b : activeMenuSelectionButtons){
            b.getStyleClass().remove("active-tab");
        }
        activeTab.getStyleClass().add("active-tab");
    }

    private static void updateActiveModelLabel(Label activeModelLabel){
        activeModelLabels.forEach(l -> {
            l.getStyleClass().remove("model-label-active");
        });
        activeModelLabel.getStyleClass().add("model-label-active");
    }

}