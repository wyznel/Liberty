package com.liberty.liberty;

import io.github.ollama4j.Ollama;
import io.github.ollama4j.exceptions.OllamaException;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResult;
import io.github.ollama4j.models.chat.OllamaChatStreamObserver;

import java.io.File;
import java.util.List;
import java.util.Map;

public class OllamaChatService {

    private final Ollama ollama = new Ollama("http://127.0.0.1:11434");
    private final OllamaChatRequest builder = OllamaChatRequest.builder().withModel("qwen2.5-coder:7b-instruct");
    private final OllamaChatRequest requestModel = builder.withMessage(OllamaChatMessageRole.SYSTEM, "Do not use any Emoji's in your responses, be direct, no fluff no waffle.").build();
    private final File conversationHistoryDirectory = new File("conversation_history");
    private OllamaChatResult chatResult = null;
    private final SmoothTyper agentResponseTyper;

    public OllamaChatService(SmoothTyper agentResponseTyper) {
        this.agentResponseTyper = agentResponseTyper;
    }

    {
        conversationHistoryDirectory.mkdirs();
    }

    public void chat(String prompt){
        new Thread(() -> {
            try{
                OllamaChatRequest requestModel = chatResult == null ?
                        builder.withMessage(OllamaChatMessageRole.USER, prompt).build() :
                        builder.withMessages(chatResult.getChatHistory()).withMessage(OllamaChatMessageRole.USER, prompt).build();

                try{
                    agentResponseTyper.clearShown();
                    OllamaChatStreamObserver streamObserver = new OllamaChatStreamObserver();
                    streamObserver.setThinkingStreamHandler(
                            s -> System.out.println(s.toLowerCase())
                    );

                    streamObserver.setResponseStreamHandler(
                            agentResponseTyper::append
                    );

                    chatResult = ollama.chat(requestModel, streamObserver);
                }catch (OllamaException e){
                    System.out.println("Server offline");
                }

            }catch (Exception e){
                System.out.println("Server offline");
            }
        }).start();
    }

    public boolean saveConversationHistory(String filename){
        try{
            File path = new File(conversationHistoryDirectory + "//" +filename+".json");
            if(path.createNewFile()){
                System.out.println("Conversation history saved to " + path.getPath());
                return true;
            }else{
                //File already exists.
                return false;
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public boolean loadPreviousConversation(String filename){
        try{
            File previousConversation = new File(conversationHistoryDirectory + "//" + filename + ".json");
            if(previousConversation.exists() && previousConversation.isFile()){

                return true;
            }else{
                return false;
            }
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SmoothTyper getTyper() {
        return agentResponseTyper;
    }

    public String getModel(){
        return requestModel.getModel();
    }

}
