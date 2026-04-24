package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import model.Toko;
import model.TokoDAO;

public class PengaturanController {

    @FXML private TextField txtNamaToko, txtTelp, txtAlamat, txtEmail;
    @FXML private Button btnEdit;

    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        muatDataToko();
    }

    private void muatDataToko() {
        Toko toko = TokoDAO.getDataToko();
        if (toko != null) {
            txtNamaToko.setText(toko.getNamaToko());
            txtTelp.setText(toko.getNomorTelepon());
            txtAlamat.setText(toko.getAlamat());
            txtEmail.setText(toko.getEmail());
        }
    }

    @FXML
    private void handleEdit() {
        if (!isEditMode) {
            // AKTIFKAN MODE EDIT
            setFieldsEditable(true);
            btnEdit.setText("Simpan");
            // Menggunakan warna hijau untuk memberi kesan 'Save'
            btnEdit.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold;");
            isEditMode = true;
        } else {
            // PROSES SIMPAN
            String nama = txtNamaToko.getText();
            String telp = txtTelp.getText();
            String alamat = txtAlamat.getText();
            String email = txtEmail.getText();

            // Panggil updateToko yang sekarang sudah menggunakan koneksi baru setiap dipanggil
            boolean sukses = TokoDAO.updateToko(nama, telp, alamat, email);

            if (sukses) {
                setFieldsEditable(false);
                btnEdit.setText("Edit");
                btnEdit.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold;");
                isEditMode = false;

                tampilkanAlert(Alert.AlertType.INFORMATION, "Update Berhasil", "Data Toko Zikry telah diperbarui!");
            } else {
                // Jika gagal, tampilkan pesan error yang sesuai screenshot
                tampilkanAlert(Alert.AlertType.ERROR, "Error", "Gagal memperbarui data. Pastikan database aktif dan ID=1 tersedia!");
            }
        }
    }

    private void setFieldsEditable(boolean value) {
        txtNamaToko.setEditable(value);
        txtTelp.setEditable(value);
        txtAlamat.setEditable(value);
        txtEmail.setEditable(value);

        // Visual feedback agar user tahu field mana yang bisa diisi
        String style = value
                ? "-fx-background-color: #FFFFFF; -fx-border-color: #3498DB; -fx-border-radius: 5; -fx-background-radius: 5;"
                : "-fx-background-color: transparent; -fx-border-color: transparent; -fx-font-weight: bold;";

        txtNamaToko.setStyle(style);
        txtTelp.setStyle(style);
        txtAlamat.setStyle(style);
        txtEmail.setStyle(style);
    }

    // Helper method agar kode lebih bersih
    private void tampilkanAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
