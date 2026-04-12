package Controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import model.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class KasirDashboardController {

    // --- KOMPONEN UI (Sesuai ID di FXML) ---
    @FXML private VBox vboxProdukList, vboxCartList;
    @FXML private Label lblTotalBelanja, lblKembalian, lblTanggal;
    @FXML private TextField txtSearch, txtBayar;
    @FXML private Button btnSimpanCetak;

    // --- DATA STATE (Penyimpanan Data Sementara) ---
    private List<Barang> allBarang; // List semua barang dari DB
    private ObservableList<Detail_Transaksi> cartItems = FXCollections.observableArrayList(); // Isi keranjang
    private double totalBelanja = 0; // Variabel hitung total

    // --- PENGATURAN AWAL (Jalan saat halaman dibuka) ---
    @FXML
    public void initialize() {
        boolean isDarkMode = MainController.isDarkMode;
        loadProducts(isDarkMode); // Ambil barang dari DB
        setupSearch(isDarkMode);  // Aktifkan fitur cari
        setupPayment();           // Aktifkan fitur bayar & kembalian
        setupRealTimeClock();     // Jalankan jam detik berjalan
    }

    // --- FITUR JAM REAL-TIME (Update tiap 1 detik) ---
    private void setupRealTimeClock() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, d MMM yyyy | HH:mm:ss 'WITA'");
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            lblTanggal.setText(LocalDateTime.now().format(dtf));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    // --- LOAD DATA (Ambil data barang dari database) ---
    private void loadProducts(boolean isDarkMode) {
        allBarang = BarangDAO.getAllBarang();
        displayProducts(allBarang, isDarkMode);
    }

    // --- TAMPILKAN LIST PRODUK (Looping data ke VBox kiri) ---
    private void displayProducts(List<Barang> products, boolean isDarkMode) {
        vboxProdukList.getChildren().clear();
        String textColor = isDarkMode ? "-fx-text-fill: white;" : "-fx-text-fill: #111111;";

        for (Barang barang : products) {
            vboxProdukList.getChildren().add(createProductRow(barang, textColor));

            // Garis pembatas tipis antar produk
            Region line = new Region();
            line.setMinHeight(1); line.setMaxHeight(1);
            line.setStyle("-fx-background-color: " + (isDarkMode ? "#333333" : "#EEEEEE") + ";");
            vboxProdukList.getChildren().add(line);
        }
    }

    // --- DESAIN BARIS PRODUK (Bikin tampilan tiap item di list kiri) ---
    private HBox createProductRow(Barang barang, String textColor) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefHeight(55);
        row.setPadding(new Insets(5, 20, 5, 20));

        Label name = new Label(barang.getNamaBarang());
        name.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(name, Priority.ALWAYS); // Nama melar ngikutin layar
        name.setStyle(textColor + "-fx-font-size: 11.5px; -fx-font-weight: bold;");

        Label stok = new Label(String.valueOf(barang.getStok()));
        stok.setPrefWidth(65);
        stok.setStyle(textColor + "-fx-font-size: 11.5px;");

        Label harga = new Label(String.format("Rp %,.0f", barang.getHargaJual()));
        harga.setPrefWidth(110);
        harga.setStyle(textColor + "-fx-font-size: 11.5px; -fx-font-weight: bold;");

        Button btnAdd = new Button("+ Tambah");
        btnAdd.setMinWidth(90);
        btnAdd.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 10.5px; -fx-font-weight: bold; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> addToCart(barang)); // Klik tambah ke keranjang

        row.getChildren().addAll(name, stok, harga, btnAdd);
        return row;
    }

    // --- LOGIKA TAMBAH KE KERANJANG (Cek stok & gabung item yang sama) ---
    private void addToCart(Barang barang) {
        if (barang.getStok() <= 0) {
            showAlert("Stok Habis", "Maaf, stok habis!");
            return;
        }

        // Cari apakah barang sudah ada di keranjang
        Detail_Transaksi existing = cartItems.stream()
                .filter(item -> item.getIdBarang().equals(barang.getIdBarang()))
                .findFirst().orElse(null);

        if (existing != null) {
            existing.setJumlah(existing.getJumlah() + 1); // Tambah Qty
            existing.setSubtotal(existing.getJumlah() * existing.getHargaSatuan());
        } else {
            // Tambah barang baru ke keranjang
            Detail_Transaksi newItem = new Detail_Transaksi();
            newItem.setIdBarang(barang.getIdBarang());
            newItem.setJumlah(1);
            newItem.setHargaSatuan(barang.getHargaJual());
            newItem.setSubtotal(barang.getHargaJual());
            cartItems.add(newItem);
        }

        barang.setStok(barang.getStok() - 1); // Kurangi stok di UI
        displayProducts(allBarang, MainController.isDarkMode); // Refresh list kiri
        updateCartUI(); // Refresh list kanan
    }

    // --- UPDATE TAMPILAN KERANJANG (Looping isi keranjang ke VBox kanan) ---
    private void updateCartUI() {
        vboxCartList.getChildren().clear();
        totalBelanja = 0;
        String textColor = MainController.isDarkMode ? "-fx-text-fill: white;" : "-fx-text-fill: #111111;";

        for (Detail_Transaksi item : cartItems) {
            Barang b = findBarangById(item.getIdBarang());
            if (b != null) {
                totalBelanja += item.getSubtotal();
                vboxCartList.getChildren().add(createCartRow(item, b, textColor));

                // Garis pembatas antar item keranjang
                Region line = new Region();
                line.setMinHeight(1); line.setMaxHeight(1);
                line.setStyle("-fx-background-color: #EEEEEE;");
                vboxCartList.getChildren().add(line);
            }
        }
        lblTotalBelanja.setText(String.format("Rp %,.0f", totalBelanja)); // Tampil total
    }

    // --- DESAIN BARIS KERANJANG (Bikin tampilan item dengan tombol +/-) ---
    private HBox createCartRow(Detail_Transaksi item, Barang barang, String textColor) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setMinHeight(50); // Tinggi bisa melar kalau teks turun ke bawah

        // Nama Barang (Auto Wrap/Turun ke bawah kalau panjang)
        Label name = new Label(barang.getNamaBarang());
        name.setPrefWidth(84);
        name.setWrapText(true);
        name.setStyle(textColor + "-fx-font-size: 10.5px; -fx-line-spacing: -1px;");

        // Kontrol Qty (Desain Pill/Kotak)
        HBox qtyBox = new HBox(0);
        qtyBox.setAlignment(Pos.CENTER);
        qtyBox.setMinWidth(66); qtyBox.setMaxHeight(24);
        qtyBox.setStyle("-fx-border-color: #D1D5DB; -fx-border-radius: 4; -fx-background-color: white; -fx-background-radius: 4;");

        String commonStyle = "-fx-text-fill: black; -fx-font-size: 10.5px; -fx-padding: 0;";

        Button btnMinus = new Button("-");
        btnMinus.setMinWidth(22); btnMinus.setPrefHeight(24);
        btnMinus.setStyle("-fx-background-color: transparent; -fx-border-color: #D1D5DB; -fx-border-width: 0 1 0 0; " + commonStyle + "-fx-cursor: hand;");

        Label lblQty = new Label(String.valueOf(item.getJumlah()));
        lblQty.setMinWidth(22); lblQty.setAlignment(Pos.CENTER);
        lblQty.setStyle(commonStyle + "-fx-font-weight: bold;");

        Button btnPlus = new Button("+");
        btnPlus.setMinWidth(22); btnPlus.setPrefHeight(24);
        btnPlus.setStyle("-fx-background-color: transparent; -fx-border-color: #D1D5DB; -fx-border-width: 0 0 0 1; " + commonStyle + "-fx-cursor: hand;");

        // Logika tombol +/-
        btnMinus.setOnAction(e -> handleMinus(item, barang));
        btnPlus.setOnAction(e -> handlePlus(item, barang));

        qtyBox.getChildren().addAll(btnMinus, lblQty, btnPlus);
        StackPane qtyContainer = new StackPane(qtyBox);
        qtyContainer.setPrefWidth(75);

        Label harga = new Label(String.format("Rp%,.0f", item.getHargaSatuan()));
        harga.setPrefWidth(60); harga.setAlignment(Pos.CENTER_RIGHT);
        harga.setStyle(textColor + "-fx-font-size: 10px;");

        Label sub = new Label(String.format("Rp%,.0f", item.getSubtotal()));
        sub.setPrefWidth(80); sub.setAlignment(Pos.CENTER_RIGHT);
        sub.setStyle(textColor + "-fx-font-weight: bold; -fx-font-size: 10.5px;");

        row.getChildren().addAll(name, qtyContainer, harga, sub);
        return row;
    }

    // --- KURANGI QTY DI KERANJANG ---
    private void handleMinus(Detail_Transaksi item, Barang barang) {
        if (item.getJumlah() > 1) {
            item.setJumlah(item.getJumlah() - 1);
            item.setSubtotal(item.getJumlah() * item.getHargaSatuan());
            barang.setStok(barang.getStok() + 1); // Balikin stok
        } else {
            cartItems.remove(item); // Hapus kalau qty jadi 0
            barang.setStok(barang.getStok() + 1);
        }
        displayProducts(allBarang, MainController.isDarkMode);
        updateCartUI();
    }

    // --- TAMBAH QTY DI KERANJANG ---
    private void handlePlus(Detail_Transaksi item, Barang barang) {
        if (barang.getStok() > 0) {
            item.setJumlah(item.getJumlah() + 1);
            item.setSubtotal(item.getJumlah() * item.getHargaSatuan());
            barang.setStok(barang.getStok() - 1); // Kurangi stok
            displayProducts(allBarang, MainController.isDarkMode);
            updateCartUI();
        }
    }

    // --- HELPER: Cari barang berdasarkan ID ---
    private Barang findBarangById(String id) {
        return allBarang.stream().filter(b -> b.getIdBarang().equals(id)).findFirst().orElse(null);
    }

    // --- FITUR PEMBAYARAN (Kembalian otomatis saat ketik nominal) ---
    private void setupPayment() {
        txtBayar.textProperty().addListener((obs, old, newVal) -> {
            try {
                String cleanVal = newVal.replaceAll("[^0-9]", ""); // Hanya angka
                double bayar = cleanVal.isEmpty() ? 0 : Double.parseDouble(cleanVal);
                double kembali = bayar - totalBelanja;
                lblKembalian.setText(String.format("Rp %,.0f", kembali));
                lblKembalian.setStyle(kembali >= 0 ? "-fx-text-fill: #2ecc71; -fx-font-weight: bold;" : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } catch (Exception e) { lblKembalian.setText("Rp 0"); }
        });
        btnSimpanCetak.setOnAction(e -> handleCheckout());
    }

    // --- FINALISASI TRANSAKSI (Simpan & Bersihkan layar) ---
    private void handleCheckout() {
        if (cartItems.isEmpty()) return;
        showAlert("Sukses", "Transaksi berhasil disimpan!");
        cartItems.clear(); // Kosongkan keranjang
        txtBayar.clear();   // Kosongkan input bayar
        updateCartUI();    // Refresh tampilan keranjang
        loadProducts(MainController.isDarkMode); // Refresh stok barang
    }

    // --- FITUR PENCARIAN (Filter list kiri saat ketik) ---
    private void setupSearch(boolean isDarkMode) {
        txtSearch.textProperty().addListener((obs, old, newVal) -> {
            List<Barang> filtered = allBarang.stream()
                    .filter(b -> b.getNamaBarang().toLowerCase().contains(newVal.toLowerCase()) ||
                            b.getIdBarang().toLowerCase().contains(newVal.toLowerCase()))
                    .collect(Collectors.toList());
            displayProducts(filtered, isDarkMode);
        });
    }

    // --- UTILITI: Popup Notifikasi ---
    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }
}