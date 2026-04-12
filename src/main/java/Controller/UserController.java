package Controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

// Import model kamu
import model.User;
import model.UserDAO;

public class UserController {

    @FXML private VBox vboxUserList; // Wadah buat list user
    @FXML private Label lblTitle, lblSubTitle;
    @FXML private Button btnTambahUser;

    @FXML
    public void initialize() {
        // Fungsi ini otomatis jalan pas halaman dibuka
        muatDataUser();
    }

    /**
     * Mengambil data dari UserDAO dan menampilkannya ke VBox
     */
    private void muatDataUser() {
        // 1. Bersihkan list lama biar gak numpuk pas di-refresh
        vboxUserList.getChildren().clear();

        // 2. Ambil data dari database melalui DAO
        // Karena di DAO-mu metodenya static, langsung panggil nama Class-nya
        List<User> list = UserDAO.getAllUsers();

        int no = 1;
        for (User u : list) {
            // 3. Buat baris (HBox) untuk setiap user
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 15, 10, 15));

            // Styling baris agar estetik (Putih dengan border bawah tipis)
            row.setStyle("-fx-background-color: white; " +
                    "-fx-border-color: #F0F0F0; " +
                    "-fx-border-width: 0 0 1 0;");

            // 4. Buat Label untuk setiap kolom
            Label lblNo = new Label(String.valueOf(no++));
            lblNo.setPrefWidth(40);

            // Pakai getUsername() sesuai model User.java kamu
            Label lblId = new Label(u.getUsername());
            lblId.setPrefWidth(120);

            Label lblNama = new Label(u.getUsername()); // Sementara pakai username sebagai nama
            lblNama.setPrefWidth(180);
            lblNama.setStyle("-fx-font-weight: bold;");

            Label lblRole = new Label(u.getRole().toUpperCase());
            lblRole.setPrefWidth(120);

            // Logika Warna Role (Biar Multimedia banget!)
            if (u.getRole().equalsIgnoreCase("admin")) {
                lblRole.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;"); // Merah buat Admin
            } else {
                lblRole.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;"); // Hijau buat Kasir
            }

            // 5. Spacer agar tombol aksi terdorong ke kanan
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // 6. Tombol Aksi (Edit & Hapus)
            Button btnEdit = new Button("Edit");
            btnEdit.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
            btnEdit.setOnAction(event -> {
                System.out.println("Edit user: " + u.getUsername());
                // Nanti tambahin logika buka form edit di sini ya Din!
            });

            Button btnHapus = new Button("Hapus");
            btnHapus.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
            btnHapus.setOnAction(event -> {
                System.out.println("Hapus user: " + u.getUsername());
                // Nanti panggil UserDAO.deleteUser(u.getUsername()) di sini
            });

            // 7. Masukkan semua komponen ke dalam baris (row)
            row.getChildren().addAll(lblNo, lblId, lblNama, lblRole, spacer, btnEdit, btnHapus);

            // 8. Masukkan baris ke wadah utama (vboxUserList)
            vboxUserList.getChildren().add(row);
        }
    }

    @FXML
    private void handleTambahUser() {
        // Logika untuk klik tombol "+ Tambah User"
        System.out.println("Membuka form tambah user...");
    }
}