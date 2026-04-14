module com.liberty.liberty {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;
    requires ollama4j;
    requires com.google.common;
    requires com.google.errorprone.annotations;
    requires org.apache.pdfbox;
    requires org.apache.pdfbox.io;


    opens com.liberty.liberty to javafx.fxml;
    exports com.liberty.liberty;
    exports com.liberty.liberty.Tools;
    opens com.liberty.liberty.Tools to javafx.fxml;
}