package Controller;

// --- 1. IMPORT (Bahan-bahannya harus di paling atas) ---
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

// Import model kamu (sesuaikan kalau nama foldernya beda)
import model.Kategori;
import model.KategoriDAO;

public class KategoriController {

    // --- 2. DEKLARASI VARIABEL FXML ---
    @FXML
    private VBox vboxKategoriList; // Pastikan fx:id di FXML sama persis!

    // --- 3. INSTANCE DAO (Biar nggak merah pas dipanggil) ---
    private KategoriDAO kategoriDAO = new KategoriDAO();

    @FXML
    public void initialize() {
        // Panggil fungsi ini pas halaman dibuka
        muatDataKategori();
    }

    // --- 4. LOGIKA UTAMA ---
    private void muatDataKategori() {
        vboxKategoriList.getChildren().clear(); // Bersihkan list lama

        // Ambil data dari database lewat DAO
        List<Kategori> list = kategoriDAO.getAllKategori();

        int no = 1;
        for (Kategori k : list) {
            // Kita buat baris (HBox) untuk setiap kategori
            HBox baris = new HBox(10);
            baris.setAlignment(Pos.CENTER_LEFT);
            baris.setPadding(new Insets(10, 15, 10, 15));

            // Style baris biar estetik (Putih, ada garis bawah tipis)
            baris.setStyle("-fx-background-color: white; " +
                    "-fx-border-color: #F0F0F0; " +
                    "-fx-border-width: 0 0 1 0;");

            // Isi Kolom (Pakai prefix lbl sesuai maumu)
            Label lblNo = new Label(String.valueOf(no++));
            lblNo.setPrefWidth(50);

            Label lblId = new Label(k.getIdKategori());
            lblId.setPrefWidth(150);

            Label lblNama = new Label(k.getNamaKategori());
            lblNama.setPrefWidth(300);
            lblNama.setStyle("-fx-font-weight: bold;");

            // Spacer biar tombol edit/hapus nempel di kanan
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Tombol Aksi (Pakai prefix btn)
            Button btnEdit = new Button("Edit");
            btnEdit.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black; -fx-background-radius: 5; -fx-cursor: hand;");

            Button btnHapus = new Button("Hapus");
            btnHapus.setStyle("-fx-background-color: #D9534F; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

            // Masukkan semua ke dalam baris
            baris.getChildren().addAll(lblNo, lblId, lblNama, spacer, btnEdit, btnHapus);

            // Masukkan baris ke dalam wadah besar (VBox)
            vboxKategoriList.getChildren().add(baris);
        }
    }
}