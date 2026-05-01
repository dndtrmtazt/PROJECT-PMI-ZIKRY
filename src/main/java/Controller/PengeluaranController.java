package Controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import DAO.PengeluaranDAO;
import model.Pengeluaran;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

public class PengeluaranController implements Initializable {
    private static final String EDIT_ICON_PATH = "/Images/icon_edit.png";
    private static final String DELETE_ICON_PATH = "/Images/icon_hapus.png";

    @FXML private VBox vboxMainContent, vboxPengeluaranList;
    @FXML private HBox hboxSearch, hboxTableHead;
    @FXML private Label lblTitle, lblSubTitle, lblSortIdPengeluaran, lblSortTanggal, lblSortNominal, lblSortJenis, lblSortPic;
    @FXML private TextField txtSearchPengeluaran;
    @FXML private DatePicker dpFilterTanggal;
    @FXML private Button btnSearch, btnTambahPengeluaran;
    @FXML private ScrollPane scrollPengeluaran;
    @FXML private VBox LyrPengeluaran;
    private SortColumn activeSortColumn;
    private boolean sortAscending = true;

    private enum SortColumn {
        ID, TANGGAL, NOMINAL, JENIS, PIC
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupSortHeaders();
        muatDataPengeluaran();
        if (txtSearchPengeluaran != null) {
            txtSearchPengeluaran.setOnAction(event -> muatDataPengeluaran());
        }
        if (dpFilterTanggal != null) {
            dpFilterTanggal.setOnAction(event -> muatDataPengeluaran());
        }
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

        List<Pengeluaran> list = getFilteredPengeluaran();
        applyCurrentSort(list);
        updateSortHeaderLabels();
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
            lblId.setMinWidth(125.0); lblId.setStyle("-fx-text-fill: " + textColor + ";");

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
            Button btnEdit = createActionButton("Edit", "#3498DB", EDIT_ICON_PATH);
            btnEdit.setOnAction(e -> showPengeluaranDialog(p));

            // Listener Hapus
            Button btnHapus = createActionButton("Hapus", "#E74C3C", DELETE_ICON_PATH);
            btnHapus.setOnAction(e -> handleHapus(p));

            actionBox.getChildren().addAll(btnEdit, btnHapus);
            baris.getChildren().addAll(lblNo, lblId, lblTgl, lblNominal, lblJenis, lblUser, spacer, actionBox);
            vboxPengeluaranList.getChildren().add(baris);
        }
    }

    private List<Pengeluaran> getFilteredPengeluaran() {
        List<Pengeluaran> allPengeluaran = PengeluaranDAO.getAllPengeluaran();
        String keyword = txtSearchPengeluaran != null && txtSearchPengeluaran.getText() != null
                ? txtSearchPengeluaran.getText().trim().toLowerCase(Locale.ROOT)
                : "";
        LocalDate selectedDate = dpFilterTanggal != null ? dpFilterTanggal.getValue() : null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return allPengeluaran.stream()
                .filter(p -> {
                    if (selectedDate == null) {
                        return true;
                    }
                    return selectedDate.equals(p.getTglPengeluaran());
                })
                .filter(p -> {
                    if (keyword.isEmpty()) {
                        return true;
                    }

                    String tanggalText = p.getTglPengeluaran() != null ? p.getTglPengeluaran().format(formatter) : "";
                    String nominalText = String.format(Locale.US, "%.0f", p.getNominal());

                    return containsIgnoreCase(p.getIdPengeluaran(), keyword)
                            || containsIgnoreCase(p.getJenis(), keyword)
                            || containsIgnoreCase(p.getIdUser(), keyword)
                            || containsIgnoreCase(tanggalText, keyword)
                            || nominalText.contains(keyword);
                })
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private void setupSortHeaders() {
        setupSortHeader(lblSortIdPengeluaran, SortColumn.ID);
        setupSortHeader(lblSortTanggal, SortColumn.TANGGAL);
        setupSortHeader(lblSortNominal, SortColumn.NOMINAL);
        setupSortHeader(lblSortJenis, SortColumn.JENIS);
        setupSortHeader(lblSortPic, SortColumn.PIC);
        updateSortHeaderLabels();
    }

    private void setupSortHeader(Label label, SortColumn column) {
        if (label == null) return;
        label.setOnMouseClicked(event -> handleSort(column));
        label.setStyle(label.getStyle() + "; -fx-cursor: hand;");
    }

    private void handleSort(SortColumn column) {
        if (activeSortColumn == column) {
            sortAscending = !sortAscending;
        } else {
            activeSortColumn = column;
            sortAscending = true;
        }
        muatDataPengeluaran();
    }

    private void applyCurrentSort(List<Pengeluaran> list) {
        Comparator<Pengeluaran> comparator = getSortComparator();
        list.sort(comparator);
    }

    private Comparator<Pengeluaran> getSortComparator() {
        Comparator<Pengeluaran> comparator;

        if (activeSortColumn == null) {
            return Comparator
                    .comparing(Pengeluaran::getTglPengeluaran, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Pengeluaran::getIdPengeluaran, this::compareIdPengeluaran);
        }

        switch (activeSortColumn) {
            case ID:
                comparator = (a, b) -> compareIdPengeluaran(a.getIdPengeluaran(), b.getIdPengeluaran());
                break;
            case TANGGAL:
                comparator = Comparator.comparing(Pengeluaran::getTglPengeluaran, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case NOMINAL:
                comparator = Comparator.comparingDouble(Pengeluaran::getNominal);
                break;
            case JENIS:
                comparator = Comparator.comparing(Pengeluaran::getJenis, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                break;
            case PIC:
                comparator = Comparator.comparing(Pengeluaran::getIdUser, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                break;
            default:
                comparator = (a, b) -> 0;
        }

        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        return comparator.thenComparing(Pengeluaran::getIdPengeluaran, this::compareIdPengeluaran);
    }

    private int compareIdPengeluaran(String firstId, String secondId) {
        int numberCompare = Integer.compare(extractIdNumber(firstId), extractIdNumber(secondId));
        if (numberCompare != 0) {
            return numberCompare;
        }
        return nullSafeText(firstId).compareToIgnoreCase(nullSafeText(secondId));
    }

    private int extractIdNumber(String idPengeluaran) {
        if (idPengeluaran == null || idPengeluaran.length() <= 3) {
            return Integer.MAX_VALUE;
        }

        try {
            return Integer.parseInt(idPengeluaran.substring(3));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    private String nullSafeText(String value) {
        return value == null ? "" : value;
    }

    private void updateSortHeaderLabels() {
        setSortHeaderText(lblSortIdPengeluaran, "ID Pengeluaran", SortColumn.ID);
        setSortHeaderText(lblSortTanggal, "Tanggal", SortColumn.TANGGAL);
        setSortHeaderText(lblSortNominal, "Nominal", SortColumn.NOMINAL);
        setSortHeaderText(lblSortJenis, "Jenis", SortColumn.JENIS);
        setSortHeaderText(lblSortPic, "PIC (User)", SortColumn.PIC);
    }

    private void setSortHeaderText(Label label, String baseText, SortColumn column) {
        if (label == null) return;
        String indicator = activeSortColumn == column ? (sortAscending ? " \u25B2" : " \u25BC") : "";
        label.setText(baseText + indicator);
    }

    @FXML
    private void handleCariPengeluaran() {
        muatDataPengeluaran();
    }

    private void handleHapus(Pengeluaran p) {
        if (showDeleteConfirmationDialog()) {
            if (PengeluaranDAO.deletePengeluaran(p.getIdPengeluaran())) {
                muatDataPengeluaran();
            }
        }
    }

    private boolean showDeleteConfirmationDialog() {
        final boolean[] confirmed = {false};
        boolean darkMode = MainController.isDarkMode;

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (vboxMainContent != null && vboxMainContent.getScene() != null) {
            dialog.initOwner(vboxMainContent.getScene().getWindow());
        }
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setResizable(false);

        StackPane root = new StackPane();
        root.getStyleClass().add("admin-delete-dialog-root");
        setStyleClass(root, "dark", darkMode);

        VBox card = new VBox();
        card.getStyleClass().add("admin-delete-dialog-card");
        card.setMinWidth(460);
        card.setPrefWidth(460);
        card.setMaxWidth(460);
        applyRoundedClip(card);

        HBox body = new HBox(12);
        body.getStyleClass().add("admin-delete-dialog-body");
        body.setAlignment(Pos.TOP_LEFT);

        StackPane warningIcon = new StackPane();
        warningIcon.getStyleClass().add("admin-delete-dialog-icon");
        Label warningText = new Label("!");
        warningText.getStyleClass().add("admin-delete-dialog-icon-text");
        warningIcon.getChildren().add(warningText);

        VBox textBox = new VBox(12);
        textBox.setAlignment(Pos.TOP_LEFT);
        Label title = new Label("Konfirmasi Hapus?");
        title.getStyleClass().add("admin-delete-dialog-title");
        Label message = new Label("Anda yakin ingin menghapus data pengeluaran ini?");
        message.getStyleClass().add("admin-delete-dialog-message");
        message.setWrapText(true);
        message.setMaxWidth(320);
        textBox.getChildren().addAll(title, message);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("X");
        closeButton.getStyleClass().add("admin-delete-dialog-close");
        HBox.setMargin(closeButton, new Insets(-12, -2, 0, 0));
        closeButton.setOnAction(event -> dialog.close());

        body.getChildren().addAll(warningIcon, textBox, spacer, closeButton);

        HBox footer = new HBox(18);
        footer.getStyleClass().add("admin-delete-dialog-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = new Button("Batal");
        cancelButton.getStyleClass().add("admin-delete-dialog-cancel");
        cancelButton.setOnAction(event -> dialog.close());

        Button deleteButton = new Button("Hapus");
        deleteButton.getStyleClass().add("admin-delete-dialog-confirm");
        deleteButton.setOnAction(event -> {
            confirmed[0] = true;
            dialog.close();
        });

        footer.getChildren().addAll(cancelButton, deleteButton);
        card.getChildren().addAll(body, footer);
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

        return confirmed[0];
    }

    private void applyRoundedClip(Region region) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        clip.widthProperty().bind(region.widthProperty());
        clip.heightProperty().bind(region.heightProperty());
        region.setClip(clip);
    }

    private Button createActionButton(String text, String color, String iconPath) {
        Button btn = new Button(text);
        InputStream iconStream = iconPath == null ? null : getClass().getResourceAsStream(iconPath);
        if (iconStream != null) {
            ImageView iv = new ImageView(new Image(iconStream));
            iv.setFitHeight(14); iv.setFitWidth(14); iv.setPreserveRatio(true);
            btn.setGraphic(iv);
            btn.setGraphicTextGap(5);
        }
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6 12 6 12;");
        return btn;
    }

    public void setDarkMode(boolean enabled) {
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#E0E0E0";
        String headerBg = enabled ? "#2C2C2C" : "#F8F9FA";
        String inputBg = enabled ? "#2C2C2C" : "white";
        String promptColor = enabled ? "#A1A1AA" : "#9CA3AF";
        String datePickerStyle = "-fx-background-radius: 8; -fx-border-radius: 8; "
                + "-fx-background-color: " + inputBg + "; "
                + "-fx-control-inner-background: " + inputBg + "; "
                + "-fx-border-color: " + borderColor + ";";
        String dateEditorStyle = "-fx-background-color: " + inputBg + "; "
                + "-fx-control-inner-background: " + inputBg + "; "
                + "-fx-border-color: transparent; "
                + "-fx-background-insets: 0; "
                + "-fx-text-fill: " + textColor + "; "
                + "-fx-prompt-text-fill: " + promptColor + ";";

        setStyleClass(vboxMainContent, "dark", enabled);
        setStyleClass(scrollPengeluaran, "dark", enabled);
        if (vboxMainContent != null) vboxMainContent.setStyle("-fx-background-color: " + bgMain + ";");
        if (hboxSearch != null) hboxSearch.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-border-color: " + borderColor + "; -fx-border-radius: 10;");
        if (txtSearchPengeluaran != null) txtSearchPengeluaran.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-background-color: " + inputBg + "; -fx-border-color: " + borderColor + "; -fx-text-fill: " + textColor + "; -fx-prompt-text-fill: " + promptColor + ";");
        if (dpFilterTanggal != null) {
            dpFilterTanggal.setStyle(datePickerStyle);
            if (dpFilterTanggal.getEditor() != null) {
                dpFilterTanggal.getEditor().setStyle(dateEditorStyle);
            }
        }
        if (btnSearch != null) btnSearch.setStyle("-fx-background-color: #4A76A8; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        if (LyrPengeluaran != null) LyrPengeluaran.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-border-color: " + borderColor + "; -fx-border-radius: 10;");
        if (lblSubTitle != null) lblSubTitle.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
        if (hboxTableHead != null) {
            hboxTableHead.setStyle("-fx-background-color: " + headerBg + "; -fx-background-radius: 5; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0;");
            hboxTableHead.getChildren().forEach(node -> {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    label.setStyle("-fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
                }
            });
        }
        if (scrollPengeluaran != null) scrollPengeluaran.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Refresh konten agar warna baris berubah
        muatDataPengeluaran();
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
}
