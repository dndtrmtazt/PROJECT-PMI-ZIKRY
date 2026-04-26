package Controller;

import config.koneksi;
import model.Barang;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.io.IOException;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Controller untuk mengelola tampilan Data Barang.
 * Alur: Menampilkan daftar barang, fitur pencarian, dan navigasi tambah/edit.
 */
public class BarangController {

    // [1] Deklarasi komponen UI dari file FXML
    @FXML private VBox paneRoot, vboxTableCard;
    @FXML private HBox hboxSearch, hboxHeader;
    @FXML private TableView<Barang> tableBarang;
    @FXML private TableColumn<Barang, String> colId, colNama, colKategori;
    @FXML private TableColumn<Barang, Integer> colStok;
    @FXML private TableColumn<Barang, Double> colHargaBeli, colHargaJual;
    @FXML private TableColumn<Barang, String> colSatuan;
    @FXML private TextField txtCari;
    @FXML private Label lblDaftarBarang, lblTitle;

    // [2] List penampung data barang yang bersifat observable (otomatis update ke tabel)
    private ObservableList<Barang> listBarang = FXCollections.observableArrayList();

    /**
     * Method initialize: Menyiapkan data dan komponen saat halaman dibuka.
     */
    @FXML
    public void initialize() {
        // [1] Mapping properti model Barang ke kolom-kolom tabel
        colId.setCellValueFactory(new PropertyValueFactory<>("idBarang"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colKategori.setCellValueFactory(new PropertyValueFactory<>("idKategori"));
        colSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        colStok.setCellValueFactory(new PropertyValueFactory<>("stok"));

        // [2] Mengatur perataan teks dan fitur pengurutan pada kolom tabel
        tableBarang.getColumns().forEach(column -> column.setStyle("-fx-alignment: CENTER-LEFT;"));
        colId.setSortable(true);
        colNama.setSortable(true);
        colStok.setSortable(true);
        tableBarang.setItems(listBarang);

        // [3] Mengaktifkan fitur Double Click pada baris tabel untuk mengedit barang
        tableBarang.setRowFactory(tv -> {
            TableRow<Barang> row = new TableRow<>();
            row.setStyle("-fx-cursor: hand;");
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    handleEditBarang();
                }
            });
            return row;
        });

        // [4] Format angka ke mata uang Rupiah dan memuat data dari database
        setupFormatRupiah(colHargaBeli, "hargaBeli");
        setupFormatRupiah(colHargaJual, "hargaJual");
        loadData();
        setupPencarian();

        // [5] Memuat file CSS tambahan untuk mempercantik tabel
        try {
            tableBarang.getStylesheets().add(getClass().getResource("/CSS/tabel.css").toExternalForm());
        } catch (Exception e) {}

        // [6] Menyesuaikan tema (Dark/Light Mode) sesuai pengaturan global
        setDarkMode(MainController.isDarkMode);
    }

    /**
     * Method: Mengatur tampilan tema gelap (Dark Mode) secara dinamis.
     */
    public void setDarkMode(boolean enabled) {
        // [1] Menentukan variabel warna berdasarkan status tema
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#D1D5DB";

        // [2] Mengatur style latar belakang utama dan scrollbar kustom
        if (paneRoot != null) {
            paneRoot.setStyle("-fx-background-color: " + bgMain + ";");

            paneRoot.getStylesheets().removeIf(s -> s.startsWith("data:text/css"));
            if (enabled) {
                paneRoot.getStylesheets().add("data:text/css," +
                        ".scroll-bar:vertical {" +
                        "    -fx-background-color: #2b2b2b;" +
                        "    -fx-pref-width: 14;" +
                        "    -fx-min-width: 14;" +
                        "}" +
                        ".scroll-bar .thumb {" +
                        "    -fx-background-color: #555555;" +
                        "    -fx-background-radius: 5;" +
                        "}" +
                        ".scroll-bar .thumb:hover {" +
                        "    -fx-background-color: #777777;" +
                        "}" +
                        ".scroll-bar .track {" +
                        "    -fx-background-color: #1e1e1e;" +
                        "}" +
                        ".scroll-bar .increment-button, .scroll-bar .decrement-button {" +
                        "    -fx-padding: 0;" +
                        "}" +
                        ".scroll-bar .increment-arrow, .scroll-bar .decrement-arrow {" +
                        "    -fx-shape: \"\";" +
                        "}");
            }
        }

        // [3] Mengatur warna header, teks, kotak pencarian, dan kartu tabel
        if (hboxHeader != null) hboxHeader.setStyle("-fx-background-color: #4A76A8;");
        if (lblTitle != null) lblTitle.setStyle("-fx-text-fill: white;");
        if (hboxSearch != null) hboxSearch.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        if (vboxTableCard != null) vboxTableCard.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        if (lblDaftarBarang != null) lblDaftarBarang.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold; -fx-font-size: 20px;");
        if (txtCari != null) txtCari.setStyle("-fx-background-radius: 10; -fx-background-color: " + (enabled ? "#2c2c2c" : "white") + "; -fx-text-fill: " + textColor + "; -fx-border-color: " + borderColor + "; -fx-border-radius: 10;");

        // [4] Menambah atau menghapus class 'dark' pada tabel
        if (tableBarang != null) {
            tableBarang.getStyleClass().remove("dark");
            if (enabled) {
                tableBarang.getStyleClass().add("dark");
            }
        }
    }

    /**
     * Method loadData: Mengambil data barang dari database SQLite.
     */
    private void loadData() {
        // [1] Membersihkan list lama sebelum memuat data baru
        listBarang.clear();
        String query = "SELECT b.*, k.nama_kategori FROM barang b " +
                "LEFT JOIN kategori k ON b.id_kategori = k.id_kategori";

        // [2] Menjalankan query SQL dan memasukkan hasilnya ke dalam listBarang
        try (Connection conn = koneksi.koneksiDB();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                listBarang.add(new Barang(
                        rs.getString("id_barang"),
                        rs.getString("nama_barang"),
                        rs.getString("id_kategori"),
                        rs.getString("nama_kategori") == null ? "-" : rs.getString("nama_kategori"),
                        rs.getInt("stok"),
                        rs.getString("satuan") == null ? "Pcs" : rs.getString("satuan"),
                        rs.getDouble("harga_beli"),
                        rs.getDouble("harga_jual")
                ));
            }
        } catch (SQLException e) {
            System.err.println("✗ Gagal tarik data: " + e.getMessage());
        }
    }

    /**
     * Method setupPencarian: Mengaktifkan fitur filter data secara real-time.
     */
    private void setupPencarian() {
        // [1] Membuat FilteredList berbasis list utama barang
        FilteredList<Barang> filteredData = new FilteredList<>(listBarang, p -> true);
        
        // [2] Menambahkan listener pada kolom pencarian
        txtCari.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(barang -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                // Filter berdasarkan Nama atau ID Barang
                return barang.getNamaBarang().toLowerCase().contains(lowerCaseFilter) ||
                        barang.getIdBarang().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // [3] Menghubungkan data yang sudah difilter dengan fitur pengurutan tabel
        SortedList<Barang> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableBarang.comparatorProperty());
        tableBarang.setItems(sortedData);
    }

    /**
     * Method: Mengatur format tampilan sel tabel menjadi mata uang Rupiah.
     */
    private void setupFormatRupiah(TableColumn<Barang, Double> col, String property) {
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setCellFactory(tc -> new TableCell<Barang, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) setText(null);
                else {
                    // Gunakan format mata uang Indonesia (IDR) tanpa desimal
                    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    nf.setMaximumFractionDigits(0);
                    setText(nf.format(price));
                }
            }
        });
    }

    /**
     * Method handleEditBarang: Mengalihkan tampilan ke halaman Edit Barang.
     */
    @FXML
    private void handleEditBarang() {
        // [1] Mengambil item yang dipilih dari tabel
        Barang selected = tableBarang.getSelectionModel().getSelectedItem();
        
        // [2] Jika ada item terpilih, panggil halaman EditBarang dan kirim datanya
        if (selected != null && MainController.getInstance() != null) {
            FXMLLoader loader = MainController.getInstance().panggilHalaman("EditBarang");
            if (loader != null) {
                EditBarangController controller = loader.getController();
                controller.initData(
                        selected.getIdBarang(),
                        selected.getNamaBarang(),
                        selected.getIdKategori(),
                        selected.getNamaKategori(),
                        selected.getStok(),
                        selected.getSatuan(),
                        selected.getHargaBeli(),
                        selected.getHargaJual()
                );
            }
        }
    }

    /**
     * Method handleTambahBarang: Membuka halaman formulir penambahan barang baru.
     */
    @FXML
    private void handleTambahBarang() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().panggilHalaman("TambahBarang");
        }
    }
}
