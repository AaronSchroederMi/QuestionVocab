module QuestionVocab {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.desktop;

    exports org.Main;
    opens org.Main to javafx.fxml, com.google.gson;
}
