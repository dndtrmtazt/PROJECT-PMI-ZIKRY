package Controller;

import config.koneksi;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
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
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

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
            updateGeneratedBarangId();
        }
    }

    private void updateGeneratedBarangId() {
        String idKategori = getSelectedKategoriId();
        if (idKategori.trim().isEmpty()) {
            txtIdBarang.clear();
            return;
        }

        String prefix = BarangDAO.getPrefixFromKategoriId(idKategori);
        txtIdBarang.setText(BarangDAO.getNextBarangId(prefix));
    }

    private String getSelectedKategoriId() {
        String selected = cmbKategori.getValue();
        if (selected == null || !selected.contains(" - ")) {
            return "";
        }

        return selected.split(" - ")[0].trim();
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
        if (cmbKategori != null) cmbKategori.setStyle(txtStyle);
        if (cbSatuan != null) cbSatuan.setStyle(txtStyle);

    }

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
                pstmt.setDouble(6, parseFormattedNumber(txtHargaBeli.getText()));
                pstmt.setDouble(7, parseFormattedNumber(txtHargaJual.getText()));

                pstmt.executeUpdate();
                showSuccessDialog("Berhasil ditambahkan");
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

        String idKategori = getSelectedKategoriId();
        if (!BarangDAO.isBarangIdMatchKategori(txtIdBarang.getText(), idKategori)) {
            showAlert(Alert.AlertType.WARNING, "Peringatan",
                    "ID Barang harus mengikuti prefix ID Kategori yang dipilih.");
            updateGeneratedBarangId();
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

    private void showSuccessDialog(String titleText) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (btnTambah != null && btnTambah.getScene() != null) {
            dialog.initOwner(btnTambah.getScene().getWindow());
        }
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setResizable(false);

        BorderPane root = new BorderPane();
        root.setPrefWidth(592);
        root.setPrefHeight(307);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-border-radius: 14;");

        VBox content = new VBox(22);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(34, 30, 28, 30));

        try {
            ImageView iconView = new ImageView(new Image(getClass().getResourceAsStream("/Images/iconsukses.png")));
            iconView.setFitWidth(74);
            iconView.setFitHeight(74);
            iconView.setPreserveRatio(true);
            content.getChildren().add(iconView);
        } catch (Exception ignored) {
            StackPane fallback = new StackPane();
            fallback.setPrefSize(74, 74);
            fallback.setStyle("-fx-background-color: #4A90E2; -fx-background-radius: 999;");
            Label check = new Label("✓");
            check.setStyle("-fx-text-fill: white; -fx-font-size: 30px; -fx-font-weight: bold;");
            fallback.getChildren().add(check);
            content.getChildren().add(fallback);
        }

        Label titleLabel = new Label(titleText);
        titleLabel.setStyle("-fx-text-fill: #111111; -fx-font-size: 28px; -fx-font-weight: bold;");
        content.getChildren().add(titleLabel);
        root.setCenter(content);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(22, 0, 22, 0));
        footer.setStyle("-fx-border-color: #D9D9D9 transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        Button btnOk = new Button("OK");
        btnOk.setPrefSize(150, 50);
        btnOk.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-size: 17px; -fx-font-weight: bold; -fx-cursor: hand;");
        btnOk.setOnAction(event -> dialog.close());
        footer.getChildren().add(btnOk);
        root.setBottom(footer);

        Scene scene = new Scene(root);
        scene.setFill(null);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
