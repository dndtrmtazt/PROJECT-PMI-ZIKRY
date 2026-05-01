package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class ExportLaporanDialogController {

    public enum ExportFormat {
        PDF,
        EXCEL
    }

    @FXML private Button btnClose;
    @FXML private Button btnPdf;
    @FXML private Button btnExcel;
    @FXML private Button btnCancel;

    private ExportFormat selectedFormat;

    public ExportFormat getSelectedFormat() {
        return selectedFormat;
    }

    @FXML
    private void handlePdf() {
        selectedFormat = ExportFormat.PDF;
        closeDialog(btnPdf);
    }

    @FXML
    private void handleExcel() {
        selectedFormat = ExportFormat.EXCEL;
        closeDialog(btnExcel);
    }

    @FXML
    private void handleCancel() {
        closeDialog(btnCancel);
    }

    @FXML
    private void handleClose() {
        closeDialog(btnClose);
    }

    private void closeDialog(Button source) {
        if (source != null && source.getScene() != null) {
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        }
    }
}
