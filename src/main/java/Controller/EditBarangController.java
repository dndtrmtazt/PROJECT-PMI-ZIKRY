package Controller;

import config.koneksi;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import DAO.KategoriDAO;
import model.Kategori;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import DAO.BarangDAO;

public class EditBarangController {

    @FXML private VBox paneRoot, vboxFormCard;
    @FXML private HBox hboxHeader, hboxFooter;
    @FXML private Label lblTitle, lblIdBarang, lblNamaBarang, lblIdKategori, lblStok, lblSatuan, lblHargaBeli, lblHargaJual;
    @FXML private TextField txtIdBarang, txtNamaBarang, txtStok, txtHargaBeli, txtHargaJual;
    @FXML private ComboBox<String> cmbKategori, cbSatuan; // <--- Sekarang pakai ComboBox
    @FXML private Button btnSimpan, btnBatal, btnHapus;

    private String idBarangAsli; // <--- Kunci buat nyimpen ID lama sebelum diedit
    private final NumberFormat numberFormat = NumberFormat.getIntegerInstance(new Locale("id", "ID"));

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
        setupCurrencyField(txtHargaBeli);
        setupCurrencyField(txtHargaJual);
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
        txtHargaBeli.setText(numberFormat.format((long) hBeli));
        txtHargaJual.setText(numberFormat.format((long) hJual));
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

    @FXML
    private void handleSimpan() {
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
                String idBaru = idBarangAsli;

                String idKat = cmbKategori.getValue().split(" - ")[0];

                pstmt.setString(1, idBaru);
                pstmt.setString(2, txtNamaBarang.getText());
                pstmt.setString(3, idKat);
                pstmt.setInt(4, Integer.parseInt(txtStok.getText()));
                pstmt.setString(5, cbSatuan.getValue());
                pstmt.setDouble(6, parseFormattedNumber(txtHargaBeli.getText()));
                pstmt.setDouble(7, parseFormattedNumber(txtHargaJual.getText()));
                pstmt.setString(8, idBarangAsli); // WHERE id_barang = id lama

                pstmt.executeUpdate();
                showSuccessDialog("Berhasil diperbarui");
                pindahKeHalamanUtama();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error Database", e.getMessage());
            }
        }
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

        Stage ownerStage = btnSimpan != null
                && btnSimpan.getScene() != null
                && btnSimpan.getScene().getWindow() instanceof Stage
                ? (Stage) btnSimpan.getScene().getWindow()
                : null;

        Stage dialog = new Stage();
        if (ownerStage != null) {
            dialog.initOwner(ownerStage);
            dialog.initModality(Modality.WINDOW_MODAL);
        } else {
            dialog.initModality(Modality.APPLICATION_MODAL);
        }
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setResizable(false);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent; -fx-padding: 8;");

        BorderPane card = new BorderPane();
        String dialogBg = darkMode ? "#1F1F1F" : "white";
        String titleColor = darkMode ? "white" : "#111111";
        String messageColor = darkMode ? "#D1D5DB" : "#4E4E4E";
        String closeColor = darkMode ? "#D1D5DB" : "#8B95A7";
        String closeHoverColor = "#E05A5A";
        String cancelBg = darkMode ? "#2C2C2C" : "#EFEFEF";
        String cancelText = darkMode ? "white" : "#111111";
        String cancelBorder = darkMode ? "#4B5563" : "#C6C6C6";
        String separatorColor = darkMode ? "#3A3A3A" : "#D9D9D9";
        card.setStyle("-fx-background-color: " + dialogBg + "; -fx-background-radius: 16; -fx-border-radius: 16; -fx-border-color: " + separatorColor + "; -fx-border-width: 1;");
        card.setPrefWidth(520);
        card.setPrefHeight(250);
        applyRoundedClip(card, 16);

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.setPadding(new Insets(12, 14, 0, 14));

        Button btnClose = new Button("\u2715");
        btnClose.setOnAction(event -> dialog.close());
        String closeBaseStyle = "-fx-background-color: transparent; -fx-text-fill: " + closeColor + "; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 10 6 10; -fx-background-radius: 8;";
        String closeHoverStyle = "-fx-background-color: rgba(224,90,90,0.10); -fx-text-fill: " + closeHoverColor + "; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 10 6 10; -fx-background-radius: 8;";
        btnClose.setStyle(closeBaseStyle);
        btnClose.setOnMouseEntered(event -> btnClose.setStyle(closeHoverStyle));
        btnClose.setOnMouseExited(event -> btnClose.setStyle(closeBaseStyle));
        topBar.getChildren().add(btnClose);
        card.setTop(topBar);

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
        card.setCenter(content);

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
        card.setBottom(bottomBar);
        root.getChildren().add(card);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        if (ownerStage != null) {
            dialog.setOnShown(event -> {
                dialog.setX(ownerStage.getX() + (ownerStage.getWidth() - dialog.getWidth()) / 2);
                dialog.setY(ownerStage.getY() + (ownerStage.getHeight() - dialog.getHeight()) / 2);
            });
        }
        dialog.showAndWait();

        return confirmed[0];
    }

    private void applyRoundedClip(Region region, double radius) {
        if (region == null) return;

        Rectangle clip = new Rectangle();
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        clip.widthProperty().bind(region.widthProperty());
        clip.heightProperty().bind(region.heightProperty());
        region.setClip(clip);
    }

    private void showSuccessDialog(String titleText) {
        SuccessDialogController.showDialog(
                btnSimpan == null || btnSimpan.getScene() == null ? null : btnSimpan.getScene().getWindow(),
                MainController.isDarkMode,
                titleText
        );
    }

    public void setDarkMode(boolean enabled) {
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#D1D5DB";
        String promptColor = enabled ? "#B0B0B0" : "#757575";

        setStyleClass(paneRoot, "dark", enabled);
        if (paneRoot != null) paneRoot.setStyle("-fx-background-color: " + bgMain + ";");
        if (hboxHeader != null) hboxHeader.setStyle("-fx-background-color: #4A76A8;");
        if (lblTitle != null) lblTitle.setStyle("-fx-text-fill: white;");

        if (vboxFormCard != null) vboxFormCard.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 8);");
        if (hboxFooter != null) hboxFooter.setStyle("-fx-background-color: " + (enabled ? "#1E1E1E" : "white") + "; -fx-background-radius: 0 0 15 15;");

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

        if (cmbKategori != null) {
            cmbKategori.setStyle(lockedStyle);
            setComboBoxTextColor(cmbKategori, enabled);
        }
        if (cbSatuan != null) {
            cbSatuan.setStyle(txtStyle);
            setComboBoxTextColor(cbSatuan, enabled);
        }
    }

    private void setComboBoxTextColor(ComboBox<String> comboBox, boolean darkMode) {
        if (!darkMode) {
            comboBox.setButtonCell(null);
            comboBox.setCellFactory(null);
            return;
        }

        comboBox.setButtonCell(createComboBoxButtonCell());
        comboBox.setCellFactory(listView -> createDarkComboBoxPopupCell());
    }

    private ListCell<String> createComboBoxButtonCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill: #F8FAFC; -fx-background-color: transparent; -fx-opacity: 1;");
            }
        };
    }

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
}
