package model;

import config.koneksi;
import java.sql.*;

public class UserDAO {

    /**
     * Validasi user login menggunakan struktur tabel existing
     * Tabel: user, Kolom: id_user, user_password, role
     */
    public static User validateUser(String idUser, String password) {
        String query = "SELECT id_user, user_password, role FROM user WHERE id_user = ? AND user_password = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idUser);
            ps.setString(2, password);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("id_user"));
                user.setPassword(rs.getString("user_password"));
                user.setRole(rs.getString("role"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error validating user: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static User getUserById(String idUser) {
        String query = "SELECT id_user, user_password, role FROM user WHERE id_user = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idUser);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("id_user"));
                user.setPassword(rs.getString("user_password"));
                user.setRole(rs.getString("role"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static boolean insertUser(String idUser, String password, String role) {
        String query = "INSERT INTO user (id_user, user_password, role) VALUES (?, ?, ?)";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idUser);
            ps.setString(2, password);
            ps.setString(3, role);
            
            int result = ps.executeUpdate();
            System.out.println("User inserted: " + idUser);
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateUserPassword(String idUser, String newPassword) {
        String query = "UPDATE user SET user_password = ? WHERE id_user = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, newPassword);
            ps.setString(2, idUser);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user password: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
