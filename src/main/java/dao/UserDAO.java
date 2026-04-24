package dao;

import config.koneksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.User;

public class UserDAO {

    public static List<User> getAllUsers() {
        List<User> listUser = new ArrayList<>();
        String query = "SELECT id_user, nama_lengkap, role FROM user";

        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                User user = new User();
                user.setIdUser(rs.getString("id_user"));
                user.setNamaLengkap(rs.getString("nama_lengkap"));
                user.setRole(rs.getString("role"));
                listUser.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listUser;
    }

    public static User validateUser(String idUser, String password) {
        String query = "SELECT * FROM user WHERE id_user = ? AND user_password = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idUser);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setIdUser(rs.getString("id_user"));
                user.setRole(rs.getString("role"));
                user.setNamaLengkap(rs.getString("nama_lengkap"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

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

    /**
     * DI SINI PERBAIKANNYA: Method sekarang menerima 5 parameter agar ID bisa diedit
     */
    public static boolean updateUser(String idLama, String idBaru, String nama, String role, String pass) {
        // Query ini mengubah id_user yang lama menjadi id_user yang baru
        String query = "UPDATE user SET id_user = ?, nama_lengkap = ?, role = ?, user_password = ? WHERE id_user = ?";

        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, idBaru);   // ID Baru yang diketik di aplikasi
            ps.setString(2, nama);
            ps.setString(3, role.toLowerCase());
            ps.setString(4, pass);
            ps.setString(5, idLama);   // ID Asli yang dipakai sebagai patokan WHERE

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

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
}
