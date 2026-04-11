package model;

import config.koneksi;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransaksiDAO {

    public static List<Transaksi> getAllTransaksi() {
        List<Transaksi> listTransaksi = new ArrayList<>();
        String query = "SELECT id_transaksi, tgl_transaksi, id_user, total FROM transaksi";
        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Transaksi transaksi = new Transaksi();
                transaksi.setIdTransaksi(rs.getString("id_transaksi"));
                transaksi.setTglTransaksi(rs.getTimestamp("tgl_transaksi").toLocalDateTime());
                transaksi.setIdUser(rs.getString("id_user"));
                transaksi.setTotal(rs.getDouble("total"));
                listTransaksi.add(transaksi);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all transaksi: " + e.getMessage());
            e.printStackTrace();
        }
        return listTransaksi;
    }

    public static Transaksi getTransaksiById(String idTransaksi) {
        String query = "SELECT id_transaksi, tgl_transaksi, id_user, total FROM transaksi WHERE id_transaksi = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idTransaksi);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Transaksi transaksi = new Transaksi();
                transaksi.setIdTransaksi(rs.getString("id_transaksi"));
                transaksi.setTglTransaksi(rs.getTimestamp("tgl_transaksi").toLocalDateTime());
                transaksi.setIdUser(rs.getString("id_user"));
                transaksi.setTotal(rs.getDouble("total"));
                return transaksi;
            }
        } catch (SQLException e) {
            System.err.println("Error getting transaksi by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static List<Transaksi> getTransaksiByUser(String idUser) {
        List<Transaksi> listTransaksi = new ArrayList<>();
        String query = "SELECT id_transaksi, tgl_transaksi, id_user, total FROM transaksi WHERE id_user = ? ORDER BY tgl_transaksi DESC";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idUser);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Transaksi transaksi = new Transaksi();
                transaksi.setIdTransaksi(rs.getString("id_transaksi"));
                transaksi.setTglTransaksi(rs.getTimestamp("tgl_transaksi").toLocalDateTime());
                transaksi.setIdUser(rs.getString("id_user"));
                transaksi.setTotal(rs.getDouble("total"));
                listTransaksi.add(transaksi);
            }
        } catch (SQLException e) {
            System.err.println("Error getting transaksi by user: " + e.getMessage());
            e.printStackTrace();
        }
        return listTransaksi;
    }

    public static boolean insertTransaksi(Transaksi transaksi) {
        String query = "INSERT INTO transaksi (id_transaksi, tgl_transaksi, id_user, total) VALUES (?, ?, ?, ?)";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, transaksi.getIdTransaksi());
            ps.setTimestamp(2, Timestamp.valueOf(transaksi.getTglTransaksi()));
            ps.setString(3, transaksi.getIdUser());
            ps.setDouble(4, transaksi.getTotal());
            
            int result = ps.executeUpdate();
            System.out.println("Transaksi inserted: " + transaksi.getIdTransaksi());
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting transaksi: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateTransaksi(Transaksi transaksi) {
        String query = "UPDATE transaksi SET tgl_transaksi = ?, id_user = ?, total = ? WHERE id_transaksi = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setTimestamp(1, Timestamp.valueOf(transaksi.getTglTransaksi()));
            ps.setString(2, transaksi.getIdUser());
            ps.setDouble(3, transaksi.getTotal());
            ps.setString(4, transaksi.getIdTransaksi());
            
            System.out.println("Transaksi updated: " + transaksi.getIdTransaksi());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating transaksi: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteTransaksi(String idTransaksi) {
        String query = "DELETE FROM transaksi WHERE id_transaksi = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idTransaksi);
            
            System.out.println("Transaksi deleted: " + idTransaksi);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting transaksi: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
