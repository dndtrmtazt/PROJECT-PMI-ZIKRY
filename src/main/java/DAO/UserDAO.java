package DAO;

import config.koneksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.User;

// DAO untuk membaca dan mengelola data user aplikasi.
public class UserDAO {

    // Mengambil daftar user tanpa password untuk ditampilkan di halaman kelola user.
    public static List<User> getAllUsers() {
        List<User> listUser = new ArrayList<>();
        String query = "SELECT id_user, nama_lengkap, role FROM user";

        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                listUser.add(mapUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listUser;
    }

    // Memvalidasi login berdasarkan ID user dan password.
    public static User validateUser(String idUser, String password) {
        String query = "SELECT * FROM user WHERE id_user = ? AND user_password = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idUser);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Menambahkan user baru dari form kelola user.
    public static boolean insertUser(String idUser, String nama, String role, String password) {
        String query = "INSERT INTO user (id_user, nama_lengkap, role, user_password) VALUES (?, ?, ?, ?)";

        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, idUser);
            ps.setString(2, nama);
            ps.setString(3, role.toLowerCase());
            ps.setString(4, password);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Mengubah data user, termasuk jika ID user ikut diganti.
    public static boolean updateUser(String idLama, String idBaru, String nama, String role, String pass) {
        String query = "UPDATE user SET id_user = ?, nama_lengkap = ?, role = ?, user_password = ? WHERE id_user = ?";

        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, idBaru);
            ps.setString(2, nama);
            ps.setString(3, role.toLowerCase());
            ps.setString(4, pass);
            ps.setString(5, idLama);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Menghapus user berdasarkan ID.
    public static boolean deleteUser(String idUser) {
        String query = "DELETE FROM user WHERE id_user = ?";

        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, idUser);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Mengubah ResultSet dari database menjadi objek User.
    private static User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setIdUser(rs.getString("id_user"));
        user.setNamaLengkap(rs.getString("nama_lengkap"));
        user.setRole(rs.getString("role"));
        return user;
    }
}
