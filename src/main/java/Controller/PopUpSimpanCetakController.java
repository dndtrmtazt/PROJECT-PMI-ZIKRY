package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Controller untuk Jendela Pop-Up Konfirmasi Simpan & Cetak (Checkout).
 * Alur: Menampilkan ringkasan total, uang bayar, dan menghitung kembalian sebelum transaksi benar-benar disimpan.
 */
public class PopUpSimpanCetakController {

    // [1] Deklarasi komponen UI Ringkasan Pembayaran
    @FXML private Label lblTotal;
    @FXML private Label lblBayar;
    @FXML private Label lblKembalian;
    @FXML private Button btnBatal;
    @FXML private Button btnKonfirmasi;

    // Variabel untuk menyimpan keputusan kasir
    private boolean confirmed = false;
    private final NumberFormat nfIndo = NumberFormat.getInstance(new Locale("id", "ID"));

    /**
     * Method initialize: Menyiapkan aksi tombol batal dan konfirmasi.
     */
    @FXML
    public void initialize() {
        // [1] Aksi jika tombol Batal diklik
        btnBatal.setOnAction(e -> {
            confirmed = false;
            closeStage();
        });

        // [2] Aksi jika tombol Cetak/Konfirmasi diklik
        btnKonfirmasi.setOnAction(e -> {
            confirmed = true;
            closeStage();
        });
    }

    /**
     * Method setData: Mengisi nilai angka pada pop-up dari dashboard kasir.
     * Alur: 1. Set Label Total & Bayar -> 2. Hitung Kembalian -> 3. Set warna teks kembalian.
     */
    public void setData(double total, double bayar) {
        // [1] Menampilkan angka dengan format ribuan (Indo)
        lblTotal.setText("Rp " + nfIndo.format(total));
        lblBayar.setText("Rp " + nfIndo.format(bayar));
        
        // [2] Logika perhitungan kembalian
        double kembalian = bayar - total;
        lblKembalian.setText("Rp " + nfIndo.format(kembalian));
        
        // [3] Mengubah warna kembalian: Merah jika minus (kurang), Hijau jika pas/lebih
        if (kembalian < 0) {
            lblKembalian.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
        } else {
            lblKembalian.setStyle("-fx-text-fill: #2d7a32; -fx-font-weight: bold;");
        }
    }

    /**
     * Method isConfirmed: Dipanggil oleh KasirDashboardController untuk cek apakah tombol konfirmasi ditekan.
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Method: Menutup jendela pop-up saat ini.
     */
    private void closeStage() {
        Stage stage = (Stage) btnBatal.getScene().getWindow();
        stage.close();
    }
}
