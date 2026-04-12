package Controller;

// --- 1. IMPORT TOOLS ---
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Kategori;
import model.KategoriDAO;

public class KategoriController implements Initializable {

    // --- 2. DEKLARASI VARIABEL FXML ---
    @FXML private VBox vboxKategoriList; // Wadah list kategori di ScrollPane
    private KategoriDAO kategoriDAO = new KategoriDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        muatDataKategori();
    }

    // --- 3. LOGIKA LOAD DATA ---
    private void muatDataKategori() {
        vboxKategoriList.getChildren().clear(); // Bersihkan list sebelum isi baru
        List<Kategori> list = kategoriDAO.getAllKategori();

        int no = 1;
        for (Kategori k : list) {
            // Baris HBox dengan gaya baris putih murni dan pemisah bawah
            HBox baris = new HBox(10);
            baris.setAlignment(Pos.CENTER_LEFT);
            baris.setPadding(new Insets(10, 15, 10, 15));
            baris.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");

            // Kolom 1: No (Lebar sempit)
            Label lblNo = new Label(String.valueOf(no++));
            lblNo.setPrefWidth(50); // Lebar sempit
            lblNo.setStyle("-fx-text-fill: #2C3E50;");

            // Kolom 2: ID Kategori (Lebar sempit)
            Label lblId = new Label(k.getIdKategori());
            lblId.setPrefWidth(150); // Lebar sempit
            lblId.setStyle("-fx-text-fill: #2C3E50;");

            // Kolom 3: Nama Kategori (Lebar dinamis untuk mengisi sisa ruang)
            Label lblNama = new Label(k.getNamaKategori());
            HBox.setHgrow(lblNama, Priority.ALWAYS); // Dorong ke kiri secara dinamis
            lblNama.setStyle("-fx-text-fill: #2C3E50;");

            // Kontainer Tombol Aksi (Kanan)
            HBox actionBox = new HBox(5); // Jarak kecil antar tombol
            actionBox.setAlignment(Pos.CENTER_RIGHT);

            // Tombol Edit (Biru muda dengan ikon pensil)
            ImageView iconEdit = new ImageView(new Image(getClass().getResourceAsStream("/Images/pencil_white.png"))); // Ganti dengan ikon pensil putih kamu
            iconEdit.setFitHeight(18);
            iconEdit.setFitWidth(18);
            Button btnEdit = new Button("Edit", iconEdit);
            btnEdit.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand;");

            // Tombol Hapus (Merah muda dengan ikon tempat sampah)
            ImageView iconTrash = new ImageView(new Image(getClass().getResourceAsStream("/Images/trash_white.png"))); // Ganti dengan ikon tempat sampah putih kamu
            iconTrash.setFitHeight(18);
            iconTrash.setFitWidth(18);
            Button btnHapus = new Button("Hapus", iconTrash);
            btnHapus.setStyle("-fx-background-color: #F87171; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand;");

            actionBox.getChildren().addAll(btnEdit, btnHapus);

            // Spacer untuk dorongan ke kanan
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Masukkan semua ke dalam baris dengan urutan yang benar
            baris.getChildren().addAll(lblNo, lblId, lblNama, spacer, actionBox);
            vboxKategoriList.getChildren().add(baris);
        }
    }
}