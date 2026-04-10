package com.liberty.liberty;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

import java.util.concurrent.atomic.AtomicInteger;

public class SmoothTyper {
    public final StringBuilder pending = new StringBuilder();
    private final StringBuilder shown = new StringBuilder();

    private final TextArea agentResponseTextArea;

    private Timeline mainTimeline;
    private Timeline loadingTimeline;

    public SmoothTyper(TextArea agentResponseTextArea){
        this.agentResponseTextArea = agentResponseTextArea;
    }

    public void startTyper(){
        Platform.runLater(() -> {
            mainTimeline = new Timeline(new KeyFrame(Duration.millis(20), _ -> {
                if (pending.isEmpty()) return;

                shown.append(pending.charAt(0));
                pending.deleteCharAt(0);
                agentResponseTextArea.setText(shown.toString());
            }));
            mainTimeline.setCycleCount(Animation.INDEFINITE);
            mainTimeline.play();
        });
    }

    public void stopTyper(){
        mainTimeline.stop();
    }

    public void showLoadingAnimation(String custText){
        Platform.runLater(() -> {
            AtomicInteger noOfDots = new AtomicInteger(1);
            shown.append(String.format("Loading: %s", custText));
            loadingTimeline = new Timeline(new KeyFrame(Duration.millis(200), _ -> {
                shown.append(".");
                noOfDots.getAndIncrement();
                if(noOfDots.get() > 4){
                    noOfDots.set(1);
                    shown.delete(shown.length() - 4, shown.length());
                }
                agentResponseTextArea.setText(shown.toString());
            })) ;
           loadingTimeline.setCycleCount(Animation.INDEFINITE);
           loadingTimeline.play();
        });
    }

    public void stopLoadingAnimation(){
        loadingTimeline.stop();
    }
    public void append(String text){
        pending.append(text);
    }
    public TextArea getAgentResponseTextArea() { return agentResponseTextArea; }
}
