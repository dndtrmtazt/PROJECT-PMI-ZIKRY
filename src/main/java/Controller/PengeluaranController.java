package Controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Pengeluaran;
import model.PengeluaranDAO;
import java.time.format.DateTimeFormatter;

public class PengeluaranController implements Initializable {

    @FXML private VBox vboxMainContent, vboxPengeluaranList;
    @FXML private HBox hboxSearch, hboxTableHead;
    @FXML private Label lblTitle, lblSubTitle;
    @FXML private TextField txtSearchPengeluaran;
    @FXML private DatePicker dpFilterTanggal;
    @FXML private Button btnSearch, btnTambahPengeluaran;
    @FXML private ScrollPane scrollPengeluaran;

    private PengeluaranDAO pengeluaranDAO = new PengeluaranDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        muatDataPengeluaran();
        // Pastikan MainController.isDarkMode dapat diakses
        setDarkMode(MainController.isDarkMode);
    }

    /**
     * PERBAIKAN UTAMA: Nama method disamakan dengan FXML
     */
    @FXML
    private void handleTambahPengeluaran() {
        showPengeluaranDialog(null);
    }

    /**
     * Membuka pop-up form pengeluaran
     */
    private void showPengeluaranDialog(Pengeluaran p) {
        try {
            // Path disesuaikan dengan struktur folder resources kamu
            URL fxmlLocation = getClass().getResource("/FXML/Admin/FormPengeluaran.fxml");

            if (fxmlLocation == null) {
                System.err.println("File FormPengeluaran.fxml tidak ditemukan!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Mengirim data ke FormPengeluaranController
            FormPengeluaranController controller = loader.getController();
            controller.setData(p);

            Stage stage = new Stage();
            stage.setTitle(p == null ? "Tambah Data Pengeluaran" : "Edit Data Pengeluaran");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh tabel setelah jendela ditutup
            muatDataPengeluaran();
        } catch (Exception e) {
            System.err.println("Gagal membuka Form Pengeluaran: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void muatDataPengeluaran() {
        if (vboxPengeluaranList == null) return;
        vboxPengeluaranList.getChildren().clear();

        List<Pengeluaran> list = pengeluaranDAO.getAllPengeluaran();
        boolean isDark = MainController.isDarkMode;
        String textColor = isDark ? "white" : "#2C3E50";
        String rowBg = isDark ? "#1e1e1e" : "#FFFFFF";
        String borderColor = isDark ? "#333333" : "#F0F0F0";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        int no = 1;

        for (Pengeluaran p : list) {
            HBox baris = new HBox(10);
            baris.setAlignment(Pos.CENTER_LEFT);
            baris.setPadding(new Insets(10, 15, 10, 15));
            baris.setStyle("-fx-background-color: " + rowBg + "; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0;");

            Label lblNo = new Label(String.valueOf(no++));
            lblNo.setMinWidth(40.0); lblNo.setStyle("-fx-text-fill: " + textColor + ";");

            Label lblId = new Label(p.getIdPengeluaran());
            lblId.setMinWidth(100.0); lblId.setStyle("-fx-text-fill: " + textColor + ";");

            String tglFormat = (p.getTglPengeluaran() != null) ? p.getTglPengeluaran().format(formatter) : "-";
            Label lblTgl = new Label(tglFormat);
            lblTgl.setMinWidth(110.0); lblTgl.setStyle("-fx-text-fill: " + textColor + ";");

            Label lblNominal = new Label("Rp " + String.format("%,.0f", p.getNominal()).replace(',', '.'));
            lblNominal.setMinWidth(130.0); lblNominal.setStyle("-fx-text-fill: #E67E22; -fx-font-weight: bold;");

            Label lblJenis = new Label(p.getJenis());
            lblJenis.setMinWidth(120.0); lblJenis.setStyle("-fx-text-fill: " + textColor + ";");

            Label lblUser = new Label(p.getIdUser());
            lblUser.setMinWidth(100.0); lblUser.setStyle("-fx-text-fill: " + textColor + ";");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox actionBox = new HBox(8);
            actionBox.setMinWidth(160.0); actionBox.setAlignment(Pos.CENTER);

            // Listener Edit
            Button btnEdit = createActionButton("Edit", "#3498DB", "/Images/pencil_white.png");
            btnEdit.setOnAction(e -> showPengeluaranDialog(p));

            // Listener Hapus
            Button btnHapus = createActionButton("Hapus", "#E74C3C", "/Images/trash_white.png");
            btnHapus.setOnAction(e -> handleHapus(p));

            actionBox.getChildren().addAll(btnEdit, btnHapus);
            baris.getChildren().addAll(lblNo, lblId, lblTgl, lblNominal, lblJenis, lblUser, spacer, actionBox);
            vboxPengeluaranList.getChildren().add(baris);
        }
    }

    private void handleHapus(Pengeluaran p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText(null);
        confirm.setContentText("Hapus data pengeluaran '" + p.getIdPengeluaran() + "'?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (pengeluaranDAO.deletePengeluaran(p.getIdPengeluaran())) {
                    muatDataPengeluaran();
                }
            }
        });
    }

    private Button createActionButton(String text, String color, String iconPath) {
        Button btn = new Button(text);
        try {
            ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
            iv.setFitHeight(14); iv.setFitWidth(14);
            btn.setGraphic(iv);
        } catch (Exception e) {}
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6 12 6 12;");
        return btn;
    }

    public void setDarkMode(boolean enabled) {
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#E0E0E0";

        if (vboxMainContent != null) vboxMainContent.setStyle("-fx-background-color: " + bgMain + ";");
        if (hboxSearch != null) hboxSearch.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10;");
        if (lblSubTitle != null) lblSubTitle.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");

        // Refresh konten agar warna baris berubah
        muatDataPengeluaran();
    }
}