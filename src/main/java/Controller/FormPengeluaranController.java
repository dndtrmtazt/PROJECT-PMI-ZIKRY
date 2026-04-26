package Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import DAO.PengeluaranDAO;
import model.Pengeluaran;
import java.time.LocalDate;

/**
 * Controller untuk Form Tambah/Edit Pengeluaran.
 * Alur: Validasi input nominal, deteksi mode (Tambah/Edit), dan penyimpanan ke database.
 */
public class FormPengeluaranController implements Initializable {

    // [1] Deklarasi komponen UI dari file FXML
    @FXML private VBox rootPane;
    @FXML private Label lblHeader;
    @FXML private TextField txtId, txtNominal, txtJenis;
    @FXML private DatePicker dpTanggal;
    @FXML private Button btnSimpan;

    private boolean isEdit = false;
    private String idLama;

    /**
     * Method initialize: Menyiapkan form saat pertama kali dibuka.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // [1] Set tanggal default hari ini dan aktifkan tema
        dpTanggal.setValue(LocalDate.now());
        setDarkMode(MainController.isDarkMode);

        // [2] Kunci ID agar otomatis dari sistem (ReadOnly)
        txtId.setEditable(false);
        txtId.setFocusTraversable(false);

        // [3] Validasi input nominal agar hanya menerima angka
        Tooltip tipNominal = new Tooltip("Hanya angka yang diperbolehkan!");
        tipNominal.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold;");

        txtNominal.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) return change;

            // [4] Tampilkan tooltip peringatan jika user mengetik huruf
            Point2D p = txtNominal.localToScene(0.0, 0.0);
            if (p != null && txtNominal.getScene() != null) {
                tipNominal.show(txtNominal,
                        p.getX() + txtNominal.getScene().getWindow().getX() + 10,
                        p.getY() + txtNominal.getScene().getWindow().getY() - 30
                );
                new Thread(() -> {
                    try { Thread.sleep(1500); } catch (InterruptedException e) {}
                    Platform.runLater(() -> tipNominal.hide());
                }).start();
            }
            return null;
        }));
    }

    /**
     * Method setData: Mengatur mode form (Tambah atau Edit).
     * Alur: 1. Jika p tidak null -> Mode Edit -> Isi form dengan data lama.
     *       2. Jika p null -> Mode Tambah -> Generate ID baru.
     */
    public void setData(Pengeluaran p) {
        if (p != null) {
            // [1] Mode Edit: Simpan ID lama dan isi field
            isEdit = true;
            this.idLama = p.getIdPengeluaran();
            lblHeader.setText("Edit Data Pengeluaran");
            btnSimpan.setText("Update");

            txtId.setText(p.getIdPengeluaran());
            dpTanggal.setValue(p.getTglPengeluaran());
            txtNominal.setText(String.format("%.0f", p.getNominal()));
            txtJenis.setText(p.getJenis());
        } else {
            // [2] Mode Tambah: Reset form dan ambil ID otomatis terbaru
            isEdit = false;
            lblHeader.setText("Tambah Data Pengeluaran");
            btnSimpan.setText("Simpan");
            txtId.setText(PengeluaranDAO.getNextIdPengeluaran());
            txtNominal.clear();
            txtJenis.clear();
            dpTanggal.setValue(LocalDate.now());
        }
    }

    /**
     * Method handleSimpan: Memproses penyimpanan data.
     * Alur: 1. Ambil input -> 2. Validasi kosong -> 3. Panggil DAO Add/Update -> 4. Tutup form.
     */
    @FXML
    private void handleSimpan(ActionEvent event) {
        try {
            // [1] Pengambilan data dari input field
            String id = txtId.getText().trim();
            LocalDate tgl = dpTanggal.getValue();
            String nominalStr = txtNominal.getText().replace(".", "").trim();
            String jenis = txtJenis.getText().trim();
            String idUser = "PMK001"; // Default ID Pemilik

            // [2] Validasi: Pastikan tidak ada data yang kosong
            if (id.isEmpty() || nominalStr.isEmpty() || jenis.isEmpty() || tgl == null) {
                tampilkanPesan("Semua field wajib diisi!", Alert.AlertType.WARNING);
                return;
            }

            // [3] Proses simpan ke database via DAO
            double nominal = Double.parseDouble(nominalStr);
            Pengeluaran p = new Pengeluaran(id, tgl, nominal, jenis, idUser);
            boolean sukses = isEdit ? PengeluaranDAO.updatePengeluaran(p, idLama) : PengeluaranDAO.addPengeluaran(p);

            // [4] Jika sukses, tutup jendela form
            if (sukses) { tutupJendela(event); }
            else { tampilkanPesan("Gagal menyimpan! Cek database kamu.", Alert.AlertType.ERROR); }
        } catch (NumberFormatException e) {
            tampilkanPesan("Nominal harus berupa angka valid!", Alert.AlertType.ERROR);
        }
    }

    /**
     * Method: Menutup jendela form tanpa menyimpan apapun.
     */
    @FXML private void handleBatal(ActionEvent event) { tutupJendela(event); }

    /**
     * Method: Pembantu untuk menutup Stage saat ini.
     */
    private void tutupJendela(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Method: Menampilkan pesan peringatan/error.
     */
    private void tampilkanPesan(String pesan, Alert.AlertType tipe) {
        Alert alert = new Alert(tipe);
        alert.setTitle("Informasi");
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }

    /**
     * Method setDarkMode: Mengatur warna UI sesuai tema aplikasi.
     */
    public void setDarkMode(boolean enabled) {
        // [1] Inisialisasi variabel warna tema
        String bgMain      = enabled ? "#1E1E1E" : "white";
        String textColor   = enabled ? "white" : "#1F2937";
        String borderColor = enabled ? "#3A3A3A" : "#DCDCDC";
        String inputBg     = enabled ? "#2C2C2C" : "white";

        // [2] Mengatur style panel utama dan menyisipkan CSS khusus DatePicker
        if (rootPane != null) {
            rootPane.setStyle("-fx-background-color: " + bgMain + "; -fx-background-radius: 10;");

            rootPane.getStylesheets().removeIf(s -> s.startsWith("data:text/css"));
            if (enabled) {
                rootPane.getStylesheets().add("data:text/css," +
                        ".date-picker > .arrow-button { -fx-background-color: #3A3A3A; -fx-background-radius: 0 5 5 0; }" +
                        ".date-picker > .arrow-button > .arrow { -fx-background-color: white; }" +
                        ".date-picker-popup { -fx-background-color: #1E1E1E; -fx-border-color: #3A3A3A; }" +
                        ".date-picker-popup > .calendar { -fx-background-color: #1E1E1E; }" +
                        ".date-picker-popup > .calendar > .month-year-pane { -fx-background-color: #2C2C2C; }" +
                        ".date-picker-popup > .calendar > .month-year-pane > .label { -fx-text-fill: white; }" +
                        ".date-picker-popup > .calendar > .calendar-grid { -fx-background-color: #1E1E1E; }" +
                        ".date-picker-popup > .calendar > .calendar-grid > .day-cell { -fx-text-fill: #D1D5DB; }" +
                        ".date-picker-popup > .calendar > .calendar-grid > .day-name-cell { -fx-text-fill: #9CA3AF; }" +
                        ".date-picker-popup > .calendar > .calendar-grid > .selected { -fx-background-color: #2ECC71; -fx-text-fill: white; }" +
                        ".date-picker-popup > .calendar > .calendar-grid > .today { -fx-text-fill: #2ECC71; -fx-font-weight: bold; }"
                );
            }
        }

        // [3] Mengatur style komponen input (DatePicker & TextField)
        if (dpTanggal != null) {
            dpTanggal.getEditor().setStyle("-fx-background-color: " + inputBg + "; -fx-text-fill: " + textColor + ";");
            dpTanggal.setStyle("-fx-background-color: " + inputBg + "; -fx-border-color: " + borderColor + "; -fx-border-radius: 5; -fx-background-radius: 5;");
        }

        if (txtId != null) {
            txtId.setStyle("-fx-background-color: " + (enabled ? "#262626" : "#f0f0f0") + "; -fx-border-color: " + borderColor + "; -fx-text-fill: " + (enabled ? "#b0b0b0" : "#777777") + "; -fx-background-radius: 5; -fx-border-radius: 5;");
        }
        if (txtNominal != null) txtNominal.setStyle("-fx-background-color: " + inputBg + "; -fx-border-color: " + borderColor + "; -fx-text-fill: " + textColor + "; -fx-background-radius: 5; -fx-border-radius: 5;");
        if (txtJenis != null) txtJenis.setStyle("-fx-background-color: " + inputBg + "; -fx-border-color: " + borderColor + "; -fx-text-fill: " + textColor + "; -fx-background-radius: 5; -fx-border-radius: 5;");
        if (btnSimpan != null) btnSimpan.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6;");
    }
}
