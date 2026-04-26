package Controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import DAO.KategoriDAO;
import model.Kategori;

/**
 * Controller untuk mengelola daftar Kategori Barang.
 * Alur: Menampilkan data kategori dalam bentuk baris kustom, serta menangani aksi Tambah, Edit, dan Hapus.
 */
public class KategoriController implements Initializable {

    // [1] Deklarasi komponen UI dari FXML
    @FXML private VBox paneRoot, vboxKategoriList, vboxContent;
    @FXML private HBox vboxHeader, hboxSearch, hboxTableHead;
    @FXML private Label lblTitle, lblDaftarKategori;
    @FXML private TextField txtSearchKategori;
    @FXML private ImageView imgLightMode, imgDarkMode, imgLogout;

    private KategoriDAO kategoriDAO = new KategoriDAO();

    /**
     * Method initialize: Mengatur tampilan awal halaman kategori.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // [1] Memuat data kategori dari database
        loadDataKategori();
        // [2] Menyesuaikan tema visual (Dark/Light)
        setDarkMode(MainController.isDarkMode);
    }

    /**
     * Method handleTambahKategori: Menangani aksi klik tombol tambah.
     */
    @FXML
    private void handleTambahKategori() {
        showKategoriDialog(null); // Membuka form dalam mode 'Baru'
    }

    /**
     * Method handleEdit: Menangani aksi klik tombol edit pada baris kategori.
     */
    private void handleEdit(Kategori k) {
        showKategoriDialog(k); // Membuka form dengan data kategori yang dipilih
    }

    /**
     * Method showKategoriDialog: Memunculkan jendela Pop-up Form Kategori.
     * Alur: 1. Load FXML Form -> 2. Set Data (Update/Add) -> 3. Tampilkan & Tunggu -> 4. Refresh List.
     */
    private void showKategoriDialog(Kategori k) {
        try {
            // [1] Mencari lokasi file desain form
            URL fxmlLocation = getClass().getResource("/FXML/Admin/FormKategori.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // [2] Mengirim data kategori ke controller form
            FormKategoriController controller = loader.getController();
            controller.setData(k);

            // [3] Membuat dan menampilkan Stage (Window) baru secara Modal
            Stage stage = new Stage();
            stage.setTitle(k == null ? "Tambah Data Kategori" : "Edit Data Kategori");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // [4] Refresh data di layar utama setelah form ditutup
            loadDataKategori();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method loadDataKategori: Mengambil data dari database dan merendernya ke VBox.
     * Alur: 1. Clear List -> 2. Ambil dari DAO -> 3. Looping untuk membuat baris HBox -> 4. Add ke Container.
     */
    private void loadDataKategori() {
        if (vboxKategoriList == null) return;

        // [1] Bersihkan tampilan lama
        vboxKategoriList.getChildren().clear();
        List<Kategori> list = kategoriDAO.getAllKategori();
        
        // [2] Tentukan variabel warna sesuai tema aktif
        boolean isDark = MainController.isDarkMode;
        String textColor = isDark ? "white" : "#2C3E50";
        String rowBg = isDark ? "#1e1e1e" : "#FFFFFF";
        String borderColor = isDark ? "#333333" : "#E0E0E0";

        // [3] Looping data hasil query database
        int no = 1;
        for (Kategori k : list) {
            // [4] Membuat wadah baris (HBox) secara programmatically
            HBox baris = new HBox(10);
            baris.setAlignment(Pos.CENTER_LEFT);
            baris.setPadding(new Insets(10, 15, 10, 15));
            baris.setStyle("-fx-background-color: " + rowBg + "; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0;");

            // [5] Menambahkan Label No, ID, dan Nama Kategori
            Label lblNo = new Label(String.valueOf(no++));
            lblNo.setMinWidth(91.0); lblNo.setStyle("-fx-text-fill: " + textColor + ";");

            Label lblId = new Label(k.getIdKategori());
            lblId.setMinWidth(199.0); lblId.setStyle("-fx-text-fill: " + textColor + ";");

            Label lblNama = new Label(k.getNamaKategori());
            lblNama.setMinWidth(236.0); lblNama.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // [6] Menambahkan tombol aksi (Edit & Hapus) di akhir baris
            HBox actionBox = new HBox(10);
            actionBox.setMinWidth(180.0);
            actionBox.setAlignment(Pos.CENTER_LEFT);

            Button btnEdit = createActionButton("Edit", "#4A90E2", "/Images/pencil_white.png");
            btnEdit.setOnAction(e -> handleEdit(k));

            Button btnHapus = createActionButton("Hapus", "#F87171", "/Images/trash_white.png");
            btnHapus.setOnAction(e -> handleHapus(k));

            actionBox.getChildren().addAll(btnEdit, btnHapus);
            baris.getChildren().addAll(lblNo, lblId, lblNama, spacer, actionBox);
            vboxKategoriList.getChildren().add(baris);
        }
    }

    /**
     * Method handleHapus: Menangani proses penghapusan data kategori.
     */
    private void handleHapus(Kategori k) {
        // [1] Tampilkan dialog konfirmasi hapus
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText(null);
        confirm.setContentText("Apakah Anda yakin ingin menghapus kategori '" + k.getNamaKategori() + "'?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // [2] Eksekusi penghapusan di database
                if (kategoriDAO.deleteKategori(k.getIdKategori())) {
                    loadDataKategori(); // Refresh tampilan jika sukses
                }
            }
        });
    }

    /**
     * Method createActionButton: Fungsi pembantu untuk membuat tombol bergambar (ikon).
     */
    private Button createActionButton(String text, String color, String iconPath) {
        Button btn = new Button(text);
        btn.setMinWidth(Region.USE_PREF_SIZE);
        try {
            ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
            iv.setFitHeight(14); iv.setFitWidth(14);
            btn.setGraphic(iv);
        } catch (Exception e) {}
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 5 12 5 12;");
        return btn;
    }

    /**
     * Method setDarkMode: Menyesuaikan visual halaman sesuai tema aktif.
     */
    public void setDarkMode(boolean enabled) {
        // [1] Inisialisasi variabel warna tema
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1E1E1E" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#E0E0E0";
        String headerBg = enabled ? "#2C2C2C" : "#F8FAFC";
        String inputBg = enabled ? "#2C2C2C" : "white";

        // [2] Terapkan style CSS pada kontainer dan input pencarian
        if (paneRoot != null) paneRoot.setStyle("-fx-background-color: " + bgMain + ";");
        if (vboxHeader != null) vboxHeader.setStyle("-fx-background-color: #4A76A8;");
        if (hboxSearch != null) hboxSearch.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-border-color: " + borderColor + "; -fx-border-radius: 10;");
        if (txtSearchKategori != null) txtSearchKategori.setStyle("-fx-background-color: " + inputBg + "; -fx-border-color: " + borderColor + "; -fx-text-fill: " + textColor + ";");
        if (vboxContent != null) vboxContent.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10;");

        // [3] Refresh list baris kategori agar warnanya ikut berubah
        loadDataKategori();
    }
}
