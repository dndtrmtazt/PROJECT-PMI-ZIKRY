package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        URL loginView = MainApplication.class.getResource("/FXML/LoginView.fxml");
        if (loginView == null) {
            throw new IOException("Resource /FXML/LoginView.fxml tidak ditemukan.");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(loginView);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("PMI Toko Zikry - Login");
        stage.setResizable(true);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
        stage.setMaximized(true);
    }

    public static void main(String[] args) {
        // 1. Set property DULU sebelum aplikasi jalan
        System.setProperty("glass.accessible.force", "false");

        // 2. Baru panggil launch
        launch(args);
    }
}
