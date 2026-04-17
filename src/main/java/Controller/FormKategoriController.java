package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Kategori;
import model.KategoriDAO;

public class FormKategoriController {

    @FXML private Label lblHeader, lblError;
    @FXML private TextField txtIdKategori, txtNamaKategori;
    @FXML private Button btnSimpan;

    private KategoriDAO dao = new KategoriDAO();
    private boolean isEdit = false;

    // --- PERBAIKAN 1: Tambahkan variabel ini agar baris 59 tidak merah lagi ---
    private String idLama;

    /**
     * Method ini dipanggil dari KategoriController untuk mengirim data.
     */
    public void setData(Kategori k) {
        if (k != null) {
            isEdit = true;

            // --- PERBAIKAN 2: Simpan ID asli ke idLama sebelum diedit ---
            this.idLama = k.getIdKategori();

            lblHeader.setText("Edit Data Kategori");
            btnSimpan.setText("Update");
            txtIdKategori.setText(k.getIdKategori());
            txtNamaKategori.setText(k.getNamaKategori());

            // Tips: Jika ingin ID tetap bisa diedit, hapus setEditable(false) ini
            // Tapi karena kita pakai idLama, mengedit ID sekarang jadi AMAN.
            txtIdKategori.setEditable(true);
        } else {
            isEdit = false;
            lblHeader.setText("Tambah Data Kategori");
            btnSimpan.setText("Simpan");
        }
    }

    @FXML
    private void handleSimpan(ActionEvent event) {
        String id = txtIdKategori.getText().trim();
        String nama = txtNamaKategori.getText().trim();

        // 1. Validasi
        if (id.isEmpty() || nama.isEmpty()) {
            lblError.setText("ID dan Nama wajib diisi!");
            lblError.setVisible(true);
            return;
        }

        Kategori k = new Kategori(id, nama);

        // 3. Eksekusi Simpan ke Database
        boolean sukses;
        if (isEdit) {
            // --- PERBAIKAN 3: Sekarang idLama sudah ada isinya dan bisa dipakai ---
            sukses = KategoriDAO.updateKategori(k, idLama);
        } else {
            sukses = KategoriDAO.addKategori(k);
        }

        // 4. Jika sukses, tutup jendela
        if (sukses) {
            closeWindow(event); // Menggunakan method pembantu yang sudah ada
        } else {
            // Munculkan pesan error jika gagal
            lblError.setText("Gagal simpan! Cek koneksi atau ID sudah ada.");
            lblError.setVisible(true);
        }
    }

    @FXML
    private void handleBatal(ActionEvent event) {
        closeWindow(event);
    }

    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}