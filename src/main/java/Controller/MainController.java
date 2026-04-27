package Controller;

import config.UserSession;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.SequentialTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
    private HBox activeWrapper;
    private Region activeIndicator;
    private ImageView activeIconView;
    private String activeIconName;
    private static final Color ACTIVE_MENU_BLUE = Color.web("#4072A5");
    private static final Color DARK_INACTIVE_ICON = Color.WHITE;
    private static final Duration THEME_FADE_OUT_DURATION = Duration.millis(130);
    private static final Duration THEME_FADE_IN_DURATION = Duration.millis(190);
    private boolean themeTransitionRunning = false;

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
            bukaDashboard();
        }
        else if (source == btnTransaksi) {
            setActiveState(wrapperTransaksi, indTransaksi, imgTransaksi, "ICON5.png");
            panggilHalaman("TransaksiView");
        }
        else if (source == btnDataBarang) {
            bukaDataBarang();
        }
        else if (source == btnLaporan) {
            bukaLaporan();
        }
        else if (source == btnDataKategori) {
            setActiveState(wrapperKategori, indKategori, imgKategori, "icon43.png");
            panggilHalaman("KategoriView");
        }
        else if (source == btnKelolaPengeluaran) {
            bukaPengeluaran();
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

        activeWrapper = wrapper;
        activeIndicator = indicator;
        activeIconView = iconView;
        activeIconName = iconName;
        applyActiveMenuStyle(wrapper, indicator, iconView, iconName);
    }

    public void bukaDashboard() {
        setActiveState(wrapperDashboard, indDashboard, imgDashboard, "icon39.png");
        panggilHalaman("DashboardAdminView");
    }

    public void bukaLaporan() {
        setActiveState(wrapperLaporan, indLaporan, imgLaporan, "icon42.png");
        panggilHalaman("LaporanView");
    }

    public void bukaPengeluaran() {
        setActiveState(wrapperPengeluaran, indPengeluaran, imgPengeluaran, "icon41.png");
        panggilHalaman("PengeluaranView");
    }

    public void bukaDataBarang() {
        setActiveState(wrapperDataBarang, indDataBarang, imgDataBarang, "icon40.png");
        panggilHalaman("BarangView");
    }

    private void applyActiveMenuStyle(HBox wrapper, Region indicator, ImageView iconView, String iconName) {
        wrapper.setMaxWidth(200);
        wrapper.setPrefHeight(42);
        wrapper.setStyle("");
        setStyleClass(wrapper, "active", true);

        indicator.setPrefWidth(4); indicator.setPrefHeight(24);
        indicator.setStyle("");
        setStyleClass(indicator, "active", true);

        if (wrapper.getChildren().size() >= 2 && wrapper.getChildren().get(1) instanceof Button) {
            Button btn = (Button) wrapper.getChildren().get(1);
            btn.setStyle("");
            setStyleClass(btn, "active", true);
        }
        
        iconView.setFitWidth(18);
        iconView.setFitHeight(18);
        setSidebarIconBlue(iconView, iconName);
    }

    private void restoreActiveMenu() {
        if (activeWrapper == null || activeIndicator == null || activeIconView == null || activeIconName == null) {
            return;
        }

        applyActiveMenuStyle(activeWrapper, activeIndicator, activeIconView, activeIconName);
    }

    private void resetAllMenus() {
        HBox[] wrappers = {wrapperDashboard, wrapperTransaksi, wrapperDataBarang, wrapperLaporan, wrapperKategori, wrapperPengeluaran, wrapperUser, wrapperPengaturan};
        Region[] indicators = {indDashboard, indTransaksi, indDataBarang, indLaporan, indKategori, indPengeluaran, indUser, indPengaturan};
        ImageView[] icons = {imgDashboard, imgTransaksi, imgDataBarang, imgLaporan, imgKategori, imgPengeluaran, imgUser, imgPengaturan};
        String[] originalIcons = {"icon39.png", "ICON5.png", "icon40.png", "icon42.png", "icon43.png", "icon41.png", "icon 37.png", "icon38.png"};

        for (int i = 0; i < wrappers.length; i++) {
            if (wrappers[i] != null && wrappers[i].isVisible()) {
                wrappers[i].setStyle("");
                setStyleClass(wrappers[i], "active", false);
                if (indicators[i] != null) {
                    indicators[i].setStyle("");
                    setStyleClass(indicators[i], "active", false);
                }
                
                if (wrappers[i].getChildren().size() >= 2 && wrappers[i].getChildren().get(1) instanceof Button) {
                    Button btn = (Button) wrappers[i].getChildren().get(1);
                    btn.setStyle("");
                    setStyleClass(btn, "active", false);
                }
                
                if (icons[i] != null) {
                    icons[i].setFitWidth(18); icons[i].setFitHeight(18);
                    String iconName = originalIcons[i];
                    if (isDarkMode) {
                        setSidebarIconColor(icons[i], iconName, DARK_INACTIVE_ICON);
                    } else if (icons[i] == imgDashboard) {
                        setSidebarIconBlue(icons[i], iconName);
                    } else {
                        setImageIfPresent(icons[i], "/Images/" + iconName);
                    }
                }
            }
        }
    }

    private void setSidebarIconBlue(ImageView iconView, String iconName) {
        setSidebarIconColor(iconView, iconName, ACTIVE_MENU_BLUE);
    }

    private void setSidebarIconColor(ImageView iconView, String iconName, Color color) {
        if (iconView == null || iconName == null || color == null) return;

        Image source = loadImageResource("/Images/" + iconName);
        if (source != null) {
            iconView.setImage(recolorIcon(source, color));
        }
    }

    private Image recolorIcon(Image source, Color color) {
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
        WritableImage tinted = new WritableImage(width, height);
        PixelReader reader = source.getPixelReader();
        PixelWriter writer = tinted.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixel = reader.getColor(x, y);
                writer.setColor(x, y, new Color(color.getRed(), color.getGreen(), color.getBlue(), pixel.getOpacity()));
            }
        }

        return tinted;
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
        setStyleClass(mainPane, "dark", true);
        clearInlineStyles(mainPane, sidebarVBox, lblLogo, hboxThemeToggle, btnLogout);
        
        setImageIfPresent(imgLogo, "/Images/LOGO2.png");
        setImageIfPresent(imgLightMode, "/Images/ICON3DARK.png");
        setImageIfPresent(imgDarkMode, "/Images/ICON4DARK.png");
        setImageIfPresent(imgLogout, "/Images/ICON33.png");

        setStyleClass(btnDarkMode, "active", true);
        setStyleClass(btnLightMode, "active", false);
        
        resetAllMenus();
        restoreActiveMenu();
        notifyControllerTheme();
    }

    private void applyLightMode() {
        isDarkMode = false;
        setStyleClass(mainPane, "dark", false);
        clearInlineStyles(mainPane, sidebarVBox, lblLogo, hboxThemeToggle, btnLogout);
        
        setImageIfPresent(imgLogo, "/Images/LOGO.png");
        setImageIfPresent(imgLightMode, "/Images/ICON3.png");
        setImageIfPresent(imgDarkMode, "/Images/ICON4.png");
        setImageIfPresent(imgLogout, "/Images/ICON6.png");

        setStyleClass(btnLightMode, "active", true);
        setStyleClass(btnDarkMode, "active", false);
        
        resetAllMenus();
        restoreActiveMenu();
        notifyControllerTheme();
    }

    @FXML
    private void handleThemeChange(ActionEvent event) {
        switchThemeWithAnimation(event.getSource() == btnDarkMode);
    }

    private void switchThemeWithAnimation(boolean darkModeTarget) {
        if (themeTransitionRunning || isDarkMode == darkModeTarget) {
            return;
        }

        if (mainPane == null) {
            applyThemeInstantly(darkModeTarget);
            return;
        }

        themeTransitionRunning = true;
        setThemeButtonsBlocked(true);

        FadeTransition fadeOut = new FadeTransition(THEME_FADE_OUT_DURATION, mainPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.82);
        fadeOut.setInterpolator(Interpolator.EASE_BOTH);
        fadeOut.setOnFinished(event -> applyThemeInstantly(darkModeTarget));

        FadeTransition fadeIn = new FadeTransition(THEME_FADE_IN_DURATION, mainPane);
        fadeIn.setFromValue(0.82);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_BOTH);

        SequentialTransition transition = new SequentialTransition(fadeOut, fadeIn);
        transition.setOnFinished(event -> {
            mainPane.setOpacity(1.0);
            setThemeButtonsBlocked(false);
            themeTransitionRunning = false;
        });
        transition.play();
    }

    private void applyThemeInstantly(boolean darkModeTarget) {
        if (darkModeTarget) applyDarkMode();
        else applyLightMode();
    }

    private void setThemeButtonsBlocked(boolean blocked) {
        if (btnLightMode != null) btnLightMode.setMouseTransparent(blocked);
        if (btnDarkMode != null) btnDarkMode.setMouseTransparent(blocked);
    }

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

    private void clearInlineStyles(Node... nodes) {
        if (nodes == null) return;
        for (Node node : nodes) {
            if (node != null) {
                node.setStyle("");
            }
        }
    }

    private void handleLogout() {
        try {
            UserSession.getInstance().logout();
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            URL loginView = getClass().getResource("/FXML/LoginView.fxml");
            if (loginView == null) {
                return;
            }
            Parent root = FXMLLoader.load(loginView);
            stage.setResizable(true);
            stage.setMaximized(false);
            stage.setScene(new Scene(root));
            stage.setTitle("PMI Toko Zikry - Login");
            stage.show();
            stage.setMaximized(true);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Image loadImageResource(String resourcePath) {
        if (resourcePath == null) {
            return null;
        }

        InputStream stream = getClass().getResourceAsStream(resourcePath);
        return stream != null ? new Image(stream) : null;
    }

    private void setImageIfPresent(ImageView imageView, String resourcePath) {
        if (imageView == null) {
            return;
        }

        Image image = loadImageResource(resourcePath);
        if (image != null) {
            imageView.setImage(image);
        }
    }
}
