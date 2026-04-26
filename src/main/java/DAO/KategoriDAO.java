package DAO;

import config.koneksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Kategori;

/**
 * Data Access Object (DAO) untuk tabel kategori.
 * Alur: Mengelola daftar kategori barang (Pengelompokan jenis barang).
 */
public class KategoriDAO {

    /**
     * Method getAllKategori: Mengambil seluruh kategori barang.
     */
    public static List<Kategori> getAllKategori() {
        List<Kategori> listKategori = new ArrayList<>();
        String query = "SELECT * FROM kategori";
        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            // [1] Looping dan masukkan data ke list model
            while (rs.next()) {
                Kategori kategori = new Kategori();
                kategori.setIdKategori(rs.getString("id_kategori"));
                kategori.setNamaKategori(rs.getString("nama_kategori"));
                listKategori.add(kategori);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listKategori;
    }

    /**
     * Method addKategori: Menyimpan kategori baru.
     */
    public static boolean addKategori(Kategori kategori) {
        String query = "INSERT INTO kategori (id_kategori, nama_kategori) VALUES (?, ?)";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            // [1] Set parameter ID dan Nama
            ps.setString(1, kategori.getIdKategori());
            ps.setString(2, kategori.getNamaKategori());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method updateKategori: Mengubah data kategori termasuk ID kategorinya.
     */
    public static boolean updateKategori(Kategori kategori, String oldId) {
        String query = "UPDATE kategori SET id_kategori = ?, nama_kategori = ? WHERE id_kategori = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            // [1] Update data kategori berdasarkan ID lama (oldId)
            ps.setString(1, kategori.getIdKategori());
            ps.setString(2, kategori.getNamaKategori());
            ps.setString(3, oldId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method deleteKategori: Menghapus kategori barang.
     */
    public static boolean deleteKategori(String idKategori) {
        String query = "DELETE FROM kategori WHERE id_kategori = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idKategori);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
