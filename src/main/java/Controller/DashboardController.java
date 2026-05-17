package Controller;

// --- 1. IMPORT TOOLS (Alat bantu untuk UI dan Data) ---
import DAO.BarangDAO;
import DAO.PengeluaranDAO;
import DAO.TransaksiDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import model.Barang;

// --- 2. CLASS HEADER (Rumah utama kodingan dashboard) ---
// Controller dashboard admin untuk ringkasan penjualan, pengeluaran, transaksi, dan stok menipis.
public class DashboardController implements Initializable {
    private static final int BATAS_STOK_HAMPIR_HABIS = 5;
    private static final int LIMIT_STOK_HAMPIR_HABIS = 5;

    @FXML private VBox paneRoot, stokListContainer;
    @FXML private HBox hboxHeader;
    @FXML private Label lblDashboard, lblCard1Title, lblCard1Value, lblCard2Title, lblCard2Value, lblCard3Title, lblCard3Value, lblCard4Title;
    @FXML private VBox card1, card2, card3, card4;
    @FXML private Button btnCard1, btnCard2, btnCard4, btnLogout;
    @FXML private ImageView imgLightMode, imgDarkMode, imgLogout, imgCardSales, imgCardExpense, imgCardTransaction, imgCardStock;
    @FXML private ScrollPane scrollStok;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currencyFormat.setMaximumFractionDigits(0);
        setupActions();
        loadDataDashboard();
        loadDataStokHampirHabis();
        setDarkMode(MainController.isDarkMode);
    }

    // Mengatur klik pada kartu dashboard agar bisa langsung pindah ke halaman terkait.
    private void setupActions() {
        if (card1 != null) {
            card1.setStyle(card1.getStyle() + "; -fx-cursor: hand;");
            card1.setOnMouseClicked(event -> bukaLaporan());
        }
        if (btnCard1 != null) {
            btnCard1.setOnAction(event -> bukaLaporan());
        }

        if (card2 != null) {
            card2.setStyle(card2.getStyle() + "; -fx-cursor: hand;");
            card2.setOnMouseClicked(event -> bukaPengeluaran());
        }
        if (btnCard2 != null) {
            btnCard2.setOnAction(event -> bukaPengeluaran());
        }

        if (card4 != null) {
            card4.setStyle(card4.getStyle() + "; -fx-cursor: hand;");
            card4.setOnMouseClicked(event -> bukaDataBarang());
        }
        if (btnCard4 != null) {
            btnCard4.setOnAction(event -> bukaDataBarang());
        }
    }

    // Navigasi cepat ke halaman laporan melalui MainController.
    private void bukaLaporan() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().openLaporan();
        }
    }

    // Navigasi cepat ke halaman pengeluaran.
    private void bukaPengeluaran() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().openPengeluaran();
        }
    }

    // Navigasi cepat ke halaman data barang.
    private void bukaDataBarang() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().openDataBarang();
        }
    }

    // Mengambil angka ringkasan hari ini dari DAO.
    private void loadDataDashboard() {
        LocalDate hariIni = LocalDate.now();
        double totalPenjualanHariIni = TransaksiDAO.getTotalPenjualanByDate(hariIni);
        double totalPengeluaranHariIni = PengeluaranDAO.getTotalPengeluaranByDate(hariIni);
        int jumlahTransaksiHariIni = TransaksiDAO.getJumlahTransaksiByDate(hariIni);

        if (lblCard1Value != null) {
            lblCard1Value.setText(formatCurrency(totalPenjualanHariIni));
        }
        if (lblCard2Value != null) {
            lblCard2Value.setText(formatCurrency(totalPengeluaranHariIni));
        }
        if (lblCard3Value != null) {
            lblCard3Value.setText(jumlahTransaksiHariIni + " Transaksi");
        }
    }

    // Mengubah angka nominal menjadi format Rupiah.
    private String formatCurrency(double nominal) {
        return currencyFormat.format(nominal).replace(",00", "");
    }

    // Mengatur warna dashboard dan komponen kartu sesuai tema.
    public void setDarkMode(boolean enabled) {
        setStyleClass(paneRoot, "dark", enabled);
        String bgMain = enabled ? "#121212" : "#f4f4f4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String borderColor = enabled ? "#3A3A3A" : "#D7DEE8";
        String blueBorder = enabled ? "#2F9CF4" : "#2196F3";
        String greenBorder = enabled ? "#3FA166" : "#328B51";
        String textColor = enabled ? "white" : "#2C3E50";

        paneRoot.setStyle("-fx-background-color: " + bgMain + ";");
        hboxHeader.setStyle("-fx-background-color: #4A76A8;");
        lblDashboard.setStyle("-fx-text-fill: white;");

        // Card Styles
        String cardShadow = enabled
                ? "dropshadow(three-pass-box, rgba(0,0,0,0.22), 12, 0, 0, 6)"
                : "dropshadow(three-pass-box, rgba(15,23,42,0.08), 12, 0, 0, 6)";
        String cardBase = "-fx-background-radius: 16; -fx-border-radius: 16; -fx-border-width: 1.2; -fx-effect: " + cardShadow + ";";
        
        card1.setStyle("-fx-background-color: " + (enabled ? "#1e2a3a" : "#EAF1FB") + "; -fx-border-color: " + blueBorder + "; -fx-cursor: hand; " + cardBase);
        card2.setStyle("-fx-background-color: " + bgCard + "; -fx-border-color: " + borderColor + "; -fx-cursor: hand; " + cardBase);
        card3.setStyle("-fx-background-color: " + bgCard + "; -fx-border-color: " + blueBorder + "; " + cardBase);
        card4.setStyle("-fx-background-color: " + (enabled ? "#1b2e1f" : "#E8F5E9") + "; -fx-border-color: " + greenBorder + "; -fx-cursor: hand; " + cardBase);

        lblCard1Title.setStyle("-fx-text-fill: " + (enabled ? "#64b5f6" : "#1976D2") + "; -fx-font-weight: bold;");
        lblCard1Value.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
        clearInlineStyles(btnCard1);

        lblCard2Title.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
        lblCard2Value.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
        clearInlineStyles(btnCard2);

        lblCard3Title.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
        lblCard3Value.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");

        lblCard4Title.setStyle("-fx-text-fill: " + (enabled ? "#81c784" : "#2E7D32") + "; -fx-font-weight: bold;");
        clearInlineStyles(btnCard4);
        updateActionButtonArrow(btnCard1, enabled);
        updateActionButtonArrow(btnCard2, enabled);
        updateActionButtonArrow(btnCard4, enabled);

        setImageIfPresent(imgLightMode, enabled ? "/Images/ICON3DARK.png" : "/Images/ICON3.png");
        setImageIfPresent(imgDarkMode, enabled ? "/Images/ICON4DARK.png" : "/Images/ICON4.png");
        setImageIfPresent(imgLogout, enabled ? "/Images/ICON33.png" : "/Images/ICON6.png");
        setImageIfPresent(imgCardSales, enabled ? "/Images/Component 17.png" : "/Images/Component 13.png");
        setImageIfPresent(imgCardExpense, enabled ? "/Images/Component 19.png" : "/Images/Component 15.png");
        setImageIfPresent(imgCardTransaction, enabled ? "/Images/Component 18.png" : "/Images/Component 14.png");
        setImageIfPresent(imgCardStock, enabled ? "/Images/Component 20.png" : "/Images/Component 16.png");

        if (scrollStok != null) {
            scrollStok.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            scrollStok.getStyleClass().remove("dark");
            if (enabled) {
                scrollStok.getStyleClass().add("dark");
            }
        }
        
        loadDataStokHampirHabis(); // Re-render rows with theme
    }

    // Mengambil barang dengan stok rendah lalu menampilkannya di dashboard.
    private void loadDataStokHampirHabis() {
        stokListContainer.getChildren().clear();
        boolean isDark = MainController.isDarkMode;

        List<Barang> barangMenipis = BarangDAO.getBarangStokMenipis(BATAS_STOK_HAMPIR_HABIS, LIMIT_STOK_HAMPIR_HABIS);
        if (barangMenipis.isEmpty()) {
            stokListContainer.getChildren().add(buatBarisKosong(isDark));
            return;
        }

        for (Barang barang : barangMenipis) {
            HBox baris = buatBarisStok(barang.getNamaBarang(), barang.getStok(), isDark);
            stokListContainer.getChildren().add(baris);
        }
    }

    // Baris fallback jika tidak ada barang yang stoknya menipis.
    private HBox buatBarisKosong(boolean isDark) {
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(14));
        hBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        hBox.setStyle("-fx-background-color: " + (isDark ? "#2c2c2c" : "white") + "; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: " + (isDark ? "#444444" : "#D7DEE8") + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10;");

        Label lblKosong = new Label("Belum ada barang dengan stok hampir habis.");
        lblKosong.setWrapText(true);
        lblKosong.setStyle("-fx-text-fill: " + (isDark ? "#d0d0d0" : "#4B5563") + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        hBox.getChildren().add(lblKosong);
        return hBox;
    }

    // Membuat satu baris informasi barang stok menipis.
    private HBox buatBarisStok(String nama, int sisa, boolean isDark) {
        HBox hBox = new HBox();
        hBox.setSpacing(18);
        hBox.setPadding(new Insets(12, 14, 12, 14));
        hBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        String bg = isDark ? "#2c2c2c" : "white";
        String border = isDark ? "#444444" : "#D7DEE8";
        String text = isDark ? "white" : "#2C3E50";

        hBox.setStyle("-fx-background-color: " + bg + "; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: " + border + "; -fx-border-width: 1; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label lblNama = new Label(nama);
        lblNama.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblNama, Priority.ALWAYS);
        lblNama.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + text + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblSisa = new Label("Sisa: " + sisa);
        lblSisa.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 14px; -fx-font-weight: bold;");

        hBox.getChildren().addAll(lblNama, spacer, lblSisa);
        return hBox;
    }

    // Mengganti grafik tombol kartu menjadi panah dengan warna sesuai tema.
    private void updateActionButtonArrow(Button button, boolean darkMode) {
        if (button == null) {
            return;
        }

        Label arrowLabel = new Label("→");
        arrowLabel.setAlignment(Pos.CENTER);
        arrowLabel.setStyle("-fx-text-fill: " + (darkMode ? "white" : "#111111") + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        button.setGraphic(arrowLabel);
    }

    // Helper untuk memasang atau melepas class CSS.
    private void setStyleClass(Node node, String styleClass, boolean enabled) {
        if (node == null || styleClass == null) return;

        if (enabled) {
            if (!node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }
        } else {
            node.getStyleClass().remove(styleClass);
        }
    }

    // Membersihkan inline style agar CSS/tema baru bisa diterapkan rapi.
    private void clearInlineStyles(Node... nodes) {
        if (nodes == null) return;
        for (Node node : nodes) {
            if (node != null) {
                node.setStyle("");
            }
        }
    }

    // Mengganti gambar ImageView jika resource tersedia.
    private void setImageIfPresent(ImageView imageView, String resourcePath) {
        if (imageView == null || resourcePath == null) {
            return;
        }

        InputStream stream = getClass().getResourceAsStream(resourcePath);
        if (stream != null) {
            imageView.setImage(new Image(stream));
        }
    }
}
