package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Controller utama untuk mengelola Layout Admin (Shell).
 * Alur: Mengatur navigasi antar halaman, manajemen tema (Dark/Light), dan kontrol sidebar.
 */
public class MainController {

    // [1] Singleton Instance agar bisa dipanggil dari controller lain
    private static MainController instance;
    public static boolean isDarkMode = false;

    public static MainController getInstance() {
        return instance;
    }

    // [2] Deklarasi komponen UI Layout Utama
    @FXML private BorderPane mainPane;
    @FXML private AnchorPane contentArea;
    @FXML private VBox sidebarVBox;
    @FXML private Label lblLogo;
    @FXML private ImageView imgLogo, imgLightMode, imgDarkMode, imgLogout;
    @FXML private HBox hboxThemeToggle;

    private Object currentController;
    private HBox activeWrapper;
    private Region activeIndicator;
    private ImageView activeIconView;
    private String activeMenuName;

    // [3] Deklarasi Button, Wrapper, Indicator, dan Ikon Sidebar
    @FXML private Button btnDashboard, btnTransaksi, btnDataBarang, btnLaporan, btnUser, btnDataKategori, btnKelolaPengeluaran, btnPengaturan, btnLogout, btnLightMode, btnDarkMode;
    @FXML private HBox wrapperDashboard, wrapperTransaksi, wrapperDataBarang, wrapperLaporan, wrapperKategori, wrapperPengeluaran, wrapperUser, wrapperPengaturan;
    @FXML private Region indDashboard, indTransaksi, indDataBarang, indLaporan, indKategori, indPengeluaran, indUser, indPengaturan;
    @FXML private ImageView imgDashboard, imgTransaksi, imgDataBarang, imgLaporan, imgKategori, imgPengeluaran, imgUser, imgPengaturan;

    /**
     * Method initialize: Pengaturan awal saat Layout Admin dimuat.
     */
    @FXML
    public void initialize() {
        instance = this;
        // [1] Terapkan tema yang sedang aktif (Global State)
        if (isDarkMode) applyDarkMode();
        else applyLightMode();
    }

    /**
     * Method setHakAkses: Membatasi menu sidebar berdasarkan role user.
     * Alur: 1. Sembunyikan semua menu -> 2. Tampilkan menu yang diizinkan -> 3. Buka halaman default.
     */
    public void setHakAkses(String role) {
        aturVisibility(false, wrapperDashboard, wrapperTransaksi, wrapperDataBarang, wrapperLaporan, wrapperKategori, wrapperPengeluaran, wrapperUser, wrapperPengaturan);

        if ("kasir".equalsIgnoreCase(role)) {
            aturVisibility(true, wrapperTransaksi);
            setActiveState(wrapperTransaksi, indTransaksi, imgTransaksi, "TRANSAKSI");
            panggilHalaman("TransaksiView");
        } else if ("pemilik".equalsIgnoreCase(role)) {
            aturVisibility(true, wrapperDashboard, wrapperDataBarang, wrapperLaporan, wrapperKategori, wrapperPengeluaran, wrapperUser, wrapperPengaturan);
            setActiveState(wrapperDashboard, indDashboard, imgDashboard, "DASHBOARD");
            panggilHalaman("DashboardAdminView");
        }
    }

