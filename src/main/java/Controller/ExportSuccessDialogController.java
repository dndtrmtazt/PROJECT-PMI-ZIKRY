package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

// Controller popup informasi bahwa export laporan sudah berhasil.
public class ExportSuccessDialogController {

    @FXML private Button btnClose;
    @FXML private Button btnOk;
    @FXML private Label lblFormatBadge;
    @FXML private Label lblPath;
    @FXML private Label lblInfo;

    // Mengisi badge format, path file, dan pesan sesuai hasil export PDF atau Excel.
    public void setExportResult(ExportLaporanDialogController.ExportFormat format, String path) {
        boolean pdf = format == ExportLaporanDialogController.ExportFormat.PDF;
        lblFormatBadge.setText(pdf ? "PDF" : "XLS");
        lblFormatBadge.getStyleClass().setAll(pdf ? "export-success-format-pdf" : "export-success-format-excel");
        lblPath.setText(path);
        lblPath.setTooltip(new Tooltip(path));
        lblPath.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        lblInfo.setText(pdf
                ? "File telah disimpan dalam format PDF."
                : "File telah disimpan dalam format Excel.");
    }

    // Tombol X menutup popup sukses.
    @FXML
    private void handleClose() {
        closeDialog(btnClose);
    }

    // Tombol OK menutup popup sukses.
    @FXML
    private void handleOk() {
        closeDialog(btnOk);
    }

    // Menutup Stage tempat tombol berada.
    private void closeDialog(Button source) {
        if (source != null && source.getScene() != null) {
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        }
    }
}
