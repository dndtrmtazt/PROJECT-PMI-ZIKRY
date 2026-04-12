package Controller;

// --- 1. IMPORT (Bahan-bahan masakan) ---
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

// SESUAIKAN: Pastikan folder model kamu beneran ada
import model.Pengeluaran;
import model.PengeluaranDAO;

// --- 2. CLASS (Rumah Utama) ---
public class PengeluaranController {

    // --- 3. DEKLARASI VARIABEL FXML ---
    @FXML private VBox vboxPengeluaranList;
    @FXML private TextField txtSearchPengeluaran;
    @FXML private DatePicker dpFilterTanggal;

    // --- 4. DEKLARASI DAO (Biar nggak merah lagi) ---
    private PengeluaranDAO pengeluaranDAO = new PengeluaranDAO();

    @FXML
    public void initialize() {
        // Panggil fungsi ini pas halaman dibuka biar datanya langsung muncul
        muatDataPengeluaran();
    }

    // --- 5. FUNGSI LOGIKA ---
    private void muatDataPengeluaran() {
        vboxPengeluaranList.getChildren().clear();

        // Panggil langsung pakai nama Class karena method-nya STATIC
        // Gunakan nama fungsi yang benar: getAllPengeluaran()
        List<Pengeluaran> list = PengeluaranDAO.getAllPengeluaran();

        int no = 1;
        for (Pengeluaran p : list) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 15, 8, 15));
            row.setStyle("-fx-background-color: white; -fx-border-color: #F0F0F0; -fx-border-width: 0 0 1 0;");

            Label lblNo = new Label(String.valueOf(no++));
            lblNo.setPrefWidth(40);

            Label lblId = new Label(p.getIdPengeluaran());
            lblId.setPrefWidth(100);

            // Sesuaikan: di DAO kamu pakainya p.getTglPengeluaran()
            Label lblTgl = new Label(p.getTglPengeluaran().toString());
            lblTgl.setPrefWidth(110);

            Label lblNom = new Label("Rp " + String.format("%,.0f", p.getNominal()));
            lblNom.setPrefWidth(130);
            lblNom.setStyle("-fx-font-weight: bold; -fx-text-fill: #E67E22;");

            Label lblJenis = new Label(p.getJenis());
            lblJenis.setPrefWidth(120);

            Label lblUser = new Label(p.getIdUser());
            lblUser.setPrefWidth(100);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button btnEdit = new Button("✎");
            btnEdit.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

            Button btnHapus = new Button("X");
            btnHapus.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

            row.getChildren().addAll(lblNo, lblId, lblTgl, lblNom, lblJenis, lblUser, spacer, btnEdit, btnHapus);
            vboxPengeluaranList.getChildren().add(row);
        }
    }
    }