package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import model.UserDAO;

public class TambahUserController {

    @FXML private TextField txtNama, txtID, txtPass;
    @FXML private ComboBox<String> cbRole;
    @FXML private Button btnSimpan, btnBatal;

    private boolean saved = false;
    private boolean isEdit = false;
    private String idLama; // Menampung ID asli sebelum diedit

    @FXML
    public void initialize() {
        cbRole.getItems().addAll("PEMILIK", "KASIR");
    }

    public void setEditMode(User user) {
        this.isEdit = true;
        this.idLama = user.getIdUser(); // Simpan ID asli di sini

        txtID.setText(user.getIdUser());
        txtID.setEditable(true); // Sekarang bisa diedit sesukamu
        txtNama.setText(user.getNamaLengkap());

        if (user.getRole() != null) {
            cbRole.setValue(user.getRole().toUpperCase());
        }

        txtID.setStyle("-fx-background-color: white; -fx-border-color: #BABABA; -fx-border-radius: 5;");
    }

    @FXML
    private void handleSimpan() {
        String idBaru = txtID.getText();
        String nama = txtNama.getText();
        String role = (cbRole.getValue() != null) ? cbRole.getValue().toLowerCase() : "";
        String pass = txtPass.getText();

        if (idBaru.isEmpty() || nama.isEmpty() || role.isEmpty() || pass.isEmpty()) {
            alertError("Semua field harus diisi!");
            return;
        }

        boolean sukses;
        if (isEdit) {
            // Gunakan method baru yang menerima idLama dan idBaru
            sukses = UserDAO.updateUser(idLama, idBaru, nama, role, pass);
        } else {
            sukses = UserDAO.insertUser(idBaru, nama, role, pass);
        }

        if (sukses) {
            this.saved = true;
            closeWindow();
        } else {
            alertError("Gagal menyimpan! Pastikan ID baru belum digunakan.");
        }
    }

    @FXML
    private void handleBatal() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnSimpan.getScene().getWindow();
        if (stage != null) stage.close();
    }

    public boolean isSaved() {
        return saved;
    }

    private void alertError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}