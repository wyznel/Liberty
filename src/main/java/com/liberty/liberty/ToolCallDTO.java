package com.liberty.liberty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolCallDTO {
    private String id;
    private String type;
    private ToolFunctionDTO function;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public ToolFunctionDTO getFunction() { return function; }
    public void setFunction(ToolFunctionDTO function) { this.function = function; }
}