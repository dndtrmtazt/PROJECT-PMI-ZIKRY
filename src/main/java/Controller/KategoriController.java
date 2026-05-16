package Controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // Tambahkan ini
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent; // Tambahkan ini
import javafx.scene.Scene; // Tambahkan ini
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality; // Tambahkan ini
import javafx.stage.Stage; // Tambahkan ini
import javafx.stage.StageStyle;
import DAO.KategoriDAO;
import model.Kategori;
import java.io.InputStream;

public class KategoriController implements Initializable {
    private static final String EDIT_ICON_PATH = "/Images/icon_edit.png";
    private static final String DELETE_ICON_PATH = "/Images/icon_hapus.png";

    @FXML private VBox paneRoot, vboxKategoriList, vboxContent;
    @FXML private HBox vboxHeader, hboxSearch, hboxTableHead;
    @FXML private Label lblTitle, lblDaftarKategori;
    @FXML private TextField txtSearchKategori;
    @FXML private ScrollPane scrollKategori;
    @FXML private ImageView imgLightMode, imgDarkMode, imgLogout;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadDataKategori();
        setupSearchKategori();
        // Pastikan MainController.isDarkMode sudah terdefinisi
        setDarkMode(MainController.isDarkMode);
    }

    private void setupSearchKategori() {
        if (txtSearchKategori != null) {
            txtSearchKategori.textProperty().addListener((obs, oldValue, newValue) -> applyKategoriFilter(newValue));
        }
    }

    private void applyKategoriFilter(String keywordText) {
        String keyword = keywordText == null ? "" : keywordText.trim().toLowerCase();
        List<Kategori> list = KategoriDAO.getAllKategori();

        if (!keyword.isEmpty()) {
            list = list.stream()
                    .filter(k -> matchesSearch(k, keyword))
                    .collect(Collectors.toList());
        }

        tampilkanDataKategori(list);
    }

    private boolean matchesSearch(Kategori kategori, String keyword) {
        String idKategori = kategori.getIdKategori() == null ? "" : kategori.getIdKategori().toLowerCase();
        String namaKategori = kategori.getNamaKategori() == null ? "" : kategori.getNamaKategori().toLowerCase();
        return idKategori.contains(keyword) || namaKategori.contains(keyword);
    }

    // --- 1. PERBAIKAN TOMBOL TAMBAH ---
    @FXML
    private void handleTambahKategori() {
        showKategoriDialog(null); // Membuka dialog kosong
    }

    // --- 2. PERBAIKAN TOMBOL EDIT ---
    private void handleEdit(Kategori k) {
        showKategoriDialog(k); // Membuka dialog dengan data
    }

    private void showKategoriDialog(Kategori k) {
        try {
            // PERBAIKAN: Sesuai struktur folder di image_e93605.png
            URL fxmlLocation = getClass().getResource("/FXML/Admin/FormKategori.fxml");

            if (fxmlLocation == null) {
                throw new java.io.FileNotFoundException("File FormKategori.fxml tidak ada di resources/FXML/Admin/");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            FormKategoriController controller = loader.getController();
            controller.setData(k);

            Stage stage = new Stage();
            stage.setTitle(k == null ? "Tambah Data Kategori" : "Edit Data Kategori");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadDataKategori();
        } catch (Exception e) {
            System.err.println("Gagal membuka Form Kategori: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDataKategori() {
        applyKategoriFilter(txtSearchKategori == null ? "" : txtSearchKategori.getText());
    }

    private void tampilkanDataKategori(List<Kategori> list) {
        if (vboxKategoriList == null) return;

        vboxKategoriList.getChildren().clear();
        boolean isDark = MainController.isDarkMode;
        String textColor = isDark ? "white" : "#2C3E50";
        String rowBg = isDark ? "#1e1e1e" : "#FFFFFF";
        String borderColor = isDark ? "#333333" : "#E0E0E0";

        if (list.isEmpty()) {
            vboxKategoriList.getChildren().add(createKategoriPlaceholder(isDark));
            return;
        }

        int no = 1;
        for (Kategori k : list) {
            HBox baris = new HBox(10);
            baris.setAlignment(Pos.CENTER_LEFT);
            baris.setPadding(new Insets(10, 15, 10, 15));
            baris.setStyle("-fx-background-color: " + rowBg + "; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0;");

            Label lblNo = new Label(String.valueOf(no++));
            lblNo.setMinWidth(91.0); lblNo.setStyle("-fx-text-fill: " + textColor + ";");

            Label lblId = new Label(k.getIdKategori());
            lblId.setMinWidth(199.0); lblId.setStyle("-fx-text-fill: " + textColor + ";");

            Label lblNama = new Label(k.getNamaKategori());
            lblNama.setMinWidth(236.0); lblNama.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox actionBox = new HBox(10);
            actionBox.setMinWidth(180.0);
            actionBox.setAlignment(Pos.CENTER);

            Button btnEdit = createActionButton("Edit", "#3498DB", EDIT_ICON_PATH);
            btnEdit.setOnAction(e -> handleEdit(k)); // Listener klik Edit

            Button btnHapus = createActionButton("Hapus", "#E74C3C", DELETE_ICON_PATH);
            btnHapus.setOnAction(e -> handleHapus(k)); // Listener klik Hapus

            actionBox.getChildren().addAll(btnEdit, btnHapus);
            baris.getChildren().addAll(lblNo, lblId, lblNama, spacer, actionBox);
            vboxKategoriList.getChildren().add(baris);
        }
    }

    private StackPane createKategoriPlaceholder(boolean isDark) {
        Label placeholder = new Label("Kategori tidak ditemukan");
        placeholder.setStyle("-fx-text-fill: " + (isDark ? "white" : "black") + ";");

        StackPane placeholderPane = new StackPane(placeholder);
        placeholderPane.setAlignment(Pos.CENTER);
        placeholderPane.setMinHeight(getKategoriPlaceholderHeight());
        placeholderPane.setPrefHeight(getKategoriPlaceholderHeight());
        placeholderPane.setMaxWidth(Double.MAX_VALUE);
        return placeholderPane;
    }

    private double getKategoriPlaceholderHeight() {
        if (scrollKategori != null && scrollKategori.getViewportBounds().getHeight() > 0) {
            return scrollKategori.getViewportBounds().getHeight();
        }
        return 520.0;
    }

    private void handleHapus(Kategori k) {
        if (showDeleteConfirmationDialog()) {
            // Pastikan method ini ada di DAO kamu
            if (KategoriDAO.deleteKategori(k.getIdKategori())) {
                loadDataKategori(); // Refresh tampilan
            } else {
                System.err.println("Gagal menghapus data dari DB.");
            }
        }
    }

    private boolean showDeleteConfirmationDialog() {
        return ConfirmDeleteDialogController.showDialog(
                paneRoot == null || paneRoot.getScene() == null ? null : paneRoot.getScene().getWindow(),
                MainController.isDarkMode,
                "Konfirmasi Hapus?",
                "Anda yakin ingin menghapus kategori ini?",
                "Hapus"
        );
    }

    private Button createActionButton(String text, String color, String iconPath) {
        Button btn = new Button(text);
        btn.setMinWidth(Region.USE_PREF_SIZE);
        InputStream iconStream = iconPath == null ? null : getClass().getResourceAsStream(iconPath);
        if (iconStream != null) {
            ImageView iv = new ImageView(new Image(iconStream));
            iv.setFitHeight(14); iv.setFitWidth(14); iv.setPreserveRatio(true);
            btn.setGraphic(iv);
            btn.setGraphicTextGap(5);
        }
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 5 12 5 12;");
        return btn;
    }

    public void setDarkMode(boolean enabled) {
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1E1E1E" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#E0E0E0";
        String headerBg = enabled ? "#2C2C2C" : "#F8FAFC";
        String inputBg = enabled ? "#2C2C2C" : "white";

        setStyleClass(paneRoot, "dark", enabled);
        setStyleClass(scrollKategori, "dark", enabled);
        if (paneRoot != null) paneRoot.setStyle("-fx-background-color: " + bgMain + ";");
        if (vboxHeader != null) vboxHeader.setStyle("-fx-background-color: #4A76A8;");
        if (lblTitle != null) lblTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        if (hboxSearch != null) {
            hboxSearch.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-border-color: " + borderColor + "; -fx-border-radius: 10;");
        }
        if (txtSearchKategori != null) {
            txtSearchKategori.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-background-color: " + inputBg + "; -fx-border-color: " + borderColor + "; -fx-text-fill: " + textColor + "; -fx-prompt-text-fill: " + (enabled ? "#A1A1AA" : "#9CA3AF") + ";");
        }
        if (vboxContent != null) {
            vboxContent.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-border-color: " + borderColor + "; -fx-border-radius: 10;");
        }
        if (lblDaftarKategori != null) {
            lblDaftarKategori.setStyle("-fx-font-family: 'Inter Medium'; -fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        }
        if (hboxTableHead != null) {
            hboxTableHead.setStyle("-fx-background-color: " + headerBg + "; -fx-background-radius: 5; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0;");
            hboxTableHead.getChildren().forEach(node -> {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    label.setStyle("-fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
                }
            });
        }
        loadDataKategori();
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
