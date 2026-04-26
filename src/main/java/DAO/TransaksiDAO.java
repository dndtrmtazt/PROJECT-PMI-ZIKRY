package DAO;

import config.koneksi;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import model.Detail_Transaksi;
import model.Transaksi;

/**
 * Data Access Object (DAO) untuk tabel transaksi.
 * Alur: Menangani penyimpanan transaksi induk dan proses checkout aman (Atomic Transaction).
 */
public class TransaksiDAO {
    private static final DateTimeFormatter SQL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Method getNextIdTransaksi: Membuat ID transaksi baru (TRK001, TRK002, dst).
     */
    public static String getNextIdTransaksi() {
        String query = "SELECT id_transaksi FROM transaksi ORDER BY id_transaksi DESC LIMIT 1";
        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                // [1] Ambil ID terakhir dan tambah angka urutnya
                String lastId = rs.getString("id_transaksi");
                int lastNum = Integer.parseInt(lastId.substring(3));
                return String.format("TRK%03d", lastNum + 1);
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }
        return "TRK001";
    }

    /**
     * Method prosesCheckout: Alur penyimpanan transaksi paling penting (Atomic).
     * Alur: 1. Simpan Header -> 2. Simpan Detail -> 3. Kurangi Stok -> 4. Commit/Rollback.
     */
    public static boolean prosesCheckout(Transaksi transaksi, List<Detail_Transaksi> details) {
        String queryInsertTrx = "INSERT INTO transaksi (id_transaksi, tgl_transaksi, id_user, total) VALUES (?, ?, ?, ?)";
        try (Connection conn = koneksi.koneksiDB()) {
            // [1] MATIKAN auto-commit agar semua operasi menjadi satu kesatuan (All or Nothing)
            conn.setAutoCommit(false);
            try (PreparedStatement psTrx = conn.prepareStatement(queryInsertTrx)) {
                // [2] Langkah 1: Simpan data utama Transaksi
                psTrx.setString(1, transaksi.getIdTransaksi());
                psTrx.setString(2, formatDateTime(transaksi.getTglTransaksi()));
                psTrx.setString(3, transaksi.getIdUser());
                psTrx.setDouble(4, transaksi.getTotal());
                psTrx.executeUpdate();

                // [3] Langkah 2: Looping untuk simpan setiap item detail dan kurangi stok barang
                for (Detail_Transaksi detail : details) {
                    // Simpan baris detail barang
                    if (!DetailTransaksiDAO.insertDetail(conn, detail)) {
                        throw new SQLException("Gagal simpan detail barang");
                    }
                    // Kurangi stok fisik di gudang, jika gagal (stok kurang) maka lempar exception
                    if (!BarangDAO.reduceStok(conn, detail.getIdBarang(), detail.getJumlah())) {
                        throw new SQLException("Stok barang tidak mencukupi");
                    }
                }
                // [4] Final: Jika semua aman, simpan permanen ke disk
                conn.commit();
                return true;
            } catch (SQLException ex) {
                // [5] Rollback: Batalkan semua jika ada satu saja yang error untuk menjaga konsistensi
                conn.rollback();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Method getTotalPenjualanByDate: Menghitung total omset uang masuk pada tanggal tertentu.
     */
    public static double getTotalPenjualanByDate(LocalDate tanggal) {
        String query = "SELECT COALESCE(SUM(total), 0) FROM transaksi WHERE DATE(tgl_transaksi) = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, tanggal.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Method getJumlahTransaksiByDate: Menghitung berapa kali transaksi terjadi pada tanggal tertentu.
     */
    public static int getJumlahTransaksiByDate(LocalDate tanggal) {
        String query = "SELECT COUNT(*) FROM transaksi WHERE DATE(tgl_transaksi) = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, tanggal.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static LocalDateTime readDateTime(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if (value == null || value.trim().isEmpty()) return null;
        String normalizedValue = value.replace('T', ' ');
        if (normalizedValue.length() == 16) normalizedValue += ":00";
        try {
            return LocalDateTime.parse(normalizedValue, SQL_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            Timestamp timestamp = rs.getTimestamp(columnName);
            return timestamp != null ? timestamp.toLocalDateTime() : null;
        }
    }

    private static String formatDateTime(LocalDateTime value) {
        return (value == null) ? null : value.format(SQL_DATE_TIME_FORMATTER);
    }
}
