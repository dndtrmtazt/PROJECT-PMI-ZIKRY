package model;

import config.koneksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetailTransaksiDAO {

    public static List<Detail_Transaksi> getDetailByTransaksi(String idTransaksi) {
        List<Detail_Transaksi> listDetail = new ArrayList<>();
        String query = "SELECT id_detail, id_transaksi, id_barang, jumlah, harga_satuan, subtotal FROM detail_transaksi WHERE id_transaksi = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idTransaksi);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Detail_Transaksi detail = new Detail_Transaksi();
                detail.setIdDetail(rs.getString("id_detail"));
                detail.setIdTransaksi(rs.getString("id_transaksi"));
                detail.setIdBarang(rs.getString("id_barang"));
                detail.setJumlah(rs.getInt("jumlah"));
                detail.setHargaSatuan(rs.getDouble("harga_satuan"));
                detail.setSubtotal(rs.getDouble("subtotal"));
                listDetail.add(detail);
            }
        } catch (SQLException e) {
            System.err.println("Error getting detail transaksi: " + e.getMessage());
            e.printStackTrace();
        }
        return listDetail;
    }

    public static Detail_Transaksi getDetailById(String idDetail) {
        String query = "SELECT id_detail, id_transaksi, id_barang, jumlah, harga_satuan, subtotal FROM detail_transaksi WHERE id_detail = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idDetail);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Detail_Transaksi detail = new Detail_Transaksi();
                detail.setIdDetail(rs.getString("id_detail"));
                detail.setIdTransaksi(rs.getString("id_transaksi"));
                detail.setIdBarang(rs.getString("id_barang"));
                detail.setJumlah(rs.getInt("jumlah"));
                detail.setHargaSatuan(rs.getDouble("harga_satuan"));
                detail.setSubtotal(rs.getDouble("subtotal"));
                return detail;
            }
        } catch (SQLException e) {
            System.err.println("Error getting detail by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static boolean insertDetail(Detail_Transaksi detail) {
        String query = "INSERT INTO detail_transaksi (id_detail, id_transaksi, id_barang, jumlah, harga_satuan) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, detail.getIdDetail());
            ps.setString(2, detail.getIdTransaksi());
            ps.setString(3, detail.getIdBarang());
            ps.setInt(4, detail.getJumlah());
            ps.setDouble(5, detail.getHargaSatuan());
            
            int result = ps.executeUpdate();
            System.out.println("Detail transaksi inserted: " + detail.getIdDetail());
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting detail transaksi: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateDetail(Detail_Transaksi detail) {
        String query = "UPDATE detail_transaksi SET id_barang = ?, jumlah = ?, harga_satuan = ? WHERE id_detail = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, detail.getIdBarang());
            ps.setInt(2, detail.getJumlah());
            ps.setDouble(3, detail.getHargaSatuan());
            ps.setString(4, detail.getIdDetail());
            
            System.out.println("Detail transaksi updated: " + detail.getIdDetail());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating detail transaksi: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteDetail(String idDetail) {
        String query = "DELETE FROM detail_transaksi WHERE id_detail = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idDetail);
            
            System.out.println("Detail transaksi deleted: " + idDetail);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting detail transaksi: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
