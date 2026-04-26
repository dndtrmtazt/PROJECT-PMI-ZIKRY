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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import model.Barang;

/**
 * Controller untuk mengelola halaman Dashboard Utama.
 * Alur: Menampilkan ringkasan data harian (Penjualan, Pengeluaran, Transaksi) dan daftar stok menipis.
 */
public class DashboardController implements Initializable {
    // [1] Pengaturan batas stok untuk notifikasi barang yang hampir habis
    private static final int BATAS_STOK_HAMPIR_HABIS = 5;
    private static final int LIMIT_STOK_HAMPIR_HABIS = 5;

    // [2] Deklarasi komponen UI dari file FXML (Label, Button, Container)
    @FXML private VBox paneRoot, stokListContainer;
    @FXML private HBox hboxHeader;
    @FXML private Label lblDashboard, lblCard1Title, lblCard1Value, lblCard2Title, lblCard2Value, lblCard3Title, lblCard3Value, lblCard4Title;
    @FXML private VBox card1, card2, card3, card4;
    @FXML private Button btnCard1, btnCard2, btnCard4, btnLogout;
    @FXML private ImageView imgLightMode, imgDarkMode, imgLogout;
    @FXML private ScrollPane scrollStok;

    // [3] Formatter untuk mengubah angka menjadi format mata uang Rupiah
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    /**
     * Method initialize: Menyiapkan dashboard saat pertama kali dimuat.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // [1] Konfigurasi format mata uang tanpa desimal
        currencyFormat.setMaximumFractionDigits(0);
        
        // [2] Menyiapkan event click (Action) untuk navigasi antar halaman
        setupActions();
        
        // [3] Memuat data statistik harian dan daftar stok menipis
        muatDataDashboard();
        muatDataStokHampirHabis(); 
        
        // [4] Menyesuaikan tema tampilan (Gelap/Terang)
        setDarkMode(MainController.isDarkMode);
    }

    /**
     * Method setupActions: Menghubungkan kartu (card) dan tombol dengan fungsi navigasi.
     */
    private void setupActions() {
        // [1] Navigasi kartu 1 ke halaman Laporan
        if (card1 != null) {
            card1.setStyle(card1.getStyle() + "; -fx-cursor: hand;");
            card1.setOnMouseClicked(event -> bukaLaporan());
        }
        if (btnCard1 != null) {
            btnCard1.setOnAction(event -> bukaLaporan());
        }

        // [2] Navigasi kartu 2 ke halaman Pengeluaran
        if (card2 != null) {
            card2.setStyle(card2.getStyle() + "; -fx-cursor: hand;");
            card2.setOnMouseClicked(event -> bukaPengeluaran());
        }
        if (btnCard2 != null) {
            btnCard2.setOnAction(event -> bukaPengeluaran());
        }

        // [3] Navigasi kartu 4 ke halaman Data Barang
        if (card4 != null) {
            card4.setStyle(card4.getStyle() + "; -fx-cursor: hand;");
            card4.setOnMouseClicked(event -> bukaDataBarang());
        }
        if (btnCard4 != null) {
            btnCard4.setOnAction(event -> bukaDataBarang());
        }
    }

