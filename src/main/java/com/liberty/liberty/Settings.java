package com.liberty.liberty;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Objects;
public class Settings {

    private static Scene scene;
    private static BorderPane root;
    private static Scene chatScene;

    private static ArrayList<Button> activeMenuSelectionButtons = new ArrayList<>();

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
        return leftPanel;
    }

    private static VBox modelTab(){
        VBox modelTab = new VBox();



        return modelTab;
    }

    private static void updateActiveMenuSelectionButton(Button activeTab){
        for(Button b : activeMenuSelectionButtons){
            b.getStyleClass().remove("active-tab");
        }
        activeTab.getStyleClass().add("active-tab");
    }
}