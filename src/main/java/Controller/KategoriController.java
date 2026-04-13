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
import model.Kategori;
import model.KategoriDAO;

public class KategoriController implements Initializable {

    @FXML private VBox vboxKategoriList;
    private KategoriDAO kategoriDAO = new KategoriDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadDataKategori();
    }

    private void loadDataKategori() {
        vboxKategoriList.getChildren().clear();
        List<Kategori> list = kategoriDAO.getAllKategori();

        int no = 1;
        for (Kategori k : list) {
            HBox baris = new HBox(10);
            baris.setAlignment(Pos.CENTER_LEFT);
            baris.setPadding(new Insets(10, 15, 10, 15));
            baris.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");

            // 1. Kolom NO (91)
            Label lblNo = new Label(String.valueOf(no++));
            lblNo.setMinWidth(91.0); lblNo.setPrefWidth(91.0); lblNo.setMaxWidth(91.0);
            lblNo.setStyle("-fx-text-fill: #2C3E50;");

            // 2. Kolom ID KATEGORI (199)
            Label lblId = new Label(k.getIdKategori());
            lblId.setMinWidth(199.0); lblId.setPrefWidth(199.0); lblId.setMaxWidth(199.0);
            lblId.setStyle("-fx-text-fill: #2C3E50;");

            // 3. Kolom NAMA KATEGORI (236)
            Label lblNama = new Label(k.getNamaKategori());
            lblNama.setMinWidth(236.0); lblNama.setPrefWidth(236.0); lblNama.setMaxWidth(236.0);
            lblNama.setStyle("-fx-text-fill: #2C3E50; -fx-font-weight: bold;");

            // SPACER
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // --- 4. KONTAINER AKSI (Diperlebar ke 180 biar teks tidak kepotong) ---
            HBox actionBox = new HBox(10);
            actionBox.setMinWidth(180.0); actionBox.setPrefWidth(180.0); actionBox.setMaxWidth(180.0);
            actionBox.setAlignment(Pos.CENTER_LEFT);

            // Tombol Edit
            Button btnEdit = new Button("Edit");
            btnEdit.setMinWidth(Region.USE_PREF_SIZE); // Jurus biar teks "Edit" tampil full
            try {
                ImageView ivEdit = new ImageView(new Image(getClass().getResourceAsStream("/Images/pencil_white.png")));
                ivEdit.setFitHeight(14); ivEdit.setFitWidth(14);
                btnEdit.setGraphic(ivEdit);
            } catch (Exception e) {}
            btnEdit.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 5 12 5 12;");

            // Tombol Hapus
            Button btnHapus = new Button("Hapus");
            btnHapus.setMinWidth(Region.USE_PREF_SIZE); // Jurus biar teks "Hapus" tampil full
            try {
                ImageView ivTrash = new ImageView(new Image(getClass().getResourceAsStream("/Images/trash_white.png")));
                ivTrash.setFitHeight(14); ivTrash.setFitWidth(14);
                btnHapus.setGraphic(ivTrash);
            } catch (Exception e) {}
            btnHapus.setStyle("-fx-background-color: #F87171; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 5 12 5 12;");

            actionBox.getChildren().addAll(btnEdit, btnHapus);

            baris.getChildren().addAll(lblNo, lblId, lblNama, spacer, actionBox);
            vboxKategoriList.getChildren().add(baris);
        }
    }
}