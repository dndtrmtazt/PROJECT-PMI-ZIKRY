package DAO;

import config.koneksi;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import model.Pengeluaran;

/**
 * Data Access Object (DAO) untuk tabel pengeluaran.
 * Alur: Mengelola pencatatan pengeluaran operasional toko (List, Add, Update, Delete).
 */
public class PengeluaranDAO {

    /**
     * Method getAllPengeluaran: Mengambil daftar riwayat pengeluaran terbaru.
     */
    public static List<Pengeluaran> getAllPengeluaran() {
        List<Pengeluaran> listPengeluaran = new ArrayList<>();
        String query = "SELECT * FROM pengeluaran ORDER BY tgl_pengeluaran DESC, id_pengeluaran ASC";
        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            // [1] Iterasi baris data dan masukkan ke objek model
            while (rs.next()) {
                Pengeluaran p = new Pengeluaran();
                p.setIdPengeluaran(rs.getString("id_pengeluaran"));
                p.setTglPengeluaran(readLocalDate(rs, "tgl_pengeluaran"));
                p.setNominal(rs.getDouble("nominal"));
                p.setJenis(rs.getString("jenis"));
                p.setIdUser(rs.getString("id_user"));
                listPengeluaran.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listPengeluaran;
    }

    /**
     * Method getNextIdPengeluaran: Membuat ID pengeluaran urut (PGN001, PGN002, dst).
     */
    public static String getNextIdPengeluaran() {
        String query = "SELECT id_pengeluaran FROM pengeluaran ORDER BY id_pengeluaran DESC LIMIT 1";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                // [1] Potong prefix 'PGN' dan tambah angka terakhirnya
                String lastId = rs.getString("id_pengeluaran");
                int lastNumber = Integer.parseInt(lastId.substring(3));
                return String.format("PGN%03d", lastNumber + 1);
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }
        return "PGN001";
    }

    /**
     * Method addPengeluaran: Menyimpan catatan pengeluaran baru.
     */
    public static boolean addPengeluaran(Pengeluaran p) {
        String query = "INSERT INTO pengeluaran (id_pengeluaran, tgl_pengeluaran, nominal, jenis, id_user) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            // [1] Mapping data model ke parameter SQL
            ps.setString(1, p.getIdPengeluaran());
            ps.setString(2, p.getTglPengeluaran().toString());
            ps.setDouble(3, p.getNominal());
            ps.setString(4, p.getJenis());
            ps.setString(5, p.getIdUser());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method updatePengeluaran: Mengubah data pengeluaran yang sudah tersimpan.
     */
    public static boolean updatePengeluaran(Pengeluaran p, String oldId) {
        String query = "UPDATE pengeluaran SET id_pengeluaran = ?, tgl_pengeluaran = ?, nominal = ?, jenis = ?, id_user = ? WHERE id_pengeluaran = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            // [1] Set nilai baru dan gunakan ID lama sebagai patokan WHERE
            ps.setString(1, p.getIdPengeluaran());
            ps.setString(2, p.getTglPengeluaran().toString());
            ps.setDouble(3, p.getNominal());
            ps.setString(4, p.getJenis());
            ps.setString(5, p.getIdUser());
            ps.setString(6, oldId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method deletePengeluaran: Menghapus catatan pengeluaran.
     */
    public static boolean deletePengeluaran(String idPengeluaran) {
        String query = "DELETE FROM pengeluaran WHERE id_pengeluaran = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idPengeluaran);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method getTotalPengeluaranByDate: Menjumlahkan seluruh biaya pengeluaran pada tanggal tertentu.
     */
    public static double getTotalPengeluaranByDate(LocalDate tanggal) {
        String query = "SELECT COALESCE(SUM(nominal), 0) FROM pengeluaran WHERE tgl_pengeluaran = ?";
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

    private static LocalDate readLocalDate(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            Date sqlDate = rs.getDate(columnName);
            return sqlDate != null ? sqlDate.toLocalDate() : null;
        }
    }
}
