package com.liberty.liberty;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

public class SmoothTyper {
    public final StringBuilder pending = new StringBuilder();
    private final StringBuilder shown = new StringBuilder();

    private final TextArea agentResponseTextArea;

    public SmoothTyper(TextArea agentResponseTextArea){
        this.agentResponseTextArea = agentResponseTextArea;
    }

    public void startTyper(){
        Platform.runLater(() -> {
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(20), e -> {
                if (pending.isEmpty()) return;

                shown.append(pending.charAt(0));
                pending.deleteCharAt(0);
                agentResponseTextArea.setText(shown.toString());
            }));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        });
    }

    public void append(String text){
        pending.append(text);
    }
    public TextArea getAgentResponseTextArea() { return agentResponseTextArea; }
}
