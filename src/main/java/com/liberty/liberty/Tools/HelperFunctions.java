package com.liberty.liberty.Tools;

import java.io.File;

/**
 * Utility class that provides helper functions for the tools.
 */
public class HelperFunctions {
    /**
     * Ensures any path has a "/" at the end.
     * @param proposedPath the path to be checked.
     * @return correctly formatted path.
     */
    public static String formatPath(String proposedPath) {
        if(!proposedPath.endsWith("/")){
            proposedPath+="/";
        }
        return proposedPath;
    }


    /**
     * Retrieves the absolute path of a valid file after validating its existence,
     * type, and structure. The method ensures the provided file path is properly
     * formatted and checks if the file exists, is a regular file, and not a directory.
     *
     * @param path the base directory path where the file is located
     * @param filename the name of the file to validate
     * @return the absolute path of the file if it is valid, otherwise a descriptive error message
     */
    public static String getValidFile(String path, String filename){
        File file = new File(HelperFunctions.formatPath(path) + filename);
        if(!file.exists()) return "File does not exist.";
        if(!file.isFile()) return "File is not a valid file.";
        if(file.isDirectory()) return "File is a directory.";

        return file.getAbsolutePath();
    }

    /**
     * Recursively lists all files and directories starting from the given directory.
     *
     * @param dir the root directory from which the file and directory listing begins
     * @param sb a StringBuilder instance used to store the paths of the files and directories as they are discovered
     */
    public static void listFilesRecursively(File dir, StringBuilder sb){
        try{
            File[] files = dir.listFiles();
            if(files == null) return;

            for(File file : files){
                sb.append(file.getPath()).append(System.lineSeparator());
                if(file.isDirectory()){
                    listFilesRecursively(file, sb);
                }
            }
        }catch (Exception e){
            System.err.println(e.getMessage());
        }
    }
}
