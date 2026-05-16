package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import DAO.TokoDAO;
import model.Toko;

// Controller halaman pengaturan profil toko.
public class PengaturanController {

    @FXML private VBox vboxMainContent, vboxCard, VBP;
    @FXML private HBox hboxHeader;
    @FXML private Label lblTitle, lblSubTitle;
    @FXML private TextField txtNamaToko, txtTelp, txtAlamat, txtEmail;
    @FXML private Button btnEdit, btnBatal;

    private boolean isEditMode = false;
    private String namaAwal;
    private String telpAwal;
    private String alamatAwal;
    private String emailAwal;

    @FXML
    public void initialize() {
        muatDataToko();
        setFieldsEditable(false);
        setDarkMode(MainController.isDarkMode);
    }

    // Mengambil data toko dari database dan menampilkannya ke form.
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
    // Tombol edit berfungsi ganda: masuk mode edit atau menyimpan perubahan.
    private void handleEdit() {
        if (!isEditMode) {
            // AKTIFKAN MODE EDIT
            simpanDataAwalEdit();
            isEditMode = true;
            updateModeEditView();
            setFieldsEditable(true);
            btnEdit.setText("Simpan");
            // Menggunakan warna hijau untuk memberi kesan 'Save'
            btnEdit.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        } else {
            // PROSES SIMPAN
            String nama = txtNamaToko.getText();
            String telp = txtTelp.getText();
            String alamat = txtAlamat.getText();
            String email = txtEmail.getText();

            // Panggil updateToko yang sekarang sudah menggunakan koneksi baru setiap dipanggil
            boolean sukses = TokoDAO.updateToko(nama, telp, alamat, email);

            if (sukses) {
                isEditMode = false;
                updateModeEditView();
                setFieldsEditable(false);
                btnEdit.setText("Edit");

                showUpdateSuccessDialog();
            } else {
                // Jika gagal, tampilkan pesan error yang sesuai screenshot
                tampilkanAlert(Alert.AlertType.ERROR, "Error", "Gagal memperbarui data. Pastikan database aktif dan ID=1 tersedia!");
            }
        }
    }

    @FXML
    // Membatalkan edit dan mengembalikan data awal.
    private void handleBatal() {
        kembalikanDataAwalEdit();
        isEditMode = false;
        updateModeEditView();
        setFieldsEditable(false);
        btnEdit.setText("Edit");
    }

    // Menyimpan nilai awal agar bisa dikembalikan jika user batal.
    private void simpanDataAwalEdit() {
        namaAwal = txtNamaToko.getText();
        telpAwal = txtTelp.getText();
        alamatAwal = txtAlamat.getText();
        emailAwal = txtEmail.getText();
    }

    // Mengembalikan field ke nilai sebelum mode edit.
    private void kembalikanDataAwalEdit() {
        txtNamaToko.setText(namaAwal);
        txtTelp.setText(telpAwal);
        txtAlamat.setText(alamatAwal);
        txtEmail.setText(emailAwal);
    }

    // Mengubah teks tombol dan status field sesuai mode edit.
    private void updateModeEditView() {
        if (lblTitle != null) {
            lblTitle.setText(isEditMode ? "Edit Pengaturan Toko" : "Pengaturan Toko");
        }
        if (btnBatal != null) {
            btnBatal.setVisible(isEditMode);
            btnBatal.setManaged(isEditMode);
        }
        if (btnEdit != null) {
            if (isEditMode) {
                btnEdit.setText("Simpan");
                btnEdit.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
            } else {
                btnEdit.setText("Edit");
                btnEdit.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
            }
        }
    }

    // Mengunci atau membuka field pengaturan toko.
    private void setFieldsEditable(boolean value) {
        txtNamaToko.setEditable(value);
        txtTelp.setEditable(value);
        txtAlamat.setEditable(value);
        txtEmail.setEditable(value);

        boolean enabled = MainController.isDarkMode;

        // Field tetap terlihat seperti input normal, tapi hanya bisa diketik saat mode edit aktif.
        String style = value
                ? "-fx-background-color: " + (enabled ? "#2C2C2C" : "#FFFFFF") + "; -fx-text-fill: " + (enabled ? "white" : "#1F2937") + "; -fx-border-color: #3498DB; -fx-border-radius: 5; -fx-background-radius: 5;"
                : "-fx-background-color: " + (enabled ? "#2C2C2C" : "#FFFFFF") + "; -fx-border-color: " + (enabled ? "#444444" : "#D1D5DB") + "; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-weight: bold; -fx-text-fill: " + (enabled ? "white" : "#1F2937") + ";";

        txtNamaToko.setStyle(style);
        txtTelp.setStyle(style);
        txtAlamat.setStyle(style);
        txtEmail.setStyle(style);
    }

    // Mengatur warna halaman pengaturan sesuai tema aktif.
    public void setDarkMode(boolean enabled) {
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1E1E1E" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String mutedText = enabled ? "#D1D5DB" : "#374151";
        String borderColor = enabled ? "#333333" : "#E5E7EB";

        setStyleClass(vboxMainContent, "dark", enabled);

        if (vboxMainContent != null) {
            vboxMainContent.setStyle("-fx-background-color: " + bgMain + ";");
        }
        if (hboxHeader != null) {
            hboxHeader.setStyle("-fx-background-color: #4A76A8;");
        }
        if (lblTitle != null) {
            lblTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        }
        if (vboxCard != null) {
            vboxCard.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 15; -fx-border-color: " + borderColor + "; -fx-border-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.18), 15, 0, 0, 5);");
        }
        if (lblSubTitle != null) {
            lblSubTitle.setStyle("-fx-font-family: 'Inter Medium'; -fx-font-size: 20; -fx-text-fill: " + textColor + ";");
        }

        if (VBP != null) {
            VBP.getChildren().forEach(node -> {
                if (node instanceof HBox) {
                    HBox row = (HBox) node;
                    row.getChildren().forEach(child -> {
                        if (child instanceof Label) {
                            Label label = (Label) child;
                            label.setStyle("-fx-text-fill: " + mutedText + "; -fx-font-size: 14px;");
                        } else if (child instanceof TextField) {
                            TextField field = (TextField) child;
                            field.setStyle((field.isEditable()
                                    ? "-fx-background-color: " + (enabled ? "#2C2C2C" : "#FFFFFF") + "; -fx-border-color: #3498DB; -fx-border-radius: 5; -fx-background-radius: 5;"
                                    : "-fx-background-color: " + (enabled ? "#2C2C2C" : "#FFFFFF") + "; -fx-border-color: " + (enabled ? "#444444" : "#D1D5DB") + "; -fx-border-radius: 5; -fx-background-radius: 5;")
                                    + " -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
                        }
                    });
                }
            });
        }

        updateModeEditView();

        setFieldsEditable(isEditMode);
    }

    // Helper untuk memasang atau melepas class CSS.
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

    // Menampilkan popup sukses setelah pengaturan toko disimpan.
    private void showUpdateSuccessDialog() {
        Window owner = vboxMainContent != null && vboxMainContent.getScene() != null
                ? vboxMainContent.getScene().getWindow()
                : null;

        SuccessDialogController.showDialog(
                owner,
                MainController.isDarkMode,
                "Update Berhasil",
                "Data Toko Zikry telah diperbarui!"
        );
    }

    // Helper method agar kode lebih bersih
    // Menampilkan alert untuk error atau informasi.
    private void tampilkanAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
