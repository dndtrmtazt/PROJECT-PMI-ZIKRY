package DAO;

import config.koneksi;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import model.Pengeluaran;

public class PengeluaranDAO {

    // 1. Tambahkan 'static' agar bisa dipanggil langsung tanpa 'new'
    public static List<Pengeluaran> getAllPengeluaran() {
        List<Pengeluaran> listPengeluaran = new ArrayList<>();
        String query = "SELECT * FROM pengeluaran ORDER BY tgl_pengeluaran DESC, id_pengeluaran ASC";

        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

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
            System.err.println("Gagal Mengambil Data Pengeluaran: " + e.getMessage());
        }
        return listPengeluaran;
    }

    // 2. Tambahkan 'static'
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

    // 3. Tambahkan 'static' dan parameter idLama tetap dipertahankan
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

    // 4. Tambahkan 'static'
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

    public static double getTotalPengeluaranByDate(LocalDate tanggal) {
        String query = "SELECT COALESCE(SUM(nominal), 0) FROM pengeluaran WHERE tgl_pengeluaran = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, tanggal.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Gagal Mengambil Total Pengeluaran Harian: " + e.getMessage());
        }
        return 0;
    }

    private static LocalDate readLocalDate(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            Date sqlDate = rs.getDate(columnName);
            return sqlDate != null ? sqlDate.toLocalDate() : null;
        }
    }
}
