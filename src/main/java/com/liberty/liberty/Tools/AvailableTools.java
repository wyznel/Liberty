package com.liberty.liberty.Tools;

import io.github.ollama4j.tools.annotations.ToolProperty;
import io.github.ollama4j.tools.annotations.ToolSpec;

import java.io.*;
import java.util.ArrayList;

public class AvailableTools {

    @ToolSpec(desc = "Creates a new file with a given file name and file format.")
    public static String createNewFile(@ToolProperty(name = "filename", desc = "Name of file") String filename){
        System.out.println("Creating a new file: " + filename);
        try{
            File file = new File( "sandbox/" + filename);
            if(file.exists()){
                return "File already exists.";
            }
            if(file.createNewFile()){
                return "File created successfully at: " +  file.getAbsolutePath();
            }
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
        return "An unexpected error occurred, failed to create file.";
    }

    @ToolSpec(desc = "Write to a file.")
    public static String writeToFile(@ToolProperty(name = "filename", desc = "Name of file") String filename, @ToolProperty(name = "data", desc = "data to be written to file") String data){
        System.out.println("Writing to file: " + filename);
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("sandbox/" + filename)));
            writer.write(data);
            writer.close();
            System.out.println("Written to: " + filename);
            return "File written successfully.";
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
        return "An unexpected error occurred, failed to write to file.";
    }
    @ToolSpec(desc = "Read a file")
    public static String readFromFile(@ToolProperty(name = "filename", desc = "Name of file") String filename){
        try{
            BufferedReader reader = new BufferedReader(new FileReader("sandbox/" + filename));
            String line = reader.readLine();
            ArrayList<String> lines = new ArrayList<>();
            while(line != null){
                lines.add(line);
                line = reader.readLine();
            }

            return String.join("\n", lines);
        }catch (Exception e){
            System.err.println(e.getMessage());
        }

        return "FAILED TO READ FILE - INFORM USER";
    }
}
