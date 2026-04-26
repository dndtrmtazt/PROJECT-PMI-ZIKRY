package Controller;

import config.koneksi;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

public class DetailBarangController {

    @FXML private Label lblHeaderDetail;
    @FXML private TextField txtIdBarang, txtNamaBarang, txtIdKategori, txtStok, txtHargaBeli, txtHargaJual;
    @FXML private Button btnBatal, btnSimpan, btnHapus;
    private final NumberFormat numberFormat = NumberFormat.getIntegerInstance(new Locale("id", "ID"));

    /**
     * FUNGSI UTAMA: Menampilkan data dari tabel ke dalam form.
     */
    public void initData(String id, String nama, String kategori, int stok, double hBeli, double hJual) {
        lblHeaderDetail.setText("Detail Barang: " + id);

        txtIdBarang.setText(id);
        txtNamaBarang.setText(nama);
        txtIdKategori.setText(kategori);
        txtStok.setText(String.valueOf(stok));

        // Untuk editing, kita masukkan angka murni saja tanpa "Rp" agar mudah di-parse
        txtHargaBeli.setText(numberFormat.format((long) hBeli));
        txtHargaJual.setText(numberFormat.format((long) hJual));
    }

    /**
     * LOGIKA SIMPAN (UPDATE): Menyimpan perubahan data ke Database.
     */
    @FXML
    private void handleSimpan() {
        String sql = "UPDATE barang SET nama_barang=?, id_kategori=?, stok=?, harga_beli=?, harga_jual=? WHERE id_barang=?";

        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int stok = Integer.parseInt(txtStok.getText());
            double hargaBeli = parseFormattedNumber(txtHargaBeli.getText());
            double hargaJual = parseFormattedNumber(txtHargaJual.getText());

            if (stok < 0) {
                showAlert("Error Input", "Stok tidak boleh negatif!");
                return;
            }

            if (hargaBeli < 0 || hargaJual < 0) {
                showAlert("Error Input", "Harga tidak boleh negatif!");
                return;
            }

            pstmt.setString(1, txtNamaBarang.getText());
            pstmt.setString(2, txtIdKategori.getText());
            pstmt.setInt(3, stok);
            pstmt.setDouble(4, hargaBeli);
            pstmt.setDouble(5, hargaJual);
            pstmt.setString(6, txtIdBarang.getText());

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
    public void initialize() {
        setupCurrencyField(txtHargaBeli);
        setupCurrencyField(txtHargaJual);
        setDarkMode(MainController.isDarkMode);
    }

    private void setupCurrencyField(TextField field) {
        if (field == null) {
            return;
        }

        field.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            String digitsOnly = newText.replaceAll("[^0-9]", "");

            if (digitsOnly.isEmpty()) {
                change.setText("");
                change.setRange(0, change.getControlText().length());
                return change;
            }

            try {
                long value = Long.parseLong(digitsOnly);
                String formatted = numberFormat.format(value);
                change.setText(formatted);
                change.setRange(0, change.getControlText().length());
                return change;
            } catch (NumberFormatException e) {
                return null;
            }
        }));
    }

    private double parseFormattedNumber(String value) {
        String normalized = value == null ? "" : value.replace(".", "").replaceAll("[^0-9]", "").trim();
        return normalized.isEmpty() ? 0 : Double.parseDouble(normalized);
    }

    public void setDarkMode(boolean enabled) {
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#D1D5DB";

        // Assuming paneRoot exists or we use the parent of lblHeaderDetail
        if (lblHeaderDetail != null && lblHeaderDetail.getScene() != null) {
             Node root = lblHeaderDetail.getScene().getRoot();
             if (root != null) root.setStyle("-fx-background-color: " + bgMain + ";");
        }
        
        // Update Labels Color
        Label[] formLabels = {lblHeaderDetail};
        for (Label lbl : formLabels) {
            if (lbl != null) lbl.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
        }

        String txtStyle = "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + borderColor + "; -fx-background-color: " + (enabled ? "#2c2c2c" : "white") + "; -fx-text-fill: " + textColor + ";";
        TextField[] fields = {txtIdBarang, txtNamaBarang, txtIdKategori, txtStok, txtHargaBeli, txtHargaJual};
        for (TextField f : fields) {
            if (f != null) f.setStyle(txtStyle);
        }
    }

    @FXML
    private void handleBatal(ActionEvent event) {
        pindahKeHalamanUtama();
    }

    /**
     * HELPER: Kembali ke tabel utama dengan Layout FULL (Fix Menciut).
     */
    private void pindahKeHalamanUtama() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().panggilHalaman("BarangView");
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
