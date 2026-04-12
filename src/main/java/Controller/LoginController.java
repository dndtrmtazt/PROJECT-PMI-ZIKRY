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
import javafx.util.Duration;
import model.User;
import model.UserDAO;
import config.UserSession;

import java.io.IOException;

public class LoginController {

    @FXML
    private StackPane rootPane;

    @FXML
    private AnchorPane loginContainer;

    @FXML
    private HBox themeToggleBox;

    @FXML
    private HBox usernameBox;

    @FXML
    private HBox passwordBox;

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private Label userIdLabel;

    @FXML
    private Label passwordLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @FXML
    private ImageView logoImageView;

    @FXML
    private ImageView sunIcon;

    @FXML
    private ImageView moonIcon;

    @FXML
    private ImageView usernameIcon;

    @FXML
    private ImageView passwordIcon;

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

        // Validasi user dari database
        User user = UserDAO.validateUser(userId, password);
        if (user != null) {
            errorLabel.setVisible(false);
            System.out.println("✓ Login Berhasil!");
            System.out.println("  User: " + user.getUsername() + " | Role: " + user.getRole());

            // Store user in session
            UserSession.getInstance().setCurrentUser(user);

            navigateToDashboard(event, user);
        } else {
            errorLabel.setText("Username atau Password salah!");
            errorLabel.setVisible(true);
            System.out.println("✗ Login Gagal - User ID: " + userId);
        }
    }

    private void navigateToDashboard(ActionEvent event, User user) {
        try {
            // 1. Tentukan "Isi Tengah" pertama kali (Default)
            String halamanAwal;
            if ("kasir".equalsIgnoreCase(user.getRole())) {
                halamanAwal = "KasirDashboardView";
            } else if ("pemilik".equalsIgnoreCase(user.getRole())) {
                halamanAwal = "DashboardAdminView";
            } else {
                errorLabel.setText("Role tidak dikenal!");
                errorLabel.setVisible(true);
                return;
            }

            // 2. LOAD BINGKAI UTAMA (MainLayout)
            // Pastikan path-nya benar. Kalau foldernya FXML, pakai /FXML/
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MainLayout.fxml"));
            Parent root = loader.load();

            // 3. PANGGIL KONTEN TENGAH (Penting!)
            // Kita panggil controller si MainLayout buat nempelin dashboard di tengah
            MainController mainController = loader.getController();
            mainController.panggilHalaman(halamanAwal);

            // 4. TAMPILKAN KE LAYAR
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Atur ukuran jendela biar pas sama desain MainLayout kamu (1100x650)
            stage.setWidth(1100);
            stage.setHeight(650);
            stage.setTitle("Toko Zikry - " + user.getRole().toUpperCase());
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            errorLabel.setText("Gagal masuk ke Layout Utama!");
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML
    void handleDarkMode(MouseEvent event) {
        if (!isDarkMode) {
            applyFadeTransition(this::setDarkMode);
        }
    }

    @FXML
    void handleLightMode(MouseEvent event) {
        if (isDarkMode) {
            applyFadeTransition(this::setLightMode);
        }
    }

    private void applyFadeTransition(Runnable action) {
        // Create fade out
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), rootPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.3);
        fadeOut.setOnFinished(e -> {
            action.run(); // Change styles
            // Create fade in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), rootPane);
            fadeIn.setFromValue(0.3);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void setDarkMode() {
        isDarkMode = true;

        // Update images
        try {
            sunIcon.setImage(new Image(getClass().getResourceAsStream("/Images/ICON3DARK.png")));
            moonIcon.setImage(new Image(getClass().getResourceAsStream("/Images/ICON4DARK.png")));
            logoImageView.setImage(new Image(getClass().getResourceAsStream("/Images/LOGO2.png")));
        } catch (Exception e) {
            System.err.println("Error loading dark mode images: " + e.getMessage());
        }

        // Update styles
        rootPane.setStyle("-fx-background-color: #121212;");
        loginContainer.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(255,255,255,0.05), 20, 0, 0, 10);");
        titleLabel.setStyle("-fx-text-fill: white;");
        subtitleLabel.setStyle("-fx-text-fill: #cccccc;");
        userIdLabel.setStyle("-fx-text-fill: white;");
        passwordLabel.setStyle("-fx-text-fill: white;");

        // Update input boxes
        usernameBox.setStyle("-fx-background-radius: 8; -fx-border-color: #555555; -fx-border-radius: 8; -fx-background-color: #2c2c2c;");
        passwordBox.setStyle("-fx-background-radius: 8; -fx-border-color: #555555; -fx-border-radius: 8; -fx-background-color: #2c2c2c;");
        usernameField.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 0;");
        passwordField.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 0;");

        // Ensure icons are contrast (make them lighter)
        ColorAdjust iconContrast = new ColorAdjust();
        iconContrast.setBrightness(0.8);
        usernameIcon.setEffect(iconContrast);
        passwordIcon.setEffect(iconContrast);
    }

    private void setLightMode() {
        isDarkMode = false;

        // Update images
        try {
            sunIcon.setImage(new Image(getClass().getResourceAsStream("/Images/ICON3.png")));
            moonIcon.setImage(new Image(getClass().getResourceAsStream("/Images/ICON4.png")));
            logoImageView.setImage(new Image(getClass().getResourceAsStream("/Images/LOGO.png")));
        } catch (Exception e) {
            System.err.println("Error loading light mode images: " + e.getMessage());
        }

        // Update styles
        rootPane.setStyle("-fx-background-color: #f4f6f9;");
        loginContainer.setStyle("-fx-background-color: #E3ECF7; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 20, 0, 0, 10);");
        titleLabel.setStyle("-fx-text-fill: black;");
        subtitleLabel.setStyle("-fx-text-fill: #a1b3c6;");
        userIdLabel.setStyle("-fx-text-fill: black;");
        passwordLabel.setStyle("-fx-text-fill: black;");

        // Update input boxes
        usernameBox.setStyle("-fx-background-radius: 8; -fx-border-color: #d1d5db; -fx-border-radius: 8; -fx-background-color: #f9fafb;");
        passwordBox.setStyle("-fx-background-radius: 8; -fx-border-color: #d1d5db; -fx-border-radius: 8; -fx-background-color: #f9fafb;");
        usernameField.setStyle("-fx-background-color: transparent; -fx-text-fill: black; -fx-padding: 0;");
        passwordField.setStyle("-fx-background-color: transparent; -fx-text-fill: black; -fx-padding: 0;");

        // Reset icons contrast
        usernameIcon.setEffect(null);
        passwordIcon.setEffect(null);
    }
}
