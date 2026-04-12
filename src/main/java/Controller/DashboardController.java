package Controller;

// --- 1. IMPORT TOOLS (Alat bantu untuk UI dan Data) ---
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

// --- 2. CLASS HEADER (Rumah utama kodingan dashboard) ---
public class DashboardController implements Initializable {

    // --- 3. JEMBATAN FXML (Hubungin ID di Scene Builder ke Java) ---
    @FXML
    private VBox stokListContainer; // Kontainer buat daftar stok hampir habis

    // --- 4. AUTO-RUN (Fungsi yang otomatis jalan pas halaman dibuka) ---
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        muatDataStokHampirHabis(); // Langsung isi data pas aplikasi tampil
    }

    // --- 5. LOGIKA DATA (Blok buat ngambil data dari sistem) ---
    private void muatDataStokHampirHabis() {
        stokListContainer.getChildren().clear(); // Bersihin list biar gak numpuk

        // Contoh perulangan data (Nanti tinggal ganti ke Database asli)
        for (int i = 1; i <= 5; i++) {
            // Masukin baris desain ke dalam kontainer utama
            HBox baris = buatBarisStok("Barang Contoh " + i, i + 2);
            stokListContainer.getChildren().add(baris);
        }
    }

    // --- 6. DESIGNER LOGIC (Blok khusus buat bikin tampilan baris yang rapi) ---
    private HBox buatBarisStok(String nama, int sisa) {
        HBox hBox = new HBox();
        hBox.setSpacing(20);
        hBox.setPadding(new Insets(10));
        hBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Styling: Kasih background putih, sudut tumpul, dan bayangan tipis
        hBox.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        // Bikin Label Nama Barang
        Label lblNama = new Label(nama);
        lblNama.setPrefWidth(180);
        lblNama.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        // Bikin Label Angka Stok (Warna merah biar kelihatan penting)
        Label lblSisa = new Label("Sisa: " + sisa);
        lblSisa.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");

        // Masukin label ke dalam satu baris (HBox)
        hBox.getChildren().addAll(lblNama, lblSisa);
        return hBox;
    }
}