package Controller;

import config.koneksi;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EditBarangController {

    @FXML private VBox paneRoot, vboxFormCard;
    @FXML private HBox hboxHeader, hboxFooter;
    @FXML private Label lblTitle, lblIdBarang, lblNamaBarang, lblIdKategori, lblStok, lblHargaBeli, lblHargaJual;
    @FXML private TextField txtIdBarang, txtNamaBarang, txtIdKategori, txtStok, txtHargaBeli, txtHargaJual;
    @FXML private Button btnSimpan, btnBatal, btnHapus;

    @FXML
    public void initialize() {
        setDarkMode(MainController.isDarkMode);
    }

    public void initData(String id, String nama, String kategori, int stok, double hBeli, double hJual) {
        txtIdBarang.setText(id);
        txtNamaBarang.setText(nama);
        txtIdKategori.setText(kategori);
        txtStok.setText(String.valueOf(stok));
        txtHargaBeli.setText(String.valueOf((long)hBeli));
        txtHargaJual.setText(String.valueOf((long)hJual));
    }

    @FXML
    private void handleSimpan() {
        if (isInputValid()) {
            String sql = "UPDATE barang SET nama_barang=?, id_kategori=?, stok=?, harga_beli=?, harga_jual=? WHERE id_barang=?";

            try (Connection conn = koneksi.koneksiDB();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, txtNamaBarang.getText());
                pstmt.setString(2, txtIdKategori.getText());
                pstmt.setInt(3, Integer.parseInt(txtStok.getText()));
                pstmt.setDouble(4, Double.parseDouble(txtHargaBeli.getText()));
                pstmt.setDouble(5, Double.parseDouble(txtHargaJual.getText()));
                pstmt.setString(6, txtIdBarang.getText());

                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data Barang Berhasil Diperbarui!");
                pindahKeHalamanUtama();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error Database", "Gagal memperbarui data: " + e.getMessage());
            }
        }
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

                    pstmt.setString(1, txtIdBarang.getText());
                    pstmt.executeUpdate();

                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Barang berhasil dihapus!");
                    pindahKeHalamanUtama();

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Gagal menghapus data: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleBatal() {
        pindahKeHalamanUtama();
    }

    private void pindahKeHalamanUtama() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().panggilHalaman("BarangView");
        }
    }

    private boolean isInputValid() {
        if (txtNamaBarang.getText().isEmpty() || txtStok.getText().isEmpty() || 
            txtHargaBeli.getText().isEmpty() || txtHargaJual.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Semua data wajib diisi!");
            return false;
        }
        try {
            Integer.parseInt(txtStok.getText());
            Double.parseDouble(txtHargaBeli.getText());
            Double.parseDouble(txtHargaJual.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Stok dan Harga harus berupa angka!");
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

        if (paneRoot != null) paneRoot.setStyle("-fx-background-color: " + bgMain + ";");
        if (hboxHeader != null) hboxHeader.setStyle("-fx-background-color: #4A76A8;");
        if (lblTitle != null) lblTitle.setStyle("-fx-text-fill: white;");

        if (vboxFormCard != null) vboxFormCard.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 8);");
        if (hboxFooter != null) hboxFooter.setStyle("-fx-background-color: " + (enabled ? "#2c2c2c" : "#F9FAFB") + "; -fx-background-radius: 0 0 15 15;");

        Label[] formLabels = {lblIdBarang, lblNamaBarang, lblIdKategori, lblStok, lblHargaBeli, lblHargaJual};
        for (Label lbl : formLabels) {
            if (lbl != null) lbl.setStyle("-fx-font-family: 'Inter Medium'; -fx-font-size: 13; -fx-text-fill: " + textColor + ";");
        }

        String txtStyle = "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + borderColor + "; -fx-background-color: " + (enabled ? "#2c2c2c" : "white") + "; -fx-text-fill: " + textColor + ";";
        TextField[] fields = {txtIdBarang, txtNamaBarang, txtIdKategori, txtStok, txtHargaBeli, txtHargaJual};
        for (TextField f : fields) {
            if (f != null) {
                if (f == txtIdBarang || f == txtIdKategori) {
                    f.setStyle(txtStyle + " -fx-background-color: " + (enabled ? "#333333" : "#EEEEEE") + ";");
                } else {
                    f.setStyle(txtStyle);
                }
            }
        }
    }
}
