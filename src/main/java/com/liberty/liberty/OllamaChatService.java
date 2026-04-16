package com.liberty.liberty;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liberty.liberty.Tools.FileHandlingTools;
import com.liberty.liberty.Tools.PDFTools;
import io.github.ollama4j.Ollama;
import io.github.ollama4j.exceptions.OllamaException;
import io.github.ollama4j.models.chat.*;
import io.github.ollama4j.tools.annotations.OllamaToolService;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@OllamaToolService(providers = { FileHandlingTools.class, PDFTools.class })
public class OllamaChatService {

    private final static String[] AVAILABLE_MODELS = {
            "gemma4:e4b", "qwen2.5:7b-instruct",
            "qwen3.5:4b", "qwen3:4b-instruct",
            "gemma4:e2b"
    };

    public static final File CONVERSATION_HISTORY_DIRECTORY = new File("conversation_history");
    private static final File RUNTIME_HISTORY_DIRECTORY = new File("runtime_history");

    private final Ollama ollama = new Ollama("http://127.0.0.1:11434");
    private final OllamaChatRequest builder = OllamaChatRequest.builder().withModel(AVAILABLE_MODELS[3]);
    private OllamaChatRequest requestModel = builder.withMessage(OllamaChatMessageRole.SYSTEM, """
            You are Liberty, a local desktop AI assistant.
            You may answer normally or request exactly one tool call.
            
            Available tools:
            1. createNewFile(filename: string)
            2. writeToFile(filename: string, data: string)
            3. readFromFile(filename: string)
            4. extractTextFromPDF(filename: string)
            
            Rules:
            - Be direct, no fluff, no waffle, and do not use emojis.""").build();
    private OllamaChatResult chatResult = new OllamaChatResult(new OllamaChatResponseModel(), new ArrayList<>() {
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
        if(RUNTIME_HISTORY_DIRECTORY.mkdirs()){
            System.out.println("> Runtime history directory created");
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
    public synchronized boolean saveConversationHistory(String filename, boolean calledByUser){
        try{
            File path = new File((calledByUser ? CONVERSATION_HISTORY_DIRECTORY : RUNTIME_HISTORY_DIRECTORY) + "/" +filename+".json");
            // If the file already exists, we want to overwrite it in runtime history
            if(!calledByUser && path.exists()){
                path.delete();
            }

            if(path.createNewFile() || (!calledByUser && path.exists())){
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
    public synchronized boolean loadConversation(String filename, boolean calledByUser){
        System.out.println("hello");
        try{
            File previousConversation = new File((calledByUser ? CONVERSATION_HISTORY_DIRECTORY : RUNTIME_HISTORY_DIRECTORY) + "/" + filename + ".json");
            if(!previousConversation.exists() ||  !previousConversation.isFile()){
                return false;
            }

            ObjectMapper MAPPER = new ObjectMapper();
            List<OllamaChatMessage> chatHistory = MAPPER.readValue(previousConversation, new TypeReference<>() {});

            chatResult.getChatHistory().addAll(chatHistory);
            requestModel = builder.withMessages(chatResult.getChatHistory()).build();
            System.out.println(chatResult.getChatHistory());
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

    public Ollama getOllama(){
        return ollama;
    }

    public synchronized void clearHistory(){
        chatResult.getChatHistory().clear();
        requestModel = builder.withMessages(chatResult.getChatHistory()).build();
    }

    public synchronized void clearRuntimeHistoryDir(){
        try{
            File[] files = RUNTIME_HISTORY_DIRECTORY.listFiles();
            if(files == null) return;
            for(File file : files){
                boolean deleted = file.delete();
                if(!deleted){
                    System.err.println("Failed to delete file: " + file.getName());
                }
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

}
