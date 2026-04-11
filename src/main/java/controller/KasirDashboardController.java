package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import config.UserSession;
import model.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class KasirDashboardController {

    @FXML
    private Label usernameLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private Button transaksiButton;

    @FXML
    private Button barangButton;

    @FXML
    private AnchorPane contentArea;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab transaksiTab;

    @FXML
    private Tab barangTab;

    private ObservableList<Barang> barangList;
    private ObservableList<Detail_Transaksi> transactionItems;

    @FXML
    public void initialize() {
        initializeUserInfo();
        initializeUI();
        loadBarangTab();
        loadTransaksiTab();
    }

    private void initializeUserInfo() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            usernameLabel.setText("User: " + currentUser.getUsername());
            roleLabel.setText("Role: KASIR");
            System.out.println("[KasirDashboard] Initialized for user: " + currentUser.getUsername());
        }
    }

    private void initializeUI() {
        logoutButton.setOnAction(e -> handleLogout());
        transaksiButton.setStyle("-fx-font-size: 14; -fx-padding: 10;");
        barangButton.setStyle("-fx-font-size: 14; -fx-padding: 10;");
    }

    private void loadTransaksiTab() {
        try {
            // Load transaction form
            System.out.println("[KasirDashboard] Loading Transaksi Tab...");
            
            // For now, create a simple transaction entry interface
            VBox vbox;
            vbox = new VBox(10);
            vbox.setStyle("-fx-padding: 15;");
            
            Label titleLabel = new Label("Entri Transaksi Baru");
            titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
            
            GridPane gridPane = new GridPane();
            gridPane.setHgap(15);
            gridPane.setVgap(10);
            gridPane.setPadding(new Insets(10));
            
            // Transaction ID
            Label idLabel = new Label("ID Transaksi:");
            TextField idField = new TextField();
            idField.setPrefWidth(200);
            gridPane.add(idLabel, 0, 0);
            gridPane.add(idField, 1, 0);
            
            // Date
            Label dateLabel = new Label("Tanggal:");
            DatePicker datePicker = new DatePicker(LocalDate.now());
            gridPane.add(dateLabel, 0, 1);
            gridPane.add(datePicker, 1, 1);
            
            // Product Selection
            Label productLabel = new Label("Pilih Produk:");
            ComboBox<Barang> productCombo = new ComboBox<>();
            List<Barang> barangList = BarangDAO.getAllBarang();
            productCombo.setItems(FXCollections.observableArrayList(barangList));
            productCombo.setPrefWidth(200);
            gridPane.add(productLabel, 0, 2);
            gridPane.add(productCombo, 1, 2);
            
            // Quantity
            Label qtyLabel = new Label("Jumlah:");
            Spinner<Integer> qtySpinner = new Spinner<>(1, 999, 1);
            qtySpinner.setPrefWidth(200);
            gridPane.add(qtyLabel, 0, 3);
            gridPane.add(qtySpinner, 1, 3);
            
            // Buttons
            HBox buttonBox = new HBox(10);
            buttonBox.setPadding(new Insets(15, 0, 0, 0));
            Button addButton = new Button("Tambah Item");
            Button clearButton = new Button("Bersihkan");
            Button submitButton = new Button("Selesaikan Transaksi");
            
            addButton.setStyle("-fx-font-size: 12; -fx-padding: 8;");
            clearButton.setStyle("-fx-font-size: 12; -fx-padding: 8;");
            submitButton.setStyle("-fx-font-size: 12; -fx-padding: 8; -fx-text-fill: white; -fx-background-color: green;");
            
            buttonBox.getChildren().addAll(addButton, clearButton, submitButton);
            
            vbox.getChildren().addAll(titleLabel, gridPane, buttonBox);
            
            transaksiTab.setContent(vbox);
            transaksiTab.setClosable(false);
            
            System.out.println("[KasirDashboard] Transaksi Tab loaded successfully");
        } catch (Exception e) {
            System.err.println("[KasirDashboard] Error loading Transaksi Tab: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadBarangTab() {
        try {
            System.out.println("[KasirDashboard] Loading Barang Tab...");
            
            VBox vbox = new VBox(10);
            vbox.setStyle("-fx-padding: 15;");
            
            Label titleLabel = new Label("Daftar Produk");
            titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
            
            // Create table for products
            TableView<Barang> barangTable = new TableView<>();
            barangTable.setPrefHeight(400);
            
            TableColumn<Barang, String> idCol = new TableColumn<>("ID Barang");
            idCol.setPrefWidth(100);
            idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getIdBarang()));
            
            TableColumn<Barang, String> nameCol = new TableColumn<>("Nama Barang");
            nameCol.setPrefWidth(250);
            nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNamaBarang()));
            
            TableColumn<Barang, Integer> stokCol = new TableColumn<>("Stok");
            stokCol.setPrefWidth(80);
            stokCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getStok()));
            
            TableColumn<Barang, Double> hargaCol = new TableColumn<>("Harga Jual");
            hargaCol.setPrefWidth(120);
            hargaCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getHargaJual()));
            
            barangTable.getColumns().addAll(idCol, nameCol, stokCol, hargaCol);
            
            // Load data
            List<Barang> allBarang = BarangDAO.getAllBarang();
            barangList = FXCollections.observableArrayList(allBarang);
            barangTable.setItems(barangList);
            
            // Search/Filter
            TextField searchField = new TextField();
            searchField.setPromptText("Cari produk...");
            searchField.setPrefWidth(200);
            searchField.setStyle("-fx-font-size: 12; -fx-padding: 8;");
            
            vbox.getChildren().addAll(titleLabel, searchField, barangTable);
            VBox.setVgrow(barangTable, javafx.scene.layout.Priority.ALWAYS);
            
            barangTab.setContent(vbox);
            barangTab.setClosable(false);
            
            System.out.println("[KasirDashboard] Barang Tab loaded with " + barangList.size() + " items");
        } catch (Exception e) {
            System.err.println("[KasirDashboard] Error loading Barang Tab: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            UserSession.getInstance().logout();
            System.out.println("[KasirDashboard] User logged out");
            
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/LoginView.fxml"));
            javafx.scene.Parent root = fxmlLoader.load();
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new javafx.scene.Scene(root);
            stage.setScene(scene);
            stage.setTitle("PMITokoZikry - Login");
            stage.setWidth(900);
            stage.setHeight(600);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("[KasirDashboard] Logout error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
