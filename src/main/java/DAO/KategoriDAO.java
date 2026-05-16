package DAO;

import config.koneksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Kategori;

// DAO untuk operasi CRUD tabel kategori.
public class KategoriDAO {

    // Mengambil seluruh kategori agar bisa ditampilkan di halaman kategori dan combo box barang.
    public static List<Kategori> getAllKategori() {
        List<Kategori> listKategori = new ArrayList<>();
        String query = "SELECT * FROM kategori";
        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                listKategori.add(mapKategori(rs));
            }
            System.out.println("DEBUG: Berhasil menarik " + listKategori.size() + " data dari DB.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listKategori;
    }

    // Menambahkan kategori baru ke database.
    public static boolean addKategori(Kategori kategori) {
        String query = "INSERT INTO kategori (id_kategori, nama_kategori) VALUES (?, ?)";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, kategori.getIdKategori());
            ps.setString(2, kategori.getNamaKategori());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Mengubah data kategori berdasarkan ID lama, karena ID kategori juga bisa diedit.
    public static boolean updateKategori(Kategori kategori, String oldId) {
        String query = "UPDATE kategori SET id_kategori = ?, nama_kategori = ? WHERE id_kategori = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, kategori.getIdKategori());
            ps.setString(2, kategori.getNamaKategori());
            ps.setString(3, oldId);

            int rowsUpdated = ps.executeUpdate();
            System.out.println("DEBUG: Rows updated: " + rowsUpdated);
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Error updating kategori: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Menghapus kategori berdasarkan ID yang dipilih user.
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

    // Mengubah baris ResultSet database menjadi objek Kategori.
    private static Kategori mapKategori(ResultSet rs) throws SQLException {
        Kategori kategori = new Kategori();
        kategori.setIdKategori(rs.getString("id_kategori"));
        kategori.setNamaKategori(rs.getString("nama_kategori"));
        return kategori;
    }
}
