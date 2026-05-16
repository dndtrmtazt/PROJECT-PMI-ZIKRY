package Controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

// Controller popup konfirmasi logout untuk halaman kasir.
public class KasirLogoutDialogController {

    @FXML private StackPane dialogRoot;
    @FXML private VBox dialogCard;
    @FXML private Button btnClose;
    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;

    // Aksi logout dikirim dari controller kasir agar popup tetap reusable.
    private Runnable confirmAction;

    // Memasang clip rounded agar dialog tidak keluar dari bentuk card.
    @FXML
    private void initialize() {
        applyRoundedClip(dialogCard, 14);
    }

    // Mengikuti mode gelap/terang yang sedang aktif di halaman kasir.
    public void setDarkMode(boolean enabled) {
        setStyleClass(dialogRoot, "dark", enabled);
    }

    // Menyimpan aksi yang akan dijalankan saat user menekan konfirmasi.
    public void setOnConfirm(Runnable confirmAction) {
        this.confirmAction = confirmAction;
    }

    // Tombol X menutup popup.
    @FXML
    private void handleClose() {
        closeDialog();
    }

    // Tombol batal menutup popup.
    @FXML
    private void handleCancel() {
        closeDialog();
    }

    // Tombol konfirmasi menutup popup lalu menjalankan aksi logout.
    @FXML
    private void handleConfirm() {
        closeDialog();
        if (confirmAction != null) {
            confirmAction.run();
        }
    }

    // Menutup Stage popup.
    private void closeDialog() {
        if (btnClose != null && btnClose.getScene() != null) {
            Stage stage = (Stage) btnClose.getScene().getWindow();
            stage.close();
        }
    }

    // Helper untuk sinkronisasi class CSS.
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

    // Membuat sudut popup tetap membulat walaupun Stage transparan.
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
