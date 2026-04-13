package Controller;

import config.koneksi;
import model.Barang;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class BarangController {

    @FXML private TableView<Barang> tableBarang;
    @FXML private TableColumn<Barang, String> colId;
    @FXML private TableColumn<Barang, String> colNama;
    @FXML private TableColumn<Barang, String> colKategori;
    @FXML private TableColumn<Barang, Integer> colStok;
    @FXML private TableColumn<Barang, Double> colHargaBeli;
    @FXML private TableColumn<Barang, Double> colHargaJual;
    @FXML private TextField txtCari;

    private ObservableList<Barang> listBarang = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Mapping Kolom ke Model (Sesuaikan dengan variabel di model Barang)
        colId.setCellValueFactory(new PropertyValueFactory<>("idBarang"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colKategori.setCellValueFactory(new PropertyValueFactory<>("idKategori"));
        colStok.setCellValueFactory(new PropertyValueFactory<>("stok"));
        tableBarang.setItems(listBarang);

        // 2. Format Mata Uang Rupiah untuk Kolom Harga
        setupFormatRupiah(colHargaBeli, "hargaBeli");
        setupFormatRupiah(colHargaJual, "hargaJual");

        // 3. Load Data dari Database
        loadData();

        // 4. Aktifkan Fitur Pencarian Otomatis
        setupPencarian();
    }

    private void loadData() {
        listBarang.clear();
        String query = "SELECT * FROM barang";
        try (Connection conn = koneksi.koneksiDB();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                System.out.println("Data ditemukan: " + rs.getString("nama_barang"));
                listBarang.add(new Barang(
                        rs.getString("id_barang"),
                        rs.getString("nama_barang"),
                        rs.getString("id_kategori"),
                        rs.getInt("stok"),
                        rs.getDouble("harga_beli"),
                        rs.getDouble("harga_jual")
                ));
            }
            tableBarang.setItems(listBarang);
        } catch (SQLException e) {
            System.err.println("✗ Gagal tarik data barang: " + e.getMessage());
        }
    }

    private void setupPencarian() {
        FilteredList<Barang> filteredData = new FilteredList<>(listBarang, p -> true);

        txtCari.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(barang -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();
                if (barang.getNamaBarang().toLowerCase().contains(lowerCaseFilter)) return true;
                if (barang.getIdBarang().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
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
                if (empty || price == null) {
                    setText(null);
                } else {
                    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    setText(nf.format(price));
                }
            }
        });
    }

    @FXML
    private void handleSimpan() {
        System.out.println("Tombol simpan diklik!");
    }
}