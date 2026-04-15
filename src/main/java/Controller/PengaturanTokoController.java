package Controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PengaturanTokoController {

    @FXML private VBox vboxMainContent, vboxCard, VBP;
    @FXML private HBox hboxHeader;
    @FXML private Label lblTitle, lblSubTitle, lblNamaToko, lblTelp, lblAlamat, lblEmail;
    @FXML private Label txtNamaToko, txtTelp, txtAlamat, txtEmail;
    @FXML private Button btnSimpanInfo;

    @FXML
    public void initialize() {
        setDarkMode(MainController.isDarkMode);
    }

    public void setDarkMode(boolean enabled) {
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#D1D5DB";

        if (vboxMainContent != null) vboxMainContent.setStyle("-fx-background-color: " + bgMain + ";");
        if (hboxHeader != null) hboxHeader.setStyle("-fx-background-color: #4A76A8;");
        if (lblTitle != null) lblTitle.setStyle("-fx-text-fill: white;");

        if (vboxCard != null) {
            vboxCard.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 5);");
        }
        
        if (lblSubTitle != null) lblSubTitle.setStyle("-fx-font-family: 'Inter Medium'; -fx-font-size: 20; -fx-text-fill: " + textColor + ";");
        if (lblNamaToko != null) lblNamaToko.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 14; -fx-text-fill: " + textColor + ";");
        if (lblTelp != null) lblTelp.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 14; -fx-text-fill: " + textColor + ";");
        if (lblAlamat != null) lblAlamat.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 14; -fx-text-fill: " + textColor + ";");
        if (lblEmail != null) lblEmail.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 14; -fx-text-fill: " + textColor + ";");

        // Style the value labels (previously textfields)
        String valueStyle = "-fx-font-family: 'Inter Bold'; -fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";";
        if (txtNamaToko != null) txtNamaToko.setStyle(valueStyle);
        if (txtTelp != null) txtTelp.setStyle(valueStyle);
        if (txtAlamat != null) txtAlamat.setStyle(valueStyle);
        if (txtEmail != null) txtEmail.setStyle(valueStyle);

        // Style the colon labels
        if (VBP != null) {
            for (Node hboxNode : VBP.getChildren()) {
                if (hboxNode instanceof HBox) {
                    for (Node node : ((HBox) hboxNode).getChildren()) {
                        if (node instanceof Label && ((Label) node).getText().equals(":")) {
                            ((Label) node).setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 14;");
                        }
                    }
                }
            }
        }
    }
}
