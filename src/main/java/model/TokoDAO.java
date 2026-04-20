package model;

import java.sql.*;
import config.koneksi;

public class TokoDAO {

    public static ResultSet getDataToko() {
        try {
            Connection db_koneksi = koneksi.koneksiDB();
            // PERBAIKAN: Ubah 'pengaturan' menjadi 'pengaturan_toko'
            String query = "SELECT * FROM pengaturan_toko WHERE id = 1";
            Statement s = db_koneksi.createStatement();
            return s.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean updateToko(String nama, String telp, String alamat, String email) {
        // PERBAIKAN: Ubah 'pengaturan' menjadi 'pengaturan_toko'
        String query = "UPDATE pengaturan_toko SET nama_toko=?, nomor_telepon=?, alamat=?, email=? WHERE id=1";

        try (Connection db_koneksi = koneksi.koneksiDB();
             PreparedStatement ps = db_koneksi.prepareStatement(query)) {

            ps.setString(1, nama);
            ps.setString(2, telp);
            ps.setString(3, alamat);
            ps.setString(4, email);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Gagal Update! Alasan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}