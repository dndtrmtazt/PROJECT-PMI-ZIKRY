package DAO;

import config.koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Toko;

/**
 * Data Access Object (DAO) untuk tabel pengaturan_toko.
 * Alur: Mengelola data tunggal profil toko (Nama, Alamat, Kontak).
 */
public class TokoDAO {

    /**
     * Method getDataToko: Mengambil informasi identitas toko (ID=1).
     */
    public static Toko getDataToko() {
        String query = "SELECT * FROM pengaturan_toko WHERE id = 1";
        try (Connection dbKoneksi = koneksi.koneksiDB();
             PreparedStatement ps = dbKoneksi.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            // [1] Jika data ditemukan, kembalikan objek Toko
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

    /**
     * Method updateToko: Menyimpan perubahan pada identitas toko.
     */
    public static boolean updateToko(String nama, String telp, String alamat, String email) {
        String query = "UPDATE pengaturan_toko SET nama_toko=?, nomor_telepon=?, alamat=?, email=? WHERE id=1";
        try (Connection dbKoneksi = koneksi.koneksiDB();
             PreparedStatement ps = dbKoneksi.prepareStatement(query)) {
            // [1] Set parameter update profil toko
            ps.setString(1, nama);
            ps.setString(2, telp);
            ps.setString(3, alamat);
            ps.setString(4, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
