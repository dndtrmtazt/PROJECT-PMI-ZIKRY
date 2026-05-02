package Controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class KasirLogoutDialogController {

    @FXML private StackPane dialogRoot;
    @FXML private Button btnClose;
    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;

    private Runnable confirmAction;

    public void setDarkMode(boolean enabled) {
        setStyleClass(dialogRoot, "dark", enabled);
    }

    public void setOnConfirm(Runnable confirmAction) {
        this.confirmAction = confirmAction;
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
        closeDialog();
        if (confirmAction != null) {
            confirmAction.run();
        }
    }

    private void closeDialog() {
        if (btnClose != null && btnClose.getScene() != null) {
            Stage stage = (Stage) btnClose.getScene().getWindow();
            stage.close();
        }
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
}
