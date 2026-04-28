package Controller;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
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
import java.io.InputStream;
import java.net.URL;

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
            showLoginError("Username dan Password tidak boleh kosong!");
            return;
        }

        // 1. Validasi User dari Database
        User user = UserDAO.validateUser(userId, password);

        if (user != null) {
            hideLoginError();

            // 2. Simpan User ke Session (Tas Ajaib)
            UserSession.getInstance().setCurrentUser(user);

            // 3. LAPOR TEMA: Kasih tau MainController kalau kita pake Mode Gelap
            // Ini harus dilakukan SEBELUM navigasi biar Dashboard langsung tau
            MainController.isDarkMode = this.isDarkMode;

            // 4. Masuk ke Dashboard Utama
            navigateToDashboard(event, user);

        } else {
            showLoginError("Username atau Password salah!");
        }
    }

    private void navigateToDashboard(ActionEvent event, User user) {
        try {
            String fxmlPath;
            if ("kasir".equalsIgnoreCase(user.getRole())) {
                fxmlPath = "/FXML/Kasir/SidebarKasir.fxml";
            } else {
                fxmlPath = "/FXML/Admin/MainLayout.fxml";
            }

            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                showLoginError("Halaman tujuan tidak ditemukan.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Jika bukan kasir, set hak akses di MainController
            if (!"kasir".equalsIgnoreCase(user.getRole())) {
                MainController mainController = loader.getController();
                mainController.setHakAkses(user.getRole());
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            showDashboardMaximized(stage, scene, "Toko Zikry - " + user.getRole().toUpperCase());

        } catch (IOException e) {
            showLoginError("Gagal masuk ke Dashboard!");
            e.printStackTrace();
        }
    }

    private void showDashboardMaximized(Stage stage, Scene scene, String title) {
        stage.setResizable(true);
        stage.setMaximized(false); // Paksa JavaFX/Windows menerapkan ulang maximize setelah scene dashboard dipasang.
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
        stage.setMaximized(true);

        Platform.runLater(() -> {
            stage.setResizable(true);
            stage.setMaximized(true);

            PauseTransition reapplyMaximized = new PauseTransition(Duration.millis(120));
            reapplyMaximized.setOnFinished(event -> {
                stage.setResizable(true);
                stage.setMaximized(true);
            });
            reapplyMaximized.play();
        });
    }

    // --- LOGIKA TEMA LOGIN (Visual Only) ---

    @FXML
    void handleDarkMode(MouseEvent event) {
        if (!isDarkMode) applyFadeTransition(() -> applyTheme(true));
    }

    @FXML
    void handleLightMode(MouseEvent event) {
        if (isDarkMode) applyFadeTransition(() -> applyTheme(false));
    }

    private void applyFadeTransition(Runnable action) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), loginContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.78);
        fadeOut.setOnFinished(e -> {
            action.run();
            FadeTransition fadeIn = new FadeTransition(Duration.millis(180), loginContainer);
            fadeIn.setFromValue(0.78);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void setDarkMode() {
        applyTheme(true);
    }

    private void setLightMode() {
        applyTheme(false);
    }

    private void applyTheme(boolean darkMode) {
        isDarkMode = darkMode;
        setStyleClass(rootPane, "dark", darkMode);
        setStyleClass(sunIcon, "active", !darkMode);
        setStyleClass(moonIcon, "active", darkMode);

        setImageIfPresent(sunIcon, darkMode ? "/Images/ICON3DARK.png" : "/Images/ICON3.png");
        setImageIfPresent(moonIcon, darkMode ? "/Images/ICON4DARK.png" : "/Images/ICON4.png");
        setImageIfPresent(logoImageView, darkMode ? "/Images/LOGO2.png" : "/Images/LOGO.png");

        if (darkMode) {
            ColorAdjust iconContrast = new ColorAdjust();
            iconContrast.setBrightness(0.8);
            usernameIcon.setEffect(iconContrast);
            passwordIcon.setEffect(iconContrast);
        } else {
            usernameIcon.setEffect(null);
            passwordIcon.setEffect(null);
        }
    }

    private void setupFocusStates() {
        usernameField.focusedProperty().addListener((obs, oldValue, focused) -> setStyleClass(usernameBox, "focused", focused));
        passwordField.focusedProperty().addListener((obs, oldValue, focused) -> setStyleClass(passwordBox, "focused", focused));
    }

    private void showLoginError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(180), errorLabel);
        fadeIn.setFromValue(errorLabel.getOpacity());
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void hideLoginError() {
        if (!errorLabel.isVisible()) return;

        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), errorLabel);
        fadeOut.setFromValue(errorLabel.getOpacity());
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> errorLabel.setVisible(false));
        fadeOut.play();
    }

    private void playEntranceAnimation() {
        loginContainer.setOpacity(0.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(260), loginContainer);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void setStyleClass(Node node, String styleClass, boolean enabled) {
        if (node == null || styleClass == null) return;

        if (enabled) {
            if (!node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }
        } else {
            node.getStyleClass().remove(styleClass);
        }
    }

    @FXML
    public void initialize() {
        // Memberitahu Java kalau tombol login adalah tombol default
        // Pas user tekan ENTER, method handleLogin() bakal langsung jalan
        loginButton.setDefaultButton(true);
        setupFocusStates();
        errorLabel.setOpacity(0.0);
        errorLabel.setVisible(false);
        
        // Sesuaikan tema awal dengan state global
        if (MainController.isDarkMode) {
            setDarkMode();
        } else {
            setLightMode();
        }

        playEntranceAnimation();
    }

    private void setImageIfPresent(ImageView imageView, String resourcePath) {
        if (imageView == null || resourcePath == null) {
            return;
        }

        InputStream stream = getClass().getResourceAsStream(resourcePath);
        if (stream != null) {
            imageView.setImage(new Image(stream));
        }
    }
}
