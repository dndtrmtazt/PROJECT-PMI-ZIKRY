package Controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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
        setDarkMode(MainController.isDarkMode);
    }

    public void setDarkMode(boolean enabled) {
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#E0E0E0";
        String inputBg = enabled ? "#2C2C2C" : "white";

        if (vboxMainContent != null) vboxMainContent.setStyle("-fx-background-color: " + bgMain + ";");
        if (lblTitle != null) {
            lblTitle.getParent().setStyle("-fx-background-color: #4A76A8;");
            lblTitle.setStyle("-fx-text-fill: white;");
        }

        if (hboxSearch != null) hboxSearch.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        
        if (txtSearchPengeluaran != null) {
            txtSearchPengeluaran.setStyle("-fx-background-color: " + bgCard + "; -fx-text-fill: " + textColor + "; -fx-prompt-text-fill: " + (enabled ? "#BBBBBB" : "#757575") + "; -fx-border-color: " + borderColor + "; -fx-border-radius: 8; -fx-background-radius: 8;");
        }
        
        if (dpFilterTanggal != null) {
            dpFilterTanggal.setStyle("-fx-control-inner-background: " + bgCard + "; -fx-background-color: " + bgCard + "; -fx-text-fill: " + textColor + "; -fx-border-color: " + borderColor + "; -fx-border-radius: 8; -fx-background-radius: 8;");
            // Also apply style to the text field inside DatePicker if possible via prompt text fill
            dpFilterTanggal.getEditor().setStyle("-fx-background-color: transparent; -fx-text-fill: " + textColor + "; -fx-prompt-text-fill: " + (enabled ? "#BBBBBB" : "#757575") + ";");
        }

        if (hboxTableHead != null) {
            hboxTableHead.setStyle("-fx-background-color: " + (enabled ? "#333333" : "#F8F9FA") + "; -fx-background-radius: 5; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0;");
            hboxTableHead.getChildren().forEach(node -> {
                if (node instanceof Label) {
                    ((Label) node).setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
                }
            });
        }

        // Find the second VBox which is the card for the list
        if (vboxMainContent != null && vboxMainContent.getChildren().size() > 1) {
            VBox contentBox = (VBox) vboxMainContent.getChildren().get(1); // The wrapper for cards
            if (contentBox.getChildren().size() > 1) {
                VBox listCard = (VBox) contentBox.getChildren().get(1);
                listCard.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
            }
        }

        if (lblSubTitle != null) lblSubTitle.setStyle("-fx-font-family: 'Inter Medium'; -fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        
        muatDataPengeluaran(); // Refresh list with theme
    }

    private void muatDataPengeluaran() {
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
            lblNo.setMinWidth(40.0); lblNo.setPrefWidth(40.0);
            lblNo.setStyle("-fx-text-fill: " + textColor + ";");

            Label lblId = new Label(p.getIdPengeluaran());
            lblId.setMinWidth(100.0); lblId.setPrefWidth(100.0);
            lblId.setStyle("-fx-text-fill: " + textColor + ";");

            String tglFormat = (p.getTglPengeluaran() != null) ? p.getTglPengeluaran().format(formatter) : "-";
            Label lblTgl = new Label(tglFormat);
            lblTgl.setMinWidth(110.0); lblTgl.setPrefWidth(110.0);
            lblTgl.setStyle("-fx-text-fill: " + textColor + ";");

            Label lblNominal = new Label("Rp " + String.format("%,.0f", p.getNominal()).replace(',', '.'));
            lblNominal.setMinWidth(130.0); lblNominal.setPrefWidth(130.0);
            lblNominal.setStyle("-fx-text-fill: #E67E22; -fx-font-weight: bold;");

            Label lblJenis = new Label(p.getJenis());
            lblJenis.setMinWidth(120.0); lblJenis.setPrefWidth(120.0);
            lblJenis.setStyle("-fx-text-fill: " + textColor + ";");

            Label lblUser = new Label(p.getIdUser());
            lblUser.setMinWidth(100.0); lblUser.setPrefWidth(100.0);
            lblUser.setStyle("-fx-text-fill: " + textColor + ";");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
HBox actionBox = new HBox(8);
actionBox.setMinWidth(160.0); actionBox.setAlignment(Pos.CENTER);

// Tombol Edit
Button btnEdit = new Button("Edit");
try {
    ImageView ivEdit = new ImageView(new Image(getClass().getResourceAsStream("/Images/pencil_white.png")));
    ivEdit.setFitHeight(14); ivEdit.setFitWidth(14);
    btnEdit.setGraphic(ivEdit);
} catch (Exception e) {}
btnEdit.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6 12 6 12;");

// Tombol Hapus
Button btnHapus = new Button("Hapus");
try {
    ImageView ivTrash = new ImageView(new Image(getClass().getResourceAsStream("/Images/trash_white.png")));
    ivTrash.setFitHeight(14); ivTrash.setFitWidth(14);
    btnHapus.setGraphic(ivTrash);
} catch (Exception e) {}
btnHapus.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6 12 6 12;");

actionBox.getChildren().addAll(btnEdit, btnHapus);
            baris.getChildren().addAll(lblNo, lblId, lblTgl, lblNominal, lblJenis, lblUser, spacer, actionBox);
            vboxPengeluaranList.getChildren().add(baris);
        }
    }
}