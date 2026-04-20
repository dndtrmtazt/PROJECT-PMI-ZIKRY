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
import javafx.stage.Stage;
import model.Pengeluaran;
import model.PengeluaranDAO;
import java.time.LocalDate;

public class FormPengeluaranController implements Initializable {

    @FXML private Label lblHeader;
    @FXML private TextField txtId, txtNominal, txtJenis;
    @FXML private DatePicker dpTanggal;
    @FXML private Button btnSimpan;

    private boolean isEdit = false;
    private String idLama;

    // Instance DAO untuk operasi database
    private PengeluaranDAO pengeluaranDAO = new PengeluaranDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dpTanggal.setValue(LocalDate.now());

        // Membuat Tooltip peringatan visual
        Tooltip tipNominal = new Tooltip("Hanya angka yang diperbolehkan!");
        tipNominal.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold;");

        // Validasi input nominal: Hanya angka yang diperbolehkan
        txtNominal.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();

            // Cek apakah input baru sesuai dengan pola angka (\d*)
            if (newText.matches("\\d*")) {
                return change;
            }

            // Jika user mengetik huruf, tampilkan peringatan Tooltip
            Point2D p = txtNominal.localToScene(0.0, 0.0);
            if (p != null && txtNominal.getScene() != null) {
                tipNominal.show(txtNominal,
                        p.getX() + txtNominal.getScene().getWindow().getX() + 10,
                        p.getY() + txtNominal.getScene().getWindow().getY() - 30
                );

                // Sembunyikan tooltip otomatis setelah 1.5 detik
                new Thread(() -> {
                    try { Thread.sleep(1500); } catch (InterruptedException e) {}
                    Platform.runLater(() -> tipNominal.hide());
                }).start();
            }

            return null; // Menolak perubahan jika bukan angka
        }));
    }

    public void setData(Pengeluaran p) {
        if (p != null) {
            isEdit = true;
            this.idLama = p.getIdPengeluaran(); // Simpan ID asli untuk WHERE clause saat update

            lblHeader.setText("Edit Data Pengeluaran");
            btnSimpan.setText("Update");

            txtId.setText(p.getIdPengeluaran());
            dpTanggal.setValue(p.getTglPengeluaran());
            // Format agar nominal tidak muncul desimal (.0) di form
            txtNominal.setText(String.format("%.0f", p.getNominal()));
            txtJenis.setText(p.getJenis());
        } else {
            isEdit = false;
            lblHeader.setText("Tambah Data Pengeluaran");
            btnSimpan.setText("Simpan");

            txtId.clear();
            txtNominal.clear();
            txtJenis.clear();
            dpTanggal.setValue(LocalDate.now());
        }
    }

    @FXML
    private void handleSimpan(ActionEvent event) {
        try {
            String id = txtId.getText().trim();
            LocalDate tgl = dpTanggal.getValue();
            String nominalStr = txtNominal.getText().replace(".", "").trim();
            String jenis = txtJenis.getText().trim();
            String idUser = "PMK001"; // ID User default

            if (id.isEmpty() || nominalStr.isEmpty() || jenis.isEmpty() || tgl == null) {
                tampilkanPesan("Semua field wajib diisi!", Alert.AlertType.WARNING);
                return;
            }

            double nominal = Double.parseDouble(nominalStr);
            Pengeluaran p = new Pengeluaran(id, tgl, nominal, jenis, idUser);

            boolean sukses;
            if (isEdit) {
                // Pastikan method updatePengeluaran di DAO bersifat static atau panggil lewat instance
                sukses = PengeluaranDAO.updatePengeluaran(p, idLama);
            } else {
                sukses = PengeluaranDAO.addPengeluaran(p);
            }

            if (sukses) {
                tutupJendela(event);
            } else {
                tampilkanPesan("Gagal menyimpan! Cek apakah ID sudah ada.", Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            tampilkanPesan("Nominal harus berupa angka valid!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBatal(ActionEvent event) {
        tutupJendela(event);
    }

    private void tutupJendela(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void tampilkanPesan(String pesan, Alert.AlertType tipe) {
        Alert alert = new Alert(tipe);
        alert.setTitle("Informasi");
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }

}