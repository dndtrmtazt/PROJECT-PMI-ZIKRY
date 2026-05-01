package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

public class ExportSuccessDialogController {

    @FXML private Button btnClose;
    @FXML private Button btnOk;
    @FXML private Label lblFormatBadge;
    @FXML private Label lblPath;
    @FXML private Label lblInfo;

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

    @FXML
    private void handleClose() {
        closeDialog(btnClose);
    }

    @FXML
    private void handleOk() {
        closeDialog(btnOk);
    }

    private void closeDialog(Button source) {
        if (source != null && source.getScene() != null) {
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        }
    }
}
