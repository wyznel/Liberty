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
    public static String readPDF(
            @ToolProperty(name = "filename", desc = "Name of PDF (INCLUDES .PDF)") String filename,
            @ToolProperty(name = "path", desc = "Path to PDF.") String path) {

        File file = new File(HelperFunctions.getValidFile(HelperFunctions.formatPath(path), filename));

        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(file))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            System.out.println("Extracted text from PDF:\n" + text);
            return text;
        } catch (Throwable e) {
            System.out.println("Error while extracting text from PDF: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return "Error while extracting text from PDF: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

}
