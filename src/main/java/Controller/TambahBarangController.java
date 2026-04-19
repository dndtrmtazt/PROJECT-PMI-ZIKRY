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
import javafx.scene.control.ListCell;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class TambahBarangController {

    @FXML
    private VBox paneRoot;
    @FXML
    private VBox vboxFormCard;
    @FXML
    private HBox hboxHeader, hboxFooter;
    @FXML
    private Label lblTitle, lblIdBarang, lblNamaBarang, lblIdKategori, lblStok, lblHargaBeli, lblHargaJual;
    @FXML
    private TextField txtIdBarang, txtNamaBarang, txtStok, txtHargaBeli, txtHargaJual;
    @FXML
    private ComboBox<String> cmbKategori;
    @FXML
    private Button btnTambah, btnBatal;
    @FXML
    private ComboBox<String> cbSatuan;
    @FXML
    private Label lblSatuan;

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
        javafx.collections.ObservableList<String> listSatuan = javafx.collections.FXCollections.observableArrayList(
                "Pcs", "Liter", "Butir", "Kg", "Gram", "Box"
        );
        // PAKSA WARNA TEKS COMBOBOX BIAR IKUT DARK MODE
        setupComboBoxStyle(cbSatuan);
        setupComboBoxStyle(cmbKategori);


        cbSatuan.setItems(listSatuan);

        // (Opsional) Set nilai default biar user gak perlu klik lagi kalau mayoritas barang itu 'Pcs'
        cbSatuan.setValue("Pcs");
    }

    // Bikin method bantuan ini di bawah initialize
    private void setupComboBoxStyle(ComboBox<String> cb) {
        cb.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(cb.getPromptText());
                    setStyle("-fx-text-fill: derive(-fx-control-inner-background, -30%);"); // Warna prompt
                } else {
                    setText(item);
                    // CEK APAKAH LAGI DARK MODE
                    if (MainController.isDarkMode) {
                        setStyle("-fx-text-fill: white;");
                    } else {
                        setStyle("-fx-text-fill: #2C3E50;");
                    }
                }
            }
        });
    }

    private void loadKategori() {
        // 1. WAJIB: Bersihkan dulu isi dropdown-nya sebelum diisi ulang
        if (cmbKategori != null) {
            cmbKategori.getItems().clear();
        }

        List<Kategori> listKategori = KategoriDAO.getAllKategori();
        for (Kategori kat : listKategori) {
            cmbKategori.getItems().add(kat.getIdKategori() + " - " + kat.getNamaKategori());
        }
        if (!cmbKategori.getItems().isEmpty()) {
            cmbKategori.getSelectionModel().selectFirst();
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

        if (vboxFormCard != null)
            vboxFormCard.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 8);");
        if (hboxFooter != null)
            hboxFooter.setStyle("-fx-background-color: " + (enabled ? "#1e1e1e" : "#FFFFFF") + "; -fx-background-radius: 0 0 15 15;");

        // 1. Pastikan SEMUA label masuk ke dalam array ini
        Label[] formLabels = {
                lblIdBarang, lblNamaBarang, lblIdKategori,
                lblStok, lblSatuan, lblHargaBeli, lblHargaJual
        };

        for (Label lbl : formLabels) {
            if (lbl != null) {
                // 2. Tambahkan '-fx-font-weight: bold;' di dalam setStyle ini
                lbl.setStyle(
                        "-fx-font-family: 'Inter Medium'; " +
                                "-fx-font-size: 14px; " +
                                "-fx-font-weight: bold; " + // <--- INI KUNCINYA
                                "-fx-text-fill: " + textColor + ";"
                );
            }
        }

        // 1. Tentukan warna untuk Prompt Text (Tulisan bayangan di dalam ComboBox/TextField)
        // Kalau dark mode, kita kasih putih/abu terang biar kelihatan
        String promptColor = enabled ? "#B0B0B0" : "#757575";

        // 2. Tambahkan -fx-prompt-text-fill ke dalam txtStyle
        String txtStyle = "-fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: " + borderColor + "; " +
                "-fx-background-color: " + (enabled ? "#2c2c2c" : "white") + "; " +
                "-fx-text-fill: " + textColor + "; " + // Ini buat teks yang sudah dipilih
                "-fx-prompt-text-fill: " + (enabled ? "white" : "#757575") + ";"; // Ini buat prompt "Pcs"

        // 3. Terapkan ke semua TextField
        TextField[] fields = {txtIdBarang, txtNamaBarang, txtStok, txtHargaBeli, txtHargaJual};
        for (TextField f : fields) {
            if (f != null) f.setStyle(txtStyle);
        }

        // 4. Terapkan ke kedua ComboBox agar tulisannya terang
        if (cmbKategori != null) cmbKategori.setStyle(txtStyle);
        if (cbSatuan != null) cbSatuan.setStyle(txtStyle);

    }

    @FXML
    private void handleSimpan() {
        if (isInputValid()) {
            // 1. Tambahkan 'satuan' ke dalam list kolom dan tambahkan satu '?' lagi
            String sql = "INSERT INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = koneksi.koneksiDB();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                String selectedKategori = cmbKategori.getValue();
                String idKategori = selectedKategori.split(" - ")[0];

                // --- AMBIL NILAI DARI COMBOBOX SATUAN ---
                String satuan = cbSatuan.getValue();

                // 2. Sesuaikan nomor urut (indeks) pstmt
                pstmt.setString(1, txtIdBarang.getText());
                pstmt.setString(2, txtNamaBarang.getText());
                pstmt.setString(3, idKategori);
                pstmt.setInt(4, Integer.parseInt(txtStok.getText()));

                // --- SET DATA SATUAN (Indeks ke-5) ---
                pstmt.setString(5, satuan);

                // Indeks sisanya bergeser jadi 6 dan 7
                pstmt.setDouble(6, Double.parseDouble(txtHargaBeli.getText()));
                pstmt.setDouble(7, Double.parseDouble(txtHargaJual.getText()));

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
                cmbKategori.getValue() == null || cbSatuan.getValue() == null || // Tambahin cek satuan
                txtStok.getText().isEmpty() || txtHargaBeli.getText().isEmpty() || txtHargaJual.getText().isEmpty()) {

            showAlert(Alert.AlertType.WARNING, "Peringatan", "Semua data wajib diisi!");
            return false;
        }

        // CEK APAKAH STOK & HARGA BENARAN ANGKA
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
}
