package Controller;

import config.koneksi;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import dao.KategoriDAO;
import model.Kategori;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import dao.BarangDAO;

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
        if (txtIdBarang != null) {
            txtIdBarang.setEditable(false);
            txtIdBarang.setFocusTraversable(false);
        }
        if (cmbKategori != null) {
            cmbKategori.setDisable(true);
            cmbKategori.setFocusTraversable(false);
        }

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

    private String getSelectedKategoriId() {
        String selectedKategori = cmbKategori.getValue();
        if (selectedKategori == null || !selectedKategori.contains(" - ")) {
            return "";
        }

        return selectedKategori.split(" - ")[0].trim();
    }

    private void loadKategori() {
        if (cmbKategori != null) {
            cmbKategori.getItems().clear();
        }

        // Ambil data terbaru dari database
        List<Kategori> list = KategoriDAO.getAllKategori();
        for (model.Kategori k : list) {
            cmbKategori.getItems().add(k.getIdKategori() + " - " + k.getNamaKategori());
        }
    }

    @FXML
    private void handleSimpan() {
        String idBaru = idBarangAsli;

        // 1. CEK DUPLIKAT JIKA ID DIUBAH
        if (!idBaru.equals(idBarangAsli)) {
            if (isIdExists(idBaru)) {
                showAlert(Alert.AlertType.ERROR, "Kode Digunakan",
                        "Kode barang '" + idBaru + "' sudah ada di database. Gunakan kode lain!");
                return;
            }
        }

        if (isInputValid()) {
            boolean confirmed = showCustomConfirmationDialog(
                    "Konfirmasi perubahan?",
                    "Anda yakin ingin menyimpan perubahan detail barang ini?",
                    "Simpan",
                    "#5AC463"
            );
            if (!confirmed) {
                return;
            }

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
                showSuccessDialog("Berhasil diperbarui");
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
        boolean confirmed = showCustomConfirmationDialog(
                "Konfirmasi Hapus?",
                "Anda yakin ingin menghapus barang ini?",
                "Hapus",
                "#FF5757"
        );
        if (!confirmed) {
            return;
        }

        String sql = "DELETE FROM barang WHERE id_barang = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idBarangAsli);
            pstmt.executeUpdate();
            showSuccessDialog("Berhasil dihapus");
            pindahKeHalamanUtama();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal hapus: " + e.getMessage());
        }
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

        try {
            Integer.parseInt(txtStok.getText());
            Double.parseDouble(txtHargaBeli.getText());
            Double.parseDouble(txtHargaJual.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Stok dan Harga harus berupa angka!");
            return false;
        }

        if (!BarangDAO.isBarangIdMatchKategori(txtIdBarang.getText(), getSelectedKategoriId())) {
            showAlert(Alert.AlertType.WARNING, "Peringatan",
                    "ID Barang harus sesuai dengan prefix ID Kategori yang dipilih.");
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

    private boolean showCustomConfirmationDialog(String title, String message, String confirmText, String confirmColor) {
        final boolean[] confirmed = {false};
        boolean darkMode = MainController.isDarkMode;

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (btnSimpan != null && btnSimpan.getScene() != null) {
            dialog.initOwner(btnSimpan.getScene().getWindow());
        }
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setResizable(false);

        BorderPane root = new BorderPane();
        String dialogBg = darkMode ? "#1F1F1F" : "white";
        String titleColor = darkMode ? "white" : "#111111";
        String messageColor = darkMode ? "#D1D5DB" : "#4E4E4E";
        String closeColor = darkMode ? "#D1D5DB" : "#9E9E9E";
        String cancelBg = darkMode ? "#2C2C2C" : "#EFEFEF";
        String cancelText = darkMode ? "white" : "#111111";
        String cancelBorder = darkMode ? "#4B5563" : "#C6C6C6";
        String separatorColor = darkMode ? "#3A3A3A" : "#D9D9D9";
        root.setStyle("-fx-background-color: " + dialogBg + "; -fx-background-radius: 14; -fx-border-radius: 14; -fx-border-color: " + separatorColor + "; -fx-border-width: 1;");
        root.setPrefWidth(520);
        root.setPrefHeight(250);

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.setPadding(new Insets(12, 14, 0, 14));

        Button btnClose = new Button("×");
        btnClose.setOnAction(event -> dialog.close());
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: " + closeColor + "; -fx-font-size: 24px; -fx-cursor: hand; -fx-padding: 0;");
        topBar.getChildren().add(btnClose);
        root.setTop(topBar);

        HBox content = new HBox(20);
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(4, 34, 20, 34));

        StackPane iconWrapper = new StackPane();
        iconWrapper.setMinSize(62, 62);
        iconWrapper.setPrefSize(62, 62);
        iconWrapper.setMaxSize(62, 62);

        try {
            ImageView iconView = new ImageView(new Image(getClass().getResourceAsStream("/Images/ICON36.png")));
            iconView.setFitWidth(62);
            iconView.setFitHeight(62);
            iconView.setPreserveRatio(true);
            iconWrapper.getChildren().add(iconView);
        } catch (Exception ignored) {
            Label fallback = new Label("!");
            fallback.setStyle("-fx-text-fill: white; -fx-font-size: 42px; -fx-font-weight: bold;");
            iconWrapper.getChildren().add(fallback);
        }

        VBox textBox = new VBox(14);
        textBox.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + titleColor + "; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: " + messageColor + "; -fx-font-size: 14px;");

        textBox.getChildren().addAll(titleLabel, messageLabel);
        content.getChildren().addAll(iconWrapper, textBox);
        root.setCenter(content);

        HBox bottomBar = new HBox(24);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setPadding(new Insets(16, 26, 16, 26));
        bottomBar.setStyle("-fx-border-color: " + separatorColor + " transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnCancel = new Button("Batal");
        btnCancel.setOnAction(event -> dialog.close());
        btnCancel.setPrefSize(128, 44);
        btnCancel.setStyle("-fx-background-color: " + cancelBg + "; -fx-text-fill: " + cancelText + "; -fx-background-radius: 10; -fx-border-color: " + cancelBorder + "; -fx-border-radius: 10; -fx-font-size: 15px; -fx-cursor: hand;");

        Button btnConfirm = new Button(confirmText);
        btnConfirm.setOnAction(event -> {
            confirmed[0] = true;
            dialog.close();
        });
        btnConfirm.setPrefSize(128, 44);
        btnConfirm.setStyle("-fx-background-color: " + confirmColor + "; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand;");

        bottomBar.getChildren().addAll(spacer, btnCancel, btnConfirm);
        root.setBottom(bottomBar);

        Scene scene = new Scene(root);
        scene.setFill(null);
        dialog.setScene(scene);
        dialog.showAndWait();

        return confirmed[0];
    }

    private void showSuccessDialog(String titleText) {
        boolean darkMode = MainController.isDarkMode;
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (btnSimpan != null && btnSimpan.getScene() != null) {
            dialog.initOwner(btnSimpan.getScene().getWindow());
        }
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setResizable(false);

        BorderPane root = new BorderPane();
        root.setPrefWidth(520);
        root.setPrefHeight(250);
        String dialogBg = darkMode ? "#1F1F1F" : "white";
        String titleColor = darkMode ? "white" : "#111111";
        String separatorColor = darkMode ? "#3A3A3A" : "#D9D9D9";
        root.setStyle("-fx-background-color: " + dialogBg + "; -fx-background-radius: 14; -fx-border-radius: 14; -fx-border-color: " + separatorColor + "; -fx-border-width: 1;");

        VBox content = new VBox(18);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(28, 24, 22, 24));

        try {
            ImageView iconView = new ImageView(new Image(getClass().getResourceAsStream("/Images/iconsukses.png")));
            iconView.setFitWidth(64);
            iconView.setFitHeight(64);
            iconView.setPreserveRatio(true);
            content.getChildren().add(iconView);
        } catch (Exception ignored) {
            StackPane fallback = new StackPane();
            fallback.setPrefSize(64, 64);
            fallback.setStyle("-fx-background-color: #4A90E2; -fx-background-radius: 999;");
            Label check = new Label("✓");
            check.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;");
            fallback.getChildren().add(check);
            content.getChildren().add(fallback);
        }

        Label titleLabel = new Label(titleText);
        titleLabel.setStyle("-fx-text-fill: " + titleColor + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        content.getChildren().add(titleLabel);
        root.setCenter(content);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(18, 0, 18, 0));
        footer.setStyle("-fx-border-color: " + separatorColor + " transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        Button btnOk = new Button("OK");
        btnOk.setPrefSize(138, 46);
        btnOk.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");
        btnOk.setOnAction(event -> dialog.close());
        footer.getChildren().add(btnOk);
        root.setBottom(footer);

        Scene scene = new Scene(root);
        scene.setFill(null);
        dialog.setScene(scene);
        dialog.showAndWait();
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
        String lockedStyle = "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + borderColor + "; " +
                "-fx-background-color: " + (enabled ? "#262626" : "#F3F4F6") + "; " +
                "-fx-text-fill: " + textColor + "; -fx-opacity: 1;";

        TextField[] fields = {txtIdBarang, txtNamaBarang, txtStok, txtHargaBeli, txtHargaJual};
        for (TextField f : fields) { if (f != null) f.setStyle(txtStyle); }

        if (txtIdBarang != null) txtIdBarang.setStyle(lockedStyle);

        if (cmbKategori != null) cmbKategori.setStyle(lockedStyle);
        if (cbSatuan != null) cbSatuan.setStyle(txtStyle);
    }
}
