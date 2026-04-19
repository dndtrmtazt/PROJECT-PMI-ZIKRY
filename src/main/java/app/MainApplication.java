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
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        // 1. Set property DULU sebelum aplikasi jalan
        System.setProperty("glass.accessible.force", "false");

        // 2. Baru panggil launch
        launch(args);
    }
}
