package com.liberty.liberty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseDTO {
    private String model;
    private String created_at;
    private boolean done;
    private MessageDTO message;

    public boolean isDone() { return done; }
    public String getModel() { return model; }
    public String getCreated_at() { return created_at; }
    public MessageDTO getMessage() { return message; }

    public void setModel(String model) { this.model = model; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
    public void setMessage(MessageDTO message) { this.message = message; }
    public void setDone(boolean done) { this.done = done; }

}