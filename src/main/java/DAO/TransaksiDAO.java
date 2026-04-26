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

public class TransaksiDAO {
    private static final DateTimeFormatter SQL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String getNextIdTransaksi() {
        String query = "SELECT id_transaksi FROM transaksi ORDER BY id_transaksi DESC LIMIT 1";
        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                String lastId = rs.getString("id_transaksi");
                int lastNum = Integer.parseInt(lastId.substring(3));
                return String.format("TRK%03d", lastNum + 1);
            }
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Error generating next id_transaksi: " + e.getMessage());
        }
        return "TRK001";
    }

    public static List<Transaksi> getAllTransaksi() {
        List<Transaksi> listTransaksi = new ArrayList<>();
        String query = "SELECT id_transaksi, tgl_transaksi, id_user, total FROM transaksi";
        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Transaksi transaksi = new Transaksi();
                transaksi.setIdTransaksi(rs.getString("id_transaksi"));
                transaksi.setTglTransaksi(readDateTime(rs, "tgl_transaksi"));
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
                transaksi.setTglTransaksi(readDateTime(rs, "tgl_transaksi"));
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
                transaksi.setTglTransaksi(readDateTime(rs, "tgl_transaksi"));
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
            ps.setString(2, formatDateTime(transaksi.getTglTransaksi()));
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

    public static boolean saveTransaksiWithDetails(Transaksi transaksi, List<Detail_Transaksi> details) {
        if (transaksi == null || details == null || details.isEmpty()) {
            return false;
        }

        String transaksiQuery = "INSERT INTO transaksi (id_transaksi, tgl_transaksi, id_user, total) VALUES (?, ?, ?, ?)";
        String detailQuery = "INSERT INTO detail_transaksi (id_detail, id_transaksi, id_barang, jumlah, harga_satuan) VALUES (?, ?, ?, ?, ?)";
        String stokQuery = "UPDATE barang SET stok = stok - ? WHERE id_barang = ? AND stok >= ?";

        try (Connection conn = koneksi.koneksiDB()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement transaksiPs = conn.prepareStatement(transaksiQuery);
                 PreparedStatement detailPs = conn.prepareStatement(detailQuery);
                 PreparedStatement stokPs = conn.prepareStatement(stokQuery)) {

                transaksiPs.setString(1, transaksi.getIdTransaksi());
                transaksiPs.setString(2, formatDateTime(transaksi.getTglTransaksi()));
                transaksiPs.setString(3, transaksi.getIdUser());
                transaksiPs.setDouble(4, transaksi.getTotal());

                if (transaksiPs.executeUpdate() != 1) {
                    throw new SQLException("Gagal menyimpan header transaksi.");
                }

                for (Detail_Transaksi detail : details) {
                    if (detail == null || detail.getIdBarang() == null || detail.getJumlah() <= 0) {
                        throw new SQLException("Detail transaksi tidak valid.");
                    }

                    String nextIdDetail = getNextIdDetail(conn);
                    detail.setIdDetail(nextIdDetail);
                    detail.setIdTransaksi(transaksi.getIdTransaksi());

                    detailPs.clearParameters();
                    detailPs.setString(1, detail.getIdDetail());
                    detailPs.setString(2, detail.getIdTransaksi());
                    detailPs.setString(3, detail.getIdBarang());
                    detailPs.setInt(4, detail.getJumlah());
                    detailPs.setDouble(5, detail.getHargaSatuan());

                    if (detailPs.executeUpdate() != 1) {
                        throw new SQLException("Gagal menyimpan detail transaksi untuk barang " + detail.getIdBarang() + ".");
                    }

                    stokPs.clearParameters();
                    stokPs.setInt(1, detail.getJumlah());
                    stokPs.setString(2, detail.getIdBarang());
                    stokPs.setInt(3, detail.getJumlah());

                    if (stokPs.executeUpdate() != 1) {
                        throw new SQLException("Stok barang " + detail.getIdBarang() + " tidak mencukupi.");
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error saving transaksi atomically: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            System.err.println("Error preparing transaksi save: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateTransaksi(Transaksi transaksi) {
        String query = "UPDATE transaksi SET tgl_transaksi = ?, id_user = ?, total = ? WHERE id_transaksi = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, formatDateTime(transaksi.getTglTransaksi()));
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

    public static double getTotalPenjualanByDate(LocalDate tanggal) {
        String query = "SELECT COALESCE(SUM(total), 0) FROM transaksi WHERE DATE(tgl_transaksi) = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, tanggal.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total penjualan by date: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public static int getJumlahTransaksiByDate(LocalDate tanggal) {
        String query = "SELECT COUNT(*) FROM transaksi WHERE DATE(tgl_transaksi) = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, tanggal.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting jumlah transaksi by date: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    private static LocalDateTime readDateTime(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalizedValue = value.replace('T', ' ');
        if (normalizedValue.length() == 16) {
            normalizedValue += ":00";
        }

        try {
            return LocalDateTime.parse(normalizedValue, SQL_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            Timestamp timestamp = rs.getTimestamp(columnName);
            return timestamp != null ? timestamp.toLocalDateTime() : null;
        }
    }

    private static String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.format(SQL_DATE_TIME_FORMATTER);
    }

    private static String getNextIdDetail(Connection conn) throws SQLException {
        String query = "SELECT id_detail FROM detail_transaksi ORDER BY id_detail DESC LIMIT 1";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                String lastId = rs.getString("id_detail");
                int lastNum = Integer.parseInt(lastId.substring(3));
                return String.format("DTL%03d", lastNum + 1);
            }
        } catch (NumberFormatException e) {
            throw new SQLException("Format id_detail tidak valid.", e);
        }
        return "DTL001";
    }
}
