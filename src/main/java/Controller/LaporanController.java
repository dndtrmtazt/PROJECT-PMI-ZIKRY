package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import java.net.URL;
import java.util.ResourceBundle;

public class LaporanController implements Initializable {

    // --- 1. JEMBATAN FXML (fx:id) ---
    @FXML private DatePicker datePicker;
    @FXML private TableView<?> tableLaporan; // Ganti '?' dengan model Laporan kamu nanti
    @FXML private Label lblTotalPenjualan, lblTotalPengeluaran, lblTotalTransaksi;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Logika awal saat halaman laporan dibuka (misal: load data bulan ini)
        System.out.println("Halaman Laporan Siap!");
    }

    // --- 2. LOGIKA FILTER (Ini yang tadinya bikin merah) ---
    @FXML
    public void handleFilter(ActionEvent event) {
        if (datePicker.getValue() == null) {
            System.out.println("Pilih tanggal dulu, Din!");
            return;
        }
        // Nanti kodingan ambil data dari database berdasarkan tanggal taruh di sini
        System.out.println("Filtering data untuk tanggal: " + datePicker.getValue());
    }

    // --- 3. LOGIKA CETAK (Biar tombol cetak gak merah juga) ---
    @FXML
    public void handleCetak(ActionEvent event) {
        // Nanti kodingan buat export PDF atau Print taruh di sini
        System.out.println("Sedang mencetak laporan...");
    }
}