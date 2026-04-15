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

public class MainController {

    private static MainController instance;
    public static boolean isDarkMode = false;

    public static MainController getInstance() {
        return instance;
    }

    @FXML private BorderPane mainPane;
    @FXML private AnchorPane contentArea;
    @FXML private VBox sidebarVBox;
    @FXML private Label lblLogo;
    @FXML private ImageView imgLogo, imgLightMode, imgDarkMode, imgLogout;
    @FXML private HBox hboxThemeToggle;

    private Object currentController;

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
        instance = this;
        if (isDarkMode) applyDarkMode();
        else applyLightMode();
    }

    public void setHakAkses(String role) {
        aturVisibility(false, wrapperDashboard, wrapperTransaksi, wrapperDataBarang,
                wrapperLaporan, wrapperKategori, wrapperPengeluaran,
                wrapperUser, wrapperPengaturan);

        if ("kasir".equalsIgnoreCase(role)) {
            aturVisibility(true, wrapperTransaksi);
            setActiveState(wrapperTransaksi, indTransaksi, imgTransaksi, "ICON5.png");
            panggilHalaman("TransaksiView");
        } else if ("pemilik".equalsIgnoreCase(role)) {
            aturVisibility(true, wrapperDashboard, wrapperDataBarang, wrapperLaporan,
                    wrapperKategori, wrapperPengeluaran, wrapperUser, wrapperPengaturan);
            setActiveState(wrapperDashboard, indDashboard, imgDashboard, "icon39.png");
            panggilHalaman("DashboardAdminView");
        }
    }

    @FXML
    private void handleMenuAction(ActionEvent event) {
        Object source = event.getSource();

        if (source == btnDashboard) {
            setActiveState(wrapperDashboard, indDashboard, imgDashboard, "icon39.png");
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
            panggilHalaman("KategoriView");
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

    public FXMLLoader panggilHalaman(String fxmlFile) {
        try {
            // Cek di folder Admin atau Kasir jika tidak ditemukan di root FXML
            String path = "/FXML/" + fxmlFile + ".fxml";
            java.net.URL url = getClass().getResource(path);
            
            if (url == null) {
                path = "/FXML/Admin/" + fxmlFile + ".fxml";
                url = getClass().getResource(path);
            }
            
            if (url == null) {
                path = "/FXML/Kasir/" + fxmlFile + ".fxml";
                url = getClass().getResource(path);
            }

            if (url == null) {
                System.err.println("✗ File FXML tidak ditemukan: " + fxmlFile);
                return null;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            currentController = loader.getController();

            contentArea.getChildren().clear();
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            contentArea.getChildren().add(root);
            
            // Notify controller about theme
            notifyControllerTheme();
            return loader;
        } catch (IOException e) {
            System.err.println("✗ Gagal memuat: " + fxmlFile);
            e.printStackTrace();
            return null;
        }
    }

    private void notifyControllerTheme() {
        if (currentController == null) return;
        
        // Gunakan reflection untuk memanggil setDarkMode agar lebih fleksibel dan menghindari error 'cannot find symbol'
        try {
            java.lang.reflect.Method method = currentController.getClass().getMethod("setDarkMode", boolean.class);
            method.invoke(currentController, isDarkMode);
        } catch (NoSuchMethodException e) {
            // Abaikan jika controller tidak punya fitur Dark Mode
            System.out.println("ℹ Info: Controller " + currentController.getClass().getSimpleName() + " tidak mendukung setDarkMode.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setActiveState(HBox wrapper, Region indicator, ImageView iconView, String iconName) {
        if (wrapper == null || indicator == null || iconView == null) return;
        resetAllMenus();
        
        wrapper.setMaxWidth(200);
        wrapper.setPrefHeight(42);
        wrapper.setStyle("-fx-background-color: " + (isDarkMode ? "#2c3e50" : "#E3F2FD") + "; -fx-background-radius: 0 12 12 0;");
        
        indicator.setPrefWidth(4); indicator.setPrefHeight(24);
        indicator.setStyle("-fx-background-color: #4072A5; -fx-background-radius: 5;");
        
        if (wrapper.getChildren().size() >= 2 && wrapper.getChildren().get(1) instanceof Button) {
            Button btn = (Button) wrapper.getChildren().get(1);
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4072A5; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 0 0 0 10;");
        }
        
        try {
            iconView.setFitWidth(18); iconView.setFitHeight(18);
            iconView.setImage(new Image(getClass().getResourceAsStream("/Images/" + iconName)));
        } catch (Exception e) {}
    }

    private void resetAllMenus() {
        HBox[] wrappers = {wrapperDashboard, wrapperTransaksi, wrapperDataBarang, wrapperLaporan, wrapperKategori, wrapperPengeluaran, wrapperUser, wrapperPengaturan};
        Region[] indicators = {indDashboard, indTransaksi, indDataBarang, indLaporan, indKategori, indPengeluaran, indUser, indPengaturan};
        ImageView[] icons = {imgDashboard, imgTransaksi, imgDataBarang, imgLaporan, imgKategori, imgPengeluaran, imgUser, imgPengaturan};
        String[] originalIcons = {"icon39.png", "ICON5.png", "icon40.png", "icon42.png", "icon43.png", "icon41.png", "icon 37.png", "icon38.png"};

        for (int i = 0; i < wrappers.length; i++) {
            if (wrappers[i] != null && wrappers[i].isVisible()) {
                wrappers[i].setStyle("-fx-background-color: transparent;");
                if (indicators[i] != null) indicators[i].setStyle("-fx-background-color: transparent;");
                
                if (wrappers[i].getChildren().size() >= 2 && wrappers[i].getChildren().get(1) instanceof Button) {
                    Button btn = (Button) wrappers[i].getChildren().get(1);
                    String textColor = isDarkMode ? "#E0E0E0" : "#555555";
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + textColor + "; -fx-font-weight: normal; -fx-font-size: 13px; -fx-padding: 0 0 0 10;");
                }
                
                if (icons[i] != null) {
                    icons[i].setFitWidth(18); icons[i].setFitHeight(18);
                    try {
                        String iconName = originalIcons[i];
                        if (isDarkMode) {
                            if (iconName.equals("icon39.png")) iconName = "icon44.png";
                            else if (iconName.equals("icon40.png")) iconName = "icon45.png";
                            else if (iconName.equals("icon42.png")) iconName = "icon46.png";
                            else if (iconName.equals("icon41.png")) iconName = "icon47.png";
                            else if (iconName.equals("icon 37.png")) iconName = "icon48.png";
                            else if (iconName.equals("icon38.png")) iconName = "icon49.png";
                            else if (iconName.equals("icon43.png")) iconName = "icon50.png";
                            // Transaksi (ICON5.png) tetap ICON5.png
                        }
                        icons[i].setImage(new Image(getClass().getResourceAsStream("/Images/" + iconName)));
                    } catch (Exception e) {}
                }
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
        if (mainPane != null) mainPane.setStyle("-fx-background-color: #121212;");

        if (sidebarVBox != null) sidebarVBox.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #333333; -fx-border-width: 0 1 0 0;");
        if (lblLogo != null) lblLogo.setStyle("-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: white;");
        if (hboxThemeToggle != null) hboxThemeToggle.setStyle("-fx-background-color: #2c2c2c; -fx-border-color: #444444; -fx-background-radius: 20; -fx-border-radius: 20;");
        
        try {
            if (imgLogo != null) imgLogo.setImage(new Image(getClass().getResourceAsStream("/Images/LOGO2.png")));
            if (imgLightMode != null) imgLightMode.setImage(new Image(getClass().getResourceAsStream("/Images/ICON3DARK.png")));
            if (imgDarkMode != null) imgDarkMode.setImage(new Image(getClass().getResourceAsStream("/Images/ICON4DARK.png")));
            if (imgLogout != null) imgLogout.setImage(new Image(getClass().getResourceAsStream("/Images/ICON33.png")));
        } catch (Exception e) {}

        if (btnDarkMode != null) btnDarkMode.setStyle("-fx-background-color: #444444; -fx-background-radius: 20;");
        if (btnLightMode != null) btnLightMode.setStyle("-fx-background-color: transparent;");
        if (btnLogout != null) btnLogout.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand;");
        
        resetAllMenus();
        notifyControllerTheme();
    }

    private void applyLightMode() {
        isDarkMode = false;
        if (mainPane != null) mainPane.setStyle("-fx-background-color: #F4F4F4;");

        if (sidebarVBox != null) sidebarVBox.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 0 1 0 0;");
        if (lblLogo != null) lblLogo.setStyle("-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: black;");
        if (hboxThemeToggle != null) hboxThemeToggle.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 20; -fx-border-color: #D1D5DB; -fx-border-radius: 20;");
        
        try {
            if (imgLogo != null) imgLogo.setImage(new Image(getClass().getResourceAsStream("/Images/LOGO.png")));
            if (imgLightMode != null) imgLightMode.setImage(new Image(getClass().getResourceAsStream("/Images/ICON3.png")));
            if (imgDarkMode != null) imgDarkMode.setImage(new Image(getClass().getResourceAsStream("/Images/ICON4.png")));
            if (imgLogout != null) imgLogout.setImage(new Image(getClass().getResourceAsStream("/Images/ICON6.png")));
        } catch (Exception e) {}

        if (btnLightMode != null) btnLightMode.setStyle("-fx-background-color: #efefef; -fx-background-radius: 20;");
        if (btnDarkMode != null) btnDarkMode.setStyle("-fx-background-color: transparent;");
        if (btnLogout != null) btnLogout.setStyle("-fx-background-color: transparent; -fx-text-fill: black; -fx-cursor: hand;");
        
        resetAllMenus();
        notifyControllerTheme();
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
}