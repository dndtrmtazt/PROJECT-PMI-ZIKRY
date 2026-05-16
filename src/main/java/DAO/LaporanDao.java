package DAO;

import config.koneksi;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Laporan;

// DAO khusus laporan, menggabungkan data transaksi dan pengeluaran per tanggal.
public class LaporanDao {

    // Mengambil seluruh laporan harian dari gabungan tabel transaksi dan pengeluaran.
    public static List<Laporan> getAllLaporan() {
        List<Laporan> listLaporan = new ArrayList<>();
        // UNION dipakai agar tanggal yang hanya punya transaksi atau hanya punya pengeluaran tetap muncul.
        String query = "SELECT " +
                       "    t.tanggal, " +
                       "    COALESCE(p.total_penjualan, 0) as total_penjualan, " +
                       "    COALESCE(exp.total_pengeluaran, 0) as total_pengeluaran, " +
                       "    COALESCE(p.jumlah_transaksi, 0) as jumlah_transaksi " +
                       "FROM ( " +
                       "    SELECT DATE(tgl_transaksi) as tanggal FROM transaksi " +
                       "    UNION " +
                       "    SELECT tgl_pengeluaran FROM pengeluaran " +
                       ") as t " +
                       "LEFT JOIN ( " +
                       "    SELECT DATE(tgl_transaksi) as tanggal, SUM(total) as total_penjualan, COUNT(*) as jumlah_transaksi " +
                       "    FROM transaksi " +
                       "    GROUP BY DATE(tgl_transaksi) " +
                       ") as p ON t.tanggal = p.tanggal " +
                       "LEFT JOIN ( " +
                       "    SELECT tgl_pengeluaran as tanggal, SUM(nominal) as total_pengeluaran " +
                       "    FROM pengeluaran " +
                       "    GROUP BY tgl_pengeluaran " +
                       ") as exp ON t.tanggal = exp.tanggal " +
                       "ORDER BY t.tanggal DESC";

        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                listLaporan.add(mapLaporan(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all laporan: " + e.getMessage());
            e.printStackTrace();
        }
        return listLaporan;
    }

    // Mengambil laporan berdasarkan rentang tanggal yang dipilih user.
    public static List<Laporan> getLaporanByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Laporan> listLaporan = new ArrayList<>();
        // Query ini sama seperti laporan penuh, tetapi dibatasi oleh tanggal awal dan akhir.
        String query = "SELECT " +
                       "    t.tanggal, " +
                       "    COALESCE(p.total_penjualan, 0) as total_penjualan, " +
                       "    COALESCE(exp.total_pengeluaran, 0) as total_pengeluaran, " +
                       "    COALESCE(p.jumlah_transaksi, 0) as jumlah_transaksi " +
                       "FROM ( " +
                       "    SELECT DATE(tgl_transaksi) as tanggal FROM transaksi " +
                       "    UNION " +
                       "    SELECT tgl_pengeluaran FROM pengeluaran " +
                       ") as t " +
                       "LEFT JOIN ( " +
                       "    SELECT DATE(tgl_transaksi) as tanggal, SUM(total) as total_penjualan, COUNT(*) as jumlah_transaksi " +
                       "    FROM transaksi " +
                       "    GROUP BY DATE(tgl_transaksi) " +
                       ") as p ON t.tanggal = p.tanggal " +
                       "LEFT JOIN ( " +
                       "    SELECT tgl_pengeluaran as tanggal, SUM(nominal) as total_pengeluaran " +
                       "    FROM pengeluaran " +
                       "    GROUP BY tgl_pengeluaran " +
                       ") as exp ON t.tanggal = exp.tanggal " +
                       "WHERE t.tanggal BETWEEN ? AND ? " +
                       "ORDER BY t.tanggal DESC";

        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, startDate.toString());
            ps.setString(2, endDate.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listLaporan.add(mapLaporan(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting laporan by date range: " + e.getMessage());
            e.printStackTrace();
        }
        return listLaporan;
    }

    // Membungkus hasil query laporan menjadi model Laporan untuk TableView.
    private static Laporan mapLaporan(ResultSet rs) throws SQLException {
        return new Laporan(
                rs.getString("tanggal"),
                rs.getDouble("total_penjualan"),
                rs.getDouble("total_pengeluaran"),
                rs.getInt("jumlah_transaksi")
        );
    }
}
