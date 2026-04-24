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
import dao.UserDAO;
import model.User;
import config.UserSession;

import java.io.IOException;

public class LoginController {

    @FXML private StackPane rootPane;
    @FXML private AnchorPane loginContainer;
    @FXML private HBox themeToggleBox, usernameBox, passwordBox;
    @FXML private Label titleLabel, subtitleLabel, userIdLabel, passwordLabel, errorLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private ImageView logoImageView, sunIcon, moonIcon, usernameIcon, passwordIcon;

    private boolean isDarkMode = false;

    @FXML
    void handleLogin(ActionEvent event) {
        String userId = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (userId.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username dan Password tidak boleh kosong!");
            errorLabel.setVisible(true);
            return;
        }

        // 1. Validasi User dari Database
        User user = UserDAO.validateUser(userId, password);

        if (user != null) {
            errorLabel.setVisible(false);

            // 2. Simpan User ke Session (Tas Ajaib)
            UserSession.getInstance().setCurrentUser(user);

            // 3. LAPOR TEMA: Kasih tau MainController kalau kita pake Mode Gelap
            // Ini harus dilakukan SEBELUM navigasi biar Dashboard langsung tau
            MainController.isDarkMode = this.isDarkMode;

            // 4. Masuk ke Dashboard Utama
            navigateToDashboard(event, user);

        } else {
            errorLabel.setText("Username atau Password salah!");
            errorLabel.setVisible(true);
        }
    }

    private void navigateToDashboard(ActionEvent event, User user) {
        try {
            String fxmlPath;
            if ("kasir".equalsIgnoreCase(user.getRole())) {
                fxmlPath = "/FXML/Kasir/KasirDashboardView.fxml";
            } else {
                fxmlPath = "/FXML/Admin/MainLayout.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Jika bukan kasir, set hak akses di MainController
            if (!"kasir".equalsIgnoreCase(user.getRole())) {
                MainController mainController = loader.getController();
                mainController.setHakAkses(user.getRole());
            }

            // Tampilkan Stage baru
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            if ("kasir".equalsIgnoreCase(user.getRole())) {
                stage.setScene(scene);
                stage.setMaximized(true);
                Platform.runLater(() -> {
                    stage.setMaximized(false);
                    stage.setMaximized(true);
                });
            } else {
                stage.setScene(scene);
                stage.setMaximized(false);
                stage.setWidth(1100);
                stage.setHeight(650);
                stage.centerOnScreen();
            }
            
            stage.setTitle("Toko Zikry - " + user.getRole().toUpperCase());
            stage.show();

        } catch (IOException e) {
            errorLabel.setText("Gagal masuk ke Dashboard!");
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    // --- LOGIKA TEMA LOGIN (Visual Only) ---

    @FXML
    void handleDarkMode(MouseEvent event) {
        if (!isDarkMode) applyFadeTransition(this::setDarkMode);
    }

    @FXML
    void handleLightMode(MouseEvent event) {
        if (isDarkMode) applyFadeTransition(this::setLightMode);
    }

    private void applyFadeTransition(Runnable action) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), rootPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.3);
        fadeOut.setOnFinished(e -> {
            action.run();
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), rootPane);
            fadeIn.setFromValue(0.3);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void setDarkMode() {
        isDarkMode = true;
        try {
            sunIcon.setImage(new Image(getClass().getResourceAsStream("/Images/ICON3DARK.png")));
            moonIcon.setImage(new Image(getClass().getResourceAsStream("/Images/ICON4DARK.png")));
            logoImageView.setImage(new Image(getClass().getResourceAsStream("/Images/LOGO2.png")));
        } catch (Exception e) {}

        rootPane.setStyle("-fx-background-color: #121212;");
        loginContainer.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 15;");
        titleLabel.setStyle("-fx-text-fill: white;");
        subtitleLabel.setStyle("-fx-text-fill: #cccccc;");
        userIdLabel.setStyle("-fx-text-fill: white;");
        passwordLabel.setStyle("-fx-text-fill: white;");

        usernameBox.setStyle("-fx-background-color: #2c2c2c; -fx-border-color: #555555; -fx-background-radius: 8; -fx-border-radius: 8;");
        passwordBox.setStyle("-fx-background-color: #2c2c2c; -fx-border-color: #555555; -fx-background-radius: 8; -fx-border-radius: 8;");
        usernameField.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        passwordField.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");

        ColorAdjust iconContrast = new ColorAdjust();
        iconContrast.setBrightness(0.8);
        usernameIcon.setEffect(iconContrast);
        passwordIcon.setEffect(iconContrast);
    }

    private void setLightMode() {
        isDarkMode = false;
        try {
            sunIcon.setImage(new Image(getClass().getResourceAsStream("/Images/ICON3.png")));
            moonIcon.setImage(new Image(getClass().getResourceAsStream("/Images/ICON4.png")));
            logoImageView.setImage(new Image(getClass().getResourceAsStream("/Images/LOGO.png")));
        } catch (Exception e) {}

        rootPane.setStyle("-fx-background-color: #f4f6f9;");
        loginContainer.setStyle("-fx-background-color: #E3ECF7; -fx-background-radius: 15;");
        titleLabel.setStyle("-fx-text-fill: black;");
        subtitleLabel.setStyle("-fx-text-fill: #a1b3c6;");
        userIdLabel.setStyle("-fx-text-fill: black;");
        passwordLabel.setStyle("-fx-text-fill: black;");

        usernameBox.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #d1d5db; -fx-background-radius: 8; -fx-border-radius: 8;");
        passwordBox.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #d1d5db; -fx-background-radius: 8; -fx-border-radius: 8;");
        usernameField.setStyle("-fx-background-color: transparent; -fx-text-fill: black;");
        passwordField.setStyle("-fx-background-color: transparent; -fx-text-fill: black;");

        usernameIcon.setEffect(null);
        passwordIcon.setEffect(null);
    }
    @FXML
    public void initialize() {
        // Memberitahu Java kalau tombol login adalah tombol default
        // Pas user tekan ENTER, method handleLogin() bakal langsung jalan
        loginButton.setDefaultButton(true);
        
        // Sesuaikan tema awal dengan state global
        if (MainController.isDarkMode) {
            setDarkMode();
        } else {
            setLightMode();
        }
    }
}
