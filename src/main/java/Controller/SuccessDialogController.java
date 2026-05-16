package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.net.URL;

// Controller popup sukses umum yang dipakai beberapa form.
public class SuccessDialogController {

    @FXML private StackPane dialogRoot;
    @FXML private Label lblTitle;
    @FXML private Label lblMessage;
    @FXML private Button btnOk;

    // Versi singkat jika hanya perlu judul sukses.
    public static void showDialog(Window owner, boolean darkMode, String title) {
        showDialog(owner, darkMode, title, null);
    }

    // Membuka popup sukses sebagai dialog modal di atas window pemanggil.
    public static void showDialog(Window owner, boolean darkMode, String title, String message) {
        try {
            URL dialogView = SuccessDialogController.class.getResource("/FXML/Dialog/SuccessDialog.fxml");
            if (dialogView == null) {
                return;
            }

            FXMLLoader loader = new FXMLLoader(dialogView);
            Parent root = loader.load();

            SuccessDialogController controller = loader.getController();
            controller.setContent(title, message);
            controller.setDarkMode(darkMode);

            Stage dialog = new Stage();
            if (owner != null) {
                dialog.initOwner(owner);
            }
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initStyle(StageStyle.TRANSPARENT);
            dialog.setResizable(false);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            scene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                    dialog.close();
                }
            });

            dialog.setScene(scene);
            if (owner != null) {
                dialog.setOnShown(event -> {
                    dialog.setX(owner.getX() + (owner.getWidth() - dialog.getWidth()) / 2);
                    dialog.setY(owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2);
                });
            }
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Mengganti judul popup tanpa pesan tambahan.
    public void setTitle(String title) {
        setContent(title, null);
    }

    // Mengisi judul dan pesan opsional pada popup.
    public void setContent(String title, String message) {
        lblTitle.setText(isBlank(title) ? "Berhasil" : title);

        boolean hasMessage = !isBlank(message);
        if (lblMessage != null) {
            lblMessage.setText(hasMessage ? message : "");
            lblMessage.setVisible(hasMessage);
            lblMessage.setManaged(hasMessage);
        }
    }

    // Mengikuti tema gelap/terang dari halaman pemanggil.
    public void setDarkMode(boolean enabled) {
        setStyleClass(dialogRoot, "dark", enabled);
    }

    // Tombol OK menutup popup.
    @FXML
    private void handleOk() {
        if (btnOk != null && btnOk.getScene() != null) {
            Stage stage = (Stage) btnOk.getScene().getWindow();
            stage.close();
        }
    }

    // Mengecek teks kosong agar bisa memakai nilai default.
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // Helper untuk memasang atau melepas class CSS.
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
}
