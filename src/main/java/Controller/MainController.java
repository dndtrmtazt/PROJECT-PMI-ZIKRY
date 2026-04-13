package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import java.io.IOException;

public class MainController {

    public static boolean isDarkMode = false;

    @FXML private BorderPane mainPane;
    @FXML private AnchorPane contentArea;

    // --- BUTTONS ---
    @FXML private Button btnDashboard, btnTransaksi, btnDataBarang, btnLaporan,
            btnUser, btnDataKategori, btnKelolaPengeluaran, btnPengaturan, btnLogout, btnLightMode, btnDarkMode;

    // --- WRAPPERS ---
    @FXML private HBox wrapperDashboard, wrapperTransaksi, wrapperDataBarang, wrapperLaporan,
            wrapperKategori, wrapperPengeluaran, wrapperUser, wrapperPengaturan;

    // --- INDICATORS ---
    @FXML private Region indDashboard, indTransaksi, indDataBarang, indLaporan,
            indKategori, indPengeluaran, indUser, indPengaturan;

    // --- IMAGE VIEWS ---
    @FXML private ImageView imgDashboard, imgTransaksi, imgDataBarang, imgLaporan,
            imgKategori, imgPengeluaran, imgUser, imgPengaturan;

    @FXML
    public void initialize() {
        if (isDarkMode) applyDarkMode();
        else applyLightMode();
    }

    // --- LOGIKA HAK AKSES (Filter Menu Pemilik vs Kasir) ---
    public void setHakAkses(String role) {
        aturVisibility(false, wrapperDashboard, wrapperTransaksi, wrapperDataBarang,
                wrapperLaporan, wrapperKategori, wrapperPengeluaran,
                wrapperUser, wrapperPengaturan);

        if ("kasir".equalsIgnoreCase(role)) {
            aturVisibility(true, wrapperTransaksi);
            setActiveState(wrapperTransaksi, indTransaksi, imgTransaksi, "ICON5_blue.png");
            panggilHalaman("TransaksiView");
        } else if ("pemilik".equalsIgnoreCase(role)) {
            aturVisibility(true, wrapperDashboard, wrapperDataBarang, wrapperLaporan,
                    wrapperKategori, wrapperPengeluaran, wrapperUser, wrapperPengaturan);

            // Langsung buka Dashboard Admin pas login
            setActiveState(wrapperDashboard, indDashboard, imgDashboard, "icon39_blue.png");
            panggilHalaman("DashboardAdminView");
        }
    }

    // --- NAVIGASI: EVENT HANDLER SEMUA TOMBOL ---
    @FXML
    private void handleMenuAction(ActionEvent event) {
        Object source = event.getSource();

        if (source == btnDashboard) {
            setActiveState(wrapperDashboard, indDashboard, imgDashboard, "ICON7.png");
            panggilHalaman("DashboardAdminView");
        }
        else if (source == btnTransaksi) {
            setActiveState(wrapperTransaksi, indTransaksi, imgTransaksi, "ICON5.png");
            panggilHalaman("TransaksiView");
        }
        else if (source == btnDataBarang) {
            setActiveState(wrapperDataBarang, indDataBarang, imgDataBarang, "icon40.png");
            panggilHalaman("BarangView");
        }
        else if (source == btnLaporan) {
            setActiveState(wrapperLaporan, indLaporan, imgLaporan, "icon42.png");
            panggilHalaman("LaporanView");
        }
        else if (source == btnDataKategori) {
            setActiveState(wrapperKategori, indKategori, imgKategori, "icon43.png");
            panggilHalaman("DataKategoriView");
        }
        else if (source == btnKelolaPengeluaran) {
            setActiveState(wrapperPengeluaran, indPengeluaran, imgPengeluaran, "icon41.png");
            panggilHalaman("PengeluaranView");
        }
        else if (source == btnUser) {
            setActiveState(wrapperUser, indUser, imgUser, "icon 37.png");
            panggilHalaman("KelolaUserView");
        }
        else if (source == btnPengaturan) {
            setActiveState(wrapperPengaturan, indPengaturan, imgPengaturan, "icon38.png");
            panggilHalaman("PengaturanView");
        }
        else if (source == btnLogout) {
            handleLogout();
        }
    }

