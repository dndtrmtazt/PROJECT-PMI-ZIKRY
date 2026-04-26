package Controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import DAO.UserDAO;
import model.User;

/**
 * Controller untuk Form Tambah/Edit User.
 * Alur: Menangani input data pengguna (ID, Nama, Role, Password) dan menyimpannya ke database.
 */
public class TambahUserController {

    // [1] Deklarasi komponen UI dari FXML
    @FXML private VBox rootPane;
    @FXML private Label lblFormTitle;
    @FXML private TextField txtNama, txtID, txtPass;
    @FXML private ComboBox<String> cbRole;
    @FXML private Button btnSimpan, btnBatal;

    // Status penyimpanan dan mode edit
    private boolean saved = false;
    private boolean isEdit = false;
    private String idLama; // Menampung ID asli sebelum diedit

    /**
     * Method initialize: Menyiapkan pilihan role dan tema awal.
     */
    @FXML
    public void initialize() {
        // [1] Mengisi pilihan pada dropdown Role
        cbRole.getItems().addAll("PEMILIK", "KASIR");
        // [2] Menyesuaikan tema tampilan
        setDarkMode(MainController.isDarkMode);
    }

    /**
     * Method setEditMode: Mengisi form dengan data user yang akan diedit.
     * Alur: 1. Set status isEdit -> 2. Simpan ID asli -> 3. Tampilkan data ke TextField.
     */
    public void setEditMode(User user) {
        // [1] Menandai bahwa ini adalah proses update (Edit)
        this.isEdit = true;
        this.idLama = user.getIdUser(); // Simpan ID asli untuk query update

        // [2] Mengubah judul form
        if (lblFormTitle != null) {
            lblFormTitle.setText("Edit User");
        }

        // [3] Memasukkan data user ke dalam kolom input
        txtID.setText(user.getIdUser());
        txtID.setEditable(true); // ID diperbolehkan untuk diubah
        txtNama.setText(user.getNamaLengkap());

        if (user.getRole() != null) {
            cbRole.setValue(user.getRole().toUpperCase());
        }
        
        // [4] Refresh tema untuk memastikan konsistensi visual
        setDarkMode(MainController.isDarkMode);
    }

    /**
     * Method handleSimpan: Memproses penyimpanan data user.
     * Alur: 1. Ambil input -> 2. Validasi kosong -> 3. Eksekusi DAO -> 4. Tutup jendela.
     */
    @FXML
    private void handleSimpan(ActionEvent event) {
        // [1] Mengambil data dari TextField dan ComboBox
        String idBaru = txtID.getText();
        String nama = txtNama.getText();
        String role = (cbRole.getValue() != null) ? cbRole.getValue().toLowerCase() : "";
        String pass = txtPass.getText();

        // [2] Validasi: Pastikan tidak ada kolom yang kosong
        if (idBaru.isEmpty() || nama.isEmpty() || role.isEmpty() || pass.isEmpty()) {
            alertError("Semua field harus diisi!");
            return;
        }

        // [3] Menentukan aksi: Update jika mode Edit, Insert jika mode Tambah
        boolean sukses;
        if (isEdit) {
            sukses = UserDAO.updateUser(idLama, idBaru, nama, role, pass);
        } else {
            sukses = UserDAO.insertUser(idBaru, nama, role, pass);
        }

        // [4] Jika sukses, set status 'saved' dan tutup form
        if (sukses) {
            this.saved = true;
            closeWindow(event);
        } else {
            alertError("Gagal menyimpan! Pastikan ID baru belum digunakan.");
        }
    }

    /**
     * Method: Menangani tombol Batal.
     */
    @FXML
    private void handleBatal(ActionEvent event) {
        closeWindow(event);
    }

    /**
     * Method: Pembantu untuk menutup Stage (Window) saat ini.
     */
    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (stage != null) stage.close();
    }

    /**
     * Method: Mengecek apakah data berhasil disimpan (digunakan oleh parent controller).
     */
    public boolean isSaved() {
        return saved;
    }

    /**
     * Method: Menampilkan pesan error standar.
     */
    private void alertError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Method setDarkMode: Menyesuaikan gaya visual elemen form sesuai tema aplikasi.
     */
    public void setDarkMode(boolean enabled) {
        // [1] Inisialisasi variabel warna tema
        String bgMain = enabled ? "#1E1E1E" : "white";
        String textColor = enabled ? "white" : "#1F2937";
        String mutedText = enabled ? "#D1D5DB" : "#374151";
        String borderColor = enabled ? "#3A3A3A" : "#DCDCDC";
        String inputBg = enabled ? "#2C2C2C" : "white";

        // [2] Menerapkan style pada panel utama dan elemen anak (Grid/HBox)
        if (rootPane != null) {
            rootPane.setStyle("-fx-background-color: " + bgMain + "; -fx-background-radius: 10;");
            rootPane.getChildren().forEach(node -> {
                if (node instanceof GridPane) {
                    // [3] Styling elemen di dalam Grid (Label, TextField, ComboBox)
                    GridPane grid = (GridPane) node;
                    grid.getChildren().forEach(child -> {
                        if (child instanceof Label) {
                            ((Label) child).setStyle("-fx-text-fill: " + mutedText + ";");
                        } else if (child instanceof TextField) {
                            child.setStyle("-fx-background-color: " + inputBg + "; -fx-border-color: " + borderColor + "; -fx-text-fill: " + textColor + ";");
                        } else if (child instanceof ComboBox) {
                            child.setStyle("-fx-background-color: " + inputBg + "; -fx-border-color: " + borderColor + "; -fx-text-fill: " + textColor + ";");
                        }
                    });
                } else if (node instanceof HBox) {
                    // [4] Styling tombol di bagian footer
                    HBox buttonRow = (HBox) node;
                    buttonRow.getChildren().forEach(child -> {
                        if (child instanceof Button) {
                            if (child == btnSimpan) {
                                child.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-font-weight: bold;");
                            } else {
                                String btnBg = enabled ? "#B8BEC6" : "#E0E0E0";
                                child.setStyle("-fx-background-color: " + btnBg + "; -fx-text-fill: #111111;");
                            }
                        }
                    });
                }
            });
        }

        // [5] Menyesuaikan style judul form
        if (lblFormTitle != null) {
            lblFormTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: " + textColor + ";");
        }
    }
}
