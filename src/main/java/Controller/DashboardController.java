package Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    // 1. Deklarasi ID dari FXML (Pastikan fx:id di FXML sama persis ya!)
    @FXML
    private VBox stokListContainer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 2. Fungsi ini otomatis jalan pas halaman terbuka
        muatDataStokHampirHabis();
    }

    private void muatDataStokHampirHabis() {
        // Bersihkan dulu biar nggak dobel pas di-refresh
        stokListContainer.getChildren().clear();

        // --- CONTOH LOGIKA DUMMY (Nanti ganti pakai data Database) ---
        // Kita bikin 5 baris contoh dulu biar Dinda bisa lihat hasilnya
        for (int i = 1; i <= 5; i++) {
            HBox baris = buatBarisStok("Barang Contoh " + i, i + 2);
            stokListContainer.getChildren().add(baris);
        }
    }

    // Fungsi pembantu buat bikin desain baris stok yang rapi
    private HBox buatBarisStok(String nama, int sisa) {
        HBox hBox = new HBox();
        hBox.setSpacing(20);
        hBox.setPadding(new Insets(10));
        hBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Style biar mirip kartu kecil
        hBox.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label lblNama = new Label(nama);
        lblNama.setPrefWidth(180);
        lblNama.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        Label lblSisa = new Label("Sisa: " + sisa);
        lblSisa.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");

        hBox.getChildren().addAll(lblNama, lblSisa);
        return hBox;
    }
}