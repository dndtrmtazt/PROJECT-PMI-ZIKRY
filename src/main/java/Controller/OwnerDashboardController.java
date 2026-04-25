package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import config.UserSession;
import DAO.BarangDAO;
import DAO.PengeluaranDAO;
import DAO.TransaksiDAO;
import model.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class OwnerDashboardController {

    @FXML
    private Label usernameLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private Button laporanButton;

    @FXML
    private Button pengeluaranButton;

    @FXML
    private Button barangButton;

    @FXML
    private Button pengaturanButton;

    @FXML
    private AnchorPane contentArea;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab laporanTab;

    @FXML
    private Tab pengeluaranTab;

    @FXML
    private Tab barangTab;

    @FXML
    private Tab pengaturanTab;

    private ObservableList<Pengeluaran> pengeluaranList;
    private ObservableList<Barang> barangList;

    @FXML
    public void initialize() {
        initializeUserInfo();
        initializeUI();
        loadLaporanTab();
        loadPengeluaranTab();
        loadBarangTab();
        loadPengaturanTab();
    }

    private void initializeUserInfo() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            usernameLabel.setText("User: " + currentUser.getIdUser());
            roleLabel.setText("Role: PEMILIK");
            System.out.println("[OwnerDashboard] Initialized for user: " + currentUser.getIdUser());
        }
    }

    private void initializeUI() {
        logoutButton.setOnAction(e -> handleLogout());
        laporanButton.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        pengeluaranButton.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        barangButton.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        pengaturanButton.setStyle("-fx-font-size: 12; -fx-padding: 8;");
    }

    private void loadLaporanTab() {
        try {
            System.out.println("[OwnerDashboard] Loading Laporan Tab...");
            
            VBox vbox = new VBox(10);
            vbox.setStyle("-fx-padding: 15;");
            
            Label titleLabel = new Label("Laporan Penjualan & Analitik");
            titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
            
            // Period selector
            HBox periodBox = new HBox(10);
            Label periodLabel = new Label("Periode:");
            ComboBox<String> periodCombo = new ComboBox<>();
            periodCombo.setItems(FXCollections.observableArrayList("Hari Ini", "Minggu Ini", "Bulan Ini", "Tahun Ini", "Custom"));
            periodCombo.setValue("Bulan Ini");
            periodBox.getChildren().addAll(periodLabel, periodCombo);
            
            // Summary cards
            HBox summaryBox = new HBox(15);
            summaryBox.setStyle("-fx-padding: 10; -fx-border-color: #cccccc; -fx-border-radius: 5;");
            
            VBox totalSalesBox = createSummaryCard("Total Penjualan", "Rp 1.500.000", "#4CAF50");
            VBox totalExpensesBox = createSummaryCard("Total Pengeluaran", "Rp 456.000", "#FF9800");
            VBox profitBox = createSummaryCard("Keuntungan", "Rp 1.044.000", "#2196F3");
            VBox transactionCountBox = createSummaryCard("Jumlah Transaksi", "12", "#9C27B0");
            
            summaryBox.getChildren().addAll(totalSalesBox, totalExpensesBox, profitBox, transactionCountBox);
            
            // Transaction list
            Label transLabel = new Label("Transaksi Terbaru");
            transLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
            
            TableView<Transaksi> transTable = new TableView<>();
            TableColumn<Transaksi, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getIdTransaksi()));
            
            TableColumn<Transaksi, String> dateCol = new TableColumn<>("Tanggal");
            dateCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTglTransaksi().toString()));
            
            TableColumn<Transaksi, Double> totalCol = new TableColumn<>("Total");
            totalCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotal()));
            
            transTable.getColumns().addAll(idCol, dateCol, totalCol);
            
            List<Transaksi> transaksiList = TransaksiDAO.getAllTransaksi();
            transTable.setItems(FXCollections.observableArrayList(transaksiList));
            
            vbox.getChildren().addAll(titleLabel, periodBox, summaryBox, transLabel, transTable);
            
            laporanTab.setContent(vbox);
            laporanTab.setClosable(false);
            
            System.out.println("[OwnerDashboard] Laporan Tab loaded successfully");
        } catch (Exception e) {
            System.err.println("[OwnerDashboard] Error loading Laporan Tab: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createSummaryCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setStyle("-fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 5; -fx-padding: 10; -fx-alignment: center;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666666;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private void loadPengeluaranTab() {
        try {
            System.out.println("[OwnerDashboard] Loading Pengeluaran Tab...");
            
            VBox vbox = new VBox(10);
            vbox.setStyle("-fx-padding: 15;");
            
            Label titleLabel = new Label("Manajemen Pengeluaran");
            titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
            
            // Add expense form
            HBox formBox = new HBox(10);
            formBox.setStyle("-fx-padding: 10; -fx-border-color: #eeeeee; -fx-border-radius: 5;");
            
            TextField idField = new TextField();
            idField.setPromptText("ID Pengeluaran");
            idField.setPrefWidth(120);
            
            DatePicker dateField = new DatePicker(LocalDate.now());
            
            TextField nominalField = new TextField();
            nominalField.setPromptText("Nominal");
            nominalField.setPrefWidth(100);
            
            TextField jenisField = new TextField();
            jenisField.setPromptText("Jenis (PLN, Air, dll)");
            jenisField.setPrefWidth(150);
            
            Button addButton = new Button("Tambah");
            addButton.setStyle("-fx-padding: 8; -fx-font-size: 12;");
            
            formBox.getChildren().addAll(idField, dateField, nominalField, jenisField, addButton);
            
            // Expense list
            TableView<Pengeluaran> pengeluaranTable = new TableView<>();
            
            TableColumn<Pengeluaran, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getIdPengeluaran()));
            
            TableColumn<Pengeluaran, String> dateCol = new TableColumn<>("Tanggal");
            dateCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTglPengeluaran().toString()));
            
            TableColumn<Pengeluaran, String> jenisCol = new TableColumn<>("Jenis");
            jenisCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getJenis()));
            
            TableColumn<Pengeluaran, Double> nominalCol = new TableColumn<>("Nominal");
            nominalCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getNominal()));
            
            pengeluaranTable.getColumns().addAll(idCol, dateCol, jenisCol, nominalCol);

            // Buat instance objeknya dulu
            PengeluaranDAO dao = new PengeluaranDAO();
            pengeluaranList = FXCollections.observableArrayList(dao.getAllPengeluaran());
            pengeluaranTable.setItems(pengeluaranList);
            
            vbox.getChildren().addAll(titleLabel, formBox, pengeluaranTable);
            
            pengeluaranTab.setContent(vbox);
            pengeluaranTab.setClosable(false);
            
            System.out.println("[OwnerDashboard] Pengeluaran Tab loaded with " + pengeluaranList.size() + " items");
        } catch (Exception e) {
            System.err.println("[OwnerDashboard] Error loading Pengeluaran Tab: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadBarangTab() {
        try {
            System.out.println("[OwnerDashboard] Loading Barang Tab...");
            
            VBox vbox = new VBox(10);
            vbox.setStyle("-fx-padding: 15;");
            
            Label titleLabel = new Label("Manajemen Produk");
            titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
            
            TableView<Barang> barangTable = new TableView<>();
            
            TableColumn<Barang, String> idCol = new TableColumn<>("ID Barang");
            idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getIdBarang()));
            
            TableColumn<Barang, String> nameCol = new TableColumn<>("Nama Barang");
            nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNamaBarang()));
            
            TableColumn<Barang, Integer> stokCol = new TableColumn<>("Stok");
            stokCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getStok()));
            
            TableColumn<Barang, Double> hargaBeliCol = new TableColumn<>("Harga Beli");
            hargaBeliCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getHargaBeli()));
            
            TableColumn<Barang, Double> hargaJualCol = new TableColumn<>("Harga Jual");
            hargaJualCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getHargaJual()));
            
            barangTable.getColumns().addAll(idCol, nameCol, stokCol, hargaBeliCol, hargaJualCol);
            
            barangList = FXCollections.observableArrayList(BarangDAO.getAllBarang());
            barangTable.setItems(barangList);
            
            vbox.getChildren().addAll(titleLabel, barangTable);
            
            barangTab.setContent(vbox);
            barangTab.setClosable(false);
            
            System.out.println("[OwnerDashboard] Barang Tab loaded with " + barangList.size() + " items");
        } catch (Exception e) {
            System.err.println("[OwnerDashboard] Error loading Barang Tab: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPengaturanTab() {
        try {
            System.out.println("[OwnerDashboard] Loading Pengaturan Tab...");
            
            VBox vbox = new VBox(15);
            vbox.setStyle("-fx-padding: 15;");
            
            Label titleLabel = new Label("Pengaturan Toko");
            titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
            
            // Settings options
            VBox settingsBox = new VBox(10);
            
            Label dbLabel = new Label("Database & Sistem");
            dbLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
            
            Button backupButton = new Button("Backup Database");
            backupButton.setStyle("-fx-padding: 8; -fx-font-size: 12;");
            
            Button exportButton = new Button("Export Data");
            exportButton.setStyle("-fx-padding: 8; -fx-font-size: 12;");
            
            Label userLabel = new Label("Manajemen User");
            userLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-padding-top: 10;");
            
            Button userMgmtButton = new Button("Kelola User");
            userMgmtButton.setStyle("-fx-padding: 8; -fx-font-size: 12;");
            
            Button changePassButton = new Button("Ubah Password");
            changePassButton.setStyle("-fx-padding: 8; -fx-font-size: 12;");
            
            settingsBox.getChildren().addAll(dbLabel, backupButton, exportButton, userLabel, userMgmtButton, changePassButton);
            
            vbox.getChildren().addAll(titleLabel, settingsBox);
            
            pengaturanTab.setContent(vbox);
            pengaturanTab.setClosable(false);
            
            System.out.println("[OwnerDashboard] Pengaturan Tab loaded successfully");
        } catch (Exception e) {
            System.err.println("[OwnerDashboard] Error loading Pengaturan Tab: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            UserSession.getInstance().logout();
            System.out.println("[OwnerDashboard] User logged out");
            
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/LoginView.fxml"));
            javafx.scene.Parent root = fxmlLoader.load();
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new javafx.scene.Scene(root);
            stage.setResizable(true);
            stage.setMaximized(false);
            stage.setScene(scene);
            stage.setTitle("PMITokoZikry - Login");
            stage.show();
            stage.setMaximized(true);
        } catch (IOException e) {
            System.err.println("[OwnerDashboard] Logout error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
