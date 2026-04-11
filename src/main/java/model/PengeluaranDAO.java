package model;

import config.koneksi;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PengeluaranDAO {

    public static List<Pengeluaran> getAllPengeluaran() {
        List<Pengeluaran> listPengeluaran = new ArrayList<>();
        String query = "SELECT id_pengeluaran, tgl_pengeluaran, nominal, jenis, id_user FROM pengeluaran";
        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Pengeluaran pengeluaran = new Pengeluaran();
                pengeluaran.setIdPengeluaran(rs.getString("id_pengeluaran"));
                pengeluaran.setTglPengeluaran(rs.getDate("tgl_pengeluaran").toLocalDate());
                pengeluaran.setNominal(rs.getDouble("nominal"));
                pengeluaran.setJenis(rs.getString("jenis"));
                pengeluaran.setIdUser(rs.getString("id_user"));
                listPengeluaran.add(pengeluaran);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all pengeluaran: " + e.getMessage());
            e.printStackTrace();
        }
        return listPengeluaran;
    }

    public static Pengeluaran getPengeluaranById(String idPengeluaran) {
        String query = "SELECT id_pengeluaran, tgl_pengeluaran, nominal, jenis, id_user FROM pengeluaran WHERE id_pengeluaran = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idPengeluaran);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Pengeluaran pengeluaran = new Pengeluaran();
                pengeluaran.setIdPengeluaran(rs.getString("id_pengeluaran"));
                pengeluaran.setTglPengeluaran(rs.getDate("tgl_pengeluaran").toLocalDate());
                pengeluaran.setNominal(rs.getDouble("nominal"));
                pengeluaran.setJenis(rs.getString("jenis"));
                pengeluaran.setIdUser(rs.getString("id_user"));
                return pengeluaran;
            }
        } catch (SQLException e) {
            System.err.println("Error getting pengeluaran by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static List<Pengeluaran> getPengeluaranByUser(String idUser) {
        List<Pengeluaran> listPengeluaran = new ArrayList<>();
        String query = "SELECT id_pengeluaran, tgl_pengeluaran, nominal, jenis, id_user FROM pengeluaran WHERE id_user = ? ORDER BY tgl_pengeluaran DESC";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idUser);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Pengeluaran pengeluaran = new Pengeluaran();
                pengeluaran.setIdPengeluaran(rs.getString("id_pengeluaran"));
                pengeluaran.setTglPengeluaran(rs.getDate("tgl_pengeluaran").toLocalDate());
                pengeluaran.setNominal(rs.getDouble("nominal"));
                pengeluaran.setJenis(rs.getString("jenis"));
                pengeluaran.setIdUser(rs.getString("id_user"));
                listPengeluaran.add(pengeluaran);
            }
        } catch (SQLException e) {
            System.err.println("Error getting pengeluaran by user: " + e.getMessage());
            e.printStackTrace();
        }
        return listPengeluaran;
    }

    public static List<Pengeluaran> getPengeluaranByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Pengeluaran> listPengeluaran = new ArrayList<>();
        String query = "SELECT id_pengeluaran, tgl_pengeluaran, nominal, jenis, id_user FROM pengeluaran WHERE tgl_pengeluaran BETWEEN ? AND ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Pengeluaran pengeluaran = new Pengeluaran();
                pengeluaran.setIdPengeluaran(rs.getString("id_pengeluaran"));
                pengeluaran.setTglPengeluaran(rs.getDate("tgl_pengeluaran").toLocalDate());
                pengeluaran.setNominal(rs.getDouble("nominal"));
                pengeluaran.setJenis(rs.getString("jenis"));
                pengeluaran.setIdUser(rs.getString("id_user"));
                listPengeluaran.add(pengeluaran);
            }
        } catch (SQLException e) {
            System.err.println("Error getting pengeluaran by date range: " + e.getMessage());
            e.printStackTrace();
        }
        return listPengeluaran;
    }

    public static boolean insertPengeluaran(Pengeluaran pengeluaran) {
        String query = "INSERT INTO pengeluaran (id_pengeluaran, tgl_pengeluaran, nominal, jenis, id_user) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, pengeluaran.getIdPengeluaran());
            ps.setDate(2, Date.valueOf(pengeluaran.getTglPengeluaran()));
            ps.setDouble(3, pengeluaran.getNominal());
            ps.setString(4, pengeluaran.getJenis());
            ps.setString(5, pengeluaran.getIdUser());
            
            int result = ps.executeUpdate();
            System.out.println("Pengeluaran inserted: " + pengeluaran.getIdPengeluaran());
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting pengeluaran: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updatePengeluaran(Pengeluaran pengeluaran) {
        String query = "UPDATE pengeluaran SET tgl_pengeluaran = ?, nominal = ?, jenis = ?, id_user = ? WHERE id_pengeluaran = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDate(1, Date.valueOf(pengeluaran.getTglPengeluaran()));
            ps.setDouble(2, pengeluaran.getNominal());
            ps.setString(3, pengeluaran.getJenis());
            ps.setString(4, pengeluaran.getIdUser());
            ps.setString(5, pengeluaran.getIdPengeluaran());
            
            System.out.println("Pengeluaran updated: " + pengeluaran.getIdPengeluaran());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating pengeluaran: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deletePengeluaran(String idPengeluaran) {
        String query = "DELETE FROM pengeluaran WHERE id_pengeluaran = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idPengeluaran);
            
            System.out.println("Pengeluaran deleted: " + idPengeluaran);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting pengeluaran: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
