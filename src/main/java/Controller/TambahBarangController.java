package Controller;

import config.koneksi;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import DAO.BarangDAO;
import DAO.KategoriDAO;
import model.Kategori;
import javafx.scene.control.ListCell;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller untuk Form Tambah Barang Baru.
 * Alur: Generate ID otomatis berdasarkan kategori, validasi input lengkap, dan penyimpanan ke database.
 */
public class TambahBarangController {

    // [1] Deklarasi komponen UI dari FXML
    @FXML private VBox paneRoot, vboxFormCard;
    @FXML private HBox hboxHeader, hboxFooter;
    @FXML private Label lblTitle, lblIdBarang, lblNamaBarang, lblIdKategori, lblStok, lblHargaBeli, lblHargaJual, lblSatuan;
    @FXML private TextField txtIdBarang, txtNamaBarang, txtStok, txtHargaBeli, txtHargaJual;
    @FXML private ComboBox<String> cmbKategori, cbSatuan;
    @FXML private Button btnTambah, btnBatal;

    /**
     * Method initialize: Menyiapkan form saat halaman dibuka.
     */
    @FXML
    public void initialize() {
        // [1] Terapkan tema (Dark/Light) dan muat daftar kategori
        setDarkMode(MainController.isDarkMode);
        loadKategori();
        
        // [2] Kunci ID Barang agar otomatis (Dihasilkan dari pilihan kategori)
        if (txtIdBarang != null) txtIdBarang.setEditable(false);

        // [3] Listener: Jika kategori diubah, ID barang akan di-generate ulang
        cmbKategori.setOnAction(event -> updateGeneratedBarangId());
        
        // [4] Konfigurasi pilihan satuan barang
        javafx.collections.ObservableList<String> listSatuan = javafx.collections.FXCollections.observableArrayList("Pcs", "Liter", "Butir", "Kg", "Gram", "Box");
        cbSatuan.setItems(listSatuan);
        cbSatuan.setValue("Pcs"); // Nilai default

        // [5] Memaksa styling warna teks ComboBox agar sinkron dengan tema
        setupComboBoxStyle(cbSatuan);
        setupComboBoxStyle(cmbKategori);
    }

