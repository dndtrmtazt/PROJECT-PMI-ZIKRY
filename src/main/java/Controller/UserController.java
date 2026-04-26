package Controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import DAO.UserDAO;
import model.User;

/**
 * Controller untuk mengelola daftar Pengguna (User).
 * Alur: Menampilkan data user dalam bentuk baris kustom dan menangani aksi Tambah, Edit, serta Hapus.
 */
public class UserController {

    // [1] Deklarasi komponen UI dari file FXML
    @FXML private VBox vboxMainContent, vboxUserList, LyrUsr;
    @FXML private HBox hboxTableHead;
    @FXML private Label lblTitle, lblSubTitle;
    @FXML private Button btnTambahUser;
    @FXML private ScrollPane scrollUser;

    /**
     * Method initialize: Menyiapkan data user dan tema saat halaman pertama kali dibuka.
     */
    @FXML
    public void initialize() {
        // [1] Memuat data user dari database ke dalam tampilan
        muatDataUser();
        // [2] Menyesuaikan tema visual (Dark/Light Mode)
        setDarkMode(MainController.isDarkMode);
    }

    /**
     * Method setDarkMode: Mengatur warna visual seluruh elemen halaman sesuai tema aktif.
     */
    public void setDarkMode(boolean enabled) {
        // [1] Menentukan variabel warna berdasarkan tema
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#E0E0E0";

        // [2] Terapkan style CSS pada kontainer utama dan header
        if (vboxMainContent != null) vboxMainContent.setStyle("-fx-background-color: " + bgMain + ";");
        if (lblTitle != null) {
            lblTitle.getParent().setStyle("-fx-background-color: #4A76A8;");
            lblTitle.setStyle("-fx-text-fill: white;");
        }
        if (LyrUsr != null) {
            LyrUsr.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10;");
        }
        if (scrollUser != null) {
            scrollUser.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        }
        
        // [3] Mengatur warna header tabel dan teks labelnya
        if (hboxTableHead != null) {
            hboxTableHead.setStyle("-fx-background-color: " + (enabled ? "#333333" : "#F8F9FA") + "; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0;");
            hboxTableHead.getChildren().forEach(node -> {
                if (node instanceof Label) {
                    ((Label) node).setStyle("-fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
                }
            });
        }
        if (lblSubTitle != null) lblSubTitle.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");

        // [4] Muat ulang baris user agar warna barisnya ikut sinkron dengan tema baru
        muatDataUser();
    }

    /**
     * Method muatDataUser: Melakukan rendering baris data pengguna ke layar.
     * Alur: 1. Clear tampilan -> 2. Ambil data dari DAO -> 3. Looping render baris HBox kustom.
     */
    private void muatDataUser() {
        // [1] Bersihkan tampilan baris lama
        vboxUserList.getChildren().clear();
        
        // [2] Ambil list seluruh pengguna dari database
        List<User> list = UserDAO.getAllUsers();
        
        boolean isDark = MainController.isDarkMode;
        String textColor = isDark ? "white" : "#2C3E50";
        String rowBg = isDark ? "#1e1e1e" : "white";
        String borderColor = isDark ? "#333333" : "#F0F0F0";

        int no = 1;
        // [3] Iterasi setiap objek User untuk dibuatkan baris visualnya (HBox)
        for (User u : list) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 15, 10, 15));
            row.setStyle("-fx-background-color: " + rowBg + "; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0;");

            // [4] Menyiapkan label data (No, ID, Nama, Role)
            Label lblNo = new Label(String.valueOf(no++));
            lblNo.setPrefWidth(40); lblNo.setStyle("-fx-text-fill: " + textColor + ";");

            Label lblId = new Label(u.getIdUser());
            lblId.setPrefWidth(120); lblId.setStyle("-fx-text-fill: " + textColor + ";");

            Label lblNama = new Label(u.getNamaLengkap());
            lblNama.setPrefWidth(180); lblNama.setStyle("-fx-font-weight: bold; -fx-text-fill: " + textColor + ";");

            Label lblRole = new Label(u.getRole().toUpperCase());
            lblRole.setPrefWidth(120); lblRole.setStyle("-fx-text-fill: " + (u.getRole().equalsIgnoreCase("pemilik") ? "#E74C3C" : "#27AE60") + "; -fx-font-weight: bold;");

            Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

            // [5] Menyiapkan wadah tombol aksi (Edit & Hapus)
            HBox actionBox = new HBox(8);
            actionBox.setMinWidth(170.0); actionBox.setAlignment(Pos.CENTER);

            // --- Tombol Edit dengan Ikon ---
            Button btnEdit = new Button("Edit");
            btnEdit.setStyle("-fx-background-color: #508CE0; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
            try {
                ImageView iconEdit = new ImageView(new Image(getClass().getResourceAsStream("/Images/pencil_white.png")));
                iconEdit.setFitWidth(14); iconEdit.setFitHeight(14);
                btnEdit.setGraphic(iconEdit);
            } catch (Exception e) {}
            btnEdit.setOnAction(e -> handleEditUser(u));

            // --- Tombol Hapus dengan Ikon ---
            Button btnHapus = new Button("Hapus");
            btnHapus.setStyle("-fx-background-color: #F76065; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
            try {
                ImageView iconHapus = new ImageView(new Image(getClass().getResourceAsStream("/Images/trash_white.png")));
                iconHapus.setFitWidth(14); iconHapus.setFitHeight(14);
                btnHapus.setGraphic(iconHapus);
            } catch (Exception e) {}
            btnHapus.setOnAction(e -> handleHapusUser(u));

            // [6] Masukkan semua elemen ke dalam baris dan tambahkan ke container utama
            actionBox.getChildren().addAll(btnEdit, btnHapus);
            row.getChildren().addAll(lblNo, lblId, lblNama, lblRole, spacer, actionBox);
            vboxUserList.getChildren().add(row);
        }
    }

    /**
     * Method handleEditUser: Membuka jendela pop-up untuk mengedit data pengguna.
     * Alur: 1. Load Form -> 2. Set Mode Edit -> 3. Tampilkan -> 4. Refresh jika tersimpan.
     */
    private void handleEditUser(User user) {
        try {
            // [1] Load file FXML TambahUser
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Admin/TambahUserView.fxml"));
            Parent root = loader.load();

            // [2] Siapkan Stage modal baru
            Stage stage = new Stage();
            stage.setTitle("Edit User: " + user.getIdUser());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));

            // [3] Kirim data user ke controller pop-up untuk ditampilkan di form
            TambahUserController controller = loader.getController();
            controller.setEditMode(user);

            stage.showAndWait();

            // [4] Refresh list user jika data berhasil diperbarui
            if (controller.isSaved()) muatDataUser();
        } catch (IOException e) {
            alertError("Gagal membuka jendela edit: " + e.getMessage());
        }
    }

    /**
     * Method handleHapusUser: Memproses penghapusan user dari database.
     * Alur: 1. Konfirmasi -> 2. Eksekusi DAO -> 3. Refresh Layar.
     */
    private void handleHapusUser(User user) {
        // [1] Mintalah konfirmasi kepada admin
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText(null);
        alert.setContentText("Apakah Anda yakin ingin menghapus user: " + user.getNamaLengkap() + "?");

        // [2] Jika admin setuju, jalankan perintah hapus melalui DAO
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (UserDAO.deleteUser(user.getIdUser())) {
                muatDataUser(); // [3] Refresh tampilan jika sukses
            } else {
                alertError("Gagal menghapus user dari database.");
            }
        }
    }

    /**
     * Method handleTambahUser: Membuka jendela pop-up untuk menambah user baru.
     */
    @FXML
    private void handleTambahUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Admin/TambahUserView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Tambah User Baru");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));

            TambahUserController controller = loader.getController();
            stage.showAndWait();

            // Refresh tampilan jika user baru berhasil disimpan
            if (controller != null && controller.isSaved()) muatDataUser();
        } catch (Exception e) {
            alertError("Gagal membuka jendela: " + e.getMessage());
        }
    }

    /**
     * Method: Menampilkan pesan error standar.
     */
    private void alertError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
