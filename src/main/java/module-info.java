module com.liberty.liberty {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;
    requires ollama4j;


    opens com.liberty.liberty to javafx.fxml;
    exports com.liberty.liberty;
}