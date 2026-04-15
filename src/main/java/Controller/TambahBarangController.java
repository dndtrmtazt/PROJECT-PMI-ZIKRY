package Controller;

import config.koneksi;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Barang;
import model.BarangDAO;
import model.Kategori;
import model.KategoriDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class TambahBarangController {

    @FXML private VBox paneRoot;
    @FXML private VBox vboxFormCard;
    @FXML private HBox hboxHeader, hboxFooter;
    @FXML private Label lblTitle, lblIdBarang, lblNamaBarang, lblIdKategori, lblStok, lblHargaBeli, lblHargaJual;
    @FXML private TextField txtIdBarang, txtNamaBarang, txtStok, txtHargaBeli, txtHargaJual;
    @FXML private ComboBox<String> cmbKategori;
    @FXML private Button btnTambah, btnBatal;

    @FXML
    public void initialize() {
        setDarkMode(MainController.isDarkMode);
        loadKategori();
        
        // Listener saat kategori dipilih
        cmbKategori.setOnAction(event -> {
            String selected = cmbKategori.getValue();
            if (selected != null && selected.contains(" - ")) {
                String fullIdKat = selected.split(" - ")[0];
                // Ambil 3 huruf pertama sebagai prefix (misal ESK000 -> ESK)
                if (fullIdKat.length() >= 3) {
                    String prefix = fullIdKat.substring(0, 3);
                    String nextId = BarangDAO.getNextBarangId(prefix);
                    txtIdBarang.setText(nextId);
                }
            }
        });
    }

    private void loadKategori() {
        cmbKategori.getItems().clear();
        List<Kategori> listKategori = KategoriDAO.getAllKategori();
        for (Kategori kat : listKategori) {
            cmbKategori.getItems().add(kat.getIdKategori() + " - " + kat.getNamaKategori());
        }
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

        // Update Labels Color
        Label[] formLabels = {lblIdBarang, lblNamaBarang, lblIdKategori, lblStok, lblHargaBeli, lblHargaJual};
        for (Label lbl : formLabels) {
            if (lbl != null) lbl.setStyle("-fx-font-family: 'Inter Medium'; -fx-font-size: 13; -fx-text-fill: " + textColor + ";");
        }

        String txtStyle = "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + borderColor + "; -fx-background-color: " + (enabled ? "#2c2c2c" : "white") + "; -fx-text-fill: " + textColor + ";";
        TextField[] fields = {txtIdBarang, txtNamaBarang, txtStok, txtHargaBeli, txtHargaJual};
        for (TextField f : fields) {
            if (f != null) f.setStyle(txtStyle);
        }
        if (cmbKategori != null) {
            cmbKategori.setStyle(txtStyle);
        }
    }

    @FXML
    private void handleSimpan() {
        if (isInputValid()) {
            String sql = "INSERT INTO barang (id_barang, nama_barang, id_kategori, stok, harga_beli, harga_jual) VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection conn = koneksi.koneksiDB();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                String selectedKategori = cmbKategori.getValue();
                String idKategori = selectedKategori.split(" - ")[0];

                pstmt.setString(1, txtIdBarang.getText());
                pstmt.setString(2, txtNamaBarang.getText());
                pstmt.setString(3, idKategori);
                pstmt.setInt(4, Integer.parseInt(txtStok.getText()));
                pstmt.setDouble(5, Double.parseDouble(txtHargaBeli.getText()));
                pstmt.setDouble(6, Double.parseDouble(txtHargaJual.getText()));

                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data Barang Berhasil Disimpan!");
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
        if (MainController.getInstance() != null) {
            MainController.getInstance().panggilHalaman("BarangView");
        }
    }

    private boolean isInputValid() {
        if (txtIdBarang.getText().isEmpty() || txtNamaBarang.getText().isEmpty() ||
                cmbKategori.getValue() == null ||
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
}
