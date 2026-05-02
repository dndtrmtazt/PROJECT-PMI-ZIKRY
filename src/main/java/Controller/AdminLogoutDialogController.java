package Controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class AdminLogoutDialogController {

    @FXML private StackPane dialogRoot;
    @FXML private VBox dialogCard;
    @FXML private Button btnClose;
    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;

    private Runnable confirmAction;

    @FXML
    private void initialize() {
        applyRoundedClip(dialogCard, 14);
    }

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
