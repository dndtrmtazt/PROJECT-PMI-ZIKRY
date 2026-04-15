package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.text.NumberFormat;
import java.util.Locale;

public class PopUpSimpanCetakController {

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
        
        if (kembalian < 0) {
            lblKembalian.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
        } else {
            lblKembalian.setStyle("-fx-text-fill: #2d7a32; -fx-font-weight: bold;");
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    private void closeStage() {
        Stage stage = (Stage) btnTutup.getScene().getWindow();
        stage.close();
    }
}
