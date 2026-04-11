package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/FXML/LoginView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("PMI Toko Zikry - Login");
        stage.setScene(scene);
        stage.setWidth(900);
        stage.setHeight(600);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
