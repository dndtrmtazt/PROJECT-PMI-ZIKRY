package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

// Entry point aplikasi JavaFX Toko Zikry.
public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Aplikasi selalu dimulai dari halaman login.
        URL loginView = MainApplication.class.getResource("/FXML/LoginView.fxml");
        if (loginView == null) {
            throw new IOException("Resource /FXML/LoginView.fxml tidak ditemukan.");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(loginView);
        Scene scene = new Scene(fxmlLoader.load());
        // Stage utama dibuat maximize agar tampilan kasir/admin punya ruang penuh.
        stage.setTitle("PMI Toko Zikry - Login");
        stage.setResizable(true);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
        stage.setMaximized(true);
    }

    public static void main(String[] args) {
        // Menonaktifkan accessibility bridge tertentu agar JavaFX lebih stabil di beberapa Windows.
        System.setProperty("glass.accessible.force", "false");

        // Menjalankan lifecycle JavaFX dan memanggil method start().
        launch(args);
    }
}
