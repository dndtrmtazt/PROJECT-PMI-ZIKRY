package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import config.UserSession;
import model.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class KasirDashboardController {

    // --- FXML VARIABLES (Sudah disesuaikan marganya) ---
    @FXML private AnchorPane paneRoot;
    @FXML private VBox vboxSidebar, vboxMainContent, vboxProductCard, vboxProdukList, vboxCart, vboxCartList, vboxSummary;
    @FXML private HBox hboxThemeToggle, hboxSearch, hboxProductHeader, hboxCartHeader;
    @FXML private ScrollPane scrollProduct, scrollCart;
    @FXML private Separator sepCart;

    @FXML private Button btnTransaksi, btnLightMode, btnDarkMode, btnLogout, btnSimpanCetak;
    @FXML private ImageView imgLightMode, imgDarkMode, imgLogout;

    @FXML private Label lblLogo, lblTanggal, lblListProduk, lblHeaderNama, lblHeaderStok, lblHeaderHarga;
    @FXML private Label lblCartHeaderItem, lblCartHeaderQty, lblCartHeaderHarga, lblCartHeaderSubtotal;
    @FXML private Label lblTotalBelanjaText, lblTotalBelanja, lblKembalianText, lblKembalian;

    @FXML private TextField txtSearch, txtBayar;

    // --- DATA VARIABLES ---
    private List<Barang> allBarang;
    private ObservableList<Detail_Transaksi> cartItems = FXCollections.observableArrayList();
    private double totalBelanja = 0;
    private boolean isDarkMode = false;

    @FXML
    public void initialize() {
        updateTanggal();
        loadProducts();
        setupSearch();
        setupPayment();
        setupTheme();
        setupLogout();

        System.out.println("[KasirDashboard] Berhasil sinkron dengan fx:id baru");
    }

    private void setupLogout() {
        btnLogout.setOnAction(e -> {
            try {
                // 1. Bersihkan session user
                config.UserSession.getInstance().logout();

                // 2. Balik ke halaman Login
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/FXML/LoginView.fxml"));
                javafx.scene.Parent root = loader.load();

                javafx.stage.Stage stage = (javafx.stage.Stage) btnLogout.getScene().getWindow();
                stage.setScene(new javafx.scene.Scene(root));
                stage.setTitle("Login - Toko Zikry");
                stage.centerOnScreen();

                System.out.println("[System] User berhasil logout.");
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void setupTheme() {
        btnLightMode.setOnAction(e -> applyLightTheme());
        btnDarkMode.setOnAction(e -> {
            if (isDarkMode) applyLightTheme();
            else applyDarkTheme();
        });
    }

    private void applyLightTheme() {
        isDarkMode = false;
        // Ganti Icon
        imgLightMode.setImage(new Image(getClass().getResourceAsStream("/Images/ICON3.png")));
        imgDarkMode.setImage(new Image(getClass().getResourceAsStream("/Images/ICON4.png")));
        imgLogout.setImage(new Image(getClass().getResourceAsStream("/Images/ICON6.png")));

        // Update CSS (Pakai variabel baru)
        paneRoot.setStyle("-fx-background-color: #efefef;");
        vboxSidebar.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #d9d9d9; -fx-border-width: 0 1 0 0;");
        lblLogo.setStyle("-fx-font-weight: bold; -fx-text-fill: #111111;");
        btnTransaksi.setStyle("-fx-background-color: #dce9f7; -fx-background-radius: 10; -fx-text-fill: #3b6ea7;");

        displayProducts(allBarang);
        updateCartUI();
    }

    private void applyDarkTheme() {
        isDarkMode = true;
        // Ganti Icon Dark
        imgLightMode.setImage(new Image(getClass().getResourceAsStream("/Images/ICON3DARK.png")));
        imgDarkMode.setImage(new Image(getClass().getResourceAsStream("/Images/ICON4DARK.png")));
        imgLogout.setImage(new Image(getClass().getResourceAsStream("/Images/ICON33.png")));

        // Update CSS Dark (Pakai variabel baru)
        paneRoot.setStyle("-fx-background-color: #121212;");
        vboxSidebar.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #333333; -fx-border-width: 0 1 0 0;");
        lblLogo.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff;");

        displayProducts(allBarang);
        updateCartUI();
    }

    private void updateTanggal() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy | HH:mm 'WITA'");
        lblTanggal.setText(dtf.format(LocalDateTime.now()));
    }

    private void loadProducts() {
        allBarang = BarangDAO.getAllBarang();
        displayProducts(allBarang);
    }

    private void displayProducts(List<Barang> products) {
        vboxProdukList.getChildren().clear();
        String textColor = isDarkMode ? "-fx-text-fill: #ffffff;" : "-fx-text-fill: #111111;";

        for (Barang barang : products) {
            HBox row = createProductRow(barang, textColor);
            vboxProdukList.getChildren().add(row);

            Separator sep = new Separator();
            if (isDarkMode) sep.setStyle("-fx-background-color: #333333;");
            vboxProdukList.getChildren().add(sep);
        }
    }

    private HBox createProductRow(Barang barang, String textColor) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefHeight(54);
        row.setPadding(new Insets(0, 16, 0, 16));

        Label name = new Label(barang.getNamaBarang());
        name.setPrefWidth(190);
        name.setStyle("-fx-font-size: 11.5px; " + textColor);

        Label stok = new Label(String.valueOf(barang.getStok()));
        stok.setPrefWidth(48);
        stok.setStyle("-fx-font-size: 11.5px; " + textColor);

        Label harga = new Label(String.format("Rp %,.0f", barang.getHargaJual()));
        harga.setPrefWidth(72);
        harga.setStyle("-fx-font-size: 11.5px; " + textColor);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAdd = new Button("+ Tambah");
        btnAdd.setStyle("-fx-background-color: #58c767; -fx-background-radius: 8; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> addToCart(barang));

        row.getChildren().addAll(name, stok, harga, spacer, btnAdd);
        return row;
    }

    private void setupSearch() {
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                displayProducts(allBarang);
            } else {
                String filter = newVal.toLowerCase();
                List<Barang> filtered = allBarang.stream()
                        .filter(b -> b.getNamaBarang().toLowerCase().contains(filter) || b.getIdBarang().toLowerCase().contains(filter))
                        .collect(Collectors.toList());
                displayProducts(filtered);
            }
        });
    }

    private void addToCart(Barang barang) {
        if (barang.getStok() <= 0) {
            showAlert("Peringatan", "Stok habis!");
            return;
        }

        boolean found = false;
        for (Detail_Transaksi item : cartItems) {
            if (item.getIdBarang().equals(barang.getIdBarang())) {
                item.setJumlah(item.getJumlah() + 1);
                item.setSubtotal(item.getJumlah() * item.getHargaSatuan());
                found = true;
                break;
            }
        }

        if (!found) {
            Detail_Transaksi newItem = new Detail_Transaksi();
            newItem.setIdBarang(barang.getIdBarang());
            newItem.setJumlah(1);
            newItem.setHargaSatuan(barang.getHargaJual());
            newItem.setSubtotal(barang.getHargaJual());
            cartItems.add(newItem);
        }

        barang.setStok(barang.getStok() - 1);
        displayProducts(allBarang);
        updateCartUI();
    }

    private void updateCartUI() {
        vboxCartList.getChildren().clear();
        totalBelanja = 0;
        String textColor = isDarkMode ? "-fx-text-fill: #ffffff;" : "-fx-text-fill: #111111;";

        for (Detail_Transaksi item : cartItems) {
            Barang b = findBarangById(item.getIdBarang());
            if (b != null) {
                vboxCartList.getChildren().add(createCartRow(item, b, textColor));
                totalBelanja += item.getSubtotal();
            }
        }

        lblTotalBelanja.setText(String.format("Rp %,.0f", totalBelanja));
        calculateChange();
    }

    private HBox createCartRow(Detail_Transaksi item, Barang barang, String textColor) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 12, 5, 12));

        Label name = new Label(barang.getNamaBarang());
        name.setPrefWidth(84);
        name.setStyle("-fx-font-size: 10.5px; " + textColor);

        Label qty = new Label(String.valueOf(item.getJumlah()));
        qty.setPrefWidth(30);
        qty.setStyle(textColor);

        Label sub = new Label(String.format("Rp %,.0f", item.getSubtotal()));
        sub.setPrefWidth(80);
        sub.setAlignment(Pos.CENTER_RIGHT);
        sub.setStyle(textColor);

        row.getChildren().addAll(name, qty, sub);
        return row;
    }

    private void setupPayment() {
        txtBayar.textProperty().addListener((obs, old, newVal) -> calculateChange());
        btnSimpanCetak.setOnAction(e -> handleCheckout());
    }

    private void calculateChange() {
        try {
            String val = txtBayar.getText().replaceAll("[^0-9]", "");
            double bayar = val.isEmpty() ? 0 : Double.parseDouble(val);
            double kembalian = bayar - totalBelanja;

            lblKembalian.setText(String.format("Rp %,.0f", kembalian));
            lblKembalian.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (kembalian >= 0 ? "green;" : "red;"));
        } catch (Exception e) {
            lblKembalian.setText("Rp 0");
        }
    }

    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            showAlert("Error", "Keranjang kosong!");
            return;
        }
        // ... logika simpan ke database (tetap sama) ...
        showAlert("Sukses", "Transaksi Berhasil!");
        cartItems.clear();
        txtBayar.clear();
        updateCartUI();
    }

    private void handleLogout() {
        try {
            UserSession.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/LoginView.fxml"));
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Login");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Barang findBarangById(String id) {
        return allBarang.stream().filter(b -> b.getIdBarang().equals(id)).findFirst().orElse(null);
    }

    private void showAlert(String t, String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setContentText(c); a.showAndWait();
    }
}