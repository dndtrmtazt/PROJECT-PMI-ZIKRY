package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import DAO.LaporanDao;
import model.Laporan;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controller untuk mengelola tampilan Laporan Penjualan dan Pengeluaran.
 * Alur: Menampilkan statistik ringkasan dan riwayat transaksi harian dalam bentuk tabel.
 */
public class LaporanController implements Initializable {

    // [1] Deklarasi komponen UI dari file FXML
    @FXML private VBox paneRoot, cardPenjualan, cardPengeluaran, cardTransaksi, vboxTableContainer;
    @FXML private HBox hboxHeader, hboxSearch, hboxFooter;
    @FXML private Label lblTitle, lblTotalPenjualan, lblTotalPengeluaran, lblTotalTransaksi, lblPilihTanggal, lblRiwayat;
    @FXML private DatePicker datePicker;
    @FXML private TableView<Laporan> tableLaporan;
    @FXML private TableColumn<Laporan, String> colTanggal;
    @FXML private TableColumn<Laporan, Double> colPenjualan;
    @FXML private TableColumn<Laporan, Double> colPengeluaran;
    @FXML private TableColumn<Laporan, Integer> colTransaksi;

    // [2] Variabel penampung data dan formatter mata uang
    private ObservableList<Laporan> masterData = FXCollections.observableArrayList();
    private final Locale localeID = new Locale("id", "ID");
    private final NumberFormat nf = NumberFormat.getCurrencyInstance(localeID);

    /**
     * Method initialize: Menyiapkan tabel dan memuat data saat halaman dibuka.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // [1] Konfigurasi kolom tabel dan format mata uang
        setupTable();
        // [2] Memuat seluruh data laporan dari database
        loadData();
        // [3] Memperbarui angka statistik di bagian kartu ringkasan
        updateSummary();

        // [4] Memuat CSS tambahan untuk tabel agar lebih menarik
        try {
            tableLaporan.getStylesheets().add(getClass().getResource("/CSS/tabel.css").toExternalForm());
        } catch (Exception e) {}

        // [5] Menyesuaikan tema (Dark/Light Mode)
        setDarkMode(MainController.isDarkMode);
    }

    /**
     * Method setupTable: Melakukan mapping properti model ke kolom tabel.
     */
    private void setupTable() {
        // [1] Mapping nilai kolom
        colTanggal.setCellValueFactory(cellData -> cellData.getValue().tanggalProperty());
        colPenjualan.setCellValueFactory(cellData -> cellData.getValue().totalPenjualanProperty().asObject());
        colPengeluaran.setCellValueFactory(cellData -> cellData.getValue().totalPengeluaranProperty().asObject());
        colTransaksi.setCellValueFactory(cellData -> cellData.getValue().jumlahTransaksiProperty().asObject());

        // [2] Formatter kustom untuk kolom Penjualan agar menjadi Rupiah
        colPenjualan.setCellFactory(column -> new TableCell<Laporan, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(nf.format(item));
            }
        });

        // [3] Formatter kustom untuk kolom Pengeluaran agar menjadi Rupiah
        colPengeluaran.setCellFactory(column -> new TableCell<Laporan, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(nf.format(item));
            }
        });
    }

    /**
     * Method loadData: Mengambil seluruh data laporan melalui DAO.
     */
    private void loadData() {
        List<Laporan> data = LaporanDao.getAllLaporan();
        masterData.setAll(data);
        tableLaporan.setItems(masterData);
    }

    /**
     * Method updateSummary: Menampilkan angka statistik untuk hari ini pada kartu ringkasan.
     */
    private void updateSummary() {
        // [1] Inisialisasi variabel default
        double totalPenjualan = 0;
        double totalPengeluaran = 0;
        int totalTransaksi = 0;

        // [2] Cari data spesifik untuk tanggal hari ini dari list masterData
        String today = LocalDate.now().toString();
        Laporan summary = masterData.stream()
                .filter(l -> l.getTanggal().equals(today))
                .findFirst()
                .orElse(null);

        // [3] Jika data hari ini ada, tampilkan angkanya. Jika tidak, set ke 0.
        if (summary != null) {
            lblTotalPenjualan.setText(nf.format(summary.getTotalPenjualan()));
            lblTotalPengeluaran.setText(nf.format(summary.getTotalPengeluaran()));
            lblTotalTransaksi.setText(summary.getJumlahTransaksi() + " Transaksi");
        } else {
            lblTotalPenjualan.setText(nf.format(0));
            lblTotalPengeluaran.setText(nf.format(0));
            lblTotalTransaksi.setText("0 Transaksi");
        }
    }

    /**
     * Method setDarkMode: Mengatur visual tema gelap/terang.
     */
    public void setDarkMode(boolean enabled) {
        // [1] Tentukan variabel warna
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#E0E0E0";

        // [2] Terapkan style ke latar belakang dan header
        if (paneRoot != null) paneRoot.setStyle("-fx-background-color: " + bgMain + ";");
        if (hboxHeader != null) hboxHeader.setStyle("-fx-background-color: #4A76A8;");
        if (lblTitle != null) lblTitle.setStyle("-fx-text-fill: white;");

        // [3] Terapkan warna kontras khusus pada kartu statistik (Hijau, Merah, Biru)
        if (cardPenjualan != null) cardPenjualan.setStyle("-fx-background-color: " + (enabled ? "#1b5e20" : "#5CB85C") + "; -fx-background-radius: 10;");
        if (cardPengeluaran != null) cardPengeluaran.setStyle("-fx-background-color: " + (enabled ? "#b71c1c" : "#D9534F") + "; -fx-background-radius: 10;");
        if (cardTransaksi != null) cardTransaksi.setStyle("-fx-background-color: " + (enabled ? "#01579b" : "#5BC0DE") + "; -fx-background-radius: 10;");

        // [4] Terapkan style pada wadah tabel dan elemen pencarian
        String cardStyle = "-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);";
        if (hboxSearch != null) hboxSearch.setStyle(cardStyle);
        if (vboxTableContainer != null) vboxTableContainer.setStyle(cardStyle);
        if (hboxFooter != null) hboxFooter.setStyle("-fx-background-color: " + bgCard + "; -fx-border-color: " + borderColor + "; -fx-border-width: 1 0 0 0;");

        // [5] Menyelaraskan tampilan DatePicker dan Tabel dengan tema
        if (datePicker != null) {
            datePicker.setStyle("-fx-control-inner-background: " + bgCard + "; -fx-background-color: " + bgCard + "; -fx-text-fill: " + textColor + "; -fx-border-color: " + borderColor + "; -fx-border-radius: 5; -fx-background-radius: 5;");
            datePicker.getEditor().setStyle("-fx-background-color: transparent; -fx-text-fill: " + textColor + ";");
        }
        if (tableLaporan != null) {
            tableLaporan.getStyleClass().remove("dark");
            if (enabled) tableLaporan.getStyleClass().add("dark");
        }
    }

    /**
     * Method handleFilter: Memfilter data riwayat berdasarkan tanggal yang dipilih.
     */
    @FXML
    public void handleFilter(ActionEvent event) {
        // [1] Jika tanggal kosong, muat ulang semua data
        if (datePicker.getValue() == null) {
            loadData();
            updateSummary();
            return;
        }

        // [2] Ambil data dari DAO berdasarkan rentang tanggal yang dipilih
        LocalDate selectedDate = datePicker.getValue();
        List<Laporan> filteredData = LaporanDao.getLaporanByDateRange(selectedDate, selectedDate);
        masterData.setAll(filteredData);
        tableLaporan.setItems(masterData);

        // [3] Hitung total akumulasi dari hasil filter untuk ditampilkan di kartu ringkasan
        double sumPenjualan = filteredData.stream().mapToDouble(Laporan::getTotalPenjualan).sum();
        double sumPengeluaran = filteredData.stream().mapToDouble(Laporan::getTotalPengeluaran).sum();
        int sumTransaksi = filteredData.stream().mapToInt(Laporan::getJumlahTransaksi).sum();

        lblTotalPenjualan.setText(nf.format(sumPenjualan));
        lblTotalPengeluaran.setText(nf.format(sumPengeluaran));
        lblTotalTransaksi.setText(sumTransaksi + " Transaksi");
    }

    /**
     * Method handleCetak: Menangani aksi tombol cetak laporan.
     */
    @FXML
    public void handleCetak(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cetak Laporan");
        alert.setHeaderText(null);
        alert.setContentText("Fitur cetak laporan akan segera tersedia!");
        alert.showAndWait();
    }
}
