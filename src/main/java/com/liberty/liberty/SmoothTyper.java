package com.liberty.liberty;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.concurrent.atomic.AtomicInteger;

public class SmoothTyper {
    public final StringBuilder pending = new StringBuilder();
    private final StringBuilder shown = new StringBuilder();

    private Label agentResponseLabel;

    private Timeline mainTimeline;
    private Timeline loadingTimeline;

    public SmoothTyper(Label agentResponseLabel){
        this.agentResponseLabel = agentResponseLabel;
    }

    public synchronized void startTyper(){
        Platform.runLater(() -> {
            mainTimeline = new Timeline(new KeyFrame(Duration.millis(20), _ -> {
                if (pending.isEmpty()) {
                    return;
                }

                shown.append(pending.charAt(0));
                pending.deleteCharAt(0);
                agentResponseLabel.setText(shown.toString());

            }));
            mainTimeline.setCycleCount(Animation.INDEFINITE);
            mainTimeline.play();
        });
    }


    public synchronized void setLabel(Label agentResponseLabel){
        this.agentResponseLabel = agentResponseLabel;
    }

    public synchronized void stopTyper(){
        mainTimeline.stop();
    }

    public synchronized void showLoadingAnimation(String custText){
        if(loadingTimeline!=null) return;

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
                agentResponseLabel.setText(shown.toString());
            })) ;
           loadingTimeline.setCycleCount(Animation.INDEFINITE);
           loadingTimeline.play();
        });
    }

    public synchronized void stopLoadingAnimation(){
        if(loadingTimeline==null) return;
        append(System.lineSeparator() + System.lineSeparator());
        loadingTimeline.stop();
        loadingTimeline = null;
        System.out.println("Stopped loading animation timeline");
    }
    public synchronized void append(String text){
        pending.append(text);
    }
    public Label getAgentResponseLabel() { return agentResponseLabel; }

    public synchronized void clearShown(){
        shown.setLength(0);
    }

    public synchronized boolean isPendingEmpty(){
        return pending.isEmpty();
    }

}
