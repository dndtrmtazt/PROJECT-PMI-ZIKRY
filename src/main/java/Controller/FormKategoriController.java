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
import DAO.KategoriDAO;
import model.Kategori;

/**
 * Controller untuk Form Tambah/Edit Kategori Barang.
 * Alur: Menangani validasi dan penyimpanan data kategori (ID & Nama).
 */
public class FormKategoriController {

    // [1] Deklarasi komponen UI dari file FXML
    @FXML private VBox rootPane;
    @FXML private Label lblHeader, lblError;
    @FXML private TextField txtIdKategori, txtNamaKategori;
    @FXML private Button btnSimpan;

    private KategoriDAO dao = new KategoriDAO();
    private boolean isEdit = false;
    private String idLama;

    /**
     * Method initialize: Menyiapkan tema visual form.
     */
    @FXML
    public void initialize() {
        setDarkMode(MainController.isDarkMode);
    }

    /**
     * Method setData: Mengisi form jika dalam mode EDIT.
     * Alur: 1. Simpan ID asli ke idLama -> 2. Ubah teks header/tombol -> 3. Isi TextField.
     */
    public void setData(Kategori k) {
        if (k != null) {
            // [1] Mode Edit: Ambil data dari objek k
            isEdit = true;
            this.idLama = k.getIdKategori();
            lblHeader.setText("Edit Data Kategori");
            btnSimpan.setText("Update");
            txtIdKategori.setText(k.getIdKategori());
            txtNamaKategori.setText(k.getNamaKategori());
            txtIdKategori.setEditable(true);
        } else {
            // [2] Mode Tambah: Reset status ke mode baru
            isEdit = false;
            lblHeader.setText("Tambah Data Kategori");
            btnSimpan.setText("Simpan");
        }
    }

    /**
     * Method handleSimpan: Memproses data ke database.
     * Alur: 1. Validasi kosong -> 2. Eksekusi DAO Update/Add -> 3. Tutup jendela.
     */
    @FXML
    private void handleSimpan(ActionEvent event) {
        String id = txtIdKategori.getText().trim();
        String nama = txtNamaKategori.getText().trim();

        // [1] Validasi: Pastikan field tidak kosong
        if (id.isEmpty() || nama.isEmpty()) {
            lblError.setText("ID dan Nama wajib diisi!");
            lblError.setVisible(true);
            return;
        }

        Kategori k = new Kategori(id, nama);

        // [2] Eksekusi simpan ke database via DAO
        boolean sukses;
        if (isEdit) {
            sukses = KategoriDAO.updateKategori(k, idLama);
        } else {
            sukses = KategoriDAO.addKategori(k);
        }

        // [3] Jika sukses, tutup jendela form
        if (sukses) {
            closeWindow(event);
        } else {
            lblError.setText("Gagal simpan! Cek koneksi atau ID sudah ada.");
            lblError.setVisible(true);
        }
    }

    /**
     * Method: Menutup form tanpa menyimpan apapun.
     */
    @FXML
    private void handleBatal(ActionEvent event) {
        closeWindow(event);
    }

    /**
     * Method: Pembantu untuk menutup Stage saat ini.
     */
    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Method setDarkMode: Menyesuaikan gaya warna elemen form sesuai tema aplikasi.
     */
    public void setDarkMode(boolean enabled) {
        // [1] Inisialisasi warna tema
        String bgMain = enabled ? "#1E1E1E" : "white";
        String textColor = enabled ? "white" : "#1F2937";
        String mutedText = enabled ? "#D1D5DB" : "#444444";
        String borderColor = enabled ? "#3A3A3A" : "#DCDCDC";
        String inputBg = enabled ? "#2C2C2C" : "white";

        // [2] Iterasi elemen UI untuk menerapkan style secara massal
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
                            field.setStyle("-fx-background-radius: 8; -fx-border-color: " + borderColor + "; -fx-border-radius: 8; -fx-background-color: " + inputBg + "; -fx-text-fill: " + textColor + ";");
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

        // [3] Mengatur warna teks Header
        if (lblHeader != null) {
            lblHeader.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        }
    }
}
