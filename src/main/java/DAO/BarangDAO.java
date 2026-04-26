package DAO;

import config.koneksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Barang;

/**
 * Data Access Object (DAO) untuk tabel barang.
 * Alur: Menyediakan fungsi CRUD dan manajemen stok barang pada database SQLite.
 */
public class BarangDAO {
    // Lebar angka untuk format ID barang otomatis (contoh: MIM001)
    private static final int BARANG_ID_NUMBER_WIDTH = 3;

    /**
     * Method getAllBarang: Mengambil seluruh data barang dari database.
     */
    public static List<Barang> getAllBarang() {
        List<Barang> listBarang = new ArrayList<>();
        String query = "SELECT id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual FROM barang";
        // [1] Buka koneksi dan jalankan query
        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            // [2] Iterasi hasil query dan masukkan ke list
            while (rs.next()) {
                Barang barang = new Barang();
                barang.setIdBarang(rs.getString("id_barang"));
                barang.setNamaBarang(rs.getString("nama_barang"));
                barang.setIdKategori(rs.getString("id_kategori"));
                barang.setStok(rs.getInt("stok"));
                barang.setSatuan(rs.getString("satuan"));
                barang.setHargaBeli(rs.getDouble("harga_beli"));
                barang.setHargaJual(rs.getDouble("harga_jual"));
                listBarang.add(barang);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listBarang;
    }

    /**
     * Method getBarangById: Mencari satu data barang berdasarkan ID uniknya.
     */
    public static Barang getBarangById(String id_barang) {
        String query = "SELECT id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual FROM barang WHERE id_barang = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            // [1] Set parameter ID dan eksekusi
            ps.setString(1, id_barang);
            ResultSet rs = ps.executeQuery();
            // [2] Jika ditemukan, buat objek barang dan kembalikan
            if (rs.next()) {
                Barang barang = new Barang();
                barang.setIdBarang(rs.getString("id_barang"));
                barang.setNamaBarang(rs.getString("nama_barang"));
                barang.setIdKategori(rs.getString("id_kategori"));
                barang.setStok(rs.getInt("stok"));
                barang.setSatuan(rs.getString("satuan"));
                barang.setHargaBeli(rs.getDouble("harga_beli"));
                barang.setHargaJual(rs.getDouble("harga_jual"));
                return barang;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method insertBarang: Menambahkan data barang baru ke database.
     */
    public static boolean insertBarang(Barang barang) {
        String query = "INSERT INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            // [1] Set semua parameter data barang
            ps.setString(1, barang.getIdBarang());
            ps.setString(2, barang.getNamaBarang());
            ps.setString(3, barang.getIdKategori());
            ps.setInt(4, barang.getStok());
            ps.setString(5, isBlank(barang.getSatuan()) ? "Pcs" : barang.getSatuan());
            ps.setDouble(6, barang.getHargaBeli());
            ps.setDouble(7, barang.getHargaJual());
            // [2] Eksekusi dan cek keberhasilan
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Method updateBarang: Memperbarui data barang yang sudah ada.
     */
    public static boolean updateBarang(Barang barang) {
        String query = "UPDATE barang SET nama_barang = ?, id_kategori = ?, stok = ?, satuan = ?, harga_beli = ?, harga_jual = ? WHERE id_barang = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            // [1] Set parameter update
            ps.setString(1, barang.getNamaBarang());
            ps.setString(2, barang.getIdKategori());
            ps.setInt(3, barang.getStok());
            ps.setString(4, isBlank(barang.getSatuan()) ? "Pcs" : barang.getSatuan());
            ps.setDouble(5, barang.getHargaBeli());
            ps.setDouble(6, barang.getHargaJual());
            ps.setString(7, barang.getIdBarang());
            // [2] Jalankan update
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Method reduceStok: Mengurangi stok barang secara aman saat checkout (Transaction support).
     */
    public static boolean reduceStok(Connection conn, String id_barang, int jumlah) throws SQLException {
        String cleanId = (id_barang != null) ? id_barang.trim() : "";
        // [1] Query update stok dengan syarat stok harus cukup agar tidak minus
        String query = "UPDATE barang SET stok = stok - ? WHERE id_barang = ? AND stok >= ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, jumlah);
            ps.setString(2, cleanId);
            ps.setInt(3, jumlah);
            // [2] Return true jika baris terupdate (stok cukup)
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Method getNextBarangId: Menghasilkan ID barang urut otomatis berdasarkan kategori.
     */
    public static String getNextBarangId(String prefix) {
        String query = "SELECT id_barang FROM barang WHERE id_barang LIKE ? ORDER BY id_barang DESC LIMIT 1";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            // [1] Cari ID terakhir dengan prefix kategori (misal MIM%)
            ps.setString(1, prefix + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // [2] Ambil angka urut, tambah 1, dan format ulang (MIM001 -> MIM002)
                String lastId = rs.getString("id_barang");
                String numericPart = lastId.substring(prefix.length());
                int nextNum = Integer.parseInt(numericPart) + 1;
                return formatBarangId(prefix, nextNum);
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }
        return formatBarangId(prefix, 1);
    }

    /**
     * Method getPrefixFromKategoriId: Mengambil 3 huruf pertama kategori untuk ID barang.
     */
    public static String getPrefixFromKategoriId(String id_kategori) {
        if (id_kategori == null) return "";
        String trimmedId = id_kategori.trim().toUpperCase();
        return (trimmedId.length() < 3) ? trimmedId : trimmedId.substring(0, 3);
    }

    /**
     * Method isBarangIdMatchKategori: Memvalidasi apakah ID Barang sesuai dengan kategorinya.
     */
    public static boolean isBarangIdMatchKategori(String id_barang, String id_kategori) {
        String prefix = getPrefixFromKategoriId(id_kategori);
        if (prefix.isEmpty() || id_barang == null) return false;
        String normalizedId = id_barang.trim().toUpperCase();
        return normalizedId.startsWith(prefix) && normalizedId.substring(prefix.length()).matches("\\d+");
    }

    /**
     * Method formatBarangId: Menggabungkan prefix dan nomor urut dengan padding nol.
     */
    public static String formatBarangId(String prefix, int number) {
        return prefix + String.format("%0" + BARANG_ID_NUMBER_WIDTH + "d", number);
    }

    /**
     * Method deleteBarang: Menghapus data barang secara permanen.
     */
    public static boolean deleteBarang(String id_barang) {
        String query = "DELETE FROM barang WHERE id_barang = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, id_barang);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Method getBarangStokMenipis: Mengambil daftar barang dengan stok di bawah batas.
     */
    public static List<Barang> getBarangStokMenipis(int batasStok, int limit) {
        List<Barang> listBarang = new ArrayList<>();
        String query = "SELECT * FROM barang WHERE stok <= ? ORDER BY stok ASC LIMIT ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, batasStok);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Barang barang = new Barang();
                barang.setIdBarang(rs.getString("id_barang"));
                barang.setNamaBarang(rs.getString("nama_barang"));
                barang.setStok(rs.getInt("stok"));
                listBarang.add(barang);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listBarang;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
