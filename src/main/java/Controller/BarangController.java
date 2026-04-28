package Controller;

import config.koneksi;
import model.Barang;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.net.URL;
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
    @FXML private TableColumn<Barang, String> colSatuan;
    @FXML private TextField txtCari;
    @FXML private Label lblDaftarBarang, lblTitle;

    private final ObservableList<Barang> listBarang = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Mapping Kolom
        colId.setCellValueFactory(new PropertyValueFactory<>("idBarang"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colKategori.setCellValueFactory(new PropertyValueFactory<>("idKategori"));
        colSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        colStok.setCellValueFactory(new PropertyValueFactory<>("stok"));

        tableBarang.getColumns().forEach(column -> column.setStyle("-fx-alignment: CENTER-LEFT;"));
        colId.setSortable(true);
        colNama.setSortable(true);
        colStok.setSortable(true);
        tableBarang.setPlaceholder(new Label("Barang tidak ditemukan"));
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
        URL tableCss = getClass().getResource("/CSS/tabel.css");
        if (tableCss != null) {
            tableBarang.getStylesheets().add(tableCss.toExternalForm());
        }

        setDarkMode(MainController.isDarkMode);
    }

    public void setDarkMode(boolean enabled) {
        String bgMain = enabled ? "#121212" : "#F4F4F4";
        String bgCard = enabled ? "#1e1e1e" : "white";
        String textColor = enabled ? "white" : "#2C3E50";
        String borderColor = enabled ? "#333333" : "#D1D5DB";

        setStyleClass(paneRoot, "dark", enabled);
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

    private void setStyleClass(Node node, String styleClass, boolean enabled) {
        if (node == null) return;
        if (enabled) {
            if (!node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }
        } else {
            node.getStyleClass().remove(styleClass);
        }
    }

    private void loadData() {
        listBarang.clear();

        // 1. PASTIKAN QUERY PAKE LEFT JOIN
        String query = "SELECT b.*, k.nama_kategori FROM barang b " +
                "LEFT JOIN kategori k ON b.id_kategori = k.id_kategori";

        try (Connection conn = koneksi.koneksiDB();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                // 2. MASUKKAN 8 DATA (HARUS URUT!)
                listBarang.add(new Barang(
                        rs.getString("id_barang"),
                        rs.getString("nama_barang"),
                        rs.getString("id_kategori"),
                        rs.getString("nama_kategori") == null ? "-" : rs.getString("nama_kategori"),
                        rs.getInt("stok"),
                        rs.getString("satuan") == null ? "Pcs" : rs.getString("satuan"),
                        rs.getDouble("harga_beli"),
                        rs.getDouble("harga_jual")
                ));
            }
            System.out.println("✓ Berhasil memuat " + listBarang.size() + " data barang.");
        } catch (SQLException e) {
            System.err.println("✗ Gagal tarik data: " + e.getMessage());
            e.printStackTrace();
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

        SortedList<Barang> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableBarang.comparatorProperty());
        tableBarang.setItems(sortedData);
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

                // KIRIM 8 PARAMETER (Harus urut sesuai method di EditBarangController)
                controller.initData(
                        selected.getIdBarang(),      // 1
                        selected.getNamaBarang(),    // 2
                        selected.getIdKategori(),    // 3
                        selected.getNamaKategori(),  // 4 (DATA BARU)
                        selected.getStok(),          // 5
                        selected.getSatuan(),       // 6 (DATA BARU)
                        selected.getHargaBeli(),     // 7
                        selected.getHargaJual()      // 8
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
