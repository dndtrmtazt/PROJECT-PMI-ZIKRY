package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

// Controller popup pilihan format export laporan.
public class ExportLaporanDialogController {

    // Format yang bisa dipilih user saat export laporan.
    public enum ExportFormat {
        PDF,
        EXCEL
    }

    @FXML private Button btnClose;
    @FXML private Button btnPdf;
    @FXML private Button btnExcel;
    @FXML private Button btnCancel;

    // Nilai ini dibaca oleh LaporanController setelah popup ditutup.
    private ExportFormat selectedFormat;

    // Mengembalikan format yang dipilih user, atau null jika batal.
    public ExportFormat getSelectedFormat() {
        return selectedFormat;
    }

    // User memilih export PDF.
    @FXML
    private void handlePdf() {
        selectedFormat = ExportFormat.PDF;
        closeDialog(btnPdf);
    }

    // User memilih export Excel.
    @FXML
    private void handleExcel() {
        selectedFormat = ExportFormat.EXCEL;
        closeDialog(btnExcel);
    }

    // User membatalkan pilihan format.
    @FXML
    private void handleCancel() {
        closeDialog(btnCancel);
    }

    // Tombol X menutup dialog tanpa memilih format.
    @FXML
    private void handleClose() {
        closeDialog(btnClose);
    }

    // Menutup popup dari tombol yang ditekan.
    private void closeDialog(Button source) {
        if (source != null && source.getScene() != null) {
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        }
    }
}
