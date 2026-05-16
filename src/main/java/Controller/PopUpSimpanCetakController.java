package Controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.text.NumberFormat;
import java.util.Locale;

// Controller popup konfirmasi sebelum transaksi disimpan dan struk dicetak.
public class PopUpSimpanCetakController {

    @FXML private AnchorPane popupRoot;
    @FXML private Label lblTotal;
    @FXML private Label lblBayar;
    @FXML private Label lblKembalian;
    @FXML private Button btnTutup;
    @FXML private StackPane btnCetak;

    // Menandai apakah user memilih lanjut cetak atau membatalkan.
    private boolean confirmed = false;
    private final NumberFormat nfIndo = NumberFormat.getInstance(new Locale("id", "ID"));

    // Menghubungkan tombol tutup dan tombol cetak dengan aksi popup.
    @FXML
    public void initialize() {
        btnTutup.setOnAction(e -> {
            confirmed = false;
            closeStage();
        });

        btnCetak.setOnMouseClicked(e -> confirmAndClose());
        btnCetak.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                confirmAndClose();
                e.consume();
            }
        });
    }

    // Mengisi total, nominal bayar, dan kembalian pada popup.
    public void setData(double total, double bayar) {
        lblTotal.setText("Rp " + nfIndo.format(total));
        lblBayar.setText("Rp " + nfIndo.format(bayar));
        double kembalian = bayar - total;
        lblKembalian.setText("Rp " + nfIndo.format(kembalian));
        applyChangeState(lblKembalian, kembalian);
    }

    // Menyesuaikan popup dengan tema kasir yang aktif.
    public void setDarkMode(boolean enabled) {
        setStyleClass(popupRoot, "dark", enabled);
    }

    // Dipanggil controller kasir setelah popup ditutup untuk tahu keputusan user.
    public boolean isConfirmed() {
        return confirmed;
    }

    // Menutup window popup.
    private void closeStage() {
        Stage stage = (Stage) btnTutup.getScene().getWindow();
        stage.close();
    }

    // Menandai transaksi dikonfirmasi lalu menutup popup.
    private void confirmAndClose() {
        confirmed = true;
        closeStage();
    }

    // Memberi warna positif/negatif pada label kembalian.
    private void applyChangeState(Label label, double value) {
        setStyleClass(label, "kasir-change-positive", value >= 0);
        setStyleClass(label, "kasir-change-negative", value < 0);
    }

    // Helper untuk mengubah class CSS tanpa duplikasi.
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
