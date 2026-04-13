package Controller;

import config.koneksi;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TambahBarangController {

    @FXML private TextField txtIdBarang, txtNamaBarang, txtIdKategori, txtStok, txtSatuan, txtHargaBeli, txtHargaJual;
    @FXML private Button btnTambah, btnBatal;

    @FXML
    private void handleSimpan() {
        if (isInputValid()) {
            String sql = "INSERT INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = koneksi.koneksiDB();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, txtIdBarang.getText());
                pstmt.setString(2, txtNamaBarang.getText());
                pstmt.setString(3, txtIdKategori.getText());
                pstmt.setInt(4, Integer.parseInt(txtStok.getText()));
                pstmt.setString(5, txtSatuan.getText());
                pstmt.setDouble(6, Double.parseDouble(txtHargaBeli.getText()));
                pstmt.setDouble(7, Double.parseDouble(txtHargaJual.getText()));

                pstmt.executeUpdate();

                // Tampilkan Konfirmasi Berhasil
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data Barang Berhasil Disimpan!");

                // Otomatis pindah kembali ke halaman daftar barang
                pindahKeHalamanUtama();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error Database", "Gagal menyimpan ke database: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleBatal() {
        pindahKeHalamanUtama();
    }

    private void pindahKeHalamanUtama() {
        try {
            // Load file BarangView.fxml (Halaman Tabel)
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/BarangView.fxml"));

            // Cari kontainer contentArea dari MainLayout
            AnchorPane contentArea = (AnchorPane) btnBatal.getScene().lookup("#contentArea");

            if (contentArea != null) {
                contentArea.getChildren().setAll(root);

                // Set agar tampilan responsive memenuhi layar
                AnchorPane.setTopAnchor(root, 0.0);
                AnchorPane.setBottomAnchor(root, 0.0);
                AnchorPane.setLeftAnchor(root, 0.0);
                AnchorPane.setRightAnchor(root, 0.0);
            }
        } catch (IOException e) {
            System.err.println("Gagal memuat BarangView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isInputValid() {
        if (txtIdBarang.getText().isEmpty() || txtNamaBarang.getText().isEmpty() ||
                txtStok.getText().isEmpty() || txtHargaBeli.getText().isEmpty() || txtHargaJual.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Semua data wajib diisi!");
            return false;
        }
        try {
            Integer.parseInt(txtStok.getText());
            Double.parseDouble(txtHargaBeli.getText());
            Double.parseDouble(txtHargaJual.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Format Salah", "Stok dan Harga harus berupa angka!");
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
}
