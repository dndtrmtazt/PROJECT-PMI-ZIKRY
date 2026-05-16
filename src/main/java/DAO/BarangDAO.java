package DAO;

import config.koneksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Barang;

// DAO untuk data barang, stok, dan generator ID barang.
public class BarangDAO {
    // Lebar angka ID barang, misalnya 3 menghasilkan ESK001.
    private static final int BARANG_ID_NUMBER_WIDTH = 3;


    // Mengambil semua barang untuk halaman data barang dan transaksi kasir.
    public static List<Barang> getAllBarang() {
        List<Barang> listBarang = new ArrayList<>();
        String query = "SELECT id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual FROM barang";
        try (Connection conn = koneksi.koneksiDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                listBarang.add(mapBarang(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all barang: " + e.getMessage());
            e.printStackTrace();
        }
        return listBarang;
    }

    // Mengambil satu barang berdasarkan ID, biasanya untuk validasi stok terbaru.
    public static Barang getBarangById(String idBarang) {
        String query = "SELECT id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual FROM barang WHERE id_barang = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idBarang);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBarang(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting barang by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Mengambil barang berdasarkan kategori tertentu.
    public static List<Barang> getBarangByKategori(String idKategori) {
        List<Barang> listBarang = new ArrayList<>();
        String query = "SELECT id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual FROM barang WHERE id_kategori = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idKategori);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listBarang.add(mapBarang(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting barang by kategori: " + e.getMessage());
            e.printStackTrace();
        }
        return listBarang;
    }

    // Menyimpan barang baru ke database.
    public static boolean insertBarang(Barang barang) {
        String query = "INSERT INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, barang.getIdBarang());
            ps.setString(2, barang.getNamaBarang());
            ps.setString(3, barang.getIdKategori());
            ps.setInt(4, barang.getStok());
            ps.setString(5, isBlank(barang.getSatuan()) ? "Pcs" : barang.getSatuan());
            ps.setDouble(6, barang.getHargaBeli());
            ps.setDouble(7, barang.getHargaJual());
            
            int result = ps.executeUpdate();
            System.out.println("Barang inserted: " + barang.getIdBarang());
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting barang: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Mengubah data barang tanpa mengganti ID barang.
    public static boolean updateBarang(Barang barang) {
        String query = "UPDATE barang SET nama_barang = ?, id_kategori = ?, stok = ?, satuan = ?, harga_beli = ?, harga_jual = ? WHERE id_barang = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, barang.getNamaBarang());
            ps.setString(2, barang.getIdKategori());
            ps.setInt(3, barang.getStok());
            ps.setString(4, isBlank(barang.getSatuan()) ? "Pcs" : barang.getSatuan());
            ps.setDouble(5, barang.getHargaBeli());
            ps.setDouble(6, barang.getHargaJual());
            ps.setString(7, barang.getIdBarang());
            
            System.out.println("Barang updated: " + barang.getIdBarang());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating barang: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Mengurangi stok setelah transaksi, dengan syarat stok masih cukup.
    public static boolean reduceStok(String idBarang, int jumlah) {
        if (jumlah <= 0) {
            return false;
        }

        String query = "UPDATE barang SET stok = stok - ? WHERE id_barang = ? AND stok >= ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, jumlah);
            ps.setString(2, idBarang);
            ps.setInt(3, jumlah);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error reducing stok: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Membuat ID barang berikutnya berdasarkan prefix kategori, misalnya ESK005 menjadi ESK006.
    public static String getNextBarangId(String prefix) {
        String query = "SELECT id_barang FROM barang WHERE id_barang LIKE ? ORDER BY id_barang DESC LIMIT 1";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, prefix + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String lastId = rs.getString("id_barang");
                // Ambil bagian angka setelah prefix, misalnya ESK005 menjadi 005.
                String numericPart = lastId.substring(prefix.length());
                try {
                    int nextNum = Integer.parseInt(numericPart) + 1;
                    return formatBarangId(prefix, nextNum);
                } catch (NumberFormatException e) {
                    return formatBarangId(prefix, 1);
                }
            } else {
                return formatBarangId(prefix, 1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting next barang id: " + e.getMessage());
            e.printStackTrace();
        }
        return formatBarangId(prefix, 1);
    }

    // Mengambil 3 huruf awal dari ID kategori sebagai prefix ID barang.
    public static String getPrefixFromKategoriId(String idKategori) {
        if (idKategori == null) {
            return "";
        }

        String trimmedId = idKategori.trim().toUpperCase();
        if (trimmedId.length() < 3) {
            return trimmedId;
        }

        return trimmedId.substring(0, 3);
    }

    // Memastikan ID barang cocok dengan prefix kategori yang dipilih.
    public static boolean isBarangIdMatchKategori(String idBarang, String idKategori) {
        String prefix = getPrefixFromKategoriId(idKategori);
        if (prefix.isEmpty() || idBarang == null) {
            return false;
        }

        String normalizedIdBarang = idBarang.trim().toUpperCase();
        if (!normalizedIdBarang.startsWith(prefix)) {
            return false;
        }

        String numericPart = normalizedIdBarang.substring(prefix.length());
        return numericPart.matches("\\d{" + BARANG_ID_NUMBER_WIDTH + ",}");
    }

    // Menggabungkan prefix dan angka menjadi format ID barang yang rapi.
    public static String formatBarangId(String prefix, int number) {
        return prefix + String.format("%0" + BARANG_ID_NUMBER_WIDTH + "d", number);
    }

    // Menghapus barang berdasarkan ID.
    public static boolean deleteBarang(String idBarang) {
        String query = "DELETE FROM barang WHERE id_barang = ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idBarang);
            
            System.out.println("Barang deleted: " + idBarang);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting barang: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Mengambil daftar barang yang stoknya hampir habis untuk dashboard.
    public static List<Barang> getBarangStokMenipis(int batasStok, int limit) {
        List<Barang> listBarang = new ArrayList<>();
        String query = "SELECT id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual " +
                "FROM barang WHERE stok <= ? ORDER BY stok ASC, nama_barang ASC LIMIT ?";
        try (Connection conn = koneksi.koneksiDB();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, batasStok);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listBarang.add(mapBarang(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting barang stok menipis: " + e.getMessage());
            e.printStackTrace();
        }
        return listBarang;
    }

    // Mengubah ResultSet barang menjadi objek Barang.
    private static Barang mapBarang(ResultSet rs) throws SQLException {
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

    // Mengecek teks kosong agar nilai default bisa dipakai.
    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