    /**
     * Method handleMenuAction: Menangani aksi klik pada setiap menu di sidebar.
     */
    @FXML
    private void handleMenuAction(ActionEvent event) {
        Object source = event.getSource();

        // [1] Navigasi berdasarkan tombol yang diklik
        if (source == btnDashboard) bukaDashboard();
        else if (source == btnTransaksi) {
            setActiveState(wrapperTransaksi, indTransaksi, imgTransaksi, "TRANSAKSI");
            panggilHalaman("TransaksiView");
        }
        else if (source == btnDataBarang) bukaDataBarang();
        else if (source == btnLaporan) bukaLaporan();
        else if (source == btnDataKategori) {
            setActiveState(wrapperKategori, indKategori, imgKategori, "KATEGORI");
            panggilHalaman("KategoriView");
        }
        else if (source == btnKelolaPengeluaran) bukaPengeluaran();
        else if (source == btnUser) {
            setActiveState(wrapperUser, indUser, imgUser, "USER");
            panggilHalaman("KelolaUserView");
        }
        else if (source == btnPengaturan) {
            setActiveState(wrapperPengaturan, indPengaturan, imgPengaturan, "PENGATURAN");
            panggilHalaman("PengaturanView");
        }
        else if (source == btnLogout) handleLogout();
    }

    /**
     * Method panggilHalaman: Menampilkan file FXML ke dalam area konten utama.
     * Alur: 1. Cari file -> 2. Load Parent -> 3. Kosongkan ContentArea -> 4. Masukkan halaman baru.
     */
    public FXMLLoader panggilHalaman(String fxmlFile) {
        try {
            // [1] Mencari file di berbagai sub-folder (Root, Admin, Kasir)
            String path = "/FXML/" + fxmlFile + ".fxml";
            java.net.URL url = getClass().getResource(path);
            if (url == null) url = getClass().getResource("/FXML/Admin/" + fxmlFile + ".fxml");
            if (url == null) url = getClass().getResource("/FXML/Kasir/" + fxmlFile + ".fxml");

            if (url == null) return null;

            // [2] Load dan tampilkan halaman ke contentArea
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            currentController = loader.getController();

            contentArea.getChildren().clear();
            AnchorPane.setTopAnchor(root, 0.0); AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0); AnchorPane.setRightAnchor(root, 0.0);
            contentArea.getChildren().add(root);

            // [3] Beritahu controller halaman baru untuk menyesuaikan tema
            notifyControllerTheme();
            return loader;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Method: Mengirim status isDarkMode ke controller halaman yang sedang aktif.
     */
    private void notifyControllerTheme() {
        if (currentController == null) return;
        try {
            java.lang.reflect.Method method = currentController.getClass().getMethod("setDarkMode", boolean.class);
            method.invoke(currentController, isDarkMode);
        } catch (Exception e) {}
    }

    /**
     * Method: Mengatur status menu sidebar menjadi 'Aktif' secara visual.
     */
    private void setActiveState(HBox wrapper, Region indicator, ImageView iconView, String menuName) {
        if (wrapper == null || indicator == null || iconView == null) return;
        resetAllMenus();
        activeWrapper = wrapper; activeIndicator = indicator;
        activeIconView = iconView; activeMenuName = menuName;
        applyActiveMenuStyle(wrapper, indicator, iconView, menuName);
    }

    // --- Shortcuts untuk Navigasi Cepat ---
    public void bukaDashboard() { setActiveState(wrapperDashboard, indDashboard, imgDashboard, "DASHBOARD"); panggilHalaman("DashboardAdminView"); }
    public void bukaLaporan() { setActiveState(wrapperLaporan, indLaporan, imgLaporan, "LAPORAN"); panggilHalaman("LaporanView"); }
    public void bukaPengeluaran() { setActiveState(wrapperPengeluaran, indPengeluaran, imgPengeluaran, "PENGELUARAN"); panggilHalaman("PengeluaranView"); }
    public void bukaDataBarang() { setActiveState(wrapperDataBarang, indDataBarang, imgDataBarang, "BARANG"); panggilHalaman("BarangView"); }

    /**
     * Method applyActiveMenuStyle: Memberikan gaya visual (Warna/Ikon) pada menu yang terpilih.
     */
    private void applyActiveMenuStyle(HBox wrapper, Region indicator, ImageView iconView, String menuName) {
        // [1] Set background highlight dan warna indikator samping
        wrapper.setStyle("-fx-background-color: " + (isDarkMode ? "rgba(255,255,255,0.06)" : "rgba(64,114,165,0.12)") + "; -fx-background-radius: 0 12 12 0;");
        indicator.setStyle("-fx-background-color: " + (isDarkMode ? "#64B5F6" : "#4072A5") + "; -fx-background-radius: 5;");

        // [2] Set warna teks tombol menjadi kontras/tebal
        if (wrapper.getChildren().size() >= 2 && wrapper.getChildren().get(1) instanceof Button) {
            Button btn = (Button) wrapper.getChildren().get(1);
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + (isDarkMode ? "#64B5F6" : "#4072A5") + "; -fx-font-weight: bold; -fx-cursor: hand;");
        }

        // [3] Ganti ikon ke versi berwarna/aktif
        try {
            String themeIcon = getActiveIcon(menuName);
            iconView.setImage(new Image(getClass().getResourceAsStream("/Images/" + themeIcon)));
            applyIconOpticalScale(iconView, themeIcon);
        } catch (Exception e) {}
    }

