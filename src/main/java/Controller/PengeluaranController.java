package Controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Pengeluaran;
import model.PengeluaranDAO;
import java.time.format.DateTimeFormatter;

public class PengeluaranController implements Initializable {

    @FXML private VBox vboxPengeluaranList;
    private PengeluaranDAO pengeluaranDAO = new PengeluaranDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        muatDataPengeluaran();
    }

    private void muatDataPengeluaran() {
        vboxPengeluaranList.getChildren().clear();
        List<Pengeluaran> list = pengeluaranDAO.getAllPengeluaran();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        int no = 1;

        for (Pengeluaran p : list) {
            // --- 1. BARIS UTAMA ---
            HBox baris = new HBox(10);
            baris.setAlignment(Pos.CENTER_LEFT);
            baris.setPadding(new Insets(10, 15, 10, 15));
            baris.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #F0F0F0; -fx-border-width: 0 0 1 0;");

            // --- 2. SETTIAP KOLOM (Hard-Lock Sesuai FXML Header) ---
            Label lblNo = new Label(String.valueOf(no++));
            lblNo.setMinWidth(40.0); lblNo.setPrefWidth(40.0); lblNo.setMaxWidth(40.0);

            Label lblId = new Label(p.getIdPengeluaran());
            lblId.setMinWidth(100.0); lblId.setPrefWidth(100.0); lblId.setMaxWidth(100.0);

            String tglFormat = (p.getTglPengeluaran() != null) ? p.getTglPengeluaran().format(formatter) : "-";
            Label lblTgl = new Label(tglFormat);
            lblTgl.setMinWidth(110.0); lblTgl.setPrefWidth(110.0); lblTgl.setMaxWidth(110.0);

            Label lblNominal = new Label("Rp " + String.format("%,.0f", p.getNominal()).replace(',', '.'));
            lblNominal.setMinWidth(130.0); lblNominal.setPrefWidth(130.0); lblNominal.setMaxWidth(130.0);
            lblNominal.setStyle("-fx-text-fill: #E67E22; -fx-font-weight: bold;");

            Label lblJenis = new Label(p.getJenis());
            lblJenis.setMinWidth(120.0); lblJenis.setPrefWidth(120.0); lblJenis.setMaxWidth(120.0);

            Label lblUser = new Label(p.getIdUser());
            lblUser.setMinWidth(100.0); lblUser.setPrefWidth(100.0); lblUser.setMaxWidth(100.0);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // --- 3. KONTAINER AKSI (DENGAN IKON) ---
            HBox actionBox = new HBox(8);
            actionBox.setMinWidth(100.0); actionBox.setPrefWidth(100.0);
            actionBox.setAlignment(Pos.CENTER);

            // Tombol Edit
            Button btnEdit = new Button();
            try {
                ImageView ivEdit = new ImageView(new Image(getClass().getResourceAsStream("/Images/pencil_white.png")));
                ivEdit.setFitHeight(15); ivEdit.setFitWidth(15);
                btnEdit.setGraphic(ivEdit);
            } catch (Exception e) {
                btnEdit.setText("Edit");
            }
            btnEdit.setStyle("-fx-background-color: #3498DB; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 5;");

            // Tombol Hapus
            Button btnHapus = new Button();
            try {
                ImageView ivTrash = new ImageView(new Image(getClass().getResourceAsStream("/Images/trash_white.png")));
                ivTrash.setFitHeight(15); ivTrash.setFitWidth(15);
                btnHapus.setGraphic(ivTrash);
            } catch (Exception e) {
                btnHapus.setText("Hapus");
            }
            btnHapus.setStyle("-fx-background-color: #E74C3C; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 5;");

            actionBox.getChildren().addAll(btnEdit, btnHapus);

            // --- 4. GABUNGKAN ---
            baris.getChildren().addAll(lblNo, lblId, lblTgl, lblNominal, lblJenis, lblUser, spacer, actionBox);
            vboxPengeluaranList.getChildren().add(baris);
        }
    }
}