package Controller;

// --- 1. IMPORT TOOLS (Alat bantu untuk UI dan Data) ---
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

// --- 2. CLASS HEADER (Rumah utama kodingan dashboard) ---
public class DashboardController implements Initializable {

    @FXML private VBox paneRoot, stokListContainer;
    @FXML private HBox hboxHeader;
    @FXML private Label lblDashboard, lblCard1Title, lblCard1Value, lblCard2Title, lblCard2Value, lblCard3Title, lblCard3Value, lblCard4Title;
    @FXML private VBox card1, card2, card3, card4;
    @FXML private Button btnCard1, btnCard2, btnCard4, btnLogout;
    @FXML private ImageView imgLightMode, imgDarkMode, imgLogout;
    @FXML private ScrollPane scrollStok;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        muatDataStokHampirHabis(); 
        setDarkMode(MainController.isDarkMode);
    }

    public void setDarkMode(boolean enabled) {
        String bgMain = enabled ? "#121212" : "#f4f4f4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String borderColor = enabled ? "#333333" : "#B0B0B0";
        String textColor = enabled ? "white" : "#2C3E50";
        String mutedText = enabled ? "#bbbbbb" : "#555555";

        paneRoot.setStyle("-fx-background-color: " + bgMain + ";");
        hboxHeader.setStyle("-fx-background-color: #4A76A8;");
        lblDashboard.setStyle("-fx-text-fill: white;");

        // Card Styles
        String cardBase = "-fx-background-radius: 15; -fx-border-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);";
        
        card1.setStyle("-fx-background-color: " + (enabled ? "#1e2a3a" : "#EAF1FB") + "; -fx-border-color: #2196F3; " + cardBase);
        card2.setStyle("-fx-background-color: " + bgCard + "; -fx-border-color: " + borderColor + "; " + cardBase);
        card3.setStyle("-fx-background-color: " + bgCard + "; -fx-border-color: #2196F3; " + cardBase);
        card4.setStyle("-fx-background-color: " + (enabled ? "#1b2e1f" : "#E8F5E9") + "; -fx-border-color: #328B51; " + cardBase);

        lblCard1Title.setStyle("-fx-text-fill: " + (enabled ? "#64b5f6" : "#1976D2") + "; -fx-font-weight: bold;");
        lblCard1Value.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
        btnCard1.setStyle("-fx-background-color: " + (enabled ? "#333333" : "white") + "; -fx-text-fill: " + textColor + "; -fx-border-color: " + borderColor + "; -fx-background-radius: 8; -fx-border-radius: 8;");

        lblCard2Title.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
        lblCard2Value.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
        btnCard2.setStyle("-fx-background-color: " + (enabled ? "#333333" : "white") + "; -fx-text-fill: " + textColor + "; -fx-border-color: " + borderColor + "; -fx-background-radius: 8; -fx-border-radius: 8;");

        lblCard3Title.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
        lblCard3Value.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");

        lblCard4Title.setStyle("-fx-text-fill: " + (enabled ? "#81c784" : "#2E7D32") + "; -fx-font-weight: bold;");
        btnCard4.setStyle("-fx-background-color: " + (enabled ? "#333333" : "white") + "; -fx-text-fill: " + textColor + "; -fx-border-color: " + borderColor + "; -fx-background-radius: 8; -fx-border-radius: 8;");

        try {
            if (imgLightMode != null) imgLightMode.setImage(new Image(getClass().getResourceAsStream(enabled ? "/Images/ICON3DARK.png" : "/Images/ICON3.png")));
            if (imgDarkMode != null) imgDarkMode.setImage(new Image(getClass().getResourceAsStream(enabled ? "/Images/ICON4DARK.png" : "/Images/ICON4.png")));
            if (imgLogout != null) imgLogout.setImage(new Image(getClass().getResourceAsStream(enabled ? "/Images/ICON33.png" : "/Images/ICON6.png")));
        } catch (Exception e) {}

        scrollStok.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        muatDataStokHampirHabis(); // Re-render rows with theme
    }

    private void muatDataStokHampirHabis() {
        stokListContainer.getChildren().clear();
        boolean isDark = MainController.isDarkMode;

        for (int i = 1; i <= 5; i++) {
            HBox baris = buatBarisStok("Barang Contoh " + i, i + 2, isDark);
            stokListContainer.getChildren().add(baris);
        }
    }

    private HBox buatBarisStok(String nama, int sisa, boolean isDark) {
        HBox hBox = new HBox();
        hBox.setSpacing(20);
        hBox.setPadding(new Insets(10));
        hBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        String bg = isDark ? "#2c2c2c" : "white";
        String border = isDark ? "#444444" : "transparent";
        String text = isDark ? "white" : "#2C3E50";

        hBox.setStyle("-fx-background-color: " + bg + "; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: " + border + "; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label lblNama = new Label(nama);
        lblNama.setPrefWidth(180);
        lblNama.setStyle("-fx-font-weight: bold; -fx-text-fill: " + text + ";");

        Label lblSisa = new Label("Sisa: " + sisa);
        lblSisa.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");

        hBox.getChildren().addAll(lblNama, lblSisa);
        return hBox;
    }
}