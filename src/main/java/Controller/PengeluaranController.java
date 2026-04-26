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
import DAO.PengeluaranDAO;
import model.Pengeluaran;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Controller untuk mengelola tampilan daftar Pengeluaran Toko.
 * Alur: Menampilkan riwayat pengeluaran dengan fitur filter tanggal, pencarian kata kunci, serta aksi edit dan hapus.
 */
public class PengeluaranController implements Initializable {

    // [1] Deklarasi komponen UI dari FXML
    @FXML private VBox vboxMainContent, vboxPengeluaranList;
    @FXML private HBox hboxSearch, hboxTableHead;
    @FXML private Label lblTitle, lblSubTitle;
    @FXML private TextField txtSearchPengeluaran;
    @FXML private DatePicker dpFilterTanggal;
    @FXML private Button btnSearch, btnTambahPengeluaran;
    @FXML private ScrollPane scrollPengeluaran;
    @FXML private VBox LyrPengeluaran;

    private PengeluaranDAO pengeluaranDAO = new PengeluaranDAO();

    /**
     * Method initialize: Menyiapkan data dan filter saat halaman dimuat.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // [1] Memuat data pengeluaran awal dari database
        muatDataPengeluaran();
        
        // [2] Menyiapkan aksi otomatis saat user mengetik atau memilih tanggal
        if (txtSearchPengeluaran != null) {
            txtSearchPengeluaran.setOnAction(event -> muatDataPengeluaran());
        }
        if (dpFilterTanggal != null) {
            dpFilterTanggal.setOnAction(event -> muatDataPengeluaran());
        }
        
        // [3] Menyesuaikan tema tampilan (Gelap/Terang)
        setDarkMode(MainController.isDarkMode);
    }

    /**
     * Method handleTambahPengeluaran: Menangani klik tombol tambah pengeluaran.
     */
    @FXML
    private void handleTambahPengeluaran() {
        showPengeluaranDialog(null); // Membuka dialog form kosong
    }

    /**
     * Method showPengeluaranDialog: Membuka jendela pop-up Form Pengeluaran.
     * Alur: 1. Load FXML -> 2. Set Data (Null untuk Tambah) -> 3. Tampilkan -> 4. Refresh List.
     */
    private void showPengeluaranDialog(Pengeluaran p) {
        try {
            // [1] Memuat file desain form
            URL fxmlLocation = getClass().getResource("/FXML/Admin/FormPengeluaran.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // [2] Mengirim data pengeluaran (jika mode edit) ke controller form
            FormPengeluaranController controller = loader.getController();
            controller.setData(p);

            // [3] Membuat stage dialog baru
            Stage stage = new Stage();
            stage.setTitle(p == null ? "Tambah Data Pengeluaran" : "Edit Data Pengeluaran");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // [4] Memperbarui list data di layar utama setelah form ditutup
            muatDataPengeluaran();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method muatDataPengeluaran: Melakukan rendering baris data pengeluaran ke layar.
     * Alur: 1. Ambil data terfilter -> 2. Urutkan berdasarkan tanggal terbaru -> 3. Render baris HBox kustom.
     */
    private void muatDataPengeluaran() {
        if (vboxPengeluaranList == null) return;
        
        // [1] Bersihkan kontainer tampilan lama
        vboxPengeluaranList.getChildren().clear();

        // [2] Ambil list data yang sudah melewati filter pencarian dan tanggal
        List<Pengeluaran> list = getFilteredPengeluaran();
        
        // [3] Urutkan data berdasarkan tanggal terbaru (descending)
        list.sort(Comparator
                .comparing(Pengeluaran::getTglPengeluaran, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Pengeluaran::getIdPengeluaran, Comparator.nullsLast(String::compareToIgnoreCase)));
        
        boolean isDark = MainController.isDarkMode;
        String textColor = isDark ? "white" : "#2C3E50";
        String rowBg = isDark ? "#1e1e1e" : "#FFFFFF";
        String borderColor = isDark ? "#333333" : "#F0F0F0";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        int no = 1;

        // [4] Looping data dan buat komponen visual untuk setiap baris
        for (Pengeluaran p : list) {
            HBox baris = new HBox(10);
            baris.setAlignment(Pos.CENTER_LEFT);
            baris.setPadding(new Insets(10, 15, 10, 15));
            baris.setStyle("-fx-background-color: " + rowBg + "; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0;");

            // [5] Menambahkan Label-label data (No, ID, Tgl, Nominal, Jenis, User)
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

            Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

            // [6] Membuat wadah tombol aksi (Edit & Hapus)
            HBox actionBox = new HBox(8);
            actionBox.setMinWidth(170.0); actionBox.setAlignment(Pos.CENTER);

            Button btnEdit = new Button("Edit");
            btnEdit.setStyle("-fx-background-color: #508CE0; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
            try {
                ImageView iconEdit = new ImageView(new Image(getClass().getResourceAsStream("/Images/pencil_white.png")));
                iconEdit.setFitWidth(14); iconEdit.setFitHeight(14);
                btnEdit.setGraphic(iconEdit);
            } catch (Exception e) {}
            btnEdit.setOnAction(e -> showPengeluaranDialog(p));

            Button btnHapus = new Button("Hapus");
            btnHapus.setStyle("-fx-background-color: #F76065; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
            try {
                ImageView iconHapus = new ImageView(new Image(getClass().getResourceAsStream("/Images/trash_white.png")));
                iconHapus.setFitWidth(14); iconHapus.setFitHeight(14);
                btnHapus.setGraphic(iconHapus);
            } catch (Exception e) {}
            btnHapus.setOnAction(e -> handleHapus(p));

            actionBox.getChildren().addAll(btnEdit, btnHapus);
            baris.getChildren().addAll(lblNo, lblId, lblTgl, lblNominal, lblJenis, lblUser, spacer, actionBox);
            vboxPengeluaranList.getChildren().add(baris);
        }
    }

    /**
     * Method getFilteredPengeluaran: Menyaring data dari database berdasarkan input user.
     */
    private List<Pengeluaran> getFilteredPengeluaran() {
        // [1] Ambil seluruh data mentah dari database
        List<Pengeluaran> allPengeluaran = pengeluaranDAO.getAllPengeluaran();
        String keyword = txtSearchPengeluaran != null ? txtSearchPengeluaran.getText().trim().toLowerCase(Locale.ROOT) : "";
        LocalDate selectedDate = dpFilterTanggal != null ? dpFilterTanggal.getValue() : null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // [2] Lakukan filtering menggunakan Stream API
        return allPengeluaran.stream()
                .filter(p -> selectedDate == null || selectedDate.equals(p.getTglPengeluaran()))
                .filter(p -> {
                    if (keyword.isEmpty()) return true;
                    String tglText = p.getTglPengeluaran() != null ? p.getTglPengeluaran().format(formatter) : "";
                    String nominalText = String.format(Locale.US, "%.0f", p.getNominal());
                    return containsIgnoreCase(p.getIdPengeluaran(), keyword)
                            || containsIgnoreCase(p.getJenis(), keyword)
                            || containsIgnoreCase(p.getIdUser(), keyword)
                            || containsIgnoreCase(tglText, keyword)
                            || nominalText.contains(keyword);
                })
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    @FXML private void handleCariPengeluaran() { muatDataPengeluaran(); }

    /**
     * Method handleHapus: Memproses penghapusan data pengeluaran.
     */
    private void handleHapus(Pengeluaran p) {
        // [1] Konfirmasi penghapusan kepada user
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText(null);
        confirm.setContentText("Hapus data pengeluaran '" + p.getIdPengeluaran() + "'?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // [2] Eksekusi di database dan refresh layar
                if (pengeluaranDAO.deletePengeluaran(p.getIdPengeluaran())) {
                    muatDataPengeluaran();
                }
            }
        });
    }

    /**
     * Method setDarkMode: Menyesuaikan visual halaman sesuai tema aktif.
     */
    public void setDarkMode(boolean enabled) {
        // [1] Inisialisasi variabel warna tema
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#E0E0E0";
        String headerBg = enabled ? "#2C2C2C" : "#F8F9FA";
        String inputBg = enabled ? "#2C2C2C" : "white";

        // [2] Terapkan style ke panel utama dan input filter
        if (vboxMainContent != null) vboxMainContent.setStyle("-fx-background-color: " + bgMain + ";");
        if (hboxSearch != null) hboxSearch.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-border-color: " + borderColor + "; -fx-border-radius: 10;");
        if (txtSearchPengeluaran != null) txtSearchPengeluaran.setStyle("-fx-background-color: " + inputBg + "; -fx-text-fill: " + textColor + "; -fx-border-color: " + borderColor + ";");
        if (dpFilterTanggal != null) {
            dpFilterTanggal.setStyle("-fx-background-color: " + inputBg + "; -fx-border-color: " + borderColor + ";");
            if (dpFilterTanggal.getEditor() != null) dpFilterTanggal.getEditor().setStyle("-fx-background-color: transparent; -fx-text-fill: " + textColor + ";");
        }
        
        // [3] Terapkan style ke header tabel
        if (hboxTableHead != null) {
            hboxTableHead.setStyle("-fx-background-color: " + headerBg + "; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0;");
            hboxTableHead.getChildren().forEach(node -> { if (node instanceof Label) ((Label) node).setStyle("-fx-font-weight: bold; -fx-text-fill: " + textColor + ";"); });
        }
        
        // [4] Refresh data agar warna baris ikut berubah
        muatDataPengeluaran();
    }
}
