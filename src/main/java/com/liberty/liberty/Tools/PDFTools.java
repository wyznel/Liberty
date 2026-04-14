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
}