    /**
     * Method: Menyesuaikan visual teks di dalam dropdown ComboBox agar kontras.
     */
    private void setupComboBoxStyle(ComboBox<String> cb) {
        cb.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(cb.getPromptText());
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: " + (MainController.isDarkMode ? "white" : "#2C3E50") + ";");
                }
            }
        });
    }

    /**
     * Method loadKategori: Mengambil data kategori untuk diisi ke dropdown.
     */
    private void loadKategori() {
        if (cmbKategori != null) cmbKategori.getItems().clear();

        List<Kategori> listKategori = KategoriDAO.getAllKategori();
        for (Kategori kat : listKategori) {
            cmbKategori.getItems().add(kat.getIdKategori() + " - " + kat.getNamaKategori());
        }
        
        // [1] Pilih kategori pertama secara otomatis dan generate ID awal
        if (!cmbKategori.getItems().isEmpty()) {
            cmbKategori.getSelectionModel().selectFirst();
            updateGeneratedBarangId();
        }
    }

    /**
     * Method updateGeneratedBarangId: Membuat ID unik barang baru secara otomatis.
     * Alur: 1. Ambil Prefix Kategori -> 2. Cari nomor urut terakhir di database -> 3. Set ke TextField.
     */
    private void updateGeneratedBarangId() {
        String idKategori = getSelectedKategoriId();
        if (idKategori.trim().isEmpty()) {
            txtIdBarang.clear();
            return;
        }

        // [1] Membuat ID baru seperti 'MIM001' berdasarkan prefix 'MIM' dari kategori
        String prefix = BarangDAO.getPrefixFromKategoriId(idKategori);
        txtIdBarang.setText(BarangDAO.getNextBarangId(prefix));
    }

    /**
     * Method: Mengambil ID murni (tanpa nama) dari pilihan ComboBox.
     */
    private String getSelectedKategoriId() {
        String selected = cmbKategori.getValue();
        if (selected == null || !selected.contains(" - ")) return "";
        return selected.split(" - ")[0].trim();
    }

    /**
     * Method setDarkMode: Mengatur warna visual elemen form sesuai tema aplikasi.
     */
    public void setDarkMode(boolean enabled) {
        // [1] Inisialisasi variabel warna tema
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#D1D5DB";

        // [2] Terapkan style ke panel utama dan header
        if (paneRoot != null) paneRoot.setStyle("-fx-background-color: " + bgMain + ";");
        if (hboxHeader != null) hboxHeader.setStyle("-fx-background-color: #4A76A8;");
        if (lblTitle != null) lblTitle.setStyle("-fx-text-fill: white;");

        // [3] Update style kartu form dan label-label input
        if (vboxFormCard != null) vboxFormCard.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 8);");
        
        Label[] formLabels = {lblIdBarang, lblNamaBarang, lblIdKategori, lblStok, lblSatuan, lblHargaBeli, lblHargaJual};
        for (Label lbl : formLabels) if (lbl != null) lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + textColor + "; -fx-font-size: 14px;");

        // [4] Update style kolom input (TextField & ComboBox)
        String txtStyle = "-fx-background-radius: 8; -fx-border-color: " + borderColor + "; -fx-background-color: " + (enabled ? "#2c2c2c" : "white") + "; -fx-text-fill: " + textColor + ";";
        TextField[] fields = {txtIdBarang, txtNamaBarang, txtStok, txtHargaBeli, txtHargaJual};
        for (TextField f : fields) if (f != null) f.setStyle(txtStyle);

        if (cmbKategori != null) cmbKategori.setStyle(txtStyle);
        if (cbSatuan != null) cbSatuan.setStyle(txtStyle);

        // [5] Memberikan styling khusus tombol batal
        if (btnBatal != null) {
            String bgBatal = enabled ? "#444444" : "#E2E8F0";
            String textBatal = enabled ? "#FFFFFF" : "#1E293B";
            btnBatal.setStyle("-fx-background-color: " + bgBatal + "; -fx-text-fill: " + textBatal + "; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        }
    }

    /**
     * Method handleSimpan: Memproses penyimpanan data barang baru ke database.
     * Alur: 1. Validasi Input -> 2. Eksekusi INSERT SQL -> 3. Notifikasi Sukses -> 4. Kembali.
     */
    @FXML
    private void handleSimpan() {
        // [1] Cek apakah semua field sudah diisi dengan benar
        if (isInputValid()) {
            String sql = "INSERT INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = koneksi.koneksiDB(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // [2] Siapkan data untuk dikirim ke SQL
                String idKategori = cmbKategori.getValue().split(" - ")[0];
                pstmt.setString(1, txtIdBarang.getText());
                pstmt.setString(2, txtNamaBarang.getText());
                pstmt.setString(3, idKategori);
                pstmt.setInt(4, Integer.parseInt(txtStok.getText()));
                pstmt.setString(5, cbSatuan.getValue());
                pstmt.setDouble(6, Double.parseDouble(txtHargaBeli.getText()));
                pstmt.setDouble(7, Double.parseDouble(txtHargaJual.getText()));

                // [3] Eksekusi perintah SQL
                pstmt.executeUpdate();
                showSuccessDialog("Berhasil ditambahkan");
                pindahKeHalamanUtama();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error Database", e.getMessage());
            }
        }
    }

    /**
     * Method: Menangani tombol Batal.
     */
    @FXML private void handleBatal() { pindahKeHalamanUtama(); }

    /**
     * Method: Mengembalikan tampilan ke daftar barang utama.
     */
    private void pindahKeHalamanUtama() {
        if (MainController.getInstance() != null) MainController.getInstance().panggilHalaman("BarangView");
    }

    /**
     * Method isInputValid: Mengecek kelengkapan data sebelum disimpan.
     */
    private boolean isInputValid() {
        // [1] Validasi: Tidak boleh ada kolom yang kosong
        if (txtIdBarang.getText().isEmpty() || txtNamaBarang.getText().isEmpty() || cmbKategori.getValue() == null || cbSatuan.getValue() == null || txtStok.getText().isEmpty() || txtHargaBeli.getText().isEmpty() || txtHargaJual.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Semua data wajib diisi!");
            return false;
        }

        // [2] Validasi: Stok dan harga harus berupa angka
        try {
            Integer.parseInt(txtStok.getText());
            Double.parseDouble(txtHargaBeli.getText());
            Double.parseDouble(txtHargaJual.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Stok dan Harga harus berupa angka!");
            return false;
        }

        // [3] Validasi: ID Barang harus sesuai dengan Kategori yang dipilih
        String idKategori = getSelectedKategoriId();
        if (!BarangDAO.isBarangIdMatchKategori(txtIdBarang.getText(), idKategori)) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "ID Barang tidak sinkron dengan kategori. Memperbaiki...");
            updateGeneratedBarangId();
            return false;
        }
        return true;
    }

    /**
     * Method: Menampilkan Alert standar JavaFX.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }

    /**
     * Method showSuccessDialog: Menampilkan dialog sukses kustom yang menarik.
     */
    private void showSuccessDialog(String titleText) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        
        BorderPane root = new BorderPane();
        root.setPrefSize(592, 307);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-border-radius: 14;");

        VBox content = new VBox(22); content.setAlignment(Pos.CENTER); content.setPadding(new Insets(34, 30, 28, 30));
        
        try {
            ImageView iconView = new ImageView(new Image(getClass().getResourceAsStream("/Images/iconsukses.png")));
            iconView.setFitWidth(74); iconView.setFitHeight(74); iconView.setPreserveRatio(true);
            content.getChildren().add(iconView);
        } catch (Exception ignored) {}

        Label titleLabel = new Label(titleText);
        titleLabel.setStyle("-fx-text-fill: #111111; -fx-font-size: 28px; -fx-font-weight: bold;");
        content.getChildren().add(titleLabel);
        root.setCenter(content);

        HBox footer = new HBox(); footer.setAlignment(Pos.CENTER); footer.setPadding(new Insets(22, 0, 22, 0));
        footer.setStyle("-fx-border-color: #D9D9D9 transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        Button btnOk = new Button("OK");
        btnOk.setPrefSize(150, 50);
        btnOk.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-size: 17px; -fx-font-weight: bold; -fx-cursor: hand;");
        btnOk.setOnAction(event -> dialog.close());
        footer.getChildren().add(btnOk);
        root.setBottom(footer);

        dialog.setScene(new Scene(root)); dialog.showAndWait();
    }
}
