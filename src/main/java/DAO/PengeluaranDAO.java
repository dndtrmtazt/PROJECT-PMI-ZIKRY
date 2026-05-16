package DAO;

import config.koneksi;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import model.Pengeluaran;

// DAO untuk data pengeluaran operasional toko.
public class PengeluaranDAO {

    // Mengambil semua pengeluaran dan mengurutkannya dari tanggal terbaru.
    public static List<Pengeluaran> getAllPengeluaran() {
        List<Pengeluaran> listPengeluaran = new ArrayList<>();
        String query = "SELECT * FROM pengeluaran ORDER BY tgl_pengeluaran DESC, id_pengeluaran ASC";

        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                listPengeluaran.add(mapPengeluaran(rs));
            }
        } catch (SQLException e) {
            System.err.println("Gagal Mengambil Data Pengeluaran: " + e.getMessage());
        }
        return listPengeluaran;
    }

    // Menambahkan data pengeluaran baru.
    public static boolean addPengeluaran(Pengeluaran p) {
        String query = "INSERT INTO pengeluaran (id_pengeluaran, tgl_pengeluaran, nominal, jenis, id_user) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, p.getIdPengeluaran());
            ps.setString(2, p.getTglPengeluaran().toString());
            ps.setDouble(3, p.getNominal());
            ps.setString(4, p.getJenis());
            ps.setString(5, p.getIdUser());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal Tambah Pengeluaran: " + e.getMessage());
            return false;
        }
    }

    // Membuat ID pengeluaran berikutnya dengan format PGN001, PGN002, dan seterusnya.
    public static String getNextIdPengeluaran() {
        String query = "SELECT MAX(CAST(SUBSTR(id_pengeluaran, 4) AS INTEGER)) FROM pengeluaran WHERE id_pengeluaran LIKE 'PGN%'";

        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            int nextNumber = 1;
            if (rs.next()) {
                nextNumber = rs.getInt(1) + 1;
            }
            return String.format("PGN%03d", nextNumber);
        } catch (SQLException e) {
            System.err.println("Gagal Membuat ID Pengeluaran: " + e.getMessage());
            return "PGN001";
        }
    }

    // Mengubah data pengeluaran berdasarkan ID lama.
    public static boolean updatePengeluaran(Pengeluaran p, String oldId) {
        String query = "UPDATE pengeluaran SET id_pengeluaran = ?, tgl_pengeluaran = ?, nominal = ?, jenis = ?, id_user = ? WHERE id_pengeluaran = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, p.getIdPengeluaran());
            ps.setString(2, p.getTglPengeluaran().toString());
            ps.setDouble(3, p.getNominal());
            ps.setString(4, p.getJenis());
            ps.setString(5, p.getIdUser());
            ps.setString(6, oldId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Update Error: " + e.getMessage());
            return false;
        }
    }

    // Menghapus data pengeluaran berdasarkan ID.
    public static boolean deletePengeluaran(String idPengeluaran) {
        String query = "DELETE FROM pengeluaran WHERE id_pengeluaran = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idPengeluaran);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal Hapus Pengeluaran: " + e.getMessage());
            return false;
        }
    }

    // Menghitung total pengeluaran pada satu tanggal, dipakai di dashboard.
    public static double getTotalPengeluaranByDate(LocalDate tanggal) {
        String query = "SELECT COALESCE(SUM(nominal), 0) FROM pengeluaran WHERE tgl_pengeluaran = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, tanggal.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal Mengambil Total Pengeluaran Harian: " + e.getMessage());
        }
        return 0;
    }

    // Mengubah baris ResultSet menjadi model Pengeluaran.
    private static Pengeluaran mapPengeluaran(ResultSet rs) throws SQLException {
        Pengeluaran pengeluaran = new Pengeluaran();
        pengeluaran.setIdPengeluaran(rs.getString("id_pengeluaran"));
        pengeluaran.setTglPengeluaran(readPengeluaranDate(rs));
        pengeluaran.setNominal(rs.getDouble("nominal"));
        pengeluaran.setJenis(rs.getString("jenis"));
        pengeluaran.setIdUser(rs.getString("id_user"));
        return pengeluaran;
    }

    // Membaca tanggal pengeluaran dengan fallback jika format tanggal dari SQLite berbeda.
    private static LocalDate readPengeluaranDate(ResultSet rs) throws SQLException {
        String value = rs.getString("tgl_pengeluaran");
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            Date sqlDate = rs.getDate("tgl_pengeluaran");
            return sqlDate != null ? sqlDate.toLocalDate() : null;
        }
    }
}
