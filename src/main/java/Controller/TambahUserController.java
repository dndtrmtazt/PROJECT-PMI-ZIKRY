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

// Controller form tambah dan edit user.
public class TambahUserController {

    @FXML private VBox rootPane;
    @FXML private Label lblFormTitle;
    @FXML private TextField txtNama, txtID, txtPass;
    @FXML private ComboBox<String> cbRole;
    @FXML private Button btnSimpan, btnBatal;

    private boolean saved = false;
    private boolean isEdit = false;
    private String idLama; // Menampung ID asli sebelum diedit

    // Mengisi pilihan role dan menyesuaikan tema form.
    @FXML
    public void initialize() {
        cbRole.getItems().addAll("PEMILIK", "KASIR");
        setDarkMode(MainController.isDarkMode);
    }

    // Mengubah form menjadi mode edit dengan data user yang dipilih.
    public void setEditMode(User user) {
        this.isEdit = true;
        this.idLama = user.getIdUser(); // Simpan ID asli di sini

        if (lblFormTitle != null) {
            lblFormTitle.setText("Edit User");
        }

        txtID.setText(user.getIdUser());
        txtID.setEditable(true); // Sekarang bisa diedit sesukamu
        txtNama.setText(user.getNamaLengkap());

        if (user.getRole() != null) {
            cbRole.setValue(user.getRole().toUpperCase());
        }
        setDarkMode(MainController.isDarkMode);
    }

    // Validasi input lalu menyimpan user baru atau update user lama.
    @FXML
    private void handleSimpan(ActionEvent event) {
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
            closeWindow(event);
        } else {
            alertError("Gagal menyimpan! Pastikan ID baru belum digunakan.");
        }
    }

    // Menutup form tanpa menyimpan.
    @FXML
    private void handleBatal(ActionEvent event) {
        closeWindow(event);
    }

    // Menutup window form dari tombol yang ditekan.
    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (stage != null) stage.close();
    }

    // Dipakai UserController untuk tahu apakah data berhasil disimpan.
    public boolean isSaved() {
        return saved;
    }

    // Menampilkan pesan error sederhana jika validasi atau simpan gagal.
    private void alertError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // Mengatur warna form agar konsisten dengan light/dark mode.
    public void setDarkMode(boolean enabled) {
        String bgMain = enabled ? "#1E1E1E" : "white";
        String textColor = enabled ? "white" : "#1F2937";
        String mutedText = enabled ? "#D1D5DB" : "#374151";
        String borderColor = enabled ? "#3A3A3A" : "#DCDCDC";
        String inputBg = enabled ? "#2C2C2C" : "white";

        setStyleClass(rootPane, "dark", enabled);

        if (rootPane != null) {
            rootPane.setStyle("-fx-background-color: " + bgMain + "; -fx-background-radius: 10;");
            rootPane.getChildren().forEach(node -> {
                if (node instanceof GridPane) {
                    GridPane grid = (GridPane) node;
                    grid.getChildren().forEach(child -> {
                        if (child instanceof Label) {
                            Label label = (Label) child;
                            label.setStyle("-fx-text-fill: " + mutedText + ";");
                        } else if (child instanceof TextField) {
                            TextField field = (TextField) child;
                            field.setStyle("-fx-background-color: " + inputBg + "; -fx-border-color: " + borderColor + "; -fx-border-radius: 5; -fx-background-radius: 5; -fx-text-fill: " + textColor + "; -fx-prompt-text-fill: " + (enabled ? "#9CA3AF" : "#9AA0A6") + ";");
                        } else if (child instanceof ComboBox) {
                            ComboBox<?> comboBox = (ComboBox<?>) child;
                            comboBox.setStyle("-fx-background-color: " + inputBg + "; -fx-border-color: " + borderColor + "; -fx-border-radius: 5; -fx-background-radius: 5; -fx-text-fill: " + textColor + ";");
                        }
                    });
                } else if (node instanceof HBox) {
                    HBox buttonRow = (HBox) node;
                    buttonRow.getChildren().forEach(child -> {
                        if (child instanceof Button) {
                            Button button = (Button) child;
                            if (button == btnSimpan) {
                                button.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-background-radius: 6;");
                            } else {
                                button.setStyle("-fx-background-color: " + (enabled ? "#B8BEC6" : "#E0E0E0") + "; -fx-text-fill: " + (enabled ? "#111111" : "#374151") + "; -fx-background-radius: 6;");
                            }
                        }
                    });
                }
            });
        }

        if (lblFormTitle != null) {
            lblFormTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: " + textColor + ";");
        }

        if (isEdit && txtID != null) {
            txtID.setStyle("-fx-background-color: " + inputBg + "; -fx-border-color: " + borderColor + "; -fx-border-radius: 5; -fx-background-radius: 5; -fx-text-fill: " + textColor + "; -fx-prompt-text-fill: " + (enabled ? "#9CA3AF" : "#9AA0A6") + ";");
        }
    }

    // Helper untuk memasang class CSS tanpa duplikasi.
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
}
