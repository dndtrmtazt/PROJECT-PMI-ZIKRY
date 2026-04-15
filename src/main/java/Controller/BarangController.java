package Controller;

import config.koneksi;
import model.Barang;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.io.IOException;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class BarangController {

    @FXML private VBox paneRoot, vboxTableCard;
    @FXML private HBox hboxSearch, hboxHeader;
    @FXML private TableView<Barang> tableBarang;
    @FXML private TableColumn<Barang, String> colId, colNama, colKategori;
    @FXML private TableColumn<Barang, Integer> colStok;
    @FXML private TableColumn<Barang, Double> colHargaBeli, colHargaJual;
    @FXML private TextField txtCari;
    @FXML private Label lblDaftarBarang, lblTitle;

    private ObservableList<Barang> listBarang = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Mapping Kolom
        colId.setCellValueFactory(new PropertyValueFactory<>("idBarang"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colKategori.setCellValueFactory(new PropertyValueFactory<>("idKategori"));
        colStok.setCellValueFactory(new PropertyValueFactory<>("stok"));

        tableBarang.getColumns().forEach(column -> column.setStyle("-fx-alignment: CENTER-LEFT;"));
        tableBarang.setItems(listBarang);

        // 2. FITUR DOUBLE CLICK PADA BARIS TABEL
        tableBarang.setRowFactory(tv -> {
            TableRow<Barang> row = new TableRow<>();
            row.setStyle("-fx-cursor: hand;");
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    handleEditBarang();
                }
            });
            return row;
        });

        // 3. Format Rupiah & Load Data
        setupFormatRupiah(colHargaBeli, "hargaBeli");
        setupFormatRupiah(colHargaJual, "hargaJual");
        loadData();
        setupPencarian();
        
        // Load CSS
        try {
            tableBarang.getStylesheets().add(getClass().getResource("/CSS/tabel.css").toExternalForm());
        } catch (Exception e) {}

        setDarkMode(MainController.isDarkMode);
    }

    public void setDarkMode(boolean enabled) {
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#D1D5DB";

        if (paneRoot != null) paneRoot.setStyle("-fx-background-color: " + bgMain + ";");
        if (hboxHeader != null) hboxHeader.setStyle("-fx-background-color: #4A76A8;");
        if (lblTitle != null) lblTitle.setStyle("-fx-text-fill: white;");
        if (hboxSearch != null) hboxSearch.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        if (vboxTableCard != null) vboxTableCard.setStyle("-fx-background-color: " + bgCard + "; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        
        if (lblDaftarBarang != null) lblDaftarBarang.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold; -fx-font-size: 20px;");
        if (txtCari != null) txtCari.setStyle("-fx-background-radius: 10; -fx-background-color: " + (enabled ? "#2c2c2c" : "white") + "; -fx-text-fill: " + textColor + "; -fx-border-color: " + borderColor + "; -fx-border-radius: 10;");

        // Sync Table Background
        if (tableBarang != null) {
            tableBarang.getStyleClass().remove("dark");
            if (enabled) {
                tableBarang.getStyleClass().add("dark");
            }
        }
    }

    private void loadData() {
        listBarang.clear();
        String query = "SELECT * FROM barang";
        try (Connection conn = koneksi.koneksiDB();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                listBarang.add(new Barang(
                        rs.getString("id_barang"),
                        rs.getString("nama_barang"),
                        rs.getString("id_kategori"),
                        rs.getInt("stok"),
                        rs.getDouble("harga_beli"),
                        rs.getDouble("harga_jual")
                ));
            }
        } catch (SQLException e) {
            System.err.println("✗ Gagal tarik data: " + e.getMessage());
        }
    }

    private void setupPencarian() {
        FilteredList<Barang> filteredData = new FilteredList<>(listBarang, p -> true);
        txtCari.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(barang -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return barang.getNamaBarang().toLowerCase().contains(lowerCaseFilter) ||
                        barang.getIdBarang().toLowerCase().contains(lowerCaseFilter);
            });
        });
        tableBarang.setItems(filteredData);
    }

    private void setupFormatRupiah(TableColumn<Barang, Double> col, String property) {
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setCellFactory(tc -> new TableCell<Barang, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) setText(null);
                else {
                    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    nf.setMaximumFractionDigits(0);
                    setText(nf.format(price));
                }
            }
        });
    }

    @FXML
    private void handleEditBarang() {
        Barang selected = tableBarang.getSelectionModel().getSelectedItem();

        if (selected != null && MainController.getInstance() != null) {
            FXMLLoader loader = MainController.getInstance().panggilHalaman("EditBarang");
            if (loader != null) {
                EditBarangController controller = loader.getController();
                controller.initData(
                        selected.getIdBarang(),
                        selected.getNamaBarang(),
                        selected.getIdKategori(),
                        selected.getStok(),
                        selected.getHargaBeli(),
                        selected.getHargaJual()
                );
            }
        }
    }

    @FXML
    private void handleTambahBarang() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().panggilHalaman("TambahBarang");
        }
    }
}