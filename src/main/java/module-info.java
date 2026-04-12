module com.example.pmitokozikry {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;

    exports app;
    opens app to javafx.fxml;
    opens Controller to javafx.fxml;
    opens model to javafx.fxml;
    opens config to javafx.fxml;
}