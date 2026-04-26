package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Class MainApplication: Titik masuk utama (Entry Point) aplikasi JavaFX.
 * Alur: Menyiapkan stage utama dan menampilkan halaman Login sebagai tampilan awal.
 */
public class MainApplication extends Application {

    /**
     * Method start: Mengatur tampilan antarmuka (GUI) saat aplikasi dimulai.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // [1] Memuat file desain (FXML) untuk halaman Login
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/FXML/LoginView.fxml"));
        
        // [2] Membuat "Scene" (wadah konten) dari file FXML yang sudah dimuat
        Scene scene = new Scene(fxmlLoader.load());
        
        // [3] Mengatur judul jendela aplikasi
        stage.setTitle("PMI Toko Zikry - Login");
        
        // [4] Memasukkan scene ke dalam stage (jendela utama)
        stage.setScene(scene);
        
        // [5] Mengatur agar jendela aplikasi terbuka dalam mode layar penuh (Maximized)
        stage.setMaximized(true);
        
        // [6] Menampilkan jendela aplikasi ke layar pengguna
        stage.show();
    }

    /**
     * Method main: Method standar Java untuk menjalankan program.
     */
    public static void main(String[] args) {
        // [1] Konfigurasi sistem: Mematikan fitur aksesibilitas tertentu untuk mencegah lag/error di beberapa OS
        System.setProperty("glass.accessible.force", "false");

        // [2] Menjalankan siklus hidup aplikasi JavaFX (memanggil method start)
        launch(args);
    }
}
