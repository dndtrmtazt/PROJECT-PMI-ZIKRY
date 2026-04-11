package controller;

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
import javafx.stage.Stage;
import model.User;
import model.UserDAO;
import config.UserSession;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

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
            String fxmlFile;
            String controllerClass;
            double windowWidth;
            double windowHeight;
            
            // Route based on user role
            if ("kasir".equalsIgnoreCase(user.getRole())) {
                fxmlFile = "/FXML/KasirDashboardView.fxml";
                controllerClass = "Controller.KasirDashboardController";
                windowWidth = 1024;
                windowHeight = 720;
            } else if ("pemilik".equalsIgnoreCase(user.getRole())) {
                fxmlFile = "/FXML/OwnerDashboardView.fxml";
                controllerClass = "Controller.OwnerDashboardController";
                windowWidth = 1280;
                windowHeight = 720;
            } else {
                errorLabel.setText("Role tidak dikenal!");
                errorLabel.setVisible(true);
                return;
            }
            
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = fxmlLoader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setWidth(windowWidth);
            stage.setHeight(windowHeight);
            stage.setTitle("PMITokoZikry - " + user.getRole().substring(0, 1).toUpperCase() + user.getRole().substring(1));
            stage.centerOnScreen();
            stage.show();
            
            System.out.println("✓ Navigasi ke dashboard: " + fxmlFile);
        } catch (IOException e) {
            errorLabel.setText("Error navigating to dashboard!");
            errorLabel.setVisible(true);
            System.err.println("Navigation error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
