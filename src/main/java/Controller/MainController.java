package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;

public class MainController {

    @FXML private Button btnDashboard, btnTransaksi, btnDataBarang, btnLaporan, btnUser, btnDataKategori, btnKelolaPengeluaran, btnPengaturan, btnLogout;
    @FXML private AnchorPane contentArea;

    // Fungsi "Satpam" buat nyembunyiin menu
    public void setHakAkses(String role) {
        if ("kasir".equalsIgnoreCase(role)) {
            // Kasir cuma boleh lihat Transaksi & Pengaturan (misalnya)
            aturVisibility(false, btnDashboard, btnDataBarang, btnLaporan, btnUser, btnDataKategori, btnKelolaPengeluaran);
            aturVisibility(true, btnTransaksi, btnPengaturan, btnLogout);
        } else {
            // Admin/Pemilik lihat semuanya
            aturVisibility(true, btnDashboard, btnTransaksi, btnDataBarang, btnLaporan, btnUser, btnDataKategori, btnKelolaPengeluaran, btnPengaturan, btnLogout);
        }
    }

    private void aturVisibility(boolean tampil, Button... buttons) {
        for (Button btn : buttons) {
            btn.setVisible(tampil);
            btn.setManaged(tampil);
        }
    }

    // Fungsi Navigasi (On Action)
    @FXML
    private void handleMenuAction(ActionEvent event) {
        Object source = event.getSource();

        if (source == btnDashboard) {
            panggilHalaman("DashboardView");
        } else if (source == btnTransaksi) {
            panggilHalaman("TransaksiView");
        } else if (source == btnDataBarang) {
            panggilHalaman("DataBarangView");
        }
        // ... tambah else if buat tombol lainnya
    }

    public void panggilHalaman(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/" + fxmlFile + ".fxml"));
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            System.err.println("Gagal panggil: " + fxmlFile);
            e.printStackTrace();
        }
    }
}