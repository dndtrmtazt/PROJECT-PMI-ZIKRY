package Controller;

import javafx.animation.FadeTransition;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import DAO.BarangDAO;
import DAO.DetailTransaksiDAO;
import DAO.TransaksiDAO;
import model.Barang;
import model.Detail_Transaksi;
import model.Transaksi;
import config.UserSession;
import java.io.IOException;

/**
 * Controller utama untuk dashboard Kasir.
 * Alur: Menangani pemilihan barang, manajemen keranjang, dan proses checkout/simpan transaksi.
 */
public class KasirDashboardController {
    private enum SortField { NONE, NAMA, STOK, HARGA }

    // [1] Deklarasi komponen Sidebar & Layout Utama
    @FXML private AnchorPane paneRoot;
    @FXML private VBox vboxSidebar, vboxMainContent;
    @FXML private HBox hboxThemeToggle;
    @FXML private ImageView imgLogo, imgLightMode, imgDarkMode, imgLogout;
    @FXML private Button btnLightMode, btnDarkMode, btnLogout, btnTransaksi;
    @FXML private Label lblLogo;

    // [2] Deklarasi komponen Area Transaksi (Produk & Keranjang)
    @FXML private VBox vboxProductCard, vboxCart, vboxProdukList, vboxCartList, vboxSummary;
    @FXML private HBox hboxSearch, hboxProductHeader, hboxCartHeader;
    @FXML private ScrollPane scrollProduct, scrollCart;
    @FXML private Label lblTanggal, lblListProduk, lblHeaderNama, lblHeaderStok, lblHeaderHarga, lblKeranjangBelanja;
    @FXML private Label lblCartHeaderItem, lblCartHeaderQty, lblCartHeaderHarga, lblCartHeaderSubtotal;
    @FXML private Label lblTotalBelanjaText, lblTotalBelanja, lblKembalianText, lblKembalian, lblSearchIcon;
    @FXML private TextField txtSearch, txtBayar;
    @FXML private Button btnSimpanCetak;

    // [3] Variabel data penunjang transaksi
    private List<Barang> allBarang;
    private ObservableList<Detail_Transaksi> cartItems = FXCollections.observableArrayList();
    private double totalBelanja = 0;
    private KasirDashboardController currentContentController;
    private boolean isThemeTransitionRunning = false;
    private SortField activeSortField = SortField.NONE;
    private boolean sortAscending = true;
    private final NumberFormat nfIndo = NumberFormat.getInstance(new Locale("id", "ID"));

    /**
     * Method initialize: Menyiapkan layar kasir dan memuat data awal.
     */
    @FXML
    public void initialize() {
        nfIndo.setMaximumFractionDigits(0);
        boolean isDarkMode = MainController.isDarkMode;

        // [1] Jika ini adalah Sidebar utama (Outer Shell)
        if (vboxSidebar != null) {
            setupSidebarActions();
            loadPage("/FXML/Kasir/TransaksiView.fxml");
        } 
        // [2] Jika ini adalah Area Transaksi (Inner View)
        else {
            allBarang = BarangDAO.getAllBarang();
            setupSortHeaders();
            loadProducts(isDarkMode);
            setupSearch(isDarkMode);
            setupPayment();
            setupRealTimeClock();
        }
        setDarkMode(isDarkMode);
    }

    /**
     * Method: Menyiapkan aksi tombol-tombol pada sidebar.
     */
    private void setupSidebarActions() {
        if (btnTransaksi != null) btnTransaksi.setOnAction(e -> loadPage("/FXML/Kasir/TransaksiView.fxml"));
        if (btnLightMode != null) btnLightMode.setOnAction(e -> animateThemeTransition(false));
        if (btnDarkMode != null)  btnDarkMode.setOnAction(e -> animateThemeTransition(true));
        if (btnLogout != null)    setupLogout();
    }

    /**
     * Method: Memuat halaman FXML ke dalam area konten utama secara dinamis.
     */
    private void loadPage(String fxmlPath) {
        if (vboxMainContent == null) return;
        try {
            vboxMainContent.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            VBox.setVgrow(root, Priority.ALWAYS);
            vboxMainContent.getChildren().add(root);
            currentContentController = loader.getController();
            if (currentContentController != null) currentContentController.setDarkMode(MainController.isDarkMode);
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Method: Menangani proses keluar (logout) dan kembali ke halaman login.
     */
    private void setupLogout() {
        btnLogout.setOnAction(e -> {
            try {
                Stage stage = (Stage) btnLogout.getScene().getWindow();
                stage.close();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/LoginView.fxml"));
                Parent root = loader.load();
                Stage loginStage = new Stage();
                loginStage.setScene(new Scene(root));
                loginStage.setTitle("PMI Toko Zikry - Login");
                loginStage.show();
            } catch (IOException ex) { ex.printStackTrace(); }
        });
    }

    /**
     * Method setDarkMode: Mengatur warna visual aplikasi agar sesuai tema gelap/terang.
     */
    public void setDarkMode(boolean enabled) {
        // [1] Menentukan variabel warna berdasarkan tema
        MainController.isDarkMode = enabled;
        if (currentContentController != null) currentContentController.setDarkMode(enabled);

        String bgMain      = enabled ? "#121212" : "#efefef";
        String bgSidebar   = enabled ? "#1e1e1e" : "#f8f8f8";
        String bgCard      = enabled ? "#1e1e1e" : "#ffffff";
        String borderColor = enabled ? "#333333" : "#d9d9d9";
        String bgHeader    = enabled ? "#333333" : "#e0e0e0";
        String textColor   = enabled ? "#FFFFFF" : "#111111";

        // [2] Menerapkan style ke container utama dan sidebar
        if (paneRoot != null) paneRoot.setStyle("-fx-background-color: " + bgMain + ";");
        if (vboxSidebar != null) vboxSidebar.setStyle("-fx-background-color: " + bgSidebar + "; -fx-border-color: " + borderColor + "; -fx-border-width: 0 1 0 0;");
        if (vboxMainContent != null) vboxMainContent.setStyle("-fx-background-color: " + bgMain + ";");

        // [3] Mengganti gambar ikon dan logo sesuai tema
        try {
            if (imgLogo != null) imgLogo.setImage(new Image(getClass().getResourceAsStream(enabled ? "/Images/LOGO2.png" : "/Images/LOGO.png")));
            if (imgLogout != null) imgLogout.setImage(new Image(getClass().getResourceAsStream(enabled ? "/Images/ICON33.png" : "/Images/ICON6.png")));

            if (enabled) {
                if (imgLightMode != null) imgLightMode.setImage(new Image(getClass().getResourceAsStream("/Images/ICON3.png")));
                if (imgDarkMode != null) imgDarkMode.setImage(new Image(getClass().getResourceAsStream("/Images/ICON4DARK.png")));
            } else {
                if (imgLightMode != null) imgLightMode.setImage(new Image(getClass().getResourceAsStream("/Images/ICON3.png")));
                if (imgDarkMode != null) imgDarkMode.setImage(new Image(getClass().getResourceAsStream("/Images/ICON4.png")));
            }
        } catch (Exception e) { }

        // [4] Mengatur style tombol dan scrollbar kustom
        if (btnTransaksi != null) {
            btnTransaksi.setStyle("-fx-background-color: " + (enabled ? "#2c2c2c" : "#dce9f7") + "; -fx-background-radius: 10; -fx-border-color: transparent; -fx-text-fill: " + (enabled ? "#4da3ff" : "#3b6ea7") + "; -fx-font-weight: bold; -fx-padding: 0 0 0 10; -fx-cursor: hand;");
        }

        if (btnLogout != null) {
            btnLogout.setStyle("-fx-background-color: transparent; -fx-text-fill: " + textColor + "; -fx-font-size: 12px; -fx-cursor: hand;");
        }

        if (paneRoot != null) {
            paneRoot.getStylesheets().removeIf(s -> s.startsWith("data:text/css"));
            if (enabled) {
                paneRoot.getStylesheets().add("data:text/css," +
                        ".scroll-bar:vertical {-fx-background-color: #2b2b2b; -fx-pref-width: 14; -fx-min-width: 14;}" +
                        ".scroll-bar:horizontal {-fx-background-color: #2b2b2b; -fx-pref-height: 14; -fx-min-height: 14;}" +
                        ".scroll-bar .thumb {-fx-background-color: #555555; -fx-background-radius: 10;}" +
                        ".scroll-bar .track {-fx-background-color: #1e1e1e;}" +
                        ".scroll-bar .increment-button, .scroll-bar .decrement-button {-fx-padding: 0;}" +
                        ".scroll-bar .increment-arrow, .scroll-bar .decrement-arrow {-fx-shape: \"\";}");
            }
        }

        // [5] Mengatur style area keranjang dan ringkasan pembayaran
        String scrollAreaStyle = "-fx-background: transparent; -fx-background-color: transparent; -fx-viewport-fill: transparent;";
        if (scrollProduct != null) scrollProduct.setStyle(scrollAreaStyle);
        if (scrollCart != null) scrollCart.setStyle(scrollAreaStyle);

        if (txtBayar != null) {
            txtBayar.setStyle("-fx-background-color: " + (enabled ? "#2C2C2C" : "white") + "; -fx-text-fill: " + textColor + "; " +
                    "-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: " + borderColor + "; -fx-font-size: 15px;");
        }
        if (hboxSearch != null) hboxSearch.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-border-color: " + borderColor + "; -fx-border-radius: 10;");
        if (txtSearch != null) txtSearch.setStyle("-fx-background-color: transparent; -fx-text-fill: " + textColor + "; -fx-font-size: 15px;");
        if (vboxProductCard != null) vboxProductCard.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-border-color: " + borderColor + "; -fx-border-radius: 10;");
        if (vboxCart != null) vboxCart.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-border-color: " + borderColor + "; -fx-border-radius: 10;");

        if (hboxProductHeader != null) {
            hboxProductHeader.setStyle("-fx-background-color: " + bgHeader + ";");
            hboxProductHeader.setPrefHeight(45);
        }
        if (hboxCartHeader != null) {
            hboxCartHeader.setStyle("-fx-background-color: " + bgHeader + ";");
            hboxCartHeader.setPrefHeight(45);
        }

        if (vboxSummary != null) {
            vboxSummary.setStyle("-fx-border-color: " + bgHeader + " transparent transparent transparent; -fx-border-width: 1 0 0 0; " +
                    "-fx-background-color: " + bgCard + "; -fx-background-radius: 0 0 10 10;");
        }

        if (hboxThemeToggle != null) hboxThemeToggle.setStyle("-fx-background-color: " + (enabled ? "#2c2c2c" : "white") + "; -fx-border-color: #cfcfcf; -fx-border-radius: 30; -fx-background-radius: 30;");
        if (btnLightMode != null) btnLightMode.setStyle("-fx-background-color: " + (enabled ? "transparent" : "#efefef") + "; -fx-background-radius: 20; -fx-cursor: hand;");
        if (btnDarkMode != null) btnDarkMode.setStyle("-fx-background-color: " + (enabled ? "#555555" : "transparent") + "; -fx-background-radius: 20; -fx-cursor: hand;");

        Label[] labels = {lblListProduk, lblKeranjangBelanja, lblHeaderNama, lblHeaderStok, lblHeaderHarga,
                lblCartHeaderItem, lblCartHeaderQty, lblCartHeaderHarga, lblCartHeaderSubtotal,
                lblTotalBelanjaText, lblTotalBelanja, lblKembalianText, lblTanggal, lblLogo};
        for(Label l : labels) if(l != null) l.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");

        // [6] Refresh UI produk dan keranjang dengan warna baru
        refreshProductList(enabled);
        updateCartUI();
    }

    /**
     * Method refreshProductList: Menampilkan daftar produk terbaru sesuai filter pencarian.
     */
    private void refreshProductList(boolean isDarkMode) {
        if (vboxProdukList == null || allBarang == null) return;
        List<Barang> currentList = allBarang;
        // [1] Filter produk berdasarkan keyword pencarian
        if (txtSearch != null && !txtSearch.getText().isEmpty()) {
            String query = txtSearch.getText().toLowerCase().trim();
            currentList = currentList.stream()
                    .filter(b -> b.getNamaBarang().toLowerCase().contains(query) || b.getIdBarang().toLowerCase().contains(query))
                    .collect(Collectors.toList());
        }
        // [2] Urutkan dan tampilkan produk ke layar
        displayProducts(getSortedProducts(currentList), isDarkMode);
    }

    /**
     * Method: Memberikan efek transisi smooth saat berganti tema.
     */
    private void animateThemeTransition(boolean enabled) {
        if (MainController.isDarkMode == enabled || isThemeTransitionRunning) return;
        isThemeTransitionRunning = true;
        FadeTransition fadeOut = new FadeTransition(Duration.millis(170), paneRoot);
        fadeOut.setFromValue(1.0); fadeOut.setToValue(0.72);
        fadeOut.setOnFinished(event -> {
            setDarkMode(enabled);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(170), paneRoot);
            fadeIn.setFromValue(0.72); fadeIn.setToValue(1.0);
            fadeIn.setOnFinished(finishEvent -> { isThemeTransitionRunning = false; });
            fadeIn.play();
        });
        fadeOut.play();
    }

    /**
     * Method: Menjalankan jam digital secara real-time di pojok layar.
     */
    private void setupRealTimeClock() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, d MMM yyyy | HH:mm:ss 'WITA'");
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> { if (lblTanggal != null) lblTanggal.setText(LocalDateTime.now().format(dtf)); }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE); clock.play();
    }

    /**
     * Method: Mengambil ulang data produk dari database.
     */
    private void loadProducts(boolean isDarkMode) {
        allBarang = BarangDAO.getAllBarang();
        refreshProductList(isDarkMode);
    }

    /**
     * Method: Menyiapkan klik pada header tabel untuk fitur sorting data.
     */
    private void setupSortHeaders() {
        if (lblHeaderNama != null) lblHeaderNama.setOnMouseClicked(event -> toggleSort(SortField.NAMA));
        if (lblHeaderStok != null) lblHeaderStok.setOnMouseClicked(event -> toggleSort(SortField.STOK));
        if (lblHeaderHarga != null) lblHeaderHarga.setOnMouseClicked(event -> toggleSort(SortField.HARGA));
    }

    /**
     * Method: Mengganti urutan pengurutan (A-Z atau Z-A).
     */
    private void toggleSort(SortField sortField) {
        if (activeSortField == sortField) sortAscending = !sortAscending;
        else { activeSortField = sortField; sortAscending = true; }
        refreshProductList(MainController.isDarkMode);
    }

    /**
     * Method: Mengurutkan list produk berdasarkan kriteria yang dipilih.
     */
    private List<Barang> getSortedProducts(List<Barang> products) {
        if (products == null || activeSortField == SortField.NONE) return products;
        Comparator<Barang> comp;
        switch (activeSortField) {
            case STOK: comp = Comparator.comparingInt(this::getDisplayStock); break;
            case HARGA: comp = Comparator.comparingDouble(Barang::getHargaJual); break;
            case NAMA: default: comp = Comparator.comparing(b -> b.getNamaBarang().toLowerCase()); break;
        }
        if (!sortAscending) comp = comp.reversed();
        return products.stream().sorted(comp).collect(Collectors.toList());
    }

    /**
     * Method: Menghitung sisa stok tampilan (Stok Gudang - Jumlah di Keranjang).
     */
    private int getDisplayStock(Barang b) {
        int qtyInCart = 0;
        for (Detail_Transaksi item : cartItems) {
            if (item.getIdBarang().trim().equalsIgnoreCase(b.getIdBarang().trim())) {
                qtyInCart += item.getJumlah();
            }
        }
        return b.getStok() - qtyInCart;
    }

    /**
     * Method: Melakukan rendering baris produk ke dalam container VBox.
     */
    private void displayProducts(List<Barang> products, boolean isDarkMode) {
        if (vboxProdukList == null) return;
        vboxProdukList.getChildren().clear();
        String textColor = isDarkMode ? "white" : "#111111";
        String lineColor = isDarkMode ? "#333333" : "#EEEEEE";
        if (products == null) return;
        for (int i = 0; i < products.size(); i++) {
            vboxProdukList.getChildren().add(createProductRow(products.get(i), getDisplayStock(products.get(i)), textColor));
            if (i < products.size() - 1) {
                Region line = new Region(); line.setMinHeight(1); line.setMaxHeight(1); line.setStyle("-fx-background-color: " + lineColor + ";");
                VBox.setMargin(line, new Insets(0, 20, 0, 20)); vboxProdukList.getChildren().add(line);
            }
        }
    }

    /**
     * Method: Membuat komponen baris produk secara programmatically.
     */
    private HBox createProductRow(Barang barang, int displayStok, String textColor) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefHeight(50);
        row.setPadding(new Insets(0, 20, 0, 20));

        Label name = new Label(barang.getNamaBarang());
        name.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(name, Priority.ALWAYS);
        name.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label stok = new Label(String.valueOf(displayStok));
        stok.setMinWidth(80);
        stok.setAlignment(Pos.CENTER);
        stok.setStyle("-fx-text-fill: " + (displayStok <= 0 ? "#e74c3c" : textColor) + "; -fx-font-size: 14px;");

        Label harga = new Label("Rp " + nfIndo.format(barang.getHargaJual()));
        harga.setMinWidth(150);
        harga.setPadding(new Insets(0, 0, 0, 15));
        harga.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 14px; -fx-font-weight: bold;");

        // [1] Tombol Tambah Barang ke Keranjang
        VBox btnContainer = new VBox();
        btnContainer.setMinWidth(120);
        btnContainer.setAlignment(Pos.CENTER);

        Button btnAdd = new Button(displayStok <= 0 ? "Habis" : "+ Tambah");
        btnAdd.setMinWidth(85);
        btnAdd.setMinHeight(32);
        btnAdd.setDisable(displayStok <= 0);

        String bgGreen = "#5cb85c";
        String baseBtnStyle = "-fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-color: ";

        if (displayStok <= 0) {
            btnAdd.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #888888; -fx-background-radius: 8;");
        } else {
            btnAdd.setStyle(baseBtnStyle + bgGreen + ";");
        }

        final Barang fBarang = barang;
        btnAdd.setOnAction(e -> addToCart(fBarang));

        btnContainer.getChildren().add(btnAdd);
        row.getChildren().addAll(name, stok, harga, btnContainer);
        return row;
    }

    /**
     * Method addToCart: Menambahkan produk yang dipilih ke keranjang belanja.
     */
    private void addToCart(Barang barang) {
        // [1] Cek stok terbaru dari database
        Barang bLatest = BarangDAO.getBarangById(barang.getIdBarang());
        if (bLatest == null) return;
        
        String cleanId = bLatest.getIdBarang().trim();
        Detail_Transaksi existing = null;
        for (Detail_Transaksi item : cartItems) {
            if (item.getIdBarang().trim().equalsIgnoreCase(cleanId)) { existing = item; break; }
        }
        
        // [2] Jika barang sudah ada di keranjang, tambah jumlahnya. Jika belum, buat baru.
        int currentQtyInCart = (existing != null) ? existing.getJumlah() : 0;
        if (bLatest.getStok() > currentQtyInCart) {
            if (existing != null) {
                existing.setJumlah(existing.getJumlah() + 1);
                existing.setSubtotal(existing.getJumlah() * existing.getHargaSatuan());
            } else {
                Detail_Transaksi newItem = new Detail_Transaksi();
                newItem.setIdBarang(cleanId); newItem.setJumlah(1);
                newItem.setHargaSatuan(bLatest.getHargaJual()); newItem.setSubtotal(bLatest.getHargaJual());
                cartItems.add(newItem);
            }
        } else { new Alert(Alert.AlertType.WARNING, "Stok tidak cukup!").showAndWait(); }
        
        // [3] Refresh tampilan produk dan keranjang
        refreshProductList(MainController.isDarkMode);
        updateCartUI();
    }

    /**
     * Method updateCartUI: Merender ulang daftar barang di keranjang belanja.
     */
    private void updateCartUI() {
        if (vboxCartList == null) return;
        vboxCartList.getChildren().clear(); totalBelanja = 0;
        boolean isDark = MainController.isDarkMode;
        String textColor = isDark ? "white" : "#111111";
        String lineColor = isDark ? "#333333" : "#D9D9D9";
        
        // [1] Looping isi keranjang dan hitung total belanja
        for (int i = 0; i < cartItems.size(); i++) {
            Detail_Transaksi item = cartItems.get(i);
            Barang b = findBarangInList(item.getIdBarang());
            if (b != null) {
                totalBelanja += item.getSubtotal();
                vboxCartList.getChildren().add(createCartRow(item, b, textColor));
                if (i < cartItems.size() - 1) {
                    Region line = new Region(); line.setMinHeight(1); line.setMaxHeight(1); line.setStyle("-fx-background-color: " + lineColor + ";");
                    VBox.setMargin(line, new Insets(0, 15, 0, 15)); vboxCartList.getChildren().add(line);
                }
            }
        }
        // [2] Update teks total belanja dan kembalian
        if (lblTotalBelanja != null) lblTotalBelanja.setText("Rp " + nfIndo.format(totalBelanja));
        updateKembalian();
    }

    /**
     * Method: Mencari data Barang berdasarkan ID dari list internal.
     */
    private Barang findBarangInList(String id) {
        if (allBarang == null || id == null) return null;
        for (Barang b : allBarang) { if (b.getIdBarang().trim().equalsIgnoreCase(id.trim())) return b; }
        return null;
    }

    /**
     * Method: Membuat baris item di dalam keranjang belanja (dengan tombol +/-).
     */
    private HBox createCartRow(Detail_Transaksi item, Barang b, String textColor) {
        boolean isDark = MainController.isDarkMode;
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefHeight(65);
        row.setPadding(new Insets(0, 15, 0, 15));

        Label name = new Label(b.getNamaBarang());
        name.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(name, Priority.ALWAYS);
        name.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 13px; -fx-font-weight: bold;");

        // [1] Kontrol jumlah (Quantity Selector)
        VBox qtyContainer = new VBox();
        qtyContainer.setAlignment(Pos.CENTER);
        qtyContainer.setMinWidth(80);

        HBox qtyBox = new HBox(0);
        qtyBox.setAlignment(Pos.CENTER);
        String sepColor = isDark ? "#555555" : "#D1D5DB";
        qtyBox.setStyle("-fx-border-color: " + sepColor + "; -fx-border-radius: 6; -fx-background-color: " + (isDark ? "#2A2A2A" : "white") + "; -fx-background-radius: 6;");

        Button m = new Button("-");
        Label q = new Label(String.valueOf(item.getJumlah()));
        Button p = new Button("+");

        final Detail_Transaksi fItem = item;
        final Barang fBarang = b;

        m.setOnAction(e -> {
            if (fItem.getJumlah() > 1) {
                fItem.setJumlah(fItem.getJumlah()-1);
                fItem.setSubtotal(fItem.getJumlah()*fItem.getHargaSatuan());
            } else { cartItems.remove(fItem); }
            refreshProductList(MainController.isDarkMode); updateCartUI();
        });

        p.setOnAction(e -> handlePlus(fItem, fBarang));

        qtyBox.getChildren().addAll(m, q, p);
        qtyContainer.getChildren().add(qtyBox);

        // [2] Label harga satuan dan subtotal item
        Label s = new Label("Rp " + nfIndo.format(item.getSubtotal()));
        s.setMinWidth(90);
        s.setAlignment(Pos.CENTER_RIGHT);
        s.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold; -fx-font-size: 13px;");

        row.getChildren().addAll(name, qtyContainer, s);
        return row;
    }

    /**
     * Method: Menangani aksi tambah jumlah (+) di keranjang dengan pengecekan stok.
     */
    private void handlePlus(Detail_Transaksi item, Barang b) {
        Barang bLatest = BarangDAO.getBarangById(b.getIdBarang());
        if (bLatest != null && bLatest.getStok() > item.getJumlah()) {
            item.setJumlah(item.getJumlah() + 1); item.setSubtotal(item.getJumlah() * item.getHargaSatuan());
        } else { new Alert(Alert.AlertType.WARNING, "Stok tidak mencukupi!").showAndWait(); }
        refreshProductList(MainController.isDarkMode); updateCartUI();
    }

    /**
     * Method setupPayment: Menyiapkan logika pembayaran (input uang bayar).
     */
    private void setupPayment() {
        // [1] Pasang aksi tombol Checkout (Simpan & Cetak)
        if (btnSimpanCetak != null) {
            btnSimpanCetak.setOnAction(e -> handleCheckout());
        }

        // [2] Pasang listener format angka otomatis pada kolom Bayar
        if (txtBayar != null) {
            txtBayar.textProperty().addListener((obs, old, val) -> {
                if (!val.isEmpty()) {
                    String digits = val.replaceAll("[^0-9]", "");
                    if (!digits.equals("")) {
                        try {
                            long value = Long.parseLong(digits);
                            String formatted = nfIndo.format(value);
                            if (!val.equals(formatted)) {
                                txtBayar.setText(formatted);
                                txtBayar.positionCaret(formatted.length());
                            }
                        } catch (Exception e) {}
                    }
                }
                updateKembalian();
            });
        }
    }

    /**
     * Method: Menghitung selisih uang bayar dengan total belanja secara real-time.
     */
    private void updateKembalian() {
        if (lblKembalian == null || txtBayar == null) return;
        try {
            String payText = txtBayar.getText().replaceAll("[^0-9]", "");
            double nominalBayar = payText.isEmpty() ? 0 : Double.parseDouble(payText);
            double kembali = nominalBayar - totalBelanja;
            lblKembalian.setText("Rp " + nfIndo.format(kembali));
            lblKembalian.setStyle(kembali < 0 ? "-fx-text-fill: #d32f2f; -fx-font-weight: bold;" : "-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        } catch (Exception e) { lblKembalian.setText("Rp 0"); }
    }

    /**
     * Method handleCheckout: Memulai proses pembayaran.
     * Alur: 1. Cek keranjang -> 2. Cek kecukupan uang -> 3. Tampilkan pop-up konfirmasi.
     */
    @FXML
    private void handleCheckout() {
        // [1] Pastikan keranjang tidak kosong
        if (cartItems.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Keranjang belanja masih kosong!").showAndWait();
            return;
        }

        // [2] Pastikan uang bayar cukup
        String payText = txtBayar.getText().replaceAll("[^0-9]", "");
        double nominalBayar = payText.isEmpty() ? 0 : Double.parseDouble(payText);
        if (nominalBayar < totalBelanja) {
            new Alert(Alert.AlertType.ERROR, "Uang pembayaran tidak cukup!").showAndWait();
            return;
        }

        // [3] Memuat Pop-Up konfirmasi kustom
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Kasir/PopUpSimpan&Cetak.fxml"));
            Parent root = loader.load();

            PopUpSimpanCetakController popupController = loader.getController();
            popupController.setData(totalBelanja, nominalBayar);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UNDECORATED);
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();

            // [4] Jika user menekan tombol 'Cetak' di pop-up, jalankan simpan transaksi
            if (popupController.isConfirmed()) {
                simpanTransaksi();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Gagal memuat desain pop-up konfirmasi!").showAndWait();
        }
    }

    /**
     * Method simpanTransaksi: Menyimpan data ke database (Transaksi & Detail).
     * Alur: 1. Buat Header Transaksi -> 2. Buat ID Detail -> 3. Eksekusi Checkout DAO -> 4. Reset Form.
     */
    private void simpanTransaksi() {
        try {
            // [1] Menyiapkan objek transaksi baru
            String idT = TransaksiDAO.getNextIdTransaksi();
            Transaksi trx = new Transaksi();
            trx.setIdTransaksi(idT);
            trx.setTglTransaksi(LocalDateTime.now());
            trx.setIdUser(UserSession.getInstance().getUserId());
            trx.setTotal(totalBelanja);

            // [2] Menyiapkan ID Detail secara urut untuk setiap item di keranjang
            String lastIdDetailStr = DetailTransaksiDAO.getNextIdDetail();
            int lastDetailNum = 1;
            if (lastIdDetailStr != null && lastIdDetailStr.startsWith("DTL")) {
                try { lastDetailNum = Integer.parseInt(lastIdDetailStr.substring(3)); } catch (NumberFormatException ignored) {}
            }

            for (Detail_Transaksi i : cartItems) {
                i.setIdTransaksi(idT);
                i.setIdDetail(String.format("DTL%03d", lastDetailNum++));
            }

            // [3] Eksekusi database (Simpan data & Kurangi stok secara atomik)
            boolean isSuccess = TransaksiDAO.prosesCheckout(trx, cartItems);

            // [4] Jika berhasil, bersihkan form dan update list produk
            if (isSuccess) {
                cartItems.clear();
                txtBayar.clear();
                allBarang = BarangDAO.getAllBarang();
                refreshProductList(MainController.isDarkMode);
                updateCartUI();
                new Alert(Alert.AlertType.INFORMATION, "Transaksi Berhasil disimpan dan diproses.").showAndWait();
            } else {
                new Alert(Alert.AlertType.ERROR, "Transaksi Gagal! Cek kembali stok barang Anda.").showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method setupSearch: Mengaktifkan filter produk setiap kali user mengetik di kolom cari.
     */
    private void setupSearch(boolean isDarkMode) {
        if (txtSearch == null) return;
        txtSearch.textProperty().addListener((obs, old, val) -> refreshProductList(MainController.isDarkMode));
    }
}
