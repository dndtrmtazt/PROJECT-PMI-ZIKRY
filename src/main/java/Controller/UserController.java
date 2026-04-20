package Controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional; // Tambahkan ini untuk konfirmasi hapus
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType; // Tambahkan ini
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
import model.User;
import model.UserDAO;

public class UserController {

    @FXML private VBox vboxMainContent, vboxUserList, LyrUsr;
    @FXML private HBox hboxTableHead;
    @FXML private Label lblTitle, lblSubTitle;
    @FXML private Button btnTambahUser;
    @FXML private ScrollPane scrollUser;

    @FXML
    public void initialize() {
        muatDataUser();
        setDarkMode(MainController.isDarkMode);
    }

    public void setDarkMode(boolean enabled) {
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#E0E0E0";

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
        if (hboxTableHead != null) {
            hboxTableHead.setStyle("-fx-background-color: " + (enabled ? "#333333" : "#F8F9FA") + "; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0;");
        }
        if (lblSubTitle != null) lblSubTitle.setStyle("-fx-text-fill: " + textColor + ";");

        muatDataUser();
    }

    private void muatDataUser() {
        vboxUserList.getChildren().clear();
        List<User> list = UserDAO.getAllUsers();
        boolean isDark = MainController.isDarkMode;
        String textColor = isDark ? "white" : "#2C3E50";
        String rowBg = isDark ? "#1e1e1e" : "white";
        String borderColor = isDark ? "#333333" : "#F0F0F0";

        int no = 1;
        for (User u : list) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 15, 10, 15));
            row.setStyle("-fx-background-color: " + rowBg + "; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0;");

            Label lblNo = new Label(String.valueOf(no++));
            lblNo.setPrefWidth(40);
            lblNo.setStyle("-fx-text-fill: " + textColor + ";");

            Label lblId = new Label(u.getIdUser());
            lblId.setPrefWidth(120);
            lblId.setStyle("-fx-text-fill: " + textColor + ";");

            Label lblNama = new Label(u.getNamaLengkap());
            lblNama.setPrefWidth(180);
            lblNama.setStyle("-fx-font-weight: bold; -fx-text-fill: " + textColor + ";");

            Label lblRole = new Label(u.getRole().toUpperCase());
            lblRole.setPrefWidth(120);
            lblRole.setStyle("-fx-text-fill: " + (u.getRole().equalsIgnoreCase("admin") ? "#E74C3C" : "#27AE60") + "; -fx-font-weight: bold;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox actionBox = new HBox(8);
            actionBox.setMinWidth(160.0); actionBox.setAlignment(Pos.CENTER);

            // TOMBOL EDIT
            Button btnEdit = new Button("Edit");
            btnEdit.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5;");
            btnEdit.setOnAction(e -> handleEditUser(u)); // Panggil fungsi edit

            // TOMBOL HAPUS
            Button btnHapus = new Button("Hapus");
            btnHapus.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5;");
            btnHapus.setOnAction(e -> handleHapusUser(u)); // Panggil fungsi hapus

            actionBox.getChildren().addAll(btnEdit, btnHapus);
            row.getChildren().addAll(lblNo, lblId, lblNama, lblRole, spacer, actionBox);
            vboxUserList.getChildren().add(row);
        }
    }

    // LOGIKA EDIT USER
    private void handleEditUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Admin/TambahUserView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Edit User: " + user.getIdUser());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));

            // Ambil controller pop-up dan kirim data untuk di-edit
            TambahUserController controller = loader.getController();
            controller.setEditMode(user); // Kamu perlu membuat method setEditMode di TambahUserController

            stage.showAndWait();

            if (controller.isSaved()) {
                muatDataUser(); // Refresh tabel
            }
        } catch (IOException e) {
            alertError("Gagal membuka jendela edit: " + e.getMessage());
        }
    }

    // LOGIKA HAPUS USER
    private void handleHapusUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText(null);
        alert.setContentText("Apakah Anda yakin ingin menghapus user: " + user.getNamaLengkap() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (UserDAO.deleteUser(user.getIdUser())) { // Panggil method di DAO
                muatDataUser(); // Refresh tabel
            } else {
                alertError("Gagal menghapus user dari database.");
            }
        }
    }

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

            if (controller != null && controller.isSaved()) {
                muatDataUser();
            }
        } catch (Exception e) {
            alertError("Gagal membuka jendela: " + e.getMessage());
        }
    }

    private void alertError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}