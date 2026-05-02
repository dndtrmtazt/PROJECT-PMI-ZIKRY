package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.net.URL;

public class ConfirmDeleteDialogController {

    @FXML private StackPane dialogRoot;
    @FXML private VBox dialogCard;
    @FXML private Label lblTitle;
    @FXML private Label lblMessage;
    @FXML private Button btnClose;
    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;

    private boolean confirmed = false;

    @FXML
    private void initialize() {
        applyRoundedClip(dialogCard, 16);
    }

    public static boolean showDialog(Window owner, boolean darkMode, String title, String message, String confirmText) {
        try {
            URL dialogView = ConfirmDeleteDialogController.class.getResource("/FXML/Dialog/ConfirmDeleteDialog.fxml");
            if (dialogView == null) {
                return false;
            }

            FXMLLoader loader = new FXMLLoader(dialogView);
            Parent root = loader.load();

            ConfirmDeleteDialogController controller = loader.getController();
            controller.setDialogText(title, message, confirmText);
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
                if (event.getCode() == KeyCode.ESCAPE) {
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
            return controller.isConfirmed();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setDialogText(String title, String message, String confirmText) {
        lblTitle.setText(isBlank(title) ? "Konfirmasi Hapus?" : title);
        lblMessage.setText(isBlank(message) ? "Anda yakin ingin menghapus data ini?" : message);
        btnConfirm.setText(isBlank(confirmText) ? "Hapus" : confirmText);
    }

    public void setDarkMode(boolean enabled) {
        setStyleClass(dialogRoot, "dark", enabled);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    private void handleClose() {
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    @FXML
    private void handleConfirm() {
        confirmed = true;
        closeDialog();
    }

    private void closeDialog() {
        if (btnClose != null && btnClose.getScene() != null) {
            Stage stage = (Stage) btnClose.getScene().getWindow();
            stage.close();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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

    private void applyRoundedClip(Region region, double radius) {
        if (region == null) return;

        Rectangle clip = new Rectangle();
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        clip.widthProperty().bind(region.widthProperty());
        clip.heightProperty().bind(region.heightProperty());
        region.setClip(clip);
    }
}