    /**
     * Method resetAllMenus: Mengembalikan semua gaya menu sidebar ke kondisi normal (tidak terpilih).
     */
    private void resetAllMenus() {
        HBox[] wrappers = {wrapperDashboard, wrapperTransaksi, wrapperDataBarang, wrapperLaporan, wrapperKategori, wrapperPengeluaran, wrapperUser, wrapperPengaturan};
        Region[] indicators = {indDashboard, indTransaksi, indDataBarang, indLaporan, indKategori, indPengeluaran, indUser, indPengaturan};
        ImageView[] icons = {imgDashboard, imgTransaksi, imgDataBarang, imgLaporan, imgKategori, imgPengeluaran, imgUser, imgPengaturan};
        String[] menuNames = {"DASHBOARD", "TRANSAKSI", "BARANG", "LAPORAN", "KATEGORI", "PENGELUARAN", "USER", "PENGATURAN"};

        for (int i = 0; i < wrappers.length; i++) {
            if (wrappers[i] != null && wrappers[i].isVisible()) {
                wrappers[i].setStyle("-fx-background-color: transparent;");
                if (indicators[i] != null) indicators[i].setStyle("-fx-background-color: transparent;");

                if (wrappers[i].getChildren().size() >= 2 && wrappers[i].getChildren().get(1) instanceof Button) {
                    Button btn = (Button) wrappers[i].getChildren().get(1);
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + (isDarkMode ? "#E0E0E0" : "#555555") + "; -fx-font-weight: normal;");
                }

                if (icons[i] != null) {
                    try {
                        String normalIcon = getNormalIcon(menuNames[i]);
                        icons[i].setImage(new Image(getClass().getResourceAsStream("/Images/" + normalIcon)));
                        applyIconOpticalScale(icons[i], normalIcon);
                    } catch (Exception e) {}
                }
            }
        }
    }

    /**
     * Method applyDarkMode: Menerapkan skema warna tema gelap ke seluruh layout utama.
     */
    private void applyDarkMode() {
        isDarkMode = true;
        // [1] Update warna latar belakang utama dan sidebar
        if (mainPane != null) mainPane.setStyle("-fx-background-color: #121212;");
        if (sidebarVBox != null) sidebarVBox.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #333333; -fx-border-width: 0 1 0 0;");
        if (lblLogo != null) lblLogo.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        
        // [2] Ganti Ikon Navigasi (Logo, Logout, dsb)
        try {
            if (imgLogo != null) imgLogo.setImage(new Image(getClass().getResourceAsStream("/Images/LOGO2.png")));
            if (imgLightMode != null) imgLightMode.setImage(new Image(getClass().getResourceAsStream("/Images/ICON3DARK.png")));
            if (imgDarkMode != null) imgDarkMode.setImage(new Image(getClass().getResourceAsStream("/Images/ICON4DARK.png")));
            if (imgLogout != null) imgLogout.setImage(new Image(getClass().getResourceAsStream("/Images/ICON33.png")));
        } catch (Exception e) {}

        // [3] Refresh status menu dan beritahu controller aktif
        resetAllMenus(); restoreActiveMenu(); notifyControllerTheme();
    }

    /**
     * Method applyLightMode: Menerapkan skema warna tema terang ke seluruh layout utama.
     */
    private void applyLightMode() {
        isDarkMode = false;
        if (mainPane != null) mainPane.setStyle("-fx-background-color: #F4F4F4;");
        if (sidebarVBox != null) sidebarVBox.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 0 1 0 0;");
        if (lblLogo != null) lblLogo.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");

        try {
            if (imgLogo != null) imgLogo.setImage(new Image(getClass().getResourceAsStream("/Images/LOGO.png")));
            if (imgLightMode != null) imgLightMode.setImage(new Image(getClass().getResourceAsStream("/Images/ICON3.png")));
            if (imgDarkMode != null) imgDarkMode.setImage(new Image(getClass().getResourceAsStream("/Images/ICON4.png")));
            if (imgLogout != null) imgLogout.setImage(new Image(getClass().getResourceAsStream("/Images/ICON6.png")));
        } catch (Exception e) {}

        resetAllMenus(); restoreActiveMenu(); notifyControllerTheme();
    }

    /**
     * Method handleThemeChange: Menangani klik pada toggle switch tema.
     */
    @FXML
    private void handleThemeChange(ActionEvent event) {
        if (event.getSource() == btnDarkMode) applyDarkMode();
        else applyLightMode();
    }

    /**
     * Method handleLogout: Menutup aplikasi dan kembali ke layar login.
     */
    private void handleLogout() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.close();
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/LoginView.fxml"));
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root));
            loginStage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // --- Helper Methods: Mapping Ikon ---
    private String getNormalIcon(String name) {
        switch (name) {
            case "DASHBOARD": return isDarkMode ? "icon44.png" : "icon39.png";
            case "TRANSAKSI": return "ICON5.png";
            case "BARANG": return isDarkMode ? "icon45.png" : "ICON8.png";
            case "LAPORAN": return isDarkMode ? "icon46.png" : "ICON9.png";
            case "KATEGORI": return isDarkMode ? "icon50.png" : "ICON10.png";
            case "PENGELUARAN": return isDarkMode ? "icon47.png" : "ICON11.png";
            case "USER": return isDarkMode ? "icon48.png" : "ICON12.png";
            case "PENGATURAN": return isDarkMode ? "icon49.png" : "ICON13.png";
            default: return "icon39.png";
        }
    }

    private String getActiveIcon(String name) {
        switch (name) {
            case "DASHBOARD": return "ICON7.png";
            case "TRANSAKSI": return "ICON5.png";
            case "BARANG": return "icon40.png";
            case "LAPORAN": return "icon42.png";
            case "KATEGORI": return "icon43.png";
            case "PENGELUARAN": return "icon41.png";
            case "USER": return "icon 37.png";
            case "PENGATURAN": return "icon38.png";
            default: return "icon39.png";
        }
    }

    private void applyIconOpticalScale(ImageView iv, String icon) {
        iv.setFitWidth(20.0); iv.setFitHeight(20.0);
        iv.setScaleX(1.0); iv.setScaleY(1.0);
        if (icon.equals("icon44.png")) { iv.setScaleX(0.8); iv.setScaleY(0.8); }
        else if (icon.equals("icon48.png")) { iv.setScaleX(1.2); iv.setScaleY(1.2); }
    }

    private void restoreActiveMenu() { if (activeWrapper != null) applyActiveMenuStyle(activeWrapper, activeIndicator, activeIconView, activeMenuName); }

    private void aturVisibility(boolean t, HBox... ws) { for (HBox w : ws) if (w != null) { w.setVisible(t); w.setManaged(t); } }
}
