package com.liberty.liberty;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liberty.liberty.Tools.AvailableTools;
import com.liberty.liberty.Tools.PDFTools;
import io.github.ollama4j.Ollama;
import io.github.ollama4j.exceptions.OllamaException;
import io.github.ollama4j.models.chat.*;
import io.github.ollama4j.tools.annotations.OllamaToolService;


import java.io.*;
import java.util.ArrayList;
import java.util.List;


@OllamaToolService(providers = { AvailableTools.class, PDFTools.class })
public class OllamaChatService {

    private static String[] AVAILABLE_MODELS = {
            "gemma4:e4b", "qwen2.5:7b-instruct",
            "qwen3.5:4b"
    };


    public static final File CONVERSATION_HISTORY_DIRECTORY = new File("conversation_history");

    private final Ollama ollama = new Ollama("http://127.0.0.1:11434");
    private final OllamaChatRequest builder = OllamaChatRequest.builder().withModel(AVAILABLE_MODELS[1]);
    private OllamaChatRequest requestModel = builder.withMessage(OllamaChatMessageRole.SYSTEM, "You are Liberty, a local desktop AI assistant.\n" +
            "You may answer normally or request exactly one tool call.\n" +
            "\n" +
            "Available tools:\n" +
            "1. createNewFile(filename: string)\n" +
            "2. writeToFile(filename: string, data: string)\n" +
            "3. readFromFile(filename: string)\n" +
            "4. extractTextFromPDF(filename: string)\n" +
            "\n" +
            "Rules:\n" +
            "- Be direct, no fluff, no waffle, and do not use emojis.").build();
    private OllamaChatResult chatResult = new OllamaChatResult(new OllamaChatResponseModel(), new ArrayList<OllamaChatMessage>() {
    });
    private final SmoothTyper agentResponseTyper;

    public OllamaChatService(SmoothTyper agentResponseTyper) {
        this.agentResponseTyper = agentResponseTyper;

        try{
            ollama.registerAnnotatedTools();
        }catch(OllamaException e){
            System.err.println(e.getMessage());
        }

    }

    {
        if(CONVERSATION_HISTORY_DIRECTORY.mkdirs()){
            System.out.println("> Conversation history directory created");
        }

    }

    public void chat(String prompt){
        new Thread(() -> {
            try{
                requestModel = builder.withMessages(chatResult.getChatHistory()).withMessage(OllamaChatMessageRole.USER, prompt).build();

                try{
                    agentResponseTyper.clearShown();
                    OllamaChatStreamObserver streamObserver = new OllamaChatStreamObserver();
                    streamObserver.setThinkingStreamHandler(
                            s -> System.out.print(s.toLowerCase())
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


    /**
     * Saves the current conversation to a JSON file.
     * @param filename name of file.
     * @return TRUE / FALSE if successful.
     */
    public boolean saveConversationHistory(String filename){
        try{
            File path = new File(CONVERSATION_HISTORY_DIRECTORY + "/" +filename+".json");
            if(path.createNewFile()){
                System.out.println("Conversation history saved to " + path.getAbsolutePath());

                BufferedWriter writer = new BufferedWriter(new FileWriter(path));
                writer.write(chatResult.getChatHistory().toString());
                writer.close();
                return true;
            }else{
                //File already exists.
                return false;
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads a conversation and adds it to the LLMs context.
     * @param filename name of file.
     * @return TRUE / FALSE if successful.
     */
    public boolean loadPreviousConversation(String filename){
        try{
            File previousConversation = new File(CONVERSATION_HISTORY_DIRECTORY + "/" + filename + ".json");
            if(!previousConversation.exists() ||  !previousConversation.isFile()){
                return false;
            }

            ObjectMapper MAPPER = new ObjectMapper();
            List<OllamaChatMessage> chatHistory = MAPPER.readValue(previousConversation, new TypeReference<>() {});

            chatResult.getChatHistory().addAll(chatHistory);
            return true;

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
