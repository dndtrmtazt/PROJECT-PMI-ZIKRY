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

public class FormPengeluaranController implements Initializable {

    @FXML private VBox rootPane;
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
        setDarkMode(MainController.isDarkMode);

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

    public void setDarkMode(boolean enabled) {
        String bgMain = enabled ? "#1E1E1E" : "white";
        String textColor = enabled ? "white" : "#1F2937";
        String mutedText = enabled ? "#D1D5DB" : "#374151";
        String borderColor = enabled ? "#3A3A3A" : "#DCDCDC";
        String inputBg = enabled ? "#2C2C2C" : "white";
        String promptColor = enabled ? "#9CA3AF" : "#9AA0A6";
        String datePickerStyle = "-fx-background-color: " + inputBg + "; "
                + "-fx-control-inner-background: " + inputBg + "; "
                + "-fx-border-color: " + borderColor + "; "
                + "-fx-border-radius: 5; "
                + "-fx-background-radius: 5;";
        String dateEditorStyle = "-fx-background-color: " + inputBg + "; "
                + "-fx-control-inner-background: " + inputBg + "; "
                + "-fx-border-color: transparent; "
                + "-fx-background-insets: 0; "
                + "-fx-text-fill: " + textColor + "; "
                + "-fx-prompt-text-fill: " + promptColor + ";";

        if (rootPane != null) {
            rootPane.setStyle("-fx-background-color: " + bgMain + "; -fx-background-radius: 10;");
            rootPane.getChildren().forEach(node -> {
                if (node instanceof VBox) {
                    VBox contentBox = (VBox) node;
                    contentBox.getChildren().forEach(child -> {
                        if (child instanceof VBox) {
                            VBox fieldBox = (VBox) child;
                            fieldBox.getChildren().forEach(grandChild -> {
                                if (grandChild instanceof Label) {
                                    Label label = (Label) grandChild;
                                    label.setStyle("-fx-text-fill: " + mutedText + ";");
                                } else if (grandChild instanceof TextField) {
                                    TextField field = (TextField) grandChild;
                                    field.setStyle("-fx-background-color: " + inputBg + "; -fx-border-color: " + borderColor + "; -fx-border-radius: 5; -fx-background-radius: 5; -fx-text-fill: " + textColor + "; -fx-prompt-text-fill: " + (enabled ? "#9CA3AF" : "#9AA0A6") + ";");
                                } else if (grandChild instanceof DatePicker) {
                                    DatePicker picker = (DatePicker) grandChild;
                                    picker.setStyle(datePickerStyle);
                                    if (picker.getEditor() != null) {
                                        picker.getEditor().setStyle(dateEditorStyle);
                                    }
                                }
                            });
                        } else if (child instanceof HBox) {
                            HBox buttonRow = (HBox) child;
                            buttonRow.getChildren().forEach(buttonNode -> {
                                if (buttonNode instanceof Button) {
                                    Button button = (Button) buttonNode;
                                    if (button == btnSimpan) {
                                        button.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6;");
                                    } else {
                                        button.setStyle("-fx-background-color: " + (enabled ? "#B8BEC6" : "#BDC3C7") + "; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6;");
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }

        if (lblHeader != null) {
            lblHeader.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        }

        if (dpTanggal != null) {
            dpTanggal.setStyle(datePickerStyle);
            if (dpTanggal.getEditor() != null) {
                dpTanggal.getEditor().setStyle(dateEditorStyle);
            }
        }
    }

}
