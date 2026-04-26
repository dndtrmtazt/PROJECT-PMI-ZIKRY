package Controller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import DAO.TokoDAO;
import model.Toko;

/**
 * Controller untuk mengelola Pengaturan Profil Toko.
 * Alur: Menampilkan data toko saat ini, mengubah status menjadi Mode Edit, dan menyimpan perubahan ke database.
 */
public class PengaturanController {

    // [1] Deklarasi komponen UI dari FXML
    @FXML private VBox vboxMainContent, vboxCard, VBP;
    @FXML private HBox hboxHeader;
    @FXML private Label lblTitle, lblSubTitle;
    @FXML private TextField txtNamaToko, txtTelp, txtAlamat, txtEmail;
    @FXML private Button btnEdit, btnBatal;

    private boolean isEditMode = false;

    /**
     * Method initialize: Menyiapkan data toko saat pertama kali dibuka.
     */
    @FXML
    public void initialize() {
        // [1] Ambil data toko dari database SQLite
        muatDataToko();

        // [2] Terapkan tema (Dark/Light)
        setDarkMode(MainController.isDarkMode);

        // [3] Set kondisi awal: Readonly (Hanya baca)
        setFieldsEditable(false);
    }

    /**
     * Method muatDataToko: Mengambil informasi profil toko melalui DAO.
     */
    private void muatDataToko() {
        Toko toko = TokoDAO.getDataToko();
        if (toko != null) {
            txtNamaToko.setText(toko.getNamaToko());
            txtTelp.setText(toko.getNomorTelepon());
            txtAlamat.setText(toko.getAlamat());
            txtEmail.setText(toko.getEmail());
        }
    }

    /**
     * Method handleEdit: Menangani aksi tombol Edit/Simpan.
     * Alur: 1. Jika mode Readonly -> Masuk Mode Edit -> Buka input.
     *       2. Jika mode Edit -> Simpan ke Database -> Kembali ke Readonly.
     */
    @FXML
    private void handleEdit() {
        if (!isEditMode) {
            // [1] Masuk ke Mode Edit: Ubah status dan buka gembok input
            isEditMode = true;
            setFieldsEditable(true);

            btnEdit.setText("Simpan");
            btnEdit.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");

            // [2] Tampilkan tombol batal
            btnBatal.setVisible(true);
            btnBatal.setManaged(true);
        } else {
            // [3] Proses Simpan: Ambil teks dari field dan jalankan update database
            String nama = txtNamaToko.getText();
            String telp = txtTelp.getText();
            String alamat = txtAlamat.getText();
            String email = txtEmail.getText();

            boolean sukses = TokoDAO.updateToko(nama, telp, alamat, email);

            if (sukses) {
                // [4] Kembali ke Mode Readonly jika sukses
                isEditMode = false;
                setFieldsEditable(false);

                btnEdit.setText("Edit");
                btnEdit.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
                btnBatal.setVisible(false);
                btnBatal.setManaged(false);

                tampilkanAlert(Alert.AlertType.INFORMATION, "Update Berhasil", "Data Toko Zikry telah diperbarui!");
            } else {
                tampilkanAlert(Alert.AlertType.ERROR, "Error", "Gagal memperbarui data.");
            }
        }
    }

    /**
     * Method handleBatal: Membatalkan perubahan dan mengunci kembali input.
     */
    @FXML
    private void handleBatal() {
        // [1] Reset status mode edit
        isEditMode = false;
        setFieldsEditable(false);

        // [2] Muat ulang data asli dari database untuk membatalkan ketikan user
        muatDataToko();

        // [3] Kembalikan tampilan tombol ke kondisi awal
        btnEdit.setText("Edit");
        btnEdit.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        btnBatal.setVisible(false);
        btnBatal.setManaged(false);
    }

    /**
     * Method setFieldsEditable: Mengaktifkan atau menonaktifkan kemampuan edit pada input form.
     */
    private void setFieldsEditable(boolean value) {
        txtNamaToko.setEditable(value);
        txtTelp.setEditable(value);
        txtAlamat.setEditable(value);
        txtEmail.setEditable(value);

        // [1] Sesuaikan style visual border agar user tahu input sedang aktif atau terkunci
        boolean isDark = MainController.isDarkMode;
        applyTextFieldStyle(txtNamaToko, value, isDark);
        applyTextFieldStyle(txtTelp, value, isDark);
        applyTextFieldStyle(txtAlamat, value, isDark);
        applyTextFieldStyle(txtEmail, value, isDark);
    }

    /**
     * Method applyTextFieldStyle: Fungsi pembantu untuk mengganti gaya visual TextField secara dinamis.
     */
    private void applyTextFieldStyle(TextField field, boolean isEditable, boolean isDark) {
        field.setFocusTraversable(isEditable);
        field.setCursor(isEditable ? Cursor.TEXT : Cursor.DEFAULT);

        String bg = isDark ? "#2C2C2C" : "#FFFFFF";
        String text = isDark ? "#FFFFFF" : "#1F2937";

        if (isEditable) {
            // [1] Style saat Mode Edit Aktif (Border Biru)
            String border = isDark ? "#4A76A8" : "#3498DB";
            field.setStyle("-fx-background-color: " + bg + "; -fx-border-color: " + border + "; -fx-border-width: 1.5; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 5 10 5 10; -fx-text-fill: " + text + ";");
        } else {
            // [2] Style saat Mode Readonly (Border Abu-abu)
            String border = isDark ? "#555555" : "#D1D5DB";
            field.setStyle("-fx-background-color: " + bg + "; -fx-border-color: " + border + "; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 5 10 5 10; -fx-font-weight: bold; -fx-text-fill: " + text + ";");
        }
    }

    /**
     * Method setDarkMode: Mengatur warna elemen UI sesuai tema aplikasi.
     */
    public void setDarkMode(boolean enabled) {
        // [1] Tentukan variabel warna
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1E1E1E" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String mutedText = enabled ? "#D1D5DB" : "#374151";
        String borderColor = enabled ? "#333333" : "#E5E7EB";

        // [2] Terapkan style ke panel utama dan kartu form
        if (vboxMainContent != null) vboxMainContent.setStyle("-fx-background-color: " + bgMain + ";");
        if (hboxHeader != null) hboxHeader.setStyle("-fx-background-color: #4A76A8;");
        if (vboxCard != null) vboxCard.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 15; -fx-border-color: " + borderColor + "; -fx-border-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.18), 15, 0, 0, 5);");

        // [3] Update warna label teks pembantu
        if (VBP != null) {
            VBP.getChildren().forEach(node -> {
                if (node instanceof HBox) {
                    ((HBox) node).getChildren().forEach(child -> { if (child instanceof Label) ((Label) child).setStyle("-fx-text-fill: " + mutedText + "; -fx-font-size: 14px;"); });
                }
            });
        }

        // [4] Pastikan tombol batal juga menyesuaikan tema
        if (btnBatal != null) {
            String bgBatal = enabled ? "#374151" : "#E2E8F0";
            String textBatal = enabled ? "#E5E7EB" : "#1E293B";
            btnBatal.setStyle("-fx-background-color: " + bgBatal + "; -fx-text-fill: " + textBatal + "; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        }

        // [5] Terapkan ulang style form untuk menyinkronkan border input
        setFieldsEditable(isEditMode);
    }

    private void tampilkanAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }
}
