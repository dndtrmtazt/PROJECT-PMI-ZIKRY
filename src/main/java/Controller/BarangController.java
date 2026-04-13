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
import java.io.IOException;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class BarangController {

    @FXML private TableView<Barang> tableBarang;
    @FXML private TableColumn<Barang, String> colId, colNama, colKategori, colSatuan;
    @FXML private TableColumn<Barang, Integer> colStok;
    @FXML private TableColumn<Barang, Double> colHargaBeli, colHargaJual;
    @FXML private TextField txtCari;

    private ObservableList<Barang> listBarang = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Mapping Kolom
        colId.setCellValueFactory(new PropertyValueFactory<>("idBarang"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colKategori.setCellValueFactory(new PropertyValueFactory<>("idKategori"));
        colStok.setCellValueFactory(new PropertyValueFactory<>("stok"));
        colSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));

        tableBarang.getColumns().forEach(column -> column.setStyle("-fx-alignment: CENTER-LEFT;"));
        tableBarang.setItems(listBarang);

        // 2. FITUR DOUBLE CLICK PADA BARIS TABEL
        tableBarang.setRowFactory(tv -> {
            TableRow<Barang> row = new TableRow<>();
            // Ubah kursor jadi tangan pas di atas baris
            row.setStyle("-fx-cursor: hand;");

            row.setOnMouseClicked(event -> {
                // Jika diklik 2x dan barisnya ada isinya
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    handleDetailBarang();
                }
            });
            return row;
        });

        // 3. Format Rupiah & Load Data
        setupFormatRupiah(colHargaBeli, "hargaBeli");
        setupFormatRupiah(colHargaJual, "hargaJual");
        loadData();
        setupPencarian();
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
                        rs.getString("satuan"),
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
    private void handleDetailBarang() {
        Barang selected = tableBarang.getSelectionModel().getSelectedItem();

        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/DetailBarangView.fxml"));
                Parent view = loader.load();

                DetailBarangController controller = loader.getController();
                controller.initData(
                        selected.getIdBarang(),
                        selected.getNamaBarang(),
                        selected.getIdKategori(),
                        selected.getStok(),
                        selected.getSatuan(),
                        selected.getHargaBeli(),
                        selected.getHargaJual()
                );

                // Cari contentArea di Scene sekarang
                AnchorPane contentArea = (AnchorPane) tableBarang.getScene().lookup("#contentArea");
                if (contentArea != null) {
                    contentArea.getChildren().setAll(view);

                    // Tarik view ke pojok-pojok biar nempel (Fit to Parent)
                    AnchorPane.setTopAnchor(view, 0.0);
                    AnchorPane.setBottomAnchor(view, 0.0);
                    AnchorPane.setLeftAnchor(view, 0.0);
                    AnchorPane.setRightAnchor(view, 0.0);
                }

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Gagal memuat halaman Detail: " + e.getMessage());
            }
        } else {
            showAlert("Peringatan", "Pilih barang di tabel terlebih dahulu!");
        }
    }

    @FXML
    private void handleTambahBarang() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/TambahBarang.fxml"));
            AnchorPane contentArea = (AnchorPane) tableBarang.getScene().lookup("#contentArea");

            if (contentArea != null) {
                contentArea.getChildren().setAll(root);

                AnchorPane.setTopAnchor(root, 0.0);
                AnchorPane.setBottomAnchor(root, 0.0);
                AnchorPane.setLeftAnchor(root, 0.0);
                AnchorPane.setRightAnchor(root, 0.0);
            } else {
                showAlert("Error", "Kontainer 'contentArea' tidak ditemukan!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memuat halaman Tambah: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}