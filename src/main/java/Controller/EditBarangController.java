package Controller;

import config.koneksi;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import DAO.KategoriDAO;
import model.Kategori;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller untuk halaman Edit/Detail Barang.
 * Method ini menangani proses perubahan data, penghapusan, dan pengaturan tampilan detail barang.
 */
public class EditBarangController {

    // [1] Deklarasi komponen UI dari file FXML
    @FXML private VBox paneRoot, vboxFormCard;
    @FXML private HBox hboxHeader, hboxFooter;
    @FXML private Label lblTitle, lblIdBarang, lblNamaBarang, lblIdKategori, lblStok, lblSatuan, lblHargaBeli, lblHargaJual;
    @FXML private TextField txtIdBarang, txtNamaBarang, txtStok, txtHargaBeli, txtHargaJual;
    @FXML private ComboBox<String> cmbKategori, cbSatuan;
    @FXML private Button btnSimpan, btnBatal, btnHapus;

    // Variabel untuk menyimpan ID barang asli sebelum diubah
    private String idBarangAsli;

    /**
     * Method initialize: Menyiapkan komponen form saat halaman dimuat.
     */
    @FXML
    public void initialize() {
        // [1] Mengatur tema (Gelap/Terang) dan memuat daftar kategori dari database
        setDarkMode(MainController.isDarkMode);
        loadKategori();

        // [2] Mengunci input ID dan Kategori agar tidak bisa diubah (Hanya Detail/Update tertentu)
        if (txtIdBarang != null) {
            txtIdBarang.setEditable(false);
            txtIdBarang.setFocusTraversable(false);
        }
        if (cmbKategori != null) {
            cmbKategori.setDisable(true);
            cmbKategori.setFocusTraversable(false);
        }

        // [3] Mengisi pilihan satuan barang
        cbSatuan.setItems(FXCollections.observableArrayList("Pcs", "Liter", "Butir", "Kg", "Gram", "Box"));

        // [4] Merapikan tata letak tombol di bagian footer
        if (hboxFooter != null) {
            hboxFooter.getChildren().clear();
            hboxFooter.setAlignment(Pos.CENTER);
            hboxFooter.setSpacing(20);
            hboxFooter.getChildren().addAll(btnBatal, btnSimpan, btnHapus);
        }
    }

    /**
     * Method initData: Mengisi form dengan data barang yang dipilih dari tabel utama.
     */
    public void initData(String id, String nama, String idKat, String namaKat, int stok, String satuan, double hBeli, double hJual) {
        // [1] Menyimpan ID asli dan menampilkan judul detail
        this.idBarangAsli = id;
        if (lblTitle != null) lblTitle.setText("Detail Barang : " + id);
        
        // [2] Memasukkan data ke masing-masing input field
        txtIdBarang.setText(id);
        txtNamaBarang.setText(nama);
        cmbKategori.setValue(idKat + " - " + namaKat);
        txtStok.setText(String.valueOf(stok));
        cbSatuan.setValue(satuan);
        txtHargaBeli.setText(String.valueOf((long) hBeli));
        txtHargaJual.setText(String.valueOf((long) hJual));
    }

    /**
     * Method handleBatal: Menangani aksi tombol batal.
     */
    @FXML private void handleBatal() { pindahKeHalamanUtama(); }

    /**
     * Method handleSimpan: Memproses update data barang ke database.
     */
    @FXML
    private void handleSimpan() {
        // [1] Validasi input terlebih dahulu
        if (isInputValid()) {
            // [2] Menampilkan dialog konfirmasi simpan
            if (!showCustomConfirmationDialog("Konfirmasi?", "Simpan perubahan?", "Simpan", "#5AC463")) return;
            
            // [3] Eksekusi perintah UPDATE ke database SQLite
            String sql = "UPDATE barang SET nama_barang=?, stok=?, satuan=?, harga_beli=?, harga_jual=? WHERE id_barang=?";
            try (Connection conn = koneksi.koneksiDB(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, txtNamaBarang.getText());
                pstmt.setInt(2, Integer.parseInt(txtStok.getText()));
                pstmt.setString(3, cbSatuan.getValue());
                pstmt.setDouble(4, Double.parseDouble(txtHargaBeli.getText()));
                pstmt.setDouble(5, Double.parseDouble(txtHargaJual.getText()));
                pstmt.setString(6, idBarangAsli);
                pstmt.executeUpdate();
                
                // [4] Tampilkan notifikasi sukses dan kembali ke tabel utama
                showSuccessDialog("Berhasil Diperbarui");
                pindahKeHalamanUtama();
            } catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Error Database", e.getMessage()); }
        }
    }

