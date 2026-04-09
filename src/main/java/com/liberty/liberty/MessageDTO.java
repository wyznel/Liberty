package com.liberty.liberty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageDTO {
    private String role;
    private String content;

    @JsonProperty("tool_calls")
    private List<ToolCallDTO> tool_calls;

    @JsonProperty("thinking")
    private String thinking;


    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<ToolCallDTO> getTool_calls() { return tool_calls; }
    public void setTool_calls(List<ToolCallDTO> tool_calls) { this.tool_calls = tool_calls; }

    public String getThinking() { return thinking; }
    public void setThinking(String thinking) { this.thinking = thinking; }
}