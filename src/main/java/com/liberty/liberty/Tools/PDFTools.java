package com.liberty.liberty.Tools;

import io.github.ollama4j.tools.annotations.ToolProperty;
import io.github.ollama4j.tools.annotations.ToolSpec;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;


public class PDFTools {

    @ToolSpec(desc = "Extract text from a PDF and return the result.")
    public String extractTextFromPDF(@ToolProperty(name = "filename", desc = "Name of PDF") String filename) {
        File pdf = new File("sandbox/" + filename + ".pdf");
        if(!pdf.exists() || !pdf.isFile()) {
            return "File does not exist as pdf.";
        }
        try {
            PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile("sandbox/" + filename + ".pdf"));
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            System.out.println("Extracted text from PDF:\n" + text);
            document.close();
            return text;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ToolSpec(desc = "Write to a PDF and write values to it, if it doesn't exist, create it first.")
    public String writeToPDF(@ToolProperty(name = "filename", desc = "Name of PDF") String filename,
                            @ToolProperty(name = "path", desc = "Designated path") String path,
                            @ToolProperty(name = "data", desc = "Data to be put into PDF") String data)
    {
        try{
            System.out.println("Writing PDF to: " + filename);
            filename+=".pdf";
            File checkFile = new File(path + filename);
            if(checkFile.exists() || checkFile.isFile()) {
                return "File already exists at: " +  checkFile.getAbsolutePath();
            }

            PDDocument newDocument =  new PDDocument();
            newDocument.save(path + filename);
            return "Saved file at: " + path + filename;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    /**
     * Ensures any path has a "/" at the end.
     * @param proposedPath the path to be checked.
     * @return correctly formatted path.
     */
    private String formatPath(String proposedPath) {
        if(!proposedPath.endsWith("/")){
            proposedPath+="/";
        }
        return proposedPath;
    }

    /**
     * Ensures given file is unique and doesn't already exist/is a directory.
     * @param file file to be checked
     * @return TRUE if file doesn't already exist.
     */
    private boolean isFileUnique(File file){
        return !file.isFile() && !file.exists()
;    }
}
