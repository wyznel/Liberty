package com.liberty.liberty;

import io.github.ollama4j.tools.annotations.ToolProperty;
import io.github.ollama4j.tools.annotations.ToolSpec;

import java.io.File;

public class AvailableTools {

    @ToolSpec(desc = "Creates a new file with a given file name and file format.")
    public static String createNewFile(@ToolProperty(name = "filename", desc = "Name of file") String filename, @ToolProperty(name = "file_format", desc = "The format of the file") String file_format){
        try{
            File file = new File(filename + "." + file_format);
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

}
