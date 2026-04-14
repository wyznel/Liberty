package com.liberty.liberty.Tools;

import io.github.ollama4j.tools.annotations.ToolProperty;
import io.github.ollama4j.tools.annotations.ToolSpec;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FileHandlingTools {

    @ToolSpec(desc = "Creates a new file with a given file name and file format.")
    public static String createNewFile(@ToolProperty(name = "filename", desc = "Name of file") String filename,
                                       @ToolProperty(name = "path", desc = "Desired path to file") String path){
        try{
            File file = new File( HelperFunctions.formatPath(path) + filename);
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
    public static String writeToFile(@ToolProperty(name = "filename", desc = "Name of file") String filename,
                                     @ToolProperty(name = "data", desc = "data to be written to file") String data,
                                     @ToolProperty(name = "path", desc = "Path to file directory") String path){
        System.out.println("Writing to file: " + filename);
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(HelperFunctions.formatPath(path) + filename));
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
    public static String readFromFile(@ToolProperty(name = "filename", desc = "Name of file") String filename,
                                      @ToolProperty(name = "path", desc = "Path to file directory") String path){
        try{
            File file = new File(HelperFunctions.getValidFile(HelperFunctions.formatPath(path), filename));

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            StringBuilder sb = new StringBuilder();
            while(line != null){
                sb.append(line).append(System.lineSeparator());
                line = reader.readLine();
            }

            return sb.toString();
        }catch (Exception e){
            System.err.println(e.getMessage());
        }

        return "FAILED TO READ FILE - INFORM USER";
    }

    @ToolSpec(desc = "List files in a directory")
    public static String listFiles(@ToolProperty(name = "path", desc = "Path to list files from") String path,
                                   @ToolProperty(name = "recursive", desc = "Whether to list files recursively") Boolean recursivelyList){
        try{
            if(path == null){
                return "PATH CANNOT BE NULL";
            }

            File dir = new File(HelperFunctions.formatPath(path));
            if(!dir.exists() || !dir.isDirectory()){
                return "Directory does not exist.";
            }

            StringBuilder sb = new StringBuilder();

            if(recursivelyList){
                HelperFunctions.listFilesRecursively(dir, sb);
            }else{
                File[] filesInDir = dir.listFiles();
                if(filesInDir == null) return "Failed to read directory: " + dir.getAbsolutePath();

                if(filesInDir.length == 0) return "Directory is empty: " + dir.getAbsolutePath();

                for(File file : filesInDir){
                    sb.append(file.getPath()).append(System.lineSeparator());
                }
                return sb.toString();
            }

            return sb.toString().isBlank() ? "Directory is empty: " + dir.getPath() : sb.toString();
        }catch (Exception e){
            System.err.println(e.getMessage());
            return "FAILED TO LIST FILES: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
    }

    @ToolSpec(desc = "Check if a file exists.")
    public static Boolean fileExists(@ToolProperty(name = "filename", desc = "Name of file to be checked") String filename,
                                     @ToolProperty(name = "path", desc = "Path to file directory") String path){
        try{
            File file = new File(HelperFunctions.formatPath(path) + filename);
            return file.exists();
        }catch (Exception e){
            System.err.println("Failed to check if file exists: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return false;
        }
    }

    @ToolSpec(desc = "Check if a file is a directory. RETURN TRUE IF IS DIRECTORY, FALSE IF NOT DIRECTORY")
    public static Boolean isFileDirectory(@ToolProperty(name = "filename", desc = "Name of the file to be checked") String filename,
                                          @ToolProperty(name = "path", desc = "Path to file directory") String path){
        try{
            File file = new File(HelperFunctions.formatPath(path) + filename);
            return file.isDirectory();
        }catch (Exception e){
            System.err.println("Failed to check if file is a directory: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return false;
        }
    }

    @ToolSpec(desc = "Append data to a file.")
    public static String appendToFile(@ToolProperty(name = "filename", desc = "Name of file") String filename,
                                      @ToolProperty(name = "data", desc = "data to be appended to file") String data,
                                      @ToolProperty(name = "path", desc = "Path to file directory") String path) {
        try {
            File file = new File(HelperFunctions.formatPath(path) + filename);
            if(!file.exists()) return "File does not exist.";
            if(!file.isFile()) return "File is not a valid file.";
            if(file.isDirectory()) return "File is a directory.";

            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.append(System.lineSeparator()).append(data);
            writer.close();
            return "FILE APPENDED SUCCESSFULLY, DO NOT CALL TOOL AGAIN UNLESS ASKED.";

        }catch (Exception e) {
            System.err.println(e.getMessage());
            return "FAILED TO APPEND TO FILE: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
    }

    @ToolSpec(desc = "Read a specific range from a file.")
    public static String readRangeFromFile(@ToolProperty(name = "startLine", desc = "Starting line number") Integer startLine,
                                           @ToolProperty(name = "endLine", desc = "End line number") Integer endLine,
                                           @ToolProperty(name = "filename", desc = "File name to be read.") String filename,
                                           @ToolProperty(name = "path", desc = "Path to file directory") String path){
        try{
            StringBuilder sb = new StringBuilder();

            if(!fileExists(filename, path)){
                return "File does not exist.";
            }

            try(Stream<String> lines = Files.lines(Path.of(HelperFunctions.formatPath(path) + filename))){
                lines.skip(startLine-1).limit(endLine-startLine+1).forEach(line -> sb.append(line).append(System.lineSeparator()));
                System.out.println(sb);
            }
            return sb.toString();
        }catch (Exception e){
            System.err.println(e.getMessage());
            return "FAILED TO READ RANGE FROM FILE: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
    }
}
