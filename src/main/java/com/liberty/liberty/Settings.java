package com.liberty.liberty;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import java.util.Objects;
public class Settings {

    private static Scene scene;
    private static BorderPane root;

    public static Scene getStage(double oldWidth, double oldHeight){
        init();
        root.setPrefSize(oldWidth, oldHeight);
        return scene;
    }

    private static void init(){
        root = new BorderPane();
        root.getStyleClass().add("root-pane");

        scene = new Scene(root);


        scene.getStylesheets().add(Objects.requireNonNull(Settings.class.getResource("settings.css")).toExternalForm());

    }

}