    /**
     * Method: Mengalihkan view ke halaman Laporan Penjualan.
     */
    private void bukaLaporan() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().bukaLaporan();
        }
    }

    /**
     * Method: Mengalihkan view ke halaman Pengeluaran Toko.
     */
    private void bukaPengeluaran() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().bukaPengeluaran();
        }
    }

    /**
     * Method: Mengalihkan view ke halaman Inventaris/Data Barang.
     */
    private void bukaDataBarang() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().bukaDataBarang();
        }
    }

    /**
     * Method muatDataDashboard: Mengambil ringkasan angka dari database untuk hari ini.
     */
    private void muatDataDashboard() {
        // [1] Mendapatkan tanggal hari ini
        LocalDate hariIni = LocalDate.now();
        
        // [2] Mengambil total penjualan, pengeluaran, dan jumlah transaksi via DAO
        double totalPenjualanHariIni = TransaksiDAO.getTotalPenjualanByDate(hariIni);
        double totalPengeluaranHariIni = PengeluaranDAO.getTotalPengeluaranByDate(hariIni);
        int jumlahTransaksiHariIni = TransaksiDAO.getJumlahTransaksiByDate(hariIni);

        // [3] Menampilkan data hasil query ke Label masing-masing kartu
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

    /**
     * Method: Mengubah angka double menjadi string berformat Rupiah tanpa akhiran ,00.
     */
    private String formatCurrency(double nominal) {
        return currencyFormat.format(nominal).replace(",00", "");
    }

    /**
     * Method setDarkMode: Mengubah gaya warna seluruh elemen dashboard secara dinamis.
     */
    public void setDarkMode(boolean enabled) {
        // [1] Menyiapkan palet warna berdasarkan status tema (Gelap/Terang)
        String bgMain = enabled ? "#121212" : "#f4f4f4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String borderColor = enabled ? "#3A3A3A" : "#D7DEE8";
        String blueBorder = enabled ? "#2F9CF4" : "#2196F3";
        String greenBorder = enabled ? "#3FA166" : "#328B51";
        String textColor = enabled ? "white" : "#2C3E50";

        // [2] Mengatur warna latar belakang panel utama dan header
        paneRoot.setStyle("-fx-background-color: " + bgMain + ";");
        hboxHeader.setStyle("-fx-background-color: #4A76A8;");
        lblDashboard.setStyle("-fx-text-fill: white;");

        // [3] Mengatur style shadow dan border untuk setiap kartu statistik
        String cardShadow = enabled
                ? "dropshadow(three-pass-box, rgba(0,0,0,0.22), 12, 0, 0, 6)"
                : "dropshadow(three-pass-box, rgba(15,23,42,0.08), 12, 0, 0, 6)";
        String cardBase = "-fx-background-radius: 16; -fx-border-radius: 16; -fx-border-width: 1.2; -fx-effect: " + cardShadow + ";";
        
        card1.setStyle("-fx-background-color: " + (enabled ? "#1e2a3a" : "#EAF1FB") + "; -fx-border-color: " + blueBorder + "; -fx-cursor: hand; " + cardBase);
        card2.setStyle("-fx-background-color: " + bgCard + "; -fx-border-color: " + borderColor + "; -fx-cursor: hand; " + cardBase);
        card3.setStyle("-fx-background-color: " + bgCard + "; -fx-border-color: " + blueBorder + "; " + cardBase);
        card4.setStyle("-fx-background-color: " + (enabled ? "#1b2e1f" : "#E8F5E9") + "; -fx-border-color: " + greenBorder + "; -fx-cursor: hand; " + cardBase);

        // [4] Mengatur warna teks dan style tombol di dalam kartu
        lblCard1Title.setStyle("-fx-text-fill: " + (enabled ? "#64b5f6" : "#1976D2") + "; -fx-font-weight: bold;");
        lblCard1Value.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
        btnCard1.setStyle(getDashboardButtonStyle(enabled, textColor, borderColor));

        lblCard2Title.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
        lblCard2Value.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
        btnCard2.setStyle(getDashboardButtonStyle(enabled, textColor, borderColor));

        lblCard3Title.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
        lblCard3Value.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");

        lblCard4Title.setStyle("-fx-text-fill: " + (enabled ? "#81c784" : "#2E7D32") + "; -fx-font-weight: bold;");
        btnCard4.setStyle(getDashboardButtonStyle(enabled, textColor, borderColor));
        
        // [5] Memperbarui panah pada tombol aksi agar sesuai dengan tema
        updateActionButtonArrow(btnCard1, enabled);
        updateActionButtonArrow(btnCard2, enabled);
        updateActionButtonArrow(btnCard4, enabled);

        // [6] Mengganti ikon gambar navigasi ke versi gelap/terang
        try {
            if (imgLightMode != null) imgLightMode.setImage(new Image(getClass().getResourceAsStream(enabled ? "/Images/ICON3DARK.png" : "/Images/ICON3.png")));
            if (imgDarkMode != null) imgDarkMode.setImage(new Image(getClass().getResourceAsStream(enabled ? "/Images/ICON4DARK.png" : "/Images/ICON4.png")));
            if (imgLogout != null) imgLogout.setImage(new Image(getClass().getResourceAsStream(enabled ? "/Images/ICON33.png" : "/Images/ICON6.png")));
        } catch (Exception e) {}

        // [7] Merender ulang daftar stok hampir habis agar warnanya sinkron dengan tema baru
        scrollStok.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        muatDataStokHampirHabis();
    }

    /**
     * Method muatDataStokHampirHabis: Merender daftar barang yang stoknya di bawah batas.
     */
    private void muatDataStokHampirHabis() {
        // [1] Membersihkan kontainer lama
        stokListContainer.getChildren().clear();
        boolean isDark = MainController.isDarkMode;

        // [2] Mengambil list barang dengan stok menipis dari database
        List<Barang> barangMenipis = BarangDAO.getBarangStokMenipis(BATAS_STOK_HAMPIR_HABIS, LIMIT_STOK_HAMPIR_HABIS);
        
        // [3] Jika tidak ada barang, tampilkan pesan kosong
        if (barangMenipis.isEmpty()) {
            stokListContainer.getChildren().add(buatBarisKosong(isDark));
            return;
        }

        // [4] Looping data dan buat baris UI untuk setiap barang
        for (Barang barang : barangMenipis) {
            HBox baris = buatBarisStok(barang.getNamaBarang(), barang.getStok(), isDark);
            stokListContainer.getChildren().add(baris);
        }
    }

    /**
     * Method: Membuat tampilan baris "Data Kosong" jika semua stok masih aman.
     */
    private HBox buatBarisKosong(boolean isDark) {
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(12));
        hBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        hBox.setStyle("-fx-background-color: " + (isDark ? "#2c2c2c" : "white") + "; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: " + (isDark ? "#444444" : "#D7DEE8") + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10;");

        Label lblKosong = new Label("Belum ada barang dengan stok hampir habis.");
        lblKosong.setWrapText(true);
        lblKosong.setStyle("-fx-text-fill: " + (isDark ? "#d0d0d0" : "#4B5563") + "; -fx-font-weight: bold;");
        hBox.getChildren().add(lblKosong);
        return hBox;
    }

    /**
     * Method: Mengembalikan string CSS untuk gaya tombol di dalam kartu dashboard.
     */
    private String getDashboardButtonStyle(boolean enabled, String textColor, String borderColor) {
        String buttonBg = enabled ? "#333333" : "white";
        return "-fx-background-color: " + buttonBg + "; " +
                "-fx-text-fill: " + textColor + "; " +
                "-fx-border-color: " + borderColor + "; " +
                "-fx-border-width: 1; " +
                "-fx-background-radius: 9; " +
                "-fx-border-radius: 9; " +
                "-fx-cursor: hand;";
    }

    /**
     * Method: Membuat komponen baris stok menipis secara programmatically.
     */
    private HBox buatBarisStok(String nama, int sisa, boolean isDark) {
        // [1] Inisialisasi wadah (HBox) dan variabel warna
        HBox hBox = new HBox();
        hBox.setSpacing(20);
        hBox.setPadding(new Insets(10));
        hBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        String bg = isDark ? "#2c2c2c" : "white";
        String border = isDark ? "#444444" : "#D7DEE8";
        String text = isDark ? "white" : "#2C3E50";

        // [2] Mengatur style visual baris stok
        hBox.setStyle("-fx-background-color: " + bg + "; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: " + border + "; -fx-border-width: 1; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        // [3] Menyiapkan label nama barang dan teks sisa stok (berwarna merah)
        Label lblNama = new Label(nama);
        lblNama.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblNama, Priority.ALWAYS);
        lblNama.setStyle("-fx-font-weight: bold; -fx-text-fill: " + text + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblSisa = new Label("Sisa: " + sisa);
        lblSisa.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");

        // [4] Menggabungkan semua komponen ke dalam baris
        hBox.getChildren().addAll(lblNama, spacer, lblSisa);
        return hBox;
    }

    /**
     * Method: Mengatur ikon panah (→) pada tombol navigasi kartu agar tetap kontras.
     */
    private void updateActionButtonArrow(Button button, boolean darkMode) {
        if (button == null) {
            return;
        }

        Label arrowLabel = new Label("→");
        arrowLabel.setAlignment(Pos.CENTER);
        arrowLabel.setStyle("-fx-text-fill: " + (darkMode ? "white" : "#111111") + "; -fx-font-size: 22px; -fx-font-weight: bold;");
        button.setGraphic(arrowLabel);
    }
}
