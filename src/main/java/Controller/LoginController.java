package Controller;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.util.Duration;
import DAO.UserDAO;
import model.User;
import config.UserSession;
import java.io.IOException;

/**
 * Controller untuk mengelola proses Autentikasi/Login.
 * Alur: Validasi kredensial, manajemen sesi user, dan pengalihan ke dashboard yang sesuai (Kasir/Admin).
 */
public class LoginController {

    // [1] Deklarasi komponen UI Login
    @FXML private StackPane rootPane;
    @FXML private AnchorPane loginContainer;
    @FXML private HBox themeToggleBox, usernameBox, passwordBox;
    @FXML private Label titleLabel, subtitleLabel, userIdLabel, passwordLabel, errorLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private ImageView logoImageView, sunIcon, moonIcon, usernameIcon, passwordIcon;

    private boolean isDarkMode = false;

    /**
     * Method handleLogin: Dipanggil saat tombol login ditekan.
     * Alur: 1. Ambil Input -> 2. Validasi Database -> 3. Set Session -> 4. Pindah Halaman.
     */
    @FXML
    void handleLogin(ActionEvent event) {
        // [1] Pengambilan data username dan password
        String userId = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // [2] Validasi input tidak boleh kosong
        if (userId.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username dan Password tidak boleh kosong!");
            errorLabel.setVisible(true);
            return;
        }

        // [3] Cek keaslian akun ke database melalui DAO
        User user = UserDAO.validateUser(userId, password);

        if (user != null) {
            errorLabel.setVisible(false);
            // [4] Simpan identitas user ke dalam Sesi Global
            UserSession.getInstance().setCurrentUser(user);
            // [5] Sinkronisasi status tema (Dark/Light) sebelum pindah
            MainController.isDarkMode = this.isDarkMode;
            // [6] Alihkan tampilan ke dashboard sesuai jabatan user
            navigateToDashboard(event, user);
        } else {
            // [7] Berikan pesan error jika akun tidak ditemukan
            errorLabel.setText("Username atau Password salah!");
            errorLabel.setVisible(true);
        }
    }

    /**
     * Method navigateToDashboard: Menangani pemindahan stage (Window).
     */
    private void navigateToDashboard(ActionEvent event, User user) {
        try {
            // [1] Tentukan file FXML berdasarkan Role (Kasir atau Pemilik)
            String fxmlPath = "kasir".equalsIgnoreCase(user.getRole()) 
                    ? "/FXML/Kasir/KasirDashboardView.fxml" 
                    : "/FXML/Admin/MainLayout.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // [2] Jika Admin, konfigurasi hak akses tambahan pada MainController
            if (!"kasir".equalsIgnoreCase(user.getRole())) {
                MainController mainController = loader.getController();
                mainController.setHakAkses(user.getRole());
            }

            // [3] Siapkan Scene baru dan tutup window login yang lama
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            
            // [4] Pengaturan ukuran jendela (Kasir=Maximized, Admin=Fixed Size)
            if ("kasir".equalsIgnoreCase(user.getRole())) {
                stage.setScene(scene);
                stage.setMaximized(true);
            } else {
                stage.setScene(scene);
                stage.setWidth(1100);
                stage.setHeight(650);
                stage.centerOnScreen();
            }
            
            stage.setTitle("Toko Zikry - " + user.getRole().toUpperCase());
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Gagal masuk ke Dashboard!");
            errorLabel.setVisible(true);
        }
    }

    /**
     * Method: Mengaktifkan Tema Gelap pada layar login.
     */
    @FXML void handleDarkMode(MouseEvent event) {
        if (!isDarkMode) applyFadeTransition(this::setDarkMode);
    }

    /**
     * Method: Mengaktifkan Tema Terang pada layar login.
     */
    @FXML void handleLightMode(MouseEvent event) {
        if (isDarkMode) applyFadeTransition(this::setLightMode);
    }

    /**
     * Method: Memberikan efek transisi halus saat berganti warna tema.
     */
    private void applyFadeTransition(Runnable action) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), rootPane);
        fadeOut.setFromValue(1.0); fadeOut.setToValue(0.3);
        fadeOut.setOnFinished(e -> {
            action.run();
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), rootPane);
            fadeIn.setFromValue(0.3); fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    /**
     * Method setDarkMode: Mengatur variabel warna tema gelap.
     */
    private void setDarkMode() {
        isDarkMode = true;
        // [1] Update Gambar Ikon
        try {
            sunIcon.setImage(new Image(getClass().getResourceAsStream("/Images/ICON3DARK.png")));
            moonIcon.setImage(new Image(getClass().getResourceAsStream("/Images/ICON4DARK.png")));
            logoImageView.setImage(new Image(getClass().getResourceAsStream("/Images/LOGO2.png")));
        } catch (Exception e) {}

        // [2] Update Style Elemen UI (CSS)
        rootPane.setStyle("-fx-background-color: #121212;");
        loginContainer.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 15;");
        titleLabel.setStyle("-fx-text-fill: white;");
        subtitleLabel.setStyle("-fx-text-fill: #cccccc;");
        
        usernameBox.setStyle("-fx-background-color: #2c2c2c; -fx-border-color: #555555; -fx-background-radius: 8; -fx-border-radius: 8;");
        passwordBox.setStyle("-fx-background-color: #2c2c2c; -fx-border-color: #555555; -fx-background-radius: 8; -fx-border-radius: 8;");
        usernameField.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        passwordField.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
    }

    /**
     * Method setLightMode: Mengatur variabel warna tema terang.
     */
    private void setLightMode() {
        isDarkMode = false;
        // [1] Update Gambar Ikon
        try {
            sunIcon.setImage(new Image(getClass().getResourceAsStream("/Images/ICON3.png")));
            moonIcon.setImage(new Image(getClass().getResourceAsStream("/Images/ICON4.png")));
            logoImageView.setImage(new Image(getClass().getResourceAsStream("/Images/LOGO.png")));
        } catch (Exception e) {}

        // [2] Update Style Elemen UI (CSS)
        rootPane.setStyle("-fx-background-color: #f4f6f9;");
        loginContainer.setStyle("-fx-background-color: #E3ECF7; -fx-background-radius: 15;");
        titleLabel.setStyle("-fx-text-fill: black;");
        subtitleLabel.setStyle("-fx-text-fill: #a1b3c6;");

        usernameBox.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #d1d5db; -fx-background-radius: 8; -fx-border-radius: 8;");
        passwordBox.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #d1d5db; -fx-background-radius: 8; -fx-border-radius: 8;");
        usernameField.setStyle("-fx-background-color: transparent; -fx-text-fill: black;");
        passwordField.setStyle("-fx-background-color: transparent; -fx-text-fill: black;");
    }

    /**
     * Method initialize: Pengaturan awal saat class dimuat ke memori.
     */
    @FXML
    public void initialize() {
        // [1] Daftarkan tombol login sebagai aksi default (ENTER)
        loginButton.setDefaultButton(true);
        // [2] Sesuaikan tema dengan preferensi sistem yang ada
        if (MainController.isDarkMode) setDarkMode();
        else setLightMode();
    }
}
