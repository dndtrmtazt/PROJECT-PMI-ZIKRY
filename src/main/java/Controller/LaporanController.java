package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import DAO.LaporanDao;
import model.Laporan;
import util.LaporanExportUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    private final ObservableList<Laporan> listLaporan = FXCollections.observableArrayList();
    private final Locale localeID = new Locale("id", "ID");
    private final NumberFormat nf = NumberFormat.getCurrencyInstance(localeID);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Saat halaman laporan dibuka, data langsung dimuat dan ringkasan kartu atas diperbarui.
        setupCurrencyFormatter();
        setupTable();
        loadData();
        updateSummary();

        // CSS tabel dipasang terpisah agar tampilan TableView tetap konsisten.
        URL tableCss = getClass().getResource("/CSS/tabel.css");
        if (tableCss != null) {
            tableLaporan.getStylesheets().add(tableCss.toExternalForm());
        }

        setDarkMode(MainController.isDarkMode);
    }

    private void setupCurrencyFormatter() {
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
    }

    private void setupTable() {
        // Kolom tabel dihubungkan ke property model Laporan agar TableView otomatis membaca data.
        colTanggal.setCellValueFactory(cellData -> cellData.getValue().tanggalProperty());
        colPenjualan.setCellValueFactory(cellData -> cellData.getValue().totalPenjualanProperty().asObject());
        colPengeluaran.setCellValueFactory(cellData -> cellData.getValue().totalPengeluaranProperty().asObject());
        colTransaksi.setCellValueFactory(cellData -> cellData.getValue().jumlahTransaksiProperty().asObject());

        // Nominal penjualan dan pengeluaran ditampilkan dalam format Rupiah tanpa mengubah data aslinya.
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
        // list laporan berisi seluruh riwayat laporan sebelum user melakukan filter tanggal.
        List<Laporan> data = LaporanDao.getAllLaporan();
        listLaporan.setAll(data);
        tableLaporan.setItems(listLaporan);
    }

    private void updateSummary() {
        // Kartu ringkasan atas mengambil data hari ini supaya pemilik langsung melihat performa terbaru.
        LocalDate today = LocalDate.now();
        Laporan summary = LaporanDao.getLaporanByDateRange(today, today).stream()
                .findFirst()
                .orElse(null);

        if (summary != null) {
            lblTotalPenjualan.setText(nf.format(summary.getTotalPenjualan()));
            lblTotalPengeluaran.setText(nf.format(summary.getTotalPengeluaran()));
            lblTotalTransaksi.setText(summary.getJumlahTransaksi() + " Transaksi");
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

        setStyleClass(paneRoot, "dark", enabled);
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

    private void setStyleClass(Node node, String styleClass, boolean enabled) {
        if (node == null) return;
        if (enabled) {
            if (!node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }
        } else {
            node.getStyleClass().remove(styleClass);
        }
    }

    @FXML
    public void handleFilter(ActionEvent event) {
        // Jika tanggal kosong, tabel dikembalikan ke semua data. Jika ada tanggal, tampilkan hari itu saja.
        if (datePicker.getValue() == null) {
            loadData();
            return;
        }

        LocalDate selectedDate = datePicker.getValue();

        List<Laporan> filteredData = LaporanDao.getLaporanByDateRange(selectedDate, selectedDate);
        listLaporan.setAll(filteredData);
        tableLaporan.setItems(listLaporan);
    }

    @FXML
    public void handleCetak(ActionEvent event) {
        // Tombol Cetak Laporan mengambil data yang sedang tampil, jadi hasil export mengikuti filter tabel.
        List<Laporan> dataExport = new ArrayList<>(tableLaporan.getItems());
        if (dataExport.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Data Kosong", "Tidak ada data laporan untuk diexport.");
            return;
        }

        ExportLaporanDialogController.ExportFormat selectedFormat = showExportFormatDialog();
        if (selectedFormat == null) {
            return;
        }

        boolean exportPdf = selectedFormat == ExportLaporanDialogController.ExportFormat.PDF;
        File targetFile = pilihLokasiExport(exportPdf); //menyimpan export dimna
        if (targetFile == null) {
            return;
        }

        String periode = getPeriodeExport(); //ambil tanggal ekspor
        try {
            // logika pilih format export laporan
            if (exportPdf) {
                LaporanExportUtil.exportToPdf(targetFile, dataExport, periode);
            } else {
                LaporanExportUtil.exportToExcel(targetFile, dataExport, periode);
            }
            showExportSuccessDialog(selectedFormat, targetFile);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Export Gagal",
                    "Gagal membuat file laporan:\n" + e.getMessage());
        }
    }

    private ExportLaporanDialogController.ExportFormat showExportFormatDialog() {
        try {
            // Popup custom ini hanya memilih format, sedangkan proses simpan file tetap dilakukan setelahnya.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Admin/ExportLaporanDialog.fxml"));
            Parent root = loader.load();
            ExportLaporanDialogController controller = loader.getController();
            setStyleClass(root, "dark", MainController.isDarkMode);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            if (tableLaporan != null && tableLaporan.getScene() != null) {
                dialog.initOwner(tableLaporan.getScene().getWindow());
            }
            dialog.initStyle(StageStyle.TRANSPARENT);
            dialog.setResizable(false);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            URL css = getClass().getResource("/CSS/laporan-export-dialog.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            dialog.setScene(scene);
            if (dialog.getOwner() != null) {
                dialog.setOnShown(event -> {
                    Stage owner = (Stage) dialog.getOwner();
                    dialog.setX(owner.getX() + (owner.getWidth() - dialog.getWidth()) / 2);
                    dialog.setY(owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2);
                });
            }
            dialog.showAndWait();
            return controller.getSelectedFormat();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Popup Gagal Dibuka",
                    "Gagal membuka pilihan export laporan:\n" + e.getMessage());
            return null;
        }
    }

    private void showExportSuccessDialog(ExportLaporanDialogController.ExportFormat format, File targetFile) {
        try {
            // Popup sukses menampilkan format export dan lokasi file agar user tahu hasilnya tersimpan di mana.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Admin/ExportSuccessDialog.fxml"));
            Parent root = loader.load();
            ExportSuccessDialogController controller = loader.getController();
            controller.setExportResult(format, targetFile.getAbsolutePath());
            setStyleClass(root, "dark", MainController.isDarkMode);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            if (tableLaporan != null && tableLaporan.getScene() != null) {
                dialog.initOwner(tableLaporan.getScene().getWindow());
            }
            dialog.initStyle(StageStyle.TRANSPARENT);
            dialog.setResizable(false);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            URL css = getClass().getResource("/CSS/laporan-export-dialog.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            dialog.setScene(scene);
            if (dialog.getOwner() != null) {
                dialog.setOnShown(event -> {
                    Stage owner = (Stage) dialog.getOwner();
                    dialog.setX(owner.getX() + (owner.getWidth() - dialog.getWidth()) / 2);
                    dialog.setY(owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2);
                });
            }
            dialog.showAndWait();
        } catch (IOException e) {
            // Jika popup custom gagal dibuka, export tetap dianggap berhasil dan user diberi info lewat Alert.
            e.printStackTrace();
            showAlert(Alert.AlertType.INFORMATION, "Export Berhasil",
                    "Laporan berhasil disimpan:\n" + targetFile.getAbsolutePath());
        }
    }

    private File pilihLokasiExport(boolean pdf) {
        // FileChooser membuat user bebas memilih folder tujuan tanpa menyimpan file ke folder instalasi aplikasi.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(pdf ? "Simpan Laporan PDF" : "Simpan Laporan Excel");
        String extension = pdf ? ".pdf" : ".xlsx";
        String description = pdf ? "PDF Document (*.pdf)" : "Excel Workbook (*.xlsx)";
        String pattern = pdf ? "*.pdf" : "*.xlsx";
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, pattern));
        fileChooser.setInitialFileName("laporan-penjualan-toko-zikry-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")) + extension);

        File selectedFile = fileChooser.showSaveDialog(tableLaporan.getScene().getWindow());
        if (selectedFile == null) {
            return null;
        }

        String path = selectedFile.getAbsolutePath().toLowerCase(Locale.ROOT);
        if (!path.endsWith(extension)) {
            selectedFile = new File(selectedFile.getAbsolutePath() + extension);
        }
        return selectedFile;
    }

    private String getPeriodeExport() {
        if (datePicker != null && datePicker.getValue() != null) {
            return datePicker.getValue().toString();
        }
        return "Semua Tanggal";
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
