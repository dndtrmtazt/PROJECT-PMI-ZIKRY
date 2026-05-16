package Controller;

import config.koneksi;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import DAO.BarangDAO;
import DAO.KategoriDAO;
import model.Kategori;
import javafx.scene.control.ListCell;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

// Controller form tambah barang, termasuk generate ID otomatis dan validasi input.
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

    private final NumberFormat numberFormat = NumberFormat.getIntegerInstance(new Locale("id", "ID"));

    @FXML
    public void initialize() {
        setDarkMode(MainController.isDarkMode);
        loadKategori();
        if (txtIdBarang != null) {
            txtIdBarang.setEditable(false);
        }

        // Listener saat kategori dipilih
        cmbKategori.setOnAction(event -> {
            updateGeneratedBarangId();
        });
        javafx.collections.ObservableList<String> listSatuan = javafx.collections.FXCollections.observableArrayList(
                "Pcs", "Liter", "Butir", "Kg", "Gram", "Box"
        );
        // PAKSA WARNA TEKS COMBOBOX BIAR IKUT DARK MODE
        setupComboBoxStyle(cbSatuan);
        setupComboBoxStyle(cmbKategori);


        cbSatuan.setItems(listSatuan);
        setupCurrencyField(txtHargaBeli);
        setupCurrencyField(txtHargaJual);

        // (Opsional) Set nilai default biar user gak perlu klik lagi kalau mayoritas barang itu 'Pcs'
        cbSatuan.setValue("Pcs");
    }

    // Menyiapkan warna ComboBox agar cocok dengan tema.
    private void setupComboBoxStyle(ComboBox<String> cb) {
        if (cb == null) {
            return;
        }

        boolean darkMode = MainController.isDarkMode;
        cb.setButtonCell(createComboBoxButtonCell(cb, darkMode));
        cb.setCellFactory(darkMode ? listView -> createDarkComboBoxPopupCell() : null);
    }

    // Mengatur tampilan item yang sedang dipilih di ComboBox.
    private ListCell<String> createComboBoxButtonCell(ComboBox<String> cb, boolean darkMode) {
        return new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(cb.getPromptText());
                    setStyle("-fx-text-fill: derive(-fx-control-inner-background, -30%);"); // Warna prompt
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: " + (darkMode ? "#F8FAFC" : "#2C3E50") + ";");
                }
            }
        };
    }

    // Mengatur tampilan daftar pilihan ComboBox saat popup dibuka.
    private ListCell<String> createDarkComboBoxPopupCell() {
        return new ListCell<>() {
            {
                hoverProperty().addListener((observable, oldValue, newValue) -> updateDarkPopupCellStyle());
                selectedProperty().addListener((observable, oldValue, newValue) -> updateDarkPopupCellStyle());
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                updateDarkPopupCellStyle();
            }

            private void updateDarkPopupCellStyle() {
                String background = isSelected() ? "#0EA5C6" : isHover() ? "#3A3A3A" : "#2C2C2C";
                setStyle("-fx-background-color: " + background + "; -fx-text-fill: #F8FAFC; -fx-opacity: 1;");
            }
        };
    }

    // Mengambil kategori dari database untuk pilihan kategori barang.
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
            updateGeneratedBarangId();
        }
    }

    // Membuat ID barang otomatis berdasarkan kategori yang dipilih.
    private void updateGeneratedBarangId() {
        String idKategori = getSelectedKategoriId();
        if (idKategori.trim().isEmpty()) {
            txtIdBarang.clear();
            return;
        }

        String prefix = BarangDAO.getPrefixFromKategoriId(idKategori);
        txtIdBarang.setText(BarangDAO.getNextBarangId(prefix));
    }

    // Mengambil ID kategori dari format ComboBox "ID - Nama".
    private String getSelectedKategoriId() {
        String selected = cmbKategori.getValue();
        if (selected == null || !selected.contains(" - ")) {
            return "";
        }

        return selected.split(" - ")[0].trim();
    }

    // Membuat field harga hanya menerima angka dan menampilkan pemisah ribuan.
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

    // Mengubah teks angka berformat ribuan menjadi angka double untuk database.
    private double parseFormattedNumber(String value) {
        String normalized = value == null ? "" : value.replace(".", "").replaceAll("[^0-9]", "").trim();
        return normalized.isEmpty() ? 0 : Double.parseDouble(normalized);
    }

    public void setDarkMode(boolean enabled) {
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#D1D5DB";

        setStyleClass(paneRoot, "dark", enabled);
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
        if (cmbKategori != null) {
            cmbKategori.setStyle(txtStyle);
            setupComboBoxStyle(cmbKategori);
        }
        if (cbSatuan != null) {
            cbSatuan.setStyle(txtStyle);
            setupComboBoxStyle(cbSatuan);
        }

    }

    // Helper untuk memasang atau melepas class CSS.
    private void setStyleClass(Node node, String styleClass, boolean enabled) {
        if (node == null) return;
        if (enabled) {
            if (!node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }
        } else {
            node.getStyleClass().remove(styleClass);
        }
    }

    @FXML
    // Validasi input lalu menyimpan barang baru ke database.
    private void handleSimpan() {
        if (isInputValid()) {

            String sql = "INSERT INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = koneksi.koneksiDB();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                String selectedKategori = cmbKategori.getValue();
                String idKategori = selectedKategori.split(" - ")[0];

                // --- AMBIL NILAI DARI COMBOBOX SATUAN ---
                String satuan = cbSatuan.getValue();

                // 2. Sesuaikan nomor urut (indeks) ps
                ps.setString(1, txtIdBarang.getText());
                ps.setString(2, txtNamaBarang.getText());
                ps.setString(3, idKategori);
                ps.setInt(4, Integer.parseInt(txtStok.getText()));

                // --- SET DATA SATUAN (Indeks ke-5) ---
                ps.setString(5, satuan);

                // Indeks sisanya bergeser jadi 6 dan 7
                ps.setDouble(6, parseFormattedNumber(txtHargaBeli.getText()));
                ps.setDouble(7, parseFormattedNumber(txtHargaJual.getText()));

                ps.executeUpdate();
                showSuccessDialog("Berhasil ditambahkan");
                pindahKeHalamanUtama();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error Database", "Gagal menyimpan ke database: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    // Membatalkan input dan kembali ke halaman data barang.
    private void handleBatal() {
        pindahKeHalamanUtama();
    }

    // Navigasi kembali ke halaman daftar barang.
    private void pindahKeHalamanUtama() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().panggilHalaman("BarangView");
        }
    }

    // Mengecek field wajib dan format angka sebelum simpan.
    private boolean isInputValid() {
        if (txtIdBarang.getText().isEmpty() || txtNamaBarang.getText().isEmpty() ||
                cmbKategori.getValue() == null || cbSatuan.getValue() == null || // Tambahin cek satuan
                txtStok.getText().isEmpty() || txtHargaBeli.getText().isEmpty() || txtHargaJual.getText().isEmpty()) {

            showAlert(Alert.AlertType.WARNING, "Peringatan", "Semua data wajib diisi!");
            return false;
        }

        // CEK APAKAH STOK & HARGA BENARAN ANGKA
        try {
            int stok = Integer.parseInt(txtStok.getText());
            double hargaBeli = parseFormattedNumber(txtHargaBeli.getText());
            double hargaJual = parseFormattedNumber(txtHargaJual.getText());

            if (stok < 0) {
                showAlert(Alert.AlertType.WARNING, "Peringatan", "Stok tidak boleh negatif!");
                return false;
            }

            if (hargaBeli < 0 || hargaJual < 0) {
                showAlert(Alert.AlertType.WARNING, "Peringatan", "Harga tidak boleh negatif!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Stok dan Harga harus berupa angka!");
            return false;
        }
        return true;
    }

    // Menampilkan alert validasi atau error.
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Menampilkan popup sukses setelah barang berhasil ditambahkan.
    private void showSuccessDialog(String title) {
        SuccessDialogController.showDialog(
                btnTambah == null || btnTambah.getScene() == null ? null : btnTambah.getScene().getWindow(),
                MainController.isDarkMode,
                title
        );
    }
}
