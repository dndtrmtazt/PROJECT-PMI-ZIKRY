package model;

import config.koneksi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TokoDAO {

    public static Toko getDataToko() {
        String query = "SELECT id, nama_toko, nomor_telepon, alamat, email FROM pengaturan_toko WHERE id = 1";
        try (Connection dbKoneksi = koneksi.koneksiDB();
             PreparedStatement ps = dbKoneksi.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                Toko toko = new Toko();
                toko.setId(rs.getInt("id"));
                toko.setNamaToko(rs.getString("nama_toko"));
                toko.setNomorTelepon(rs.getString("nomor_telepon"));
                toko.setAlamat(rs.getString("alamat"));
                toko.setEmail(rs.getString("email"));
                return toko;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean updateToko(String nama, String telp, String alamat, String email) {
        String query = "UPDATE pengaturan_toko SET nama_toko=?, nomor_telepon=?, alamat=?, email=? WHERE id=1";

        try (Connection dbKoneksi = koneksi.koneksiDB();
             PreparedStatement ps = dbKoneksi.prepareStatement(query)) {

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
