package Controller;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
import model.Barang;
import model.BarangDAO;
import model.Detail_Transaksi;
import model.DetailTransaksiDAO;
import model.Transaksi;
import model.TransaksiDAO;
import config.UserSession;
import java.io.IOException;

public class KasirDashboardController {

    // --- KOMPONEN UI (Sesuai ID di FXML) ---
    @FXML private AnchorPane paneRoot;
    @FXML private VBox vboxSidebar, vboxMainContent, vboxProductCard, vboxCart, vboxProdukList, vboxCartList;
    @FXML private HBox hboxThemeToggle, hboxSearch, hboxProductHeader, hboxCartHeader;
    @FXML private ScrollPane scrollProduct, scrollCart;
    @FXML private Label lblLogo, lblTanggal, lblListProduk, lblHeaderNama, lblHeaderStok, lblHeaderHarga, lblKeranjangBelanja;
    @FXML private Label lblCartHeaderItem, lblCartHeaderQty, lblCartHeaderHarga, lblCartHeaderSubtotal;
    @FXML private Label lblTotalBelanjaText, lblTotalBelanja, lblKembalianText, lblKembalian, lblSearchIcon;
    @FXML private TextField txtSearch, txtBayar;
    @FXML private ImageView imgLogo, imgLightMode, imgDarkMode, imgLogout;
    @FXML private Button btnLightMode, btnDarkMode, btnLogout, btnSimpanCetak, btnTransaksi;

    // --- DATA STATE ---
    private List<Barang> allBarang;
    private ObservableList<Detail_Transaksi> cartItems = FXCollections.observableArrayList();
    private double totalBelanja = 0;

    private final NumberFormat nfIndo = NumberFormat.getInstance(new Locale("id", "ID"));

    // --- PENGATURAN AWAL ---
    @FXML
    public void initialize() {
        nfIndo.setMaximumFractionDigits(0);

        boolean isDarkMode = MainController.isDarkMode;
        loadProducts(isDarkMode);
        setupSearch(isDarkMode);
        setupPayment();
        setupRealTimeClock();

        if (btnLightMode != null) btnLightMode.setOnAction(e -> setDarkMode(false));
        if (btnDarkMode != null) btnDarkMode.setOnAction(e -> setDarkMode(true));
        if (btnLogout != null) setupLogout();

        setDarkMode(isDarkMode);
    }

    // --- LOGOUT ---
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
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    // --- DARK MODE ---
    public void setDarkMode(boolean enabled) {
        MainController.isDarkMode = enabled;

        String bgMain     = enabled ? "#121212" : "#efefef";
        String bgSidebar  = enabled ? "#1e1e1e" : "#f8f8f8";
        String borderColor= enabled ? "#333333" : "#d9d9d9";
        String bgCard     = enabled ? "#1e1e1e" : "#ffffff";
        String bgHeader   = enabled ? "#333333" : "#dcdcdc";
        String textColor  = enabled ? "-fx-text-fill: white;" : "-fx-text-fill: #111111;";
        String labelMuted = enabled ? "-fx-text-fill: #bbbbbb;" : "-fx-text-fill: #9a9a9a;";

        if (paneRoot != null)       paneRoot.setStyle("-fx-background-color: " + bgMain + ";");
        if (vboxSidebar != null)    vboxSidebar.setStyle("-fx-background-color: " + bgSidebar + "; -fx-border-color: " + borderColor + "; -fx-border-width: 0 1 0 0;");
        if (vboxMainContent != null) vboxMainContent.setStyle("-fx-background-color: " + bgMain + ";");

        if (lblLogo != null)            lblLogo.setStyle("-fx-font-weight: bold; " + textColor);
        if (lblTanggal != null)         lblTanggal.setStyle(textColor);
        if (lblListProduk != null)      lblListProduk.setStyle("-fx-font-weight: bold; " + textColor);
        if (lblKeranjangBelanja != null) lblKeranjangBelanja.setStyle("-fx-font-weight: bold; " + textColor);

        if (hboxSearch != null)  hboxSearch.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 8; -fx-border-color: #d3d3d3; -fx-border-radius: 8;");
        if (txtSearch != null)   txtSearch.setStyle("-fx-background-color: transparent; " + textColor + "-fx-font-size: 11px;");
        if (lblSearchIcon != null) lblSearchIcon.setStyle(labelMuted);

        if (vboxProductCard != null)   vboxProductCard.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-border-color: " + borderColor + "; -fx-border-radius: 10;");
        if (hboxProductHeader != null) hboxProductHeader.setStyle("-fx-background-color: " + bgHeader + ";");
        if (lblHeaderNama != null)     lblHeaderNama.setStyle("-fx-font-weight: bold; " + textColor);
        if (lblHeaderStok != null)     lblHeaderStok.setStyle("-fx-font-weight: bold; " + textColor);
        if (lblHeaderHarga != null)    lblHeaderHarga.setStyle("-fx-font-weight: bold; " + textColor);

        if (scrollProduct != null)  scrollProduct.setStyle("-fx-background: " + bgCard + "; -fx-background-color: transparent;");
        if (vboxProdukList != null) vboxProdukList.setStyle("-fx-background-color: " + bgCard + ";");

        if (vboxCart != null)           vboxCart.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-border-color: " + borderColor + "; -fx-border-radius: 10;");
        if (hboxCartHeader != null)     hboxCartHeader.setStyle("-fx-background-color: " + bgHeader + ";");
        if (lblCartHeaderItem != null)  lblCartHeaderItem.setStyle("-fx-font-weight: bold; " + textColor);
        if (lblCartHeaderQty != null)   lblCartHeaderQty.setStyle("-fx-font-weight: bold; " + textColor);
        if (lblCartHeaderHarga != null) lblCartHeaderHarga.setStyle("-fx-font-weight: bold; " + textColor);
        if (lblCartHeaderSubtotal != null) lblCartHeaderSubtotal.setStyle("-fx-font-weight: bold; " + textColor);

        if (scrollCart != null)   scrollCart.setStyle("-fx-background: " + bgCard + "; -fx-background-color: transparent;");
        if (vboxCartList != null) vboxCartList.setStyle("-fx-background-color: " + bgCard + ";");

        if (lblTotalBelanjaText != null) lblTotalBelanjaText.setStyle("-fx-font-weight: bold; " + textColor);
        if (lblTotalBelanja != null)     lblTotalBelanja.setStyle("-fx-font-weight: bold; " + textColor);
        if (lblKembalianText != null)    lblKembalianText.setStyle("-fx-font-weight: bold; " + textColor);

        if (txtBayar != null) txtBayar.setStyle(
                "-fx-background-color: " + (enabled ? "#333333" : "white") + "; "
                        + textColor
                        + "-fx-background-radius: 8; -fx-font-size: 10.5px; -fx-border-color: #d3d3d3; -fx-border-radius: 8;"
        );

        if (hboxThemeToggle != null) hboxThemeToggle.setStyle("-fx-background-color: " + bgCard + "; -fx-border-color: #cfcfcf; -fx-border-radius: 20; -fx-background-radius: 20;");
        if (btnLightMode != null)    btnLightMode.setStyle("-fx-background-color: " + (enabled ? "transparent" : "#efefef") + "; -fx-background-radius: 18; -fx-cursor: hand;");
        if (btnDarkMode != null)     btnDarkMode.setStyle("-fx-background-color: " + (enabled ? "#444444" : "transparent") + "; -fx-background-radius: 18; -fx-cursor: hand;");
        if (btnLogout != null)       btnLogout.setStyle("-fx-background-color: transparent; " + textColor + "-fx-font-size: 12px; -fx-padding: 0;");

        try {
            if (imgLogo != null)      imgLogo.setImage(new Image(getClass().getResourceAsStream(enabled ? "/Images/LOGO2.png" : "/Images/LOGO.png")));
            if (imgLightMode != null) imgLightMode.setImage(new Image(getClass().getResourceAsStream(enabled ? "/Images/ICON3DARK.png" : "/Images/ICON3.png")));
            if (imgDarkMode != null)  imgDarkMode.setImage(new Image(getClass().getResourceAsStream(enabled ? "/Images/ICON4DARK.png" : "/Images/ICON4.png")));
            if (imgLogout != null)    imgLogout.setImage(new Image(getClass().getResourceAsStream(enabled ? "/Images/ICON33.png" : "/Images/ICON6.png")));
        } catch (Exception e) { /* abaikan jika gambar tidak ditemukan */ }

        displayProducts(allBarang, enabled);
        updateCartUI();
    }

    // --- JAM REAL-TIME ---
    private void setupRealTimeClock() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, d MMM yyyy | HH:mm:ss 'WITA'");
        Timeline clock = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    if (lblTanggal != null) lblTanggal.setText(LocalDateTime.now().format(dtf));
                }),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    // --- LOAD DATA DARI DATABASE ---
    private void loadProducts(boolean isDarkMode) {
        allBarang = BarangDAO.getAllBarang();
        // Gunakan filter pencarian jika ada
        if (txtSearch != null && !txtSearch.getText().isEmpty()) {
            filterProducts(txtSearch.getText(), isDarkMode);
        } else {
            displayProducts(allBarang, isDarkMode);
        }
    }

    private void filterProducts(String query, boolean isDarkMode) {
        if (allBarang == null) return;
        List<Barang> filtered = allBarang.stream()
                .filter(b -> b.getNamaBarang().toLowerCase().contains(query.toLowerCase())
                        || b.getIdBarang().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        displayProducts(filtered, isDarkMode);
    }

    // --- TAMPILKAN LIST PRODUK ---
    // FIX: Hitung stok yang tersedia = stok DB - qty yang sudah di keranjang
    private void displayProducts(List<Barang> products, boolean isDarkMode) {
        if (vboxProdukList == null) return;
        vboxProdukList.getChildren().clear();
        String textColor = isDarkMode ? "-fx-text-fill: white;" : "-fx-text-fill: #111111;";

        if (products == null) return;
        for (Barang barang : products) {
            String bId = (barang.getIdBarang() != null) ? barang.getIdBarang().trim() : "";
            
            // Hitung qty barang yang sudah ada di keranjang (Gunakan loop manual agar pasti sinkron)
            int qtyInCart = 0;
            for (Detail_Transaksi item : cartItems) {
                String itemBarangId = (item.getIdBarang() != null) ? item.getIdBarang().trim() : "";
                if (itemBarangId.equalsIgnoreCase(bId)) {
                    qtyInCart += item.getJumlah();
                }
            }

            // Stok tampilan = stok asli DB dikurangi qty keranjang
            int displayStok = barang.getStok() - qtyInCart;

            vboxProdukList.getChildren().add(createProductRow(barang, displayStok, textColor));

            Region line = new Region();
            line.setMinHeight(1);
            line.setMaxHeight(1);
            line.setStyle("-fx-background-color: " + (isDarkMode ? "#333333" : "#EEEEEE") + ";");
            vboxProdukList.getChildren().add(line);
        }
    }

    // --- DESAIN BARIS PRODUK ---
    private HBox createProductRow(Barang barang, int displayStok, String textColor) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefHeight(55);
        row.setPadding(new Insets(5, 15, 5, 15));

        Label name = new Label(barang.getNamaBarang());
        name.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(name, Priority.ALWAYS);
        name.setStyle(textColor + "-fx-font-size: 11.5px; -fx-font-weight: bold;");

        // Tampilkan stok yang sudah dikurangi qty keranjang
        Label stok = new Label(String.valueOf(displayStok));
        stok.setMinWidth(100);
        stok.setPrefWidth(100);
        stok.setMaxWidth(100);
        stok.setAlignment(Pos.CENTER);
        // Warna merah jika stok habis
        stok.setStyle((displayStok <= 0 ? "-fx-text-fill: #e74c3c;" : textColor) + "-fx-font-size: 11.5px;");

        Label harga = new Label("Rp " + nfIndo.format(barang.getHargaJual()));
        harga.setMinWidth(140);
        harga.setPrefWidth(140);
        harga.setMaxWidth(140);
        harga.setAlignment(Pos.CENTER);
        harga.setStyle(textColor + "-fx-font-size: 11.5px; -fx-font-weight: bold;");

        Button btnAdd = new Button(displayStok <= 0 ? "Habis" : "+ Tambah");
        btnAdd.setMinWidth(90);
        btnAdd.setPrefWidth(90);
        btnAdd.setMaxWidth(90);
        // Disable tombol jika stok sudah habis
        btnAdd.setDisable(displayStok <= 0);
        btnAdd.setStyle(displayStok <= 0
                ? "-fx-background-color: #cccccc; -fx-text-fill: #888888; -fx-background-radius: 5; -fx-font-size: 10.5px; -fx-font-weight: bold;"
                : "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 10.5px; -fx-font-weight: bold; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> addToCart(barang));

        row.getChildren().addAll(name, stok, harga, btnAdd);
        return row;
    }

    // --- TAMBAH KE KERANJANG ---
    private void addToCart(Barang barang) {
        // Ambil data terbaru dari DB
        Barang latestBarang = BarangDAO.getBarangById(barang.getIdBarang());
        if (latestBarang == null) return;
        
        String bId = (latestBarang.getIdBarang() != null) ? latestBarang.getIdBarang().trim() : "";

        // Hitung total qty di keranjang untuk barang ini (Gunakan loop agar pasti sinkron)
        int currentQtyInCart = 0;
        Detail_Transaksi existing = null;
        for (Detail_Transaksi item : cartItems) {
            String itemBId = (item.getIdBarang() != null) ? item.getIdBarang().trim() : "";
            if (itemBId.equalsIgnoreCase(bId)) {
                currentQtyInCart += item.getJumlah();
                existing = item; // Simpan referensi jika sudah ada
            }
        }

        // Cek apakah masih bisa nambah (Stok DB > total di keranjang)
        if (latestBarang.getStok() > currentQtyInCart) {
            if (existing != null) {
                existing.setJumlah(existing.getJumlah() + 1);
                existing.setSubtotal(existing.getJumlah() * existing.getHargaSatuan());
            } else {
                Detail_Transaksi newItem = new Detail_Transaksi();
                newItem.setIdBarang(latestBarang.getIdBarang());
                newItem.setJumlah(1);
                newItem.setHargaSatuan(latestBarang.getHargaJual());
                newItem.setSubtotal(latestBarang.getHargaJual());
                cartItems.add(newItem);
            }
        } else {
            showAlert("Stok Habis", "Maaf, stok barang di gudang sudah habis!");
        }

        loadProducts(MainController.isDarkMode);
        updateCartUI();
    }

    // --- UPDATE TAMPILAN KERANJANG ---
    private void updateCartUI() {
        if (vboxCartList == null) return;
        vboxCartList.getChildren().clear();
        totalBelanja = 0;
        boolean isDark = MainController.isDarkMode;
        String textColor = isDark ? "-fx-text-fill: white;" : "-fx-text-fill: #111111;";

        for (Detail_Transaksi item : cartItems) {
            Barang b = findBarangById(item.getIdBarang());
            if (b != null) {
                totalBelanja += item.getSubtotal();
                vboxCartList.getChildren().add(createCartRow(item, b, textColor));

                Region line = new Region();
                line.setMinHeight(1);
                line.setMaxHeight(1);
                line.setStyle("-fx-background-color: " + (isDark ? "#333333" : "#EEEEEE") + ";");
                vboxCartList.getChildren().add(line);
            }
        }

        if (lblTotalBelanja != null) lblTotalBelanja.setText("Rp " + nfIndo.format(totalBelanja));
        updateKembalian();
    }

    // --- DESAIN BARIS KERANJANG ---
    private HBox createCartRow(Detail_Transaksi item, Barang barang, String textColor) {
        boolean isDark = MainController.isDarkMode;
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setMinHeight(50);

        // Nama Barang
        Label name = new Label(barang.getNamaBarang());
        name.setMinWidth(40);
        name.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(name, Priority.ALWAYS);
        name.setWrapText(true);
        name.setStyle(textColor + "-fx-font-size: 11px; -fx-line-spacing: -1px;");

        // Kontrol Qty (tombol - angka +)
        HBox qtyBox = new HBox(0);
        qtyBox.setAlignment(Pos.CENTER);
        qtyBox.setMinWidth(55);
        qtyBox.setMaxWidth(55);
        qtyBox.setPrefWidth(55);
        qtyBox.setPrefHeight(22);
        qtyBox.setStyle(
                "-fx-border-color: " + (isDark ? "#444444" : "#D1D5DB") + ";"
                        + "-fx-border-radius: 4;"
                        + "-fx-background-color: " + (isDark ? "#333333" : "white") + ";"
                        + "-fx-background-radius: 4;"
        );

        String btnTextColor = isDark ? "-fx-text-fill: white;" : "-fx-text-fill: black;";
        String commonStyle  = btnTextColor + "-fx-font-size: 10px; -fx-padding: 0;";

        Button btnMinus = new Button("-");
        btnMinus.setMinWidth(17);
        btnMinus.setPrefHeight(22);
        btnMinus.setStyle("-fx-background-color: transparent; -fx-border-color: " + (isDark ? "#444444" : "#D1D5DB") + "; -fx-border-width: 0 1 0 0; " + commonStyle + "-fx-cursor: hand;");

        Label lblQty = new Label(String.valueOf(item.getJumlah()));
        lblQty.setMinWidth(20);
        lblQty.setAlignment(Pos.CENTER);
        lblQty.setStyle(commonStyle + "-fx-font-weight: bold;");

        Button btnPlus = new Button("+");
        btnPlus.setMinWidth(17);
        btnPlus.setPrefHeight(22);
        btnPlus.setStyle("-fx-background-color: transparent; -fx-border-color: " + (isDark ? "#444444" : "#D1D5DB") + "; -fx-border-width: 0 0 0 1; " + commonStyle + "-fx-cursor: hand;");

        btnMinus.setOnAction(e -> handleMinus(item, barang));
        btnPlus.setOnAction(e -> handlePlus(item, barang));

        qtyBox.getChildren().addAll(btnMinus, lblQty, btnPlus);

        // Harga satuan
        Label harga = new Label("Rp " + nfIndo.format(item.getHargaSatuan()));
        harga.setMinWidth(80);
        harga.setPrefWidth(80);
        harga.setMaxWidth(80);
        harga.setAlignment(Pos.CENTER);
        harga.setStyle(textColor + "-fx-font-size: 10px;");

        Label sub = new Label("Rp " + nfIndo.format(item.getSubtotal()));
        sub.setMinWidth(90);
        sub.setPrefWidth(90);
        sub.setMaxWidth(Double.MAX_VALUE);
        sub.setAlignment(Pos.CENTER_RIGHT);
        sub.setStyle(textColor + "-fx-font-weight: bold; -fx-font-size: 10.5px;");

        row.getChildren().addAll(name, qtyBox, harga, sub);
        return row;
    }

    // --- KURANGI QTY DI KERANJANG ---
    private void handleMinus(Detail_Transaksi item, Barang barang) {
        if (item.getJumlah() > 1) {
            item.setJumlah(item.getJumlah() - 1);
            item.setSubtotal(item.getJumlah() * item.getHargaSatuan());
        } else {
            cartItems.remove(item);
        }
        loadProducts(MainController.isDarkMode);
        updateCartUI();
    }

    // --- TAMBAH QTY DI KERANJANG ---
    private void handlePlus(Detail_Transaksi item, Barang barang) {
        Barang latestBarang = BarangDAO.getBarangById(item.getIdBarang());
        if (latestBarang == null) return;

        String bId = (latestBarang.getIdBarang() != null) ? latestBarang.getIdBarang().trim() : "";

        // Hitung total qty di keranjang untuk barang ini
        int currentQtyInCart = 0;
        for (Detail_Transaksi cartItem : cartItems) {
            String itemBId = (cartItem.getIdBarang() != null) ? cartItem.getIdBarang().trim() : "";
            if (itemBId.equalsIgnoreCase(bId)) {
                currentQtyInCart += cartItem.getJumlah();
            }
        }

        // Cek apakah masih bisa nambah (Stok DB > total di keranjang)
        if (latestBarang.getStok() > currentQtyInCart) {
            item.setJumlah(item.getJumlah() + 1);
            item.setSubtotal(item.getJumlah() * item.getHargaSatuan());
            loadProducts(MainController.isDarkMode);
            updateCartUI();
        } else {
            showAlert("Stok Tidak Cukup", "Maaf, stok barang di gudang sudah habis!");
        }
    }

    // --- HELPER: Cari barang berdasarkan ID ---
    private Barang findBarangById(String id) {
        if (allBarang == null || id == null) return null;
        String searchId = id.trim();
        for (Barang b : allBarang) {
            String bId = (b.getIdBarang() != null) ? b.getIdBarang().trim() : "";
            if (bId.equalsIgnoreCase(searchId)) {
                return b;
            }
        }
        return null;
    }

    // --- FITUR PEMBAYARAN ---
    private void setupPayment() {
        if (txtBayar == null) return;

        txtBayar.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            String digitsOnly = newText.replaceAll("[^0-9]", "");

            if (digitsOnly.isEmpty()) {
                change.setText("");
                change.setRange(0, change.getControlText().length());
                return change;
            }

            try {
                long value = Long.parseLong(digitsOnly);
                String formatted = nfIndo.format(value);
                change.setText(formatted);
                change.setRange(0, change.getControlText().length());
                return change;
            } catch (NumberFormatException e) {
                return null;
            }
        }));

        txtBayar.textProperty().addListener((obs, old, newVal) -> updateKembalian());

        if (btnSimpanCetak != null) btnSimpanCetak.setOnAction(e -> handleCheckout());
    }

    // --- UPDATE KEMBALIAN ---
    private void updateKembalian() {
        if (lblKembalian == null || txtBayar == null) return;
        try {
            String cleanVal = txtBayar.getText().replaceAll("[^0-9]", "");
            double bayar  = cleanVal.isEmpty() ? 0 : Double.parseDouble(cleanVal);
            double kembali = bayar - totalBelanja;
            lblKembalian.setText("Rp " + nfIndo.format(kembali));
            lblKembalian.setStyle(kembali >= 0
                    ? "-fx-text-fill: #2ecc71; -fx-font-weight: bold;"
                    : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } catch (Exception e) {
            lblKembalian.setText("Rp 0");
        }
    }

    // --- CHECKOUT & SIMPAN TRANSAKSI ---
    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            showAlert("Peringatan", "Keranjang belanja masih kosong!");
            return;
        }

        String cleanVal = txtBayar.getText().replaceAll("[^0-9]", "");
        double nominalBayar = cleanVal.isEmpty() ? 0 : Double.parseDouble(cleanVal);

        if (nominalBayar < totalBelanja) {
            showAlert("Gagal", "Uang pembayaran tidak cukup!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Kasir/Pop Up Simpan & Cetak.fxml"));
            Parent root = loader.load();

            PopUpSimpanCetakController controller = loader.getController();
            controller.setData(totalBelanja, nominalBayar);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(btnSimpanCetak.getScene().getWindow());
            popupStage.initStyle(StageStyle.UNDECORATED);
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();

            if (controller.isConfirmed()) {
                simpanTransaksi();
            }

        } catch (IOException e) {
            showAlert("Error", "Gagal memuat popup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void simpanTransaksi() {
        try {
            String idTransaksi = TransaksiDAO.getNextIdTransaksi();
            String idUser = UserSession.getInstance().getUserId();

            Transaksi trx = new Transaksi();
            trx.setIdTransaksi(idTransaksi);
            trx.setTglTransaksi(LocalDateTime.now());
            trx.setIdUser(idUser);
            trx.setTotal(totalBelanja);

            boolean trxSaved = TransaksiDAO.insertTransaksi(trx);
            if (!trxSaved) {
                showAlert("Error", "Gagal menyimpan data transaksi ke database!");
                return;
            }

            for (Detail_Transaksi item : cartItems) {
                String idDetail = DetailTransaksiDAO.getNextIdDetail();
                item.setIdDetail(idDetail);
                item.setIdTransaksi(idTransaksi);
                DetailTransaksiDAO.insertDetail(item);
                
                // Kurangi stok di database
                BarangDAO.reduceStok(item.getIdBarang(), item.getJumlah());
            }

            showAlert("Sukses", "Transaksi Berhasil Disimpan!");

            cartItems.clear();
            if (txtBayar != null)        txtBayar.clear();
            totalBelanja = 0;
            if (lblTotalBelanja != null) lblTotalBelanja.setText("Rp 0");
            if (lblKembalian != null)    lblKembalian.setText("Rp 0");
            if (lblKembalian != null)    lblKembalian.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");

            loadProducts(MainController.isDarkMode);
            updateCartUI();

        } catch (Exception e) {
            showAlert("Error Database", "Gagal menyimpan transaksi: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // --- PENCARIAN PRODUK ---
    private void setupSearch(boolean isDarkMode) {
        if (txtSearch == null) return;
        txtSearch.textProperty().addListener((obs, old, newVal) -> {
            filterProducts(newVal, isDarkMode);
        });
    }

    // --- POPUP NOTIFIKASI ---
    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