    /**
     * Method handleHapus: Memproses penghapusan data barang dari database.
     */
    @FXML
    private void handleHapus() {
        // [1] Menampilkan dialog konfirmasi hapus
        if (!showCustomConfirmationDialog("Hapus?", "Hapus barang ini?", "Hapus", "#FF5757")) return;
        
        // [2] Eksekusi perintah DELETE berdasarkan ID barang
        String sql = "DELETE FROM barang WHERE id_barang = ?";
        try (Connection conn = koneksi.koneksiDB(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idBarangAsli);
            pstmt.executeUpdate();
            
            // [3] Tampilkan notifikasi sukses dan kembali ke tabel utama
            showSuccessDialog("Berhasil Dihapus");
            pindahKeHalamanUtama();
        } catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()); }
    }

    /**
     * Method: Mengembalikan navigasi ke halaman daftar barang.
     */
    private void pindahKeHalamanUtama() {
        if (MainController.getInstance() != null) MainController.getInstance().panggilHalaman("BarangView");
    }

    /**
     * Method isInputValid: Mengecek apakah semua kolom input sudah terisi.
     */
    private boolean isInputValid() {
        // [1] Cek apakah ada field teks yang masih kosong
        if (txtNamaBarang.getText().isEmpty() || txtStok.getText().isEmpty() || txtHargaBeli.getText().isEmpty() || txtHargaJual.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Semua data wajib diisi!");
            return false;
        }
        return true;
    }

    /**
     * Method loadKategori: Mengambil data kategori dari database untuk diisi ke ComboBox.
     */
    private void loadKategori() {
        if (cmbKategori != null) cmbKategori.getItems().clear();
        List<Kategori> list = KategoriDAO.getAllKategori();
        for (Kategori k : list) cmbKategori.getItems().add(k.getIdKategori() + " - " + k.getNamaKategori());
    }

    /**
     * Method: Menampilkan dialog alert standar JavaFX.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }

    /**
     * Method showCustomConfirmationDialog: Menampilkan dialog konfirmasi dengan desain kustom.
     */
    private boolean showCustomConfirmationDialog(String title, String message, String confirmText, String confirmColor) {
        // [1] Menyiapkan stage dialog baru dengan mode modal
        final boolean[] confirmed = {false};
        Stage dialog = new Stage(); dialog.initModality(Modality.APPLICATION_MODAL); dialog.initStyle(StageStyle.UNDECORATED);
        
        // [2] Mendesain tampilan dialog secara programmatically (Header, Content, Footer)
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + (MainController.isDarkMode ? "#1F1F1F" : "white") + "; -fx-background-radius: 14; -fx-border-radius: 14; -fx-border-color: #3A3A3A; -fx-border-width: 1;");
        root.setPrefSize(450, 220);
        
        VBox content = new VBox(15); content.setAlignment(Pos.CENTER); content.setPadding(new Insets(20));
        Label t = new Label(title); t.setStyle("-fx-text-fill: " + (MainController.isDarkMode ? "white" : "black") + "; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label m = new Label(message); m.setStyle("-fx-text-fill: gray;");
        content.getChildren().addAll(t, m); root.setCenter(content);
        
        // [3] Menambahkan tombol Batal dan Konfirmasi
        HBox footer = new HBox(15); footer.setAlignment(Pos.CENTER); footer.setPadding(new Insets(15));
        Button bC = new Button("Batal"); bC.setOnAction(e -> dialog.close());
        Button bK = new Button(confirmText); bK.setOnAction(e -> { confirmed[0] = true; dialog.close(); });
        bK.setStyle("-fx-background-color: " + confirmColor + "; -fx-text-fill: white; -fx-font-weight: bold;");
        footer.getChildren().addAll(bC, bK); root.setBottom(footer);
        
        // [4] Menampilkan jendela dialog dan menunggu respon pengguna
        dialog.setScene(new Scene(root)); dialog.showAndWait();
        return confirmed[0];
    }

    /**
     * Method showSuccessDialog: Menampilkan notifikasi sukses kustom yang simpel.
     */
    private void showSuccessDialog(String msg) {
        Stage dialog = new Stage(); dialog.initModality(Modality.APPLICATION_MODAL); dialog.initStyle(StageStyle.UNDECORATED);
        VBox root = new VBox(15); root.setAlignment(Pos.CENTER); root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + (MainController.isDarkMode ? "#1F1F1F" : "white") + "; -fx-background-radius: 14; -fx-border-color: #3A3A3A;");
        Label l = new Label(msg); l.setStyle("-fx-text-fill: " + (MainController.isDarkMode ? "white" : "black") + "; -fx-font-weight: bold;");
        Button b = new Button("OK"); b.setOnAction(e -> dialog.close());
        root.getChildren().addAll(l, b);
        dialog.setScene(new Scene(root, 300, 150)); dialog.showAndWait();
    }

    /**
     * Method setDarkMode: Menyesuaikan gaya warna elemen form sesuai tema yang dipilih.
     */
    public void setDarkMode(boolean enabled) {
        // [1] Menyiapkan variabel warna
        String bgMain      = enabled ? "#121212" : "#F4F4F4";
        String bgCard      = enabled ? "#1e1e1e" : "white";
        String textColor   = enabled ? "#FFFFFF" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#D1D5DB";

        // [2] Mengatur warna latar belakang dan header
        if (paneRoot != null) paneRoot.setStyle("-fx-background-color: " + bgMain + ";");
        if (hboxHeader != null) hboxHeader.setStyle("-fx-background-color: #4A76A8;");
        if (lblTitle != null) lblTitle.setStyle("-fx-text-fill: white;");

        // [3] Mengatur style kartu form dan footer
        if (vboxFormCard != null) {
            vboxFormCard.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 8);");
        }
        if (hboxFooter != null) {
            hboxFooter.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 0 0 15 15;");
        }

        // [4] Mengatur style teks Label dan Input Field
        Label[] lbs = {lblIdBarang, lblNamaBarang, lblIdKategori, lblStok, lblSatuan, lblHargaBeli, lblHargaJual};
        for (Label lb : lbs) if (lb != null) lb.setStyle("-fx-font-weight: bold; -fx-text-fill: " + textColor + "; -fx-font-size: 14px;");

        String txtStyle = "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + borderColor + "; " +
                "-fx-background-color: " + (enabled ? "#2c2c2c" : "white") + "; " +
                "-fx-text-fill: " + textColor + " !important;";

        TextField[] flds = {txtIdBarang, txtNamaBarang, txtStok, txtHargaBeli, txtHargaJual};
        for (TextField f : flds) if (f != null) f.setStyle(txtStyle);

        // [5] Menyesuaikan style ComboBox secara khusus agar stabil dan kontras
        if (cmbKategori != null) {
            cmbKategori.setStyle(txtStyle + "-fx-background-color: " + (enabled ? "#262626" : "#F3F4F6") + ";");
            cmbKategori.setButtonCell(createListCell(textColor));
            cmbKategori.setMaxWidth(Double.MAX_VALUE);
        }
        if (cbSatuan != null) {
            cbSatuan.setStyle(txtStyle);
            cbSatuan.setButtonCell(createListCell(textColor));
            cbSatuan.setMaxWidth(Double.MAX_VALUE);
        }

        // [6] Memberikan warna yang berbeda untuk setiap tombol aksi
        if (btnBatal != null) btnBatal.setStyle("-fx-background-color: " + (enabled ? "#444444" : "#E5E7EB") + "; -fx-text-fill: " + (enabled ? "white" : "#374151") + "; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        if (btnSimpan != null) btnSimpan.setStyle("-fx-background-color: #4A76A8; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        if (btnHapus != null) btnHapus.setStyle("-fx-background-color: #D9534F; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
    }

    /**
     * Method: Membuat kustomisasi tampilan sel untuk teks di dalam ComboBox.
     */
    private ListCell<String> createListCell(String color) {
        return new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: " + color + " !important; -fx-font-weight: bold; -fx-padding: 0 0 0 12; -fx-background-color: transparent;");
                }
            }
        };
    }
}
