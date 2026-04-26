package DAO;

import config.koneksi;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Laporan;

/**
 * Data Access Object (DAO) untuk pengelolaan Laporan Laba Rugi sederhana.
 * Alur: Menghitung akumulasi penjualan dan pengeluaran harian menggunakan query SQL kompleks (UNION & JOIN).
 */
public class LaporanDao {

    /**
     * Method getAllLaporan: Mengambil seluruh riwayat omset dan biaya harian.
     * Alur: 1. Ambil list tanggal unik -> 2. Gabungkan total penjualan -> 3. Gabungkan total pengeluaran -> 4. Urutkan.
     */
    public static List<Laporan> getAllLaporan() {
        List<Laporan> listLaporan = new ArrayList<>();
        // [1] Query gabungan untuk mendapatkan statistik per tanggal
        String query = "SELECT t.tanggal, COALESCE(p.total_penjualan, 0) as total_penjualan, " +
                       "COALESCE(exp.total_pengeluaran, 0) as total_pengeluaran, COALESCE(p.jumlah_transaksi, 0) as jumlah_transaksi " +
                       "FROM (SELECT DATE(tgl_transaksi) as tanggal FROM transaksi UNION SELECT tgl_pengeluaran FROM pengeluaran) as t " +
                       "LEFT JOIN (SELECT DATE(tgl_transaksi) as tanggal, SUM(total) as total_penjualan, COUNT(*) as jumlah_transaksi " +
                       "FROM transaksi GROUP BY DATE(tgl_transaksi)) as p ON t.tanggal = p.tanggal " +
                       "LEFT JOIN (SELECT tgl_pengeluaran as tanggal, SUM(nominal) as total_pengeluaran " +
                       "FROM pengeluaran GROUP BY tgl_pengeluaran) as exp ON t.tanggal = exp.tanggal " +
                       "ORDER BY t.tanggal DESC";
        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            // [2] Iterasi hasil laporan dan masukkan ke objek model
            while (rs.next()) {
                Laporan laporan = new Laporan(
                    rs.getString("tanggal"),
                    rs.getDouble("total_penjualan"),
                    rs.getDouble("total_pengeluaran"),
                    rs.getInt("jumlah_transaksi")
                );
                listLaporan.add(laporan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listLaporan;
    }

    /**
     * Method getLaporanByDateRange: Mengambil laporan dalam rentang waktu tertentu.
     */
    public static List<Laporan> getLaporanByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Laporan> listLaporan = new ArrayList<>();
        String query = "SELECT t.tanggal, COALESCE(p.total_penjualan, 0) as total_penjualan, " +
                       "COALESCE(exp.total_pengeluaran, 0) as total_pengeluaran, COALESCE(p.jumlah_transaksi, 0) as jumlah_transaksi " +
                       "FROM (SELECT DATE(tgl_transaksi) as tanggal FROM transaksi UNION SELECT tgl_pengeluaran FROM pengeluaran) as t " +
                       "LEFT JOIN (SELECT DATE(tgl_transaksi) as tanggal, SUM(total) as total_penjualan, COUNT(*) as jumlah_transaksi " +
                       "FROM transaksi GROUP BY DATE(tgl_transaksi)) as p ON t.tanggal = p.tanggal " +
                       "LEFT JOIN (SELECT tgl_pengeluaran as tanggal, SUM(nominal) as total_pengeluaran " +
                       "FROM pengeluaran GROUP BY tgl_pengeluaran) as exp ON t.tanggal = exp.tanggal " +
                       "WHERE t.tanggal BETWEEN ? AND ? ORDER BY t.tanggal DESC";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            // [1] Set filter tanggal mulai dan akhir
            ps.setString(1, startDate.toString());
            ps.setString(2, endDate.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Laporan laporan = new Laporan(rs.getString("tanggal"), rs.getDouble("total_penjualan"), rs.getDouble("total_pengeluaran"), rs.getInt("jumlah_transaksi"));
                listLaporan.add(laporan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listLaporan;
    }
}
