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

public class LaporanController implements Initializable {

    @FXML private VBox paneRoot, cardPenjualan, cardPengeluaran, cardTransaksi, vboxTableContainer;
    @FXML private HBox hboxHeader, hboxSearch, hboxFooter;
    @FXML private Label lblTitle, lblTotalPenjualan, lblTotalPengeluaran, lblTotalTransaksi, lblPilihTanggal, lblRiwayat;
    @FXML private DatePicker datePicker;
    @FXML private TableView<Laporan> tableLaporan;
    @FXML private TableColumn<Laporan, String> colTanggal;
    @FXML private TableColumn<Laporan, Double> colPenjualan;
    @FXML private TableColumn<Laporan, Double> colPengeluaran;
    @FXML private TableColumn<Laporan, Integer> colTransaksi;

    private ObservableList<Laporan> masterData = FXCollections.observableArrayList();
    private final Locale localeID = new Locale("id", "ID");
    private final NumberFormat nf = NumberFormat.getCurrencyInstance(localeID);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
        updateSummary();

        // Load CSS for table
        try {
            tableLaporan.getStylesheets().add(getClass().getResource("/CSS/tabel.css").toExternalForm());
        } catch (Exception e) {}

        setDarkMode(MainController.isDarkMode);
    }

    private void setupTable() {
        colTanggal.setCellValueFactory(cellData -> cellData.getValue().tanggalProperty());
        colPenjualan.setCellValueFactory(cellData -> cellData.getValue().totalPenjualanProperty().asObject());
        colPengeluaran.setCellValueFactory(cellData -> cellData.getValue().totalPengeluaranProperty().asObject());
        colTransaksi.setCellValueFactory(cellData -> cellData.getValue().jumlahTransaksiProperty().asObject());

        // Format currency columns
        colPenjualan.setCellFactory(column -> new TableCell<Laporan, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(nf.format(item));
                }
            }
        });

        colPengeluaran.setCellFactory(column -> new TableCell<Laporan, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(nf.format(item));
                }
            }
        });
    }

    private void loadData() {
        List<Laporan> data = LaporanDao.getAllLaporan();
        masterData.setAll(data);
        tableLaporan.setItems(masterData);
    }

    private void updateSummary() {
        double totalPenjualan = 0;
        double totalPengeluaran = 0;
        int totalTransaksi = 0;

        // For summary cards, let's show data for "today" or total if no data today
        String today = LocalDate.now().toString();
        Laporan summary = masterData.stream()
                .filter(l -> l.getTanggal().equals(today))
                .findFirst()
                .orElse(null);

        if (summary != null) {
            totalPenjualan = summary.getTotalPenjualan();
            totalPengeluaran = summary.getTotalPengeluaran();
            totalTransaksi = summary.getJumlahTransaksi();

            lblTotalPenjualan.setText(nf.format(totalPenjualan));
            lblTotalPengeluaran.setText(nf.format(totalPengeluaran));
            lblTotalTransaksi.setText(totalTransaksi + " Transaksi");
        } else {
            // If no data for today, maybe show 0 or overall average/total?
            // Let's show 0 for "Penjualan Hari Ini" as it's specific
            lblTotalPenjualan.setText(nf.format(0));
            lblTotalPengeluaran.setText(nf.format(0));
            lblTotalTransaksi.setText("0 Transaksi");
        }
    }

    public void setDarkMode(boolean enabled) {
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#E0E0E0";

        if (paneRoot != null) paneRoot.setStyle("-fx-background-color: " + bgMain + ";");
        if (hboxHeader != null) hboxHeader.setStyle("-fx-background-color: #4A76A8;");
        if (lblTitle != null) lblTitle.setStyle("-fx-text-fill: white;");

        // Card colors - slightly darker in dark mode for better contrast
        if (cardPenjualan != null) cardPenjualan.setStyle("-fx-background-color: " + (enabled ? "#1b5e20" : "#5CB85C") + "; -fx-background-radius: 10;");
        if (cardPengeluaran != null) cardPengeluaran.setStyle("-fx-background-color: " + (enabled ? "#b71c1c" : "#D9534F") + "; -fx-background-radius: 10;");
        if (cardTransaksi != null) cardTransaksi.setStyle("-fx-background-color: " + (enabled ? "#01579b" : "#5BC0DE") + "; -fx-background-radius: 10;");

        String cardStyle = "-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);";
        if (hboxSearch != null) hboxSearch.setStyle(cardStyle);
        if (vboxTableContainer != null) vboxTableContainer.setStyle(cardStyle);
        if (hboxFooter != null) hboxFooter.setStyle("-fx-background-color: " + bgCard + "; -fx-border-color: " + borderColor + "; -fx-border-width: 1 0 0 0;");

        if (lblPilihTanggal != null) lblPilihTanggal.setStyle("-fx-text-fill: " + textColor + ";");
        if (lblRiwayat != null) lblRiwayat.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");

        // Sync DatePicker Background and Prompt Text
        if (datePicker != null) {
            String promptColor = enabled ? "white" : "#999999";
            datePicker.setStyle("-fx-control-inner-background: " + bgCard + "; -fx-background-color: " + bgCard + "; -fx-text-fill: " + textColor + "; -fx-border-color: " + borderColor + "; -fx-border-radius: 5; -fx-background-radius: 5;");
            datePicker.getEditor().setStyle("-fx-background-color: transparent; -fx-text-fill: " + textColor + "; -fx-prompt-text-fill: " + promptColor + ";");
        }

        // Sync Table Background
        if (tableLaporan != null) {
            tableLaporan.getStyleClass().remove("dark");
            if (enabled) {
                tableLaporan.getStyleClass().add("dark");
            }

            // Force refresh table styles
            tableLaporan.applyCss();
            tableLaporan.layout();
        }
    }

    @FXML
    public void handleFilter(ActionEvent event) {
        if (datePicker.getValue() == null) {
            loadData();
            updateSummary();
            return;
        }

        LocalDate selectedDate = datePicker.getValue();
        // Since it's a single DatePicker but prompt says "Rentang Tanggal", 
        // maybe it should be start/end? But there's only one.
        // Let's assume it filters for that specific date or starting from that date.
        // The DAO has getLaporanByDateRange. Let's use it for the selected date as start and end.

        List<Laporan> filteredData = LaporanDao.getLaporanByDateRange(selectedDate, selectedDate);
        masterData.setAll(filteredData);
        tableLaporan.setItems(masterData);

        // Update summary based on filtered data
        double totalPenjualan = filteredData.stream().mapToDouble(Laporan::getTotalPenjualan).sum();
        double totalPengeluaran = filteredData.stream().mapToDouble(Laporan::getTotalPengeluaran).sum();
        int totalTransaksi = filteredData.stream().mapToInt(Laporan::getJumlahTransaksi).sum();

        lblTotalPenjualan.setText(nf.format(totalPenjualan));
        lblTotalPengeluaran.setText(nf.format(totalPengeluaran));
        lblTotalTransaksi.setText(totalTransaksi + " Transaksi");
    }

    @FXML
    public void handleCetak(ActionEvent event) {
        System.out.println("Cetak laporan...");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cetak Laporan");
        alert.setHeaderText(null);
        alert.setContentText("Fitur cetak laporan akan segera tersedia!");
        alert.showAndWait();
    }
}
