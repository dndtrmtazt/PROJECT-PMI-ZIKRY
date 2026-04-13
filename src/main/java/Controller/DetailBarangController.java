package Controller;

import config.koneksi;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DetailBarangController {

    @FXML private Label lblHeaderDetail;
    @FXML private TextField txtIdBarang, txtNamaBarang, txtIdKategori, txtStok, txtSatuan, txtHargaBeli, txtHargaJual;
    @FXML private Button btnBatal, btnSimpan, btnHapus;

    /**
     * FUNGSI UTAMA: Menampilkan data dari tabel ke dalam form.
     */
    public void initData(String id, String nama, String kategori, int stok, String satuan, double hBeli, double hJual) {
        lblHeaderDetail.setText("Detail Barang: " + id);

        txtIdBarang.setText(id);
        txtNamaBarang.setText(nama);
        txtIdKategori.setText(kategori);
        txtStok.setText(String.valueOf(stok));
        txtSatuan.setText(satuan);

        // Untuk editing, kita masukkan angka murni saja tanpa "Rp" agar mudah di-parse
        txtHargaBeli.setText(String.valueOf((long)hBeli));
        txtHargaJual.setText(String.valueOf((long)hJual));
    }

    /**
     * LOGIKA SIMPAN (UPDATE): Menyimpan perubahan data ke Database.
     */
    @FXML
    private void handleSimpan() {
        String sql = "UPDATE barang SET nama_barang=?, id_kategori=?, stok=?, satuan=?, harga_beli=?, harga_jual=? WHERE id_barang=?";

        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtNamaBarang.getText());
            pstmt.setString(2, txtIdKategori.getText());
            pstmt.setInt(3, Integer.parseInt(txtStok.getText()));
            pstmt.setString(4, txtSatuan.getText());
            pstmt.setDouble(5, Double.parseDouble(txtHargaBeli.getText()));
            pstmt.setDouble(6, Double.parseDouble(txtHargaJual.getText()));
            pstmt.setString(7, txtIdBarang.getText());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                showAlert("Sukses", "Data barang berhasil diperbarui!");
                pindahKeHalamanUtama();
            }

        } catch (SQLException e) {
            showAlert("Error Database", "Gagal update data: " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert("Error Input", "Stok dan Harga harus berupa angka!");
        }
    }

    /**
     * LOGIKA HAPUS: Menghapus data berdasarkan ID.
     */
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

                    pstmt.setString(1, txtIdBarang.getText());
                    pstmt.executeUpdate();

                    showAlert("Sukses", "Barang berhasil dihapus!");
                    pindahKeHalamanUtama();

                } catch (SQLException e) {
                    showAlert("Error", "Gagal menghapus data: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleBatal(ActionEvent event) {
        pindahKeHalamanUtama();
    }

    /**
     * HELPER: Kembali ke tabel utama dengan Layout FULL (Fix Menciut).
     */
    private void pindahKeHalamanUtama() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/BarangView.fxml"));

            // Cari panggung tengah di MainLayout
            AnchorPane contentArea = (AnchorPane) lblHeaderDetail.getScene().lookup("#contentArea");

            if (contentArea != null) {
                contentArea.getChildren().setAll(root);

                // --- INI SOLUSI LAYOUT MENCIUT ---
                // Paksa halaman tabel nempel ke setiap pojok contentArea
                AnchorPane.setTopAnchor(root, 0.0);
                AnchorPane.setBottomAnchor(root, 0.0);
                AnchorPane.setLeftAnchor(root, 0.0);
                AnchorPane.setRightAnchor(root, 0.0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error Navigasi", "Gagal memuat halaman tabel: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}