package Controller;

import javafx.animation.FadeTransition;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import DAO.BarangDAO;
import DAO.TransaksiDAO;
import model.Barang;
import model.Detail_Transaksi;
import model.Transaksi;
import config.UserSession;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class KasirDashboardController {
    private enum SortField {
        NONE,
        NAMA,
        STOK,
        HARGA
    }

    // --- KOMPONEN UI (Sesuai ID di FXML) ---
    @FXML private AnchorPane paneRoot;
    @FXML private VBox vboxSidebar, vboxMainContent, vboxProductCard, vboxCart, vboxProdukList, vboxCartList;
    @FXML private HBox hboxThemeToggle, hboxSearch;
    @FXML private GridPane hboxProductHeader, hboxCartHeader;
    @FXML private ScrollPane scrollProduct, scrollCart;
    @FXML private Label lblLogo, lblTanggal, lblListProduk, lblHeaderNama, lblHeaderStok, lblHeaderHarga, lblKeranjangBelanja;
    @FXML private Label lblCartHeaderItem, lblCartHeaderQty, lblCartHeaderHarga, lblCartHeaderSubtotal;
    @FXML private Label lblTotalBelanjaText, lblTotalBelanja, lblKembalianText, lblKembalian, lblSearchIcon;
    @FXML private TextField txtSearch, txtBayar;
    @FXML private ImageView imgLogo, imgLightMode, imgDarkMode, imgLogout;
    @FXML private Button btnLightMode, btnDarkMode, btnLogout, btnSimpanCetak, btnTransaksi;

    // --- DATA STATE ---
    private List<Barang> allBarang;
    private final ObservableList<Detail_Transaksi> cartItems = FXCollections.observableArrayList();
    private double totalBelanja = 0;
    private KasirDashboardController currentContentController;
    private boolean isThemeTransitionRunning = false;
    private SortField activeSortField = SortField.NONE;
    private boolean sortAscending = true;

    private final NumberFormat nfIndo = NumberFormat.getInstance(new Locale("id", "ID"));

    // --- PENGATURAN AWAL ---
    @FXML
    public void initialize() {
        nfIndo.setMaximumFractionDigits(0);
        boolean isDarkMode = MainController.isDarkMode;

        if (vboxSidebar != null) {
            // Jika kita berada di Dashboard (Shell/Layout utama)
            setupSidebarActions();
            // Load TransaksiView sebagai halaman default
            loadPage("/FXML/Kasir/TransaksiView.fxml");
        } else {
            // Jika kita berada di dalam view konten (seperti TransaksiView)
            setupSortHeaders();
            loadProducts(isDarkMode);
            setupSearch();
            setupPayment();
            setupRealTimeClock();
        }

        setDarkMode(isDarkMode);
    }

    private void setupSidebarActions() {
        if (btnTransaksi != null) {
            btnTransaksi.setOnAction(e -> loadPage("/FXML/Kasir/TransaksiView.fxml"));
            btnTransaksi.setOnMouseEntered(e -> updateTransaksiButtonStyle(MainController.isDarkMode, true));
            btnTransaksi.setOnMouseExited(e -> updateTransaksiButtonStyle(MainController.isDarkMode, false));
        }
        if (btnLightMode != null) btnLightMode.setOnAction(e -> animateThemeTransition(false));
        if (btnDarkMode != null)  btnDarkMode.setOnAction(e -> animateThemeTransition(true));
        if (btnLogout != null)    setupLogout();
    }

    private void loadPage(String fxmlPath) {
        if (vboxMainContent == null) return;
        try {
            vboxMainContent.getChildren().clear();
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("Halaman tidak ditemukan: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Agar konten mengisi seluruh area VBox
            VBox.setVgrow(root, Priority.ALWAYS);

            vboxMainContent.getChildren().add(root);

            // Sinkronisasi tema ke controller yang baru dimuat
            currentContentController = loader.getController();
            if (currentContentController != null) {
                currentContentController.setDarkMode(MainController.isDarkMode);
            }
        } catch (IOException e) {
            System.err.println("Gagal memuat halaman: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // --- LOGOUT ---
    private void setupLogout() {
        btnLogout.setOnAction(e -> showLogoutConfirmationPopup());
    }

    private void showLogoutConfirmationPopup() {
        Stage owner = (Stage) btnLogout.getScene().getWindow();
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setResizable(false);

        StackPane root = new StackPane();
        root.getStyleClass().add("kasir-logout-dialog-root");
        setStyleClass(root, "dark", MainController.isDarkMode);

        VBox card = new VBox();
        card.getStyleClass().add("kasir-logout-dialog-card");
        card.setMinWidth(460);
        card.setPrefWidth(460);
        card.setMaxWidth(460);

        HBox body = new HBox(12);
        body.getStyleClass().add("kasir-logout-dialog-body");
        body.setAlignment(Pos.TOP_LEFT);

        StackPane iconCircle = new StackPane();
        iconCircle.getStyleClass().add("kasir-logout-dialog-icon");
        Label iconText = new Label("?");
        iconText.getStyleClass().add("kasir-logout-dialog-icon-text");
        iconCircle.getChildren().add(iconText);

        VBox textBox = new VBox(8);
        textBox.setAlignment(Pos.TOP_LEFT);
        Label title = new Label("Konfirmasi Logout");
        title.getStyleClass().add("kasir-logout-dialog-title");
        Label message = new Label("Anda yakin ingin keluar dari halaman kasir?");
        message.getStyleClass().add("kasir-logout-dialog-message");
        message.setWrapText(true);
        message.setMaxWidth(340);
        textBox.getChildren().addAll(title, message);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("X");
        closeButton.getStyleClass().add("kasir-logout-dialog-close");
        HBox.setMargin(closeButton, new Insets(-12, -2, 0, 0));
        closeButton.setOnAction(e -> dialog.close());

        body.getChildren().addAll(iconCircle, textBox, spacer, closeButton);

        HBox footer = new HBox(18);
        footer.getStyleClass().add("kasir-logout-dialog-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = new Button("Batal");
        cancelButton.getStyleClass().add("kasir-logout-dialog-cancel");
        cancelButton.setOnAction(e -> dialog.close());

        Button confirmButton = new Button("Ya, Keluar");
        confirmButton.getStyleClass().add("kasir-logout-dialog-confirm");
        confirmButton.setOnAction(e -> {
            dialog.close();
            performLogout();
        });

        footer.getChildren().addAll(cancelButton, confirmButton);
        card.getChildren().addAll(body, footer);
        root.getChildren().add(card);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        URL css = getClass().getResource("/CSS/kasir-scroll.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                dialog.close();
            }
        });

        dialog.setScene(scene);
        dialog.setOnShown(e -> {
            dialog.setX(owner.getX() + (owner.getWidth() - dialog.getWidth()) / 2);
            dialog.setY(owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2);
        });
        dialog.showAndWait();
    }

    private void performLogout() {
        try {
            UserSession.getInstance().logout();
            Stage stage = (Stage) btnLogout.getScene().getWindow();

            URL loginView = getClass().getResource("/FXML/LoginView.fxml");
            if (loginView == null) {
                System.err.println("Halaman login tidak ditemukan.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(loginView);
            Parent root = loader.load();
            stage.setResizable(true);
            stage.setMaximized(false);
            stage.setScene(new Scene(root));
            stage.setTitle("PMI Toko Zikry - Login");
            stage.show();
            stage.setMaximized(true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // --- DARK MODE ---
    public void setDarkMode(boolean enabled) {
        MainController.isDarkMode = enabled;
        setStyleClass(paneRoot, "dark", enabled);
        setStyleClass(scrollProduct, "dark", enabled);
        setStyleClass(scrollCart, "dark", enabled);

        // Propagasi ke controller konten yang sedang aktif
        if (currentContentController != null) {
            currentContentController.setDarkMode(enabled);
        }

        String labelMuted = enabled ? "-fx-text-fill: #bbbbbb;" : "-fx-text-fill: #9a9a9a;";

        clearInlineStyles(paneRoot, vboxSidebar, vboxMainContent);
        clearInlineStyles(lblLogo, lblTanggal, lblListProduk, lblKeranjangBelanja);
        clearInlineStyles(hboxSearch, txtSearch);
        if (lblSearchIcon != null) lblSearchIcon.setStyle(labelMuted);

        clearInlineStyles(vboxProductCard, hboxProductHeader, lblHeaderNama, lblHeaderStok, lblHeaderHarga);
        clearInlineStyles(scrollProduct, vboxProdukList);
        clearInlineStyles(vboxCart, hboxCartHeader, lblCartHeaderItem, lblCartHeaderQty, lblCartHeaderHarga, lblCartHeaderSubtotal);
        clearInlineStyles(scrollCart, vboxCartList);
        clearInlineStyles(lblTotalBelanjaText, lblTotalBelanja, lblKembalianText, lblKembalian, txtBayar, hboxThemeToggle, btnLogout, btnSimpanCetak);

        if (btnLightMode != null)    btnLightMode.setStyle("-fx-background-color: " + (enabled ? "transparent" : "#efefef") + "; -fx-background-radius: 18; -fx-cursor: hand;");
        if (btnDarkMode != null)     btnDarkMode.setStyle("-fx-background-color: " + (enabled ? "#444444" : "transparent") + "; -fx-background-radius: 18; -fx-cursor: hand;");
        if (btnTransaksi != null)    updateTransaksiButtonStyle(enabled, btnTransaksi.isHover());

        setImageIfPresent(imgLogo, enabled ? "/Images/LOGO2.png" : "/Images/LOGO.png");
        setImageIfPresent(imgLightMode, enabled ? "/Images/ICON3DARK.png" : "/Images/ICON3.png");
        setImageIfPresent(imgDarkMode, enabled ? "/Images/ICON4DARK.png" : "/Images/ICON4.png");
        setImageIfPresent(imgLogout, enabled ? "/Images/ICON33.png" : "/Images/ICON6.png");

        displayProducts(allBarang, enabled);
        updateCartUI();
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

    private void animateThemeTransition(boolean enabled) {
        if (MainController.isDarkMode == enabled || isThemeTransitionRunning) return;
        if (paneRoot == null) {
            setDarkMode(enabled);
            return;
        }

        isThemeTransitionRunning = true;
        setThemeButtonsDisabled(true);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(170), paneRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.72);
        fadeOut.setInterpolator(Interpolator.EASE_BOTH);
        fadeOut.setOnFinished(event -> {
            setDarkMode(enabled);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(170), paneRoot);
            fadeIn.setFromValue(0.72);
            fadeIn.setToValue(1.0);
            fadeIn.setInterpolator(Interpolator.EASE_BOTH);
            fadeIn.setOnFinished(finishEvent -> {
                isThemeTransitionRunning = false;
                setThemeButtonsDisabled(false);
            });
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void setThemeButtonsDisabled(boolean disabled) {
        if (btnLightMode != null) btnLightMode.setDisable(disabled);
        if (btnDarkMode != null) btnDarkMode.setDisable(disabled);
    }

    private void updateTransaksiButtonStyle(boolean darkMode, boolean hovered) {
        if (btnTransaksi == null) return;

        String backgroundColor;
        String borderColor;
        String textColor;

        if (darkMode) {
            backgroundColor = hovered ? "#202020" : "#151515";
            borderColor = hovered ? "#353535" : "#2a2a2a";
            textColor = "#4da3ff";
        } else {
            backgroundColor = hovered ? "#cfe1f5" : "#dce9f7";
            borderColor = hovered ? "#c1d7ef" : "transparent";
            textColor = "#3b6ea7";
        }

        btnTransaksi.setStyle(
                "-fx-background-color: " + backgroundColor + "; "
                        + "-fx-background-radius: 10; "
                        + "-fx-border-color: " + borderColor + "; "
                        + "-fx-border-width: 1; "
                        + "-fx-border-radius: 10; "
                        + "-fx-text-fill: " + textColor + "; "
                        + "-fx-font-size: 12px; "
                        + "-fx-font-weight: normal; "
                        + "-fx-padding: 0 0 0 10; "
                        + "-fx-cursor: hand;"
        );
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
            displayProducts(getSortedProducts(allBarang), isDarkMode);
        }
    }

    private void filterProducts(String query, boolean isDarkMode) {
        if (allBarang == null) return;
        List<Barang> filtered = allBarang.stream()
                .filter(b -> b.getNamaBarang().toLowerCase().contains(query.toLowerCase())
                        || b.getIdBarang().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        displayProducts(getSortedProducts(filtered), isDarkMode);
    }

    private void setupSortHeaders() {
        if (lblHeaderNama != null) {
            lblHeaderNama.setOnMouseClicked(event -> toggleSort(SortField.NAMA));
        }
        if (lblHeaderStok != null) {
            lblHeaderStok.setOnMouseClicked(event -> toggleSort(SortField.STOK));
        }
        if (lblHeaderHarga != null) {
            lblHeaderHarga.setOnMouseClicked(event -> toggleSort(SortField.HARGA));
        }
        updateSortHeaderText();
    }

    private void toggleSort(SortField sortField) {
        if (activeSortField == sortField) {
            sortAscending = !sortAscending;
        } else {
            activeSortField = sortField;
            sortAscending = true;
        }

        updateSortHeaderText();
        if (txtSearch != null && !txtSearch.getText().trim().isEmpty()) {
            filterProducts(txtSearch.getText(), MainController.isDarkMode);
        } else {
            displayProducts(getSortedProducts(allBarang), MainController.isDarkMode);
        }
    }

    private List<Barang> getSortedProducts(List<Barang> products) {
        if (products == null) {
            return List.of();
        }

        Comparator<Barang> comparator;
        switch (activeSortField) {
            case NONE:
                return products;
            case STOK:
                comparator = Comparator.comparingInt(this::getDisplayStock)
                        .thenComparing(barang -> barang.getNamaBarang().toLowerCase());
                break;
            case HARGA:
                comparator = Comparator.comparingDouble(Barang::getHargaJual)
                        .thenComparing(barang -> barang.getNamaBarang().toLowerCase());
                break;
            case NAMA:
            default:
                comparator = Comparator.comparing(barang -> barang.getNamaBarang().toLowerCase());
                break;
        }

        if (!sortAscending) {
            comparator = comparator.reversed();
        }

        return products.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private int getDisplayStock(Barang barang) {
        if (barang == null) {
            return 0;
        }

        String barangId = barang.getIdBarang() == null ? "" : barang.getIdBarang().trim();
        int qtyInCart = 0;
        for (Detail_Transaksi item : cartItems) {
            String itemBarangId = item.getIdBarang() == null ? "" : item.getIdBarang().trim();
            if (itemBarangId.equalsIgnoreCase(barangId)) {
                qtyInCart += item.getJumlah();
            }
        }

        return barang.getStok() - qtyInCart;
    }

    private void updateSortHeaderText() {
        if (lblHeaderNama != null) {
            lblHeaderNama.setText(buildHeaderText("Nama Produk", SortField.NAMA));
        }
        if (lblHeaderStok != null) {
            lblHeaderStok.setText(buildHeaderText("Stok", SortField.STOK));
        }
        if (lblHeaderHarga != null) {
            lblHeaderHarga.setText(buildHeaderText("Harga", SortField.HARGA));
        }
    }

    private String buildHeaderText(String title, SortField field) {
        if (activeSortField == SortField.NONE) {
            return title;
        }

        if (activeSortField != field) {
            return title + " ↕";
        }

        return title + (sortAscending ? " ↑" : " ↓");
    }

    // --- TAMPILKAN LIST PRODUK ---
    // FIX: Hitung stok yang tersedia = stok DB - qty yang sudah di keranjang
    private void displayProducts(List<Barang> products, boolean isDarkMode) {
        if (vboxProdukList == null) return;
        vboxProdukList.getChildren().clear();
        if (products == null) return;
        for (Barang barang : products) {
            int displayStok = getDisplayStock(barang);

            vboxProdukList.getChildren().add(createProductRow(barang, displayStok));

            Region line = new Region();
            line.setMinHeight(1);
            line.setMaxHeight(1);
            line.setStyle("-fx-background-color: " + (isDarkMode ? "#333333" : "#EEEEEE") + ";");
            vboxProdukList.getChildren().add(line);
        }
    }

    // --- DESAIN BARIS PRODUK ---
    private GridPane createProductRow(Barang barang, int displayStok) {
        GridPane row = new GridPane();
        row.getStyleClass().add("kasir-product-row");
        row.setHgap(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefHeight(55);
        row.setPadding(new Insets(5, 20, 5, 20));
        row.getColumnConstraints().addAll(
                createProductColumnConstraint(-1, Priority.ALWAYS),
                createProductColumnConstraint(80, Priority.NEVER),
                createProductColumnConstraint(120, Priority.NEVER),
                createProductColumnConstraint(90, Priority.NEVER)
        );

        Label name = new Label(barang.getNamaBarang());
        name.getStyleClass().add("kasir-product-name");
        name.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(name, Priority.ALWAYS);

        // Tampilkan stok yang sudah dikurangi qty keranjang
        Label stok = new Label(String.valueOf(displayStok));
        stok.getStyleClass().add("kasir-product-stock");
        setStyleClass(stok, "kasir-stock-empty", displayStok <= 0);
        stok.setMinWidth(80);
        stok.setPrefWidth(80);
        stok.setMaxWidth(80);
        stok.setAlignment(Pos.CENTER);

        Label harga = new Label("Rp " + nfIndo.format(barang.getHargaJual()));
        harga.getStyleClass().add("kasir-product-price");
        harga.setMinWidth(120);
        harga.setPrefWidth(120);
        harga.setMaxWidth(120);
        harga.setAlignment(Pos.CENTER);

        Button btnAdd = new Button(displayStok <= 0 ? "Habis" : "+ Tambah");
        btnAdd.getStyleClass().add("kasir-add-button");
        btnAdd.setMinWidth(90);
        btnAdd.setPrefWidth(90);
        btnAdd.setMaxWidth(90);
        btnAdd.setMinHeight(30);
        btnAdd.setMaxHeight(30);
        // Disable tombol jika stok sudah habis
        btnAdd.setDisable(displayStok <= 0);
        btnAdd.setOnAction(e -> addToCart(barang));

        row.add(name, 0, 0);
        row.add(stok, 1, 0);
        row.add(harga, 2, 0);
        row.add(btnAdd, 3, 0);
        return row;
    }

    private ColumnConstraints createProductColumnConstraint(double width, Priority hgrow) {
        ColumnConstraints column = new ColumnConstraints();
        column.setHgrow(hgrow);
        if (width > 0) {
            column.setMinWidth(width);
            column.setPrefWidth(width);
            column.setMaxWidth(width);
        }
        return column;
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
        for (Detail_Transaksi item : cartItems) {
            Barang b = findBarangById(item.getIdBarang());
            if (b != null) {
                totalBelanja += item.getSubtotal();
                vboxCartList.getChildren().add(createCartRow(item, b));

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
    private GridPane createCartRow(Detail_Transaksi item, Barang barang) {
        GridPane row = new GridPane();
        row.getStyleClass().add("kasir-cart-row");
        row.setHgap(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 16, 9, 18));
        row.setMinHeight(56);
        row.setPrefHeight(56);
        row.setMaxWidth(Double.MAX_VALUE);
        row.getColumnConstraints().addAll(
                createCartColumnConstraint(-1, Priority.ALWAYS),
                createCartColumnConstraint(62, Priority.NEVER),
                createCartColumnConstraint(84, Priority.NEVER),
                createCartColumnConstraint(88, Priority.NEVER)
        );

        // Nama Barang
        Label name = new Label(barang.getNamaBarang());
        name.getStyleClass().add("kasir-cart-name");
        name.setMinWidth(100);
        name.setMaxWidth(Double.MAX_VALUE);
        name.setAlignment(Pos.CENTER_LEFT);
        GridPane.setHgrow(name, Priority.ALWAYS);
        GridPane.setValignment(name, VPos.CENTER);
        name.setWrapText(true);

        // Kontrol Qty (tombol - angka +)
        HBox qtyBox = new HBox(0);
        qtyBox.getStyleClass().add("kasir-qty-box");
        qtyBox.setAlignment(Pos.CENTER);
        qtyBox.setMinWidth(58);
        qtyBox.setMaxWidth(58);
        qtyBox.setPrefWidth(58);
        qtyBox.setPrefHeight(24);
        qtyBox.setMinHeight(24);
        qtyBox.setMaxHeight(24);
        GridPane.setHalignment(qtyBox, HPos.CENTER);
        GridPane.setValignment(qtyBox, VPos.CENTER);

        Button btnMinus = new Button("-");
        btnMinus.getStyleClass().add("kasir-qty-button");
        btnMinus.setMinWidth(18);
        btnMinus.setPrefWidth(18);
        btnMinus.setPrefHeight(24);

        Label lblQty = new Label(String.valueOf(item.getJumlah()));
        lblQty.getStyleClass().add("kasir-qty-label");
        lblQty.setMinWidth(22);
        lblQty.setPrefWidth(22);
        lblQty.setAlignment(Pos.CENTER);

        Button btnPlus = new Button("+");
        btnPlus.getStyleClass().add("kasir-qty-button");
        btnPlus.setMinWidth(18);
        btnPlus.setPrefWidth(18);
        btnPlus.setPrefHeight(24);

        btnMinus.setOnAction(e -> handleMinus(item));
        btnPlus.setOnAction(e -> handlePlus(item));

        qtyBox.getChildren().addAll(btnMinus, lblQty, btnPlus);

        // Harga satuan
        Label harga = new Label("Rp " + nfIndo.format(item.getHargaSatuan()));
        harga.getStyleClass().add("kasir-cart-price");
        harga.setMinWidth(84);
        harga.setPrefWidth(84);
        harga.setMaxWidth(84);
        harga.setAlignment(Pos.CENTER_RIGHT);
        GridPane.setHalignment(harga, HPos.RIGHT);
        GridPane.setValignment(harga, VPos.CENTER);

        Label sub = new Label("Rp " + nfIndo.format(item.getSubtotal()));
        sub.getStyleClass().add("kasir-cart-subtotal");
        sub.setMinWidth(88);
        sub.setPrefWidth(88);
        sub.setMaxWidth(88);
        sub.setAlignment(Pos.CENTER_RIGHT);
        GridPane.setHalignment(sub, HPos.RIGHT);
        GridPane.setValignment(sub, VPos.CENTER);

        row.add(name, 0, 0);
        row.add(qtyBox, 1, 0);
        row.add(harga, 2, 0);
        row.add(sub, 3, 0);
        return row;
    }

    private ColumnConstraints createCartColumnConstraint(double width, Priority hgrow) {
        ColumnConstraints column = new ColumnConstraints();
        column.setHgrow(hgrow);
        if (width > 0) {
            column.setMinWidth(width);
            column.setPrefWidth(width);
            column.setMaxWidth(width);
        }
        return column;
    }

    // --- KURANGI QTY DI KERANJANG ---
    private void handleMinus(Detail_Transaksi item) {
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
    private void handlePlus(Detail_Transaksi item) {
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
            applyChangeState(lblKembalian, kembali);
        } catch (Exception e) {
            lblKembalian.setText("Rp 0");
            applyChangeState(lblKembalian, 0);
        }
    }

    private void applyChangeState(Label label, double value) {
        setStyleClass(label, "kasir-change-positive", value >= 0);
        setStyleClass(label, "kasir-change-negative", value < 0);
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
            URL popupView = getClass().getResource("/FXML/Kasir/Pop Up Simpan & Cetak.fxml");
            if (popupView == null) {
                showAlert("Error", "Popup simpan & cetak tidak ditemukan.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(popupView);
            Parent root = loader.load();

            PopUpSimpanCetakController controller = loader.getController();
            controller.setData(totalBelanja, nominalBayar);
            controller.setDarkMode(MainController.isDarkMode);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(btnSimpanCetak.getScene().getWindow());
            popupStage.initStyle(StageStyle.UNDECORATED);
            popupStage.setScene(new Scene(root));
            popupStage.centerOnScreen();
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

            if (idUser == null || idUser.trim().isEmpty()) {
                showAlert("Error", "Session user tidak ditemukan. Silakan login ulang.");
                return;
            }

            Transaksi trx = new Transaksi();
            trx.setIdTransaksi(idTransaksi);
            trx.setTglTransaksi(LocalDateTime.now());
            trx.setIdUser(idUser);
            trx.setTotal(totalBelanja);

            boolean trxSaved = TransaksiDAO.saveTransaksiWithDetails(trx, new java.util.ArrayList<>(cartItems));
            if (!trxSaved) {
                showAlert("Error", "Gagal menyimpan transaksi. Periksa stok barang dan coba lagi.");
                return;
            }

            showAlert("Sukses", "Transaksi Berhasil Disimpan!");

            cartItems.clear();
            if (txtBayar != null)        txtBayar.clear();
            totalBelanja = 0;
            if (lblTotalBelanja != null) lblTotalBelanja.setText("Rp 0");
            if (lblKembalian != null)    lblKembalian.setText("Rp 0");
            if (lblKembalian != null)    applyChangeState(lblKembalian, 0);

            loadProducts(MainController.isDarkMode);
            updateCartUI();

        } catch (Exception e) {
            showAlert("Error Database", "Gagal menyimpan transaksi: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // --- PENCARIAN PRODUK ---
    private void setupSearch() {
        if (txtSearch == null) return;
        txtSearch.focusedProperty().addListener((obs, wasFocused, isFocused) -> setStyleClass(hboxSearch, "focused", isFocused));
        txtSearch.textProperty().addListener((obs, old, newVal) -> {
            filterProducts(newVal, MainController.isDarkMode);
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
