package model;

import config.koneksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BarangDAO {

    public static List<Barang> getAllBarang() {
        List<Barang> listBarang = new ArrayList<>();
        String query = "SELECT id_barang, nama_barang, id_kategori, stok, harga_beli, harga_jual FROM barang";
        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Barang barang = new Barang();
                barang.setIdBarang(rs.getString("id_barang"));
                barang.setNamaBarang(rs.getString("nama_barang"));
                barang.setIdKategori(rs.getString("id_kategori"));
                barang.setStok(rs.getInt("stok"));
                barang.setHargaBeli(rs.getDouble("harga_beli"));
                barang.setHargaJual(rs.getDouble("harga_jual"));
                listBarang.add(barang);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all barang: " + e.getMessage());
            e.printStackTrace();
        }
        return listBarang;
    }

    public static Barang getBarangById(String idBarang) {
        String query = "SELECT id_barang, nama_barang, id_kategori, stok, harga_beli, harga_jual FROM barang WHERE id_barang = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idBarang);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Barang barang = new Barang();
                barang.setIdBarang(rs.getString("id_barang"));
                barang.setNamaBarang(rs.getString("nama_barang"));
                barang.setIdKategori(rs.getString("id_kategori"));
                barang.setStok(rs.getInt("stok"));
                barang.setHargaBeli(rs.getDouble("harga_beli"));
                barang.setHargaJual(rs.getDouble("harga_jual"));
                return barang;
            }
        } catch (SQLException e) {
            System.err.println("Error getting barang by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static List<Barang> getBarangByKategori(String idKategori) {
        List<Barang> listBarang = new ArrayList<>();
        String query = "SELECT id_barang, nama_barang, id_kategori, stok, harga_beli, harga_jual FROM barang WHERE id_kategori = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idKategori);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Barang barang = new Barang();
                barang.setIdBarang(rs.getString("id_barang"));
                barang.setNamaBarang(rs.getString("nama_barang"));
                barang.setIdKategori(rs.getString("id_kategori"));
                barang.setStok(rs.getInt("stok"));
                barang.setHargaBeli(rs.getDouble("harga_beli"));
                barang.setHargaJual(rs.getDouble("harga_jual"));
                listBarang.add(barang);
            }
        } catch (SQLException e) {
            System.err.println("Error getting barang by kategori: " + e.getMessage());
            e.printStackTrace();
        }
        return listBarang;
    }

    public static boolean insertBarang(Barang barang) {
        String query = "INSERT INTO barang (id_barang, nama_barang, id_kategori, stok, harga_beli, harga_jual) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, barang.getIdBarang());
            ps.setString(2, barang.getNamaBarang());
            ps.setString(3, barang.getIdKategori());
            ps.setInt(4, barang.getStok());
            ps.setDouble(5, barang.getHargaBeli());
            ps.setDouble(6, barang.getHargaJual());
            
            int result = ps.executeUpdate();
            System.out.println("Barang inserted: " + barang.getIdBarang());
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting barang: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateBarang(Barang barang) {
        String query = "UPDATE barang SET nama_barang = ?, id_kategori = ?, stok = ?, harga_beli = ?, harga_jual = ? WHERE id_barang = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, barang.getNamaBarang());
            ps.setString(2, barang.getIdKategori());
            ps.setInt(3, barang.getStok());
            ps.setDouble(4, barang.getHargaBeli());
            ps.setDouble(5, barang.getHargaJual());
            ps.setString(6, barang.getIdBarang());
            
            System.out.println("Barang updated: " + barang.getIdBarang());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating barang: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteBarang(String idBarang) {
        String query = "DELETE FROM barang WHERE id_barang = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idBarang);
            
            System.out.println("Barang deleted: " + idBarang);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting barang: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
