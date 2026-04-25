package Controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.text.NumberFormat;
import java.util.Locale;

public class PopUpSimpanCetakController {

    @FXML private AnchorPane popupRoot;
    @FXML private Label lblTotal;
    @FXML private Label lblBayar;
    @FXML private Label lblKembalian;
    @FXML private Button btnTutup;
    @FXML private Button btnCetak;

    private boolean confirmed = false;
    private final NumberFormat nfIndo = NumberFormat.getInstance(new Locale("id", "ID"));

    @FXML
    public void initialize() {
        btnTutup.setOnAction(e -> {
            confirmed = false;
            closeStage();
        });

        btnCetak.setOnAction(e -> {
            confirmed = true;
            closeStage();
        });
    }

    public void setData(double total, double bayar) {
        lblTotal.setText("Rp " + nfIndo.format(total));
        lblBayar.setText("Rp " + nfIndo.format(bayar));
        double kembalian = bayar - total;
        lblKembalian.setText("Rp " + nfIndo.format(kembalian));
        applyChangeState(lblKembalian, kembalian);
    }

    public void setDarkMode(boolean enabled) {
        setStyleClass(popupRoot, "dark", enabled);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    private void closeStage() {
        Stage stage = (Stage) btnTutup.getScene().getWindow();
        stage.close();
    }

    private void applyChangeState(Label label, double value) {
        setStyleClass(label, "kasir-change-positive", value >= 0);
        setStyleClass(label, "kasir-change-negative", value < 0);
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