    // --- NAVIGASI HALAMAN (Melar & Responsive) ---
    public void panggilHalaman(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/" + fxmlFile + ".fxml"));
            Parent root = loader.load();
            contentArea.getChildren().clear();

            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

            contentArea.getChildren().add(root);
        } catch (IOException e) {
            System.err.println("✗ Gagal memuat: " + fxmlFile);
        }
    }

    // --- STYLE MENU AKTIF (Desain Slim & Pill Style) ---
    private void setActiveState(HBox wrapper, Region indicator, ImageView iconView, String blueIconName) {
        resetAllMenus();
        wrapper.setMaxWidth(200);
        wrapper.setPrefHeight(42);
        wrapper.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 0 12 12 0;");

        indicator.setPrefWidth(4);
        indicator.setPrefHeight(24);
        indicator.setStyle("-fx-background-color: #4072A5; -fx-background-radius: 5;");

        Button btn = (Button) wrapper.getChildren().get(1);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4072A5; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 0 0 0 10;");

        try {
            iconView.setFitWidth(18); iconView.setFitHeight(18);
            iconView.setImage(new Image(getClass().getResourceAsStream("/Images/" + blueIconName)));
        } catch (Exception e) {}
    }

    // --- RESET SEMUA MENU KE NORMAL ---
    private void resetAllMenus() {
        HBox[] wrappers = {wrapperDashboard, wrapperTransaksi, wrapperDataBarang, wrapperLaporan, wrapperKategori, wrapperPengeluaran, wrapperUser, wrapperPengaturan};
        Region[] indicators = {indDashboard, indTransaksi, indDataBarang, indLaporan, indKategori, indPengeluaran, indUser, indPengaturan};
        ImageView[] icons = {imgDashboard, imgTransaksi, imgDataBarang, imgLaporan, imgKategori, imgPengeluaran, imgUser, imgPengaturan};
        String[] originalIcons = {"icon39.png", "ICON5.png", "ICON8.png", "ICON9.png", "ICON10.png", "ICON11.png", "ICON12.png", "ICON13.png"};

        for (int i = 0; i < wrappers.length; i++) {
            if (wrappers[i] != null && wrappers[i].isVisible()) {
                wrappers[i].setStyle("-fx-background-color: transparent;");
                indicators[i].setStyle("-fx-background-color: transparent;");
                Button btn = (Button) wrappers[i].getChildren().get(1);
                String textColor = isDarkMode ? "#E0E0E0" : "#555555";
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + textColor + "; -fx-font-weight: normal; -fx-font-size: 13px; -fx-padding: 0 0 0 10;");
                icons[i].setFitWidth(18); icons[i].setFitHeight(18);
                icons[i].setImage(new Image(getClass().getResourceAsStream("/Images/" + originalIcons[i])));
            }
        }
    }

    private void aturVisibility(boolean tampil, HBox... wrappers) {
        for (HBox w : wrappers) {
            if (w != null) {
                w.setVisible(tampil);
                w.setManaged(tampil);
            }
        }
    }

    private void applyDarkMode() {
        isDarkMode = true;
        if (mainPane != null) {
            mainPane.getStylesheets().clear();
            mainPane.getStylesheets().add(getClass().getResource("/CSS/dark-mode.css").toExternalForm());
        }
        btnDarkMode.setStyle("-fx-background-color: #444444; -fx-background-radius: 20;");
        btnLightMode.setStyle("-fx-background-color: transparent;");
    }

    private void applyLightMode() {
        isDarkMode = false;
        if (mainPane != null) mainPane.getStylesheets().clear();
        btnLightMode.setStyle("-fx-background-color: #efefef; -fx-background-radius: 20;");
        btnDarkMode.setStyle("-fx-background-color: transparent;");
    }

    @FXML
    private void handleThemeChange(ActionEvent event) {
        if (event.getSource() == btnDarkMode) applyDarkMode();
        else applyLightMode();
    }

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
    @FXML
    private void handleTambahBarang() {
        try {
            // Panggil file FXML yang baru kita buat
            Parent node = FXMLLoader.load(getClass().getResource("/view/TambahBarangView.fxml"));

            // Ganti bagian TENGAH dashboard dengan form ini
            mainPane.setCenter(node);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}