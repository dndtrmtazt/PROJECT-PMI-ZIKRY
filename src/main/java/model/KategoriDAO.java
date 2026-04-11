package model;

import config.koneksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KategoriDAO {

    public static List<Kategori> getAllKategori() {
        List<Kategori> listKategori = new ArrayList<>();
        String query = "SELECT id_kategori, nama_kategori FROM kategori";
        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Kategori kategori = new Kategori();
                kategori.setIdKategori(rs.getString("id_kategori"));
                kategori.setNamaKategori(rs.getString("nama_kategori"));
                listKategori.add(kategori);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all kategori: " + e.getMessage());
            e.printStackTrace();
        }
        return listKategori;
    }

    public static Kategori getKategoriById(String idKategori) {
        String query = "SELECT id_kategori, nama_kategori FROM kategori WHERE id_kategori = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idKategori);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Kategori kategori = new Kategori();
                kategori.setIdKategori(rs.getString("id_kategori"));
                kategori.setNamaKategori(rs.getString("nama_kategori"));
                return kategori;
            }
        } catch (SQLException e) {
            System.err.println("Error getting kategori by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static boolean insertKategori(Kategori kategori) {
        String query = "INSERT INTO kategori (id_kategori, nama_kategori) VALUES (?, ?)";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, kategori.getIdKategori());
            ps.setString(2, kategori.getNamaKategori());
            
            int result = ps.executeUpdate();
            System.out.println("Kategori inserted: " + kategori.getIdKategori());
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting kategori: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateKategori(Kategori kategori) {
        String query = "UPDATE kategori SET nama_kategori = ? WHERE id_kategori = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, kategori.getNamaKategori());
            ps.setString(2, kategori.getIdKategori());
            
            System.out.println("Kategori updated: " + kategori.getIdKategori());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating kategori: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteKategori(String idKategori) {
        String query = "DELETE FROM kategori WHERE id_kategori = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idKategori);
            
            System.out.println("Kategori deleted: " + idKategori);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting kategori: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
