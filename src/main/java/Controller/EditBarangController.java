package Controller;

import config.koneksi;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Kategori;
import model.KategoriDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EditBarangController {

    @FXML private VBox paneRoot, vboxFormCard;
    @FXML private HBox hboxHeader, hboxFooter;
    @FXML private Label lblTitle, lblIdBarang, lblNamaBarang, lblIdKategori, lblStok, lblSatuan, lblHargaBeli, lblHargaJual;
    @FXML private TextField txtIdBarang, txtNamaBarang, txtStok, txtHargaBeli, txtHargaJual;
    @FXML private ComboBox<String> cmbKategori, cbSatuan; // <--- Sekarang pakai ComboBox
    @FXML private Button btnSimpan, btnBatal, btnHapus;

    private String idBarangAsli; // <--- Kunci buat nyimpen ID lama sebelum diedit

    @FXML
    public void initialize() {
        setDarkMode(MainController.isDarkMode);
        loadKategori();

        // Isi pilihan satuan
        cbSatuan.setItems(FXCollections.observableArrayList("Pcs", "Liter", "Butir", "Kg", "Gram", "Box"));
    }

    // Method ini dipanggil dari Halaman Utama saat mau edit barang
    public void initData(String id, String nama, String idKat, String namaKat, int stok, String satuan, double hBeli, double hJual) {
        this.idBarangAsli = id;

        // UBAH JUDUL HEADER DI SINI, DIN!
        if (lblTitle != null) {
            lblTitle.setText("Detail Barang : " + id);
        }

        txtIdBarang.setText(id);
        txtNamaBarang.setText(nama);
        cmbKategori.setValue(idKat + " - " + namaKat); // Set kategori yang tersimpan
        txtStok.setText(String.valueOf(stok));
        cbSatuan.setValue(satuan); // Set satuan yang tersimpan
        txtHargaBeli.setText(String.valueOf((long)hBeli));
        txtHargaJual.setText(String.valueOf((long)hJual));
    }

    private void loadKategori() {
        if (cmbKategori != null) {
            cmbKategori.getItems().clear();
        }

        // Ambil data terbaru dari database
        List<model.Kategori> list = model.KategoriDAO.getAllKategori();
        for (model.Kategori k : list) {
            cmbKategori.getItems().add(k.getIdKategori() + " - " + k.getNamaKategori());
        }
    }

    @FXML
    private void handleSimpan() {
        String idBaru = txtIdBarang.getText();

        // 1. CEK DUPLIKAT JIKA ID DIUBAH
        if (!idBaru.equals(idBarangAsli)) {
            if (isIdExists(idBaru)) {
                showAlert(Alert.AlertType.ERROR, "Kode Digunakan",
                        "Kode barang '" + idBaru + "' sudah ada di database. Gunakan kode lain!");
                return;
            }
        }

        if (isInputValid()) {
            // 2. QUERY UPDATE (Termasuk update ID dan Satuan)
            String sql = "UPDATE barang SET id_barang=?, nama_barang=?, id_kategori=?, stok=?, satuan=?, harga_beli=?, harga_jual=? WHERE id_barang=?";

            try (Connection conn = koneksi.koneksiDB();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                String idKat = cmbKategori.getValue().split(" - ")[0];

                pstmt.setString(1, idBaru);
                pstmt.setString(2, txtNamaBarang.getText());
                pstmt.setString(3, idKat);
                pstmt.setInt(4, Integer.parseInt(txtStok.getText()));
                pstmt.setString(5, cbSatuan.getValue());
                pstmt.setDouble(6, Double.parseDouble(txtHargaBeli.getText()));
                pstmt.setDouble(7, Double.parseDouble(txtHargaJual.getText()));
                pstmt.setString(8, idBarangAsli); // WHERE id_barang = id lama

                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data Barang Berhasil Diperbarui!");
                pindahKeHalamanUtama();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error Database", e.getMessage());
            }
        }
    }

    // Fungsi cek ID di database
    private boolean isIdExists(String id) {
        String sql = "SELECT COUNT(*) FROM barang WHERE id_barang = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @FXML
    private void handleHapus() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText(null);
        confirm.setContentText("Yakin ingin menghapus barang [" + txtNamaBarang.getText() + "]?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "DELETE FROM barang WHERE id_barang = ?";
                try (Connection conn = koneksi.koneksiDB();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, idBarangAsli);
                    pstmt.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Barang berhasil dihapus!");
                    pindahKeHalamanUtama();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Gagal hapus: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleBatal() { pindahKeHalamanUtama(); }

    private void pindahKeHalamanUtama() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().panggilHalaman("BarangView");
        }
    }

    private boolean isInputValid() {
        if (txtIdBarang.getText().isEmpty() || txtNamaBarang.getText().isEmpty() ||
                cmbKategori.getValue() == null || cbSatuan.getValue() == null ||
                txtStok.getText().isEmpty() || txtHargaBeli.getText().isEmpty() || txtHargaJual.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Semua data wajib diisi!");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setDarkMode(boolean enabled) {
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#D1D5DB";
        String promptColor = enabled ? "#B0B0B0" : "#757575";

        if (paneRoot != null) paneRoot.setStyle("-fx-background-color: " + bgMain + ";");
        if (hboxHeader != null) hboxHeader.setStyle("-fx-background-color: #4A76A8;");
        if (lblTitle != null) lblTitle.setStyle("-fx-text-fill: white;");

        if (vboxFormCard != null) vboxFormCard.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 8);");
        if (hboxFooter != null) hboxFooter.setStyle("-fx-background-color: " + (enabled ? "#2c2c2c" : "white") + "; -fx-background-radius: 0 0 15 15;");

        // Loop Label (Bold Semua)
        Label[] formLabels = {lblIdBarang, lblNamaBarang, lblIdKategori, lblStok, lblSatuan, lblHargaBeli, lblHargaJual};
        for (Label lbl : formLabels) {
            if (lbl != null) lbl.setStyle("-fx-font-family: 'Inter Medium'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        }

        String txtStyle = "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + borderColor + "; " +
                "-fx-background-color: " + (enabled ? "#2c2c2c" : "white") + "; " +
                "-fx-text-fill: " + textColor + "; -fx-prompt-text-fill: " + promptColor + ";";

        TextField[] fields = {txtIdBarang, txtNamaBarang, txtStok, txtHargaBeli, txtHargaJual};
        for (TextField f : fields) { if (f != null) f.setStyle(txtStyle); }

        if (cmbKategori != null) cmbKategori.setStyle(txtStyle);
        if (cbSatuan != null) cbSatuan.setStyle(txtStyle);
    }
}