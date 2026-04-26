package DAO;

import config.koneksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Detail_Transaksi;

/**
 * Data Access Object (DAO) untuk tabel detail_transaksi.
 * Alur: Mengelola item-item yang dibeli dalam satu transaksi tunggal.
 */
public class DetailTransaksiDAO {

    /**
     * Method getNextIdDetail: Menghasilkan ID detail urut (DTL001, dst).
     */
    public static String getNextIdDetail() {
        String query = "SELECT id_detail FROM detail_transaksi ORDER BY id_detail DESC LIMIT 1";
        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                // [1] Ambil angka dari ID terakhir dan tambah 1
                String lastId = rs.getString("id_detail");
                int lastNum = Integer.parseInt(lastId.substring(3));
                return String.format("DTL%03d", lastNum + 1);
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }
        return "DTL001";
    }

    /**
     * Method getDetailByTransaksi: Mengambil daftar barang belanjaan berdasarkan ID Transaksi induk.
     */
    public static List<Detail_Transaksi> getDetailByTransaksi(String idTransaksi) {
        List<Detail_Transaksi> listDetail = new ArrayList<>();
        String query = "SELECT * FROM detail_transaksi WHERE id_transaksi = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            // [1] Filter berdasarkan ID transaksi
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
            e.printStackTrace();
        }
        return listDetail;
    }

    /**
     * Method insertDetail: Menyimpan item barang belanjaan ke database secara aman (Transaction support).
     */
    public static boolean insertDetail(Connection conn, Detail_Transaksi detail) throws SQLException {
        String query = "INSERT INTO detail_transaksi (id_detail, id_transaksi, id_barang, jumlah, harga_satuan) VALUES (?, ?, ?, ?, ?)";
        // [1] Gunakan objek 'conn' yang sama dari TransaksiDAO agar tergabung dalam satu transaksi DB
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, detail.getIdDetail());
            ps.setString(2, detail.getIdTransaksi());
            ps.setString(3, detail.getIdBarang());
            ps.setInt(4, detail.getJumlah());
            ps.setDouble(5, detail.getHargaSatuan());
            // [2] Jalankan insert
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Method deleteDetail: Menghapus item detail tertentu.
     */
    public static boolean deleteDetail(String idDetail) {
        String query = "DELETE FROM detail_transaksi WHERE id_detail = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idDetail);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
