package Controller;

import java.net.URL;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import DAO.TokoDAO;
import model.Toko;

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
    private void handleBatal() {
        kembalikanDataAwalEdit();
        isEditMode = false;
        updateModeEditView();
        setFieldsEditable(false);
        btnEdit.setText("Edit");
    }

    private void simpanDataAwalEdit() {
        namaAwal = txtNamaToko.getText();
        telpAwal = txtTelp.getText();
        alamatAwal = txtAlamat.getText();
        emailAwal = txtEmail.getText();
    }

    private void kembalikanDataAwalEdit() {
        txtNamaToko.setText(namaAwal);
        txtTelp.setText(telpAwal);
        txtAlamat.setText(alamatAwal);
        txtEmail.setText(emailAwal);
    }

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

    private void showUpdateSuccessDialog() {
        boolean darkMode = MainController.isDarkMode;

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (vboxMainContent != null && vboxMainContent.getScene() != null) {
            dialog.initOwner(vboxMainContent.getScene().getWindow());
        }
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setResizable(false);

        StackPane root = new StackPane();
        root.getStyleClass().add("admin-update-dialog-root");
        setStyleClass(root, "dark", darkMode);

        VBox card = new VBox();
        card.getStyleClass().add("admin-update-dialog-card");
        card.setMinWidth(460);
        card.setPrefWidth(460);
        card.setMaxWidth(460);
        applyRoundedClip(card, 20);

        HBox header = new HBox();
        header.getStyleClass().add("admin-update-dialog-header");
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Update Berhasil");
        title.getStyleClass().add("admin-update-dialog-title");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, javafx.scene.layout.Priority.ALWAYS);

        Button closeButton = new Button("X");
        closeButton.getStyleClass().add("admin-update-dialog-close");
        closeButton.setOnAction(event -> dialog.close());

        header.getChildren().addAll(title, headerSpacer, closeButton);

        HBox body = new HBox(22);
        body.getStyleClass().add("admin-update-dialog-body");
        body.setAlignment(Pos.CENTER_LEFT);

        StackPane iconGlow = new StackPane();
        iconGlow.getStyleClass().add("admin-update-dialog-icon-glow");
        StackPane iconCircle = new StackPane();
        iconCircle.getStyleClass().add("admin-update-dialog-icon-circle");
        Label infoIcon = new Label("i");
        infoIcon.getStyleClass().add("admin-update-dialog-icon-text");
        iconCircle.getChildren().add(infoIcon);
        iconGlow.getChildren().add(iconCircle);

        Label message = new Label("Data Toko Zikry telah diperbarui!");
        message.getStyleClass().add("admin-update-dialog-message");
        message.setWrapText(true);
        message.setMaxWidth(280);

        body.getChildren().addAll(iconGlow, message);

        HBox footer = new HBox();
        footer.getStyleClass().add("admin-update-dialog-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button okButton = new Button("OK");
        okButton.getStyleClass().add("admin-update-dialog-ok");
        okButton.setOnAction(event -> dialog.close());
        footer.getChildren().add(okButton);

        card.getChildren().addAll(header, body, footer);
        root.getChildren().add(card);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        URL css = getClass().getResource("/CSS/admin.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        dialog.setScene(scene);
        if (dialog.getOwner() != null) {
            dialog.setOnShown(event -> {
                Stage owner = (Stage) dialog.getOwner();
                dialog.setX(owner.getX() + (owner.getWidth() - dialog.getWidth()) / 2);
                dialog.setY(owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2);
            });
        }
        dialog.showAndWait();
    }

    private void applyRoundedClip(Region region, double arc) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(arc);
        clip.setArcHeight(arc);
        clip.widthProperty().bind(region.widthProperty());
        clip.heightProperty().bind(region.heightProperty());
        region.setClip(clip);
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
