package Controller; // SESUAIKAN DI SINI

import config.koneksi; // SESUAIKAN DI SINI
import model.Barang;   // SESUAIKAN DI SINI
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;

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
        colId.setCellValueFactory(new PropertyValueFactory<>("idBarang"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colKategori.setCellValueFactory(new PropertyValueFactory<>("idKategori"));
        colStok.setCellValueFactory(new PropertyValueFactory<>("stok"));
        colHargaBeli.setCellValueFactory(new PropertyValueFactory<>("hargaBeli"));
        colHargaJual.setCellValueFactory(new PropertyValueFactory<>("hargaJual"));
        loadData();
    }
    @FXML
    private void handleSimpan() {
        // Isi kodingan simpan kamu di sini
        System.out.println("Tombol simpan diklik!");
    }
    private void loadData() {
        listBarang.clear();
        try (Connection conn = koneksi.koneksiDB();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM barang")) {
            while (rs.next()) {
                listBarang.add(new Barang(
                        rs.getString("id_barang"), rs.getString("nama_barang"),
                        rs.getString("id_kategori"), rs.getInt("stok"),
                        rs.getDouble("harga_beli"), rs.getDouble("harga_jual")
                ));
            }
            tableBarang.setItems(listBarang);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}