package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import dao.KategoriDAO;
import model.Kategori;

public class FormKategoriController {

    @FXML private VBox rootPane;
    @FXML private Label lblHeader, lblError;
    @FXML private TextField txtIdKategori, txtNamaKategori;
    @FXML private Button btnSimpan;

    private KategoriDAO dao = new KategoriDAO();
    private boolean isEdit = false;

    // --- PERBAIKAN 1: Tambahkan variabel ini agar baris 59 tidak merah lagi ---
    private String idLama;

    @FXML
    public void initialize() {
        setDarkMode(MainController.isDarkMode);
    }

    /**
     * Method ini dipanggil dari KategoriController untuk mengirim data.
     */
    public void setData(Kategori k) {
        if (k != null) {
            isEdit = true;

            // --- PERBAIKAN 2: Simpan ID asli ke idLama sebelum diedit ---
            this.idLama = k.getIdKategori();

            lblHeader.setText("Edit Data Kategori");
            btnSimpan.setText("Update");
            txtIdKategori.setText(k.getIdKategori());
            txtNamaKategori.setText(k.getNamaKategori());

            // Tips: Jika ingin ID tetap bisa diedit, hapus setEditable(false) ini
            // Tapi karena kita pakai idLama, mengedit ID sekarang jadi AMAN.
            txtIdKategori.setEditable(true);
        } else {
            isEdit = false;
            lblHeader.setText("Tambah Data Kategori");
            btnSimpan.setText("Simpan");
        }
    }

    @FXML
    private void handleSimpan(ActionEvent event) {
        String id = txtIdKategori.getText().trim();
        String nama = txtNamaKategori.getText().trim();

        // 1. Validasi
        if (id.isEmpty() || nama.isEmpty()) {
            lblError.setText("ID dan Nama wajib diisi!");
            lblError.setVisible(true);
            return;
        }

        Kategori k = new Kategori(id, nama);

        // 3. Eksekusi Simpan ke Database
        boolean sukses;
        if (isEdit) {
            // --- PERBAIKAN 3: Sekarang idLama sudah ada isinya dan bisa dipakai ---
            sukses = KategoriDAO.updateKategori(k, idLama);
        } else {
            sukses = KategoriDAO.addKategori(k);
        }

        // 4. Jika sukses, tutup jendela
        if (sukses) {
            closeWindow(event); // Menggunakan method pembantu yang sudah ada
        } else {
            // Munculkan pesan error jika gagal
            lblError.setText("Gagal simpan! Cek koneksi atau ID sudah ada.");
            lblError.setVisible(true);
        }
    }

    @FXML
    private void handleBatal(ActionEvent event) {
        closeWindow(event);
    }

    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    public void setDarkMode(boolean enabled) {
        String bgMain = enabled ? "#1E1E1E" : "white";
        String textColor = enabled ? "white" : "#1F2937";
        String mutedText = enabled ? "#D1D5DB" : "#444444";
        String borderColor = enabled ? "#3A3A3A" : "#DCDCDC";
        String inputBg = enabled ? "#2C2C2C" : "white";

        if (rootPane != null) {
            rootPane.setStyle("-fx-background-color: " + bgMain + "; -fx-background-radius: 12;");
            rootPane.getChildren().forEach(node -> {
                if (node instanceof VBox) {
                    VBox section = (VBox) node;
                    section.getChildren().forEach(child -> {
                        if (child instanceof Label) {
                            Label label = (Label) child;
                            if (label != lblHeader && label != lblError) {
                                label.setStyle("-fx-font-weight: bold; -fx-text-fill: " + mutedText + ";");
                            }
                        } else if (child instanceof TextField) {
                            TextField field = (TextField) child;
                            field.setStyle("-fx-background-radius: 8; -fx-border-color: " + borderColor + "; -fx-border-radius: 8; -fx-background-color: " + inputBg + "; -fx-text-fill: " + textColor + "; -fx-prompt-text-fill: " + (enabled ? "#9CA3AF" : "#9AA0A6") + ";");
                        }
                    });
                } else if (node instanceof HBox) {
                    HBox buttonRow = (HBox) node;
                    buttonRow.getChildren().forEach(child -> {
                        if (child instanceof Button) {
                            Button button = (Button) child;
                            if (button == btnSimpan) {
                                button.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
                            } else {
                                button.setStyle("-fx-background-color: " + (enabled ? "#B8BEC6" : "#F3F4F6") + "; -fx-text-fill: " + (enabled ? "#111111" : "#374151") + "; -fx-background-radius: 8; -fx-cursor: hand;");
                            }
                        }
                    });
                }
            });
        }

        if (lblHeader != null) {
            lblHeader.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        }
    }
}
