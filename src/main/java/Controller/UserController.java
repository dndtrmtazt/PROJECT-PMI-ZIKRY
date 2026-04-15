package Controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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

        // Card styling
        if (LyrUsr != null) {
            LyrUsr.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        }

        if (scrollUser != null) {
            scrollUser.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            // To ensure the viewport also doesn't have a background that clashes
            if (scrollUser.lookup(".viewport") != null) {
                scrollUser.lookup(".viewport").setStyle("-fx-background-color: transparent;");
            }
        }

        if (hboxTableHead != null) {
            hboxTableHead.setStyle("-fx-background-color: " + (enabled ? "#333333" : "#F8F9FA") + "; -fx-background-radius: 5; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0;");
            hboxTableHead.getChildren().forEach(node -> {
                if (node instanceof Label) {
                    ((Label) node).setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
                }
            });
        }

        if (lblSubTitle != null) lblSubTitle.setStyle("-fx-font-family: 'Inter Medium'; -fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        
        muatDataUser(); // Refresh user list with current theme
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

            Label lblNama = new Label(u.getIdUser());
            lblNama.setPrefWidth(180);
            lblNama.setStyle("-fx-font-weight: bold; -fx-text-fill: " + textColor + ";");

            Label lblRole = new Label(u.getRole().toUpperCase());
            lblRole.setPrefWidth(120);

            if (u.getRole().equalsIgnoreCase("admin")) {
                lblRole.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
            } else {
                lblRole.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
            }

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox actionBox = new HBox(8);
            actionBox.setMinWidth(160.0); actionBox.setAlignment(Pos.CENTER);

            Button btnEdit = new Button("Edit");
            try {
                ImageView ivEdit = new ImageView(new Image(getClass().getResourceAsStream("/Images/pencil_white.png")));
                ivEdit.setFitHeight(14); ivEdit.setFitWidth(14);
                btnEdit.setGraphic(ivEdit);
            } catch (Exception e) {}
            btnEdit.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6 12 6 12;");
            
            Button btnHapus = new Button("Hapus");
            try {
                ImageView ivTrash = new ImageView(new Image(getClass().getResourceAsStream("/Images/trash_white.png")));
                ivTrash.setFitHeight(14); ivTrash.setFitWidth(14);
                btnHapus.setGraphic(ivTrash);
            } catch (Exception e) {}
            btnHapus.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6 12 6 12;");

            actionBox.getChildren().addAll(btnEdit, btnHapus);
            row.getChildren().addAll(lblNo, lblId, lblNama, lblRole, spacer, actionBox);
            vboxUserList.getChildren().add(row);
        }
    }

    @FXML
    private void handleTambahUser() {
        System.out.println("Form tambah user...");
    }
}