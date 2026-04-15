package model;

import config.koneksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO (Data Access Object)
 * Kelas ini berfungsi sebagai jembatan antara aplikasi dan tabel 'user' di database.
 */
public class UserDAO {

    /**
     * Ambil SEMUA data user dari database.
     */
    public static List<User> getAllUsers() {
        // 1. Siapkan list kosong buat menampung data user
        List<User> listUser = new ArrayList<>();
        // 2. Tulis perintah SQL untuk mengambil semua kolom
        String query = "SELECT id_user, user_password, role FROM user";

        // 3. Buka koneksi dan jalankan perintahnya (Try-with-resources)
        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // 4. Ambil data baris demi baris dari database
            while (rs.next()) {
                User user = new User();
                user.setIdUser(rs.getString("id_user"));
                user.setPassword(rs.getString("user_password"));
                user.setRole(rs.getString("role"));

                // 5. Masukkan objek user ke dalam list
                listUser.add(user);
            }
        } catch (SQLException e) {
            // Tampilkan pesan jika gagal mengambil data
            System.err.println("Gagal ambil semua data user: " + e.getMessage());
            e.printStackTrace();
        }
        return listUser;
    }

    /**
     * Cek kecocokan ID dan Password (Login).
     */
    public static User validateUser(String idUser, String password) {
        // 1. Tulis perintah SQL dengan tanda tanya (?) sebagai pengaman (SQL Injection)
        String query = "SELECT id_user, user_password, role FROM user WHERE id_user = ? AND user_password = ?";

        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {

            // 2. Masukkan ID dan Password yang diketik user ke tanda tanya tadi
            ps.setString(1, idUser);
            ps.setString(2, password);

            // 3. Jalankan perintah dan cek hasilnya
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // 4. Kalau ketemu, bungkus datanya jadi objek User
                User user = new User();
                user.setIdUser(rs.getString("id_user"));
                user.setPassword(rs.getString("user_password"));
                user.setRole(rs.getString("role"));
                return user; // Berhasil Login
            }
        } catch (SQLException e) {
            System.err.println("Gagal validasi login: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Balikin null kalau user tidak ditemukan
    }

    /**
     * Cari detail satu user berdasarkan ID.
     */
    public static User getUserById(String idUser) {
        // 1. Siapkan perintah SQL pencarian spesifik ID
        String query = "SELECT id_user, user_password, role FROM user WHERE id_user = ?";

        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {

            // 2. Isi tanda tanya dengan ID yang dicari
            ps.setString(1, idUser);

            // 3. Jalankan pencarian
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // 4. Pindahkan data dari database ke objek User
                User user = new User();
                user.setIdUser(rs.getString("id_user"));
                user.setPassword(rs.getString("user_password"));
                user.setRole(rs.getString("role"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Gagal cari user pakai ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Daftarkan user baru ke tabel.
     */
    public static boolean insertUser(String idUser, String password, String role) {
        // 1. Siapkan perintah INSERT
        String query = "INSERT INTO user (id_user, user_password, role) VALUES (?, ?, ?)";

        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {

            // 2. Isi data user baru ke tanda tanya
            ps.setString(1, idUser);
            ps.setString(2, password);
            ps.setString(3, role);

            // 3. Jalankan perintah simpan (executeUpdate)
            int result = ps.executeUpdate();
            return result > 0; // Kembalikan true jika ada baris yang tersimpan
        } catch (SQLException e) {
            System.err.println("Gagal nambah user baru: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Update/Ganti password user.
     */
    public static boolean updateUserPassword(String idUser, String newPassword) {
        // 1. Siapkan perintah UPDATE berdasarkan ID
        String query = "UPDATE user SET user_password = ? WHERE id_user = ?";

        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {

            // 2. Masukkan password baru dan ID target
            ps.setString(1, newPassword);
            ps.setString(2, idUser);

            // 3. Jalankan update dan cek apakah berhasil
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal update password: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
