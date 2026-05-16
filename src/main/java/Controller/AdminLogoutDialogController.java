package Controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

// Controller popup konfirmasi logout untuk halaman admin/pemilik.
public class AdminLogoutDialogController {

    @FXML private StackPane dialogRoot;
    @FXML private VBox dialogCard;
    @FXML private Button btnClose;
    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;

    // Aksi yang dijalankan setelah user benar-benar menekan tombol keluar.
    private Runnable confirmAction;

    // Memasang clip rounded agar sudut dialog tetap rapi.
    @FXML
    private void initialize() {
        applyRoundedClip(dialogCard, 14);
    }

    // Menyesuaikan tampilan popup dengan tema aktif.
    public void setDarkMode(boolean enabled) {
        setStyleClass(dialogRoot, "dark", enabled);
    }

    // Menerima aksi logout dari controller pemanggil.
    public void setOnConfirm(Runnable confirmAction) {
        this.confirmAction = confirmAction;
    }

    // Tombol X hanya menutup popup.
    @FXML
    private void handleClose() {
        closeDialog();
    }

    // Tombol batal hanya menutup popup.
    @FXML
    private void handleCancel() {
        closeDialog();
    }

    // Tombol konfirmasi menutup popup lalu menjalankan logout.
    @FXML
    private void handleConfirm() {
        closeDialog();
        if (confirmAction != null) {
            confirmAction.run();
        }
    }

    // Mengambil Stage dialog dari tombol lalu menutupnya.
    private void closeDialog() {
        if (btnClose != null && btnClose.getScene() != null) {
            Stage stage = (Stage) btnClose.getScene().getWindow();
            stage.close();
        }
    }

    // Menambah atau menghapus class CSS sesuai kondisi.
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

    // Membuat tampilan card dialog memiliki sudut membulat.
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
