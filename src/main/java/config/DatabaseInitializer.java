package config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseInitializer {

    // Konstruktor privat agar class ini tidak bisa diinstansiasi (Utility Class)
    private DatabaseInitializer() {
    }

    /**
     * ALUR UTAMA INISIALISASI DATABASE:
     */
    public static void initialize(Connection connection) throws SQLException {
        // Method ini dipanggil saat koneksi SQLite dibuka. Tujuannya memastikan database siap dipakai aplikasi.
        // 1. Cek apakah semua tabel utama sudah ada
        if (tableExists(connection, "user")
                && tableExists(connection, "kategori")
                && tableExists(connection, "barang")
                && tableExists(connection, "transaksi")
                && tableExists(connection, "detail_transaksi")
                && tableExists(connection, "pengeluaran")
                && tableExists(connection, "pengaturan_toko")) {
            
            // 2. Jika sudah ada, jalankan migrasi/update struktur (jika ada perubahan)
            applyCompatibilityMigrations(connection);
            return;
        }

        // 3. Jika tabel belum lengkap, mulai proses pembuatan database baru
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false); // Mematikan auto-commit untuk transaksi aman

        try (Statement statement = connection.createStatement()) {
            // Semua schema, index, dan seed data dibuat dalam satu transaksi agar tidak setengah jadi.
            // 4. Eksekusi semua perintah SQL pembuat tabel (Schema)
            for (String sql : getSchemaStatements()) {
                statement.execute(sql);
            }
            // 5. Simpan perubahan secara permanen (Commit)
            connection.commit();
        } catch (SQLException e) {
            // 6. Jika gagal, batalkan semua perubahan (Rollback)
            connection.rollback();
            throw e;
        } finally {
            // 7. Kembalikan pengaturan AutoCommit ke awal
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    /**
     * ALUR MATA RANTAI MIGRASI (Update Struktur):
     */
    private static void applyCompatibilityMigrations(Connection connection) throws SQLException {
        // Migrasi ini menjaga database lama tetap kompatibel tanpa menghapus data yang sudah ada.
        try (Statement statement = connection.createStatement()) {
            // 1. Pastikan tabel inti sudah dibuat (IF NOT EXISTS)
            for (String sql : getCoreSchemaStatements()) {
                statement.execute(sql);
            }

            // 2. Pastikan kolom-kolom baru tersedia (Update versi aplikasi)
            ensureColumnExists(connection, "user", "nama_lengkap", "TEXT NOT NULL DEFAULT 'Pengguna'");
            ensureColumnExists(connection, "barang", "satuan", "TEXT NOT NULL DEFAULT 'Pcs'");

            // 3. Buat Index untuk mempercepat proses pencarian data
            for (String sql : getIndexStatements()) {
                statement.execute(sql);
            }

            // 4. Masukkan data default toko jika masih kosong
            statement.executeUpdate(
                    "INSERT OR IGNORE INTO pengaturan_toko (id, nama_toko, nomor_telepon, alamat, email) " +
                            "VALUES (1, 'Toko Zikry', '-', '-', '-')"
            );

            // 5. Perbaiki data nama_lengkap user yang mungkin kosong
            statement.executeUpdate(
                    "UPDATE user " +
                            "SET nama_lengkap = CASE " +
                            "WHEN lower(role) = 'kasir' THEN 'Kasir Utama' " +
                            "WHEN lower(role) = 'pemilik' THEN 'Pemilik Toko' " +
                            "ELSE id_user END " +
                            "WHERE nama_lengkap IS NULL OR trim(nama_lengkap) = ''"
            );

            // 6. Perbaiki data satuan barang yang mungkin kosong
            statement.executeUpdate(
                    "UPDATE barang SET satuan = 'Pcs' " +
                            "WHERE satuan IS NULL OR trim(satuan) = ''"
            );

            statement.executeUpdate(
                    "UPDATE barang SET stok = 0 " +
                            "WHERE stok < 0"
            );
        }
    }

    /**
     * ALUR PENGECEKAN TABEL:
     * 1. Mencari nama tabel di sistem internal SQLite (sqlite_master)
     */
    private static boolean tableExists(Connection connection, String tableName) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type = 'table' AND name = '" + tableName + "'";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next(); // True jika tabel ditemukan
        }
    }

    /**
     * ALUR PENAMBAHAN KOLOM OTOMATIS:
     */
    private static void ensureColumnExists(Connection connection, String tableName, String columnName, String definition) throws SQLException {
        // 1. Ambil informasi kolom yang ada di tabel saat ini
        String sql = "PRAGMA table_info(" + tableName + ")";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                // 2. Jika kolom sudah ada, batalkan proses (Return)
                if (columnName.equalsIgnoreCase(resultSet.getString("name"))) {
                    return;
                }
            }
        }

        // 3. Jika kolom tidak ditemukan, jalankan perintah ALTER TABLE untuk menambahkannya
        try (Statement alterStatement = connection.createStatement()) {
            alterStatement.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }

    /**
     * MENGGABUNGKAN SEMUA SQL (Schema + Index + Seed Data)
     */
    private static List<String> getSchemaStatements() {
        // Urutan penting: tabel dibuat dulu, index setelahnya, lalu data awal dimasukkan.
        List<String> schemaStatements = new ArrayList<>(getCoreSchemaStatements());
        schemaStatements.addAll(getIndexStatements());
        schemaStatements.addAll(getSeedStatements());
        return schemaStatements;
    }

    /**
     * DAFTAR PERINTAH PEMBUATAN TABEL (CORE SCHEMA)
     */
    private static List<String> getCoreSchemaStatements() {
        // Bagian ini adalah struktur utama database. Jangan diubah tanpa menyesuaikan DAO dan data lama.
        return List.of(
                "CREATE TABLE IF NOT EXISTS user (" +
                        "id_user TEXT PRIMARY KEY, " +
                        "nama_lengkap TEXT NOT NULL, " +
                        "user_password TEXT NOT NULL, " +
                        "role TEXT NOT NULL CHECK (lower(role) IN ('kasir', 'pemilik'))" +
                        ")",
                "CREATE TABLE IF NOT EXISTS kategori (" +
                        "id_kategori TEXT PRIMARY KEY, " +
                        "nama_kategori TEXT NOT NULL UNIQUE" +
                        ")",
                "CREATE TABLE IF NOT EXISTS barang (" +
                        "id_barang TEXT PRIMARY KEY, " +
                        "nama_barang TEXT NOT NULL, " +
                        "id_kategori TEXT, " +
                        "stok INTEGER NOT NULL DEFAULT 0 CHECK (stok >= 0), " +
                        "satuan TEXT NOT NULL DEFAULT 'Pcs', " +
                        "harga_beli NUMERIC NOT NULL, " +
                        "harga_jual NUMERIC NOT NULL, " +
                        "FOREIGN KEY (id_kategori) REFERENCES kategori(id_kategori) ON DELETE SET NULL ON UPDATE CASCADE" +
                        ")",
                "CREATE TABLE IF NOT EXISTS transaksi (" +
                        "id_transaksi TEXT PRIMARY KEY, " +
                        "tgl_transaksi TEXT DEFAULT (datetime('now', 'localtime')), " +
                        "id_user TEXT, " +
                        "total NUMERIC NOT NULL DEFAULT 0, " +
                        "FOREIGN KEY (id_user) REFERENCES user(id_user) ON DELETE SET NULL ON UPDATE CASCADE" +
                        ")",
                "CREATE TABLE IF NOT EXISTS detail_transaksi (" +
                        "id_detail TEXT PRIMARY KEY, " +
                        "id_transaksi TEXT NOT NULL, " +
                        "id_barang TEXT, " +
                        "jumlah INTEGER NOT NULL, " +
                        "harga_satuan NUMERIC NOT NULL, " +
                        "subtotal NUMERIC GENERATED ALWAYS AS (jumlah * harga_satuan) STORED, " +
                        "FOREIGN KEY (id_transaksi) REFERENCES transaksi(id_transaksi) ON DELETE CASCADE ON UPDATE CASCADE, " +
                        "FOREIGN KEY (id_barang) REFERENCES barang(id_barang) ON DELETE SET NULL ON UPDATE CASCADE" +
                        ")",
                "CREATE TABLE IF NOT EXISTS pengeluaran (" +
                        "id_pengeluaran TEXT PRIMARY KEY, " +
                        "tgl_pengeluaran TEXT NOT NULL, " +
                        "nominal NUMERIC NOT NULL, " +
                        "jenis TEXT, " +
                        "id_user TEXT, " +
                        "FOREIGN KEY (id_user) REFERENCES user(id_user) ON DELETE SET NULL ON UPDATE CASCADE" +
                        ")",
                "CREATE TABLE IF NOT EXISTS pengaturan_toko (" +
                        "id INTEGER PRIMARY KEY CHECK (id = 1), " +
                        "nama_toko TEXT NOT NULL, " +
                        "nomor_telepon TEXT, " +
                        "alamat TEXT, " +
                        "email TEXT" +
                        ")"
        );
    }

    /**
     * DAFTAR PERINTAH PEMBUATAN INDEX (OPTIMASI KECEPATAN)
     */
    private static List<String> getIndexStatements() {
        // Index membantu pencarian/join lebih cepat, terutama untuk laporan dan detail transaksi.
        return List.of(
                "CREATE INDEX IF NOT EXISTS idx_barang_kategori ON barang(id_kategori)",
                "CREATE INDEX IF NOT EXISTS idx_transaksi_user ON transaksi(id_user)",
                "CREATE INDEX IF NOT EXISTS idx_detail_transaksi ON detail_transaksi(id_transaksi)",
                "CREATE INDEX IF NOT EXISTS idx_detail_barang ON detail_transaksi(id_barang)",
                "CREATE INDEX IF NOT EXISTS idx_pengeluaran_user ON pengeluaran(id_user)"
        );
    }

    /**
     * DAFTAR DATA AWAL (SEED DATA) SAAT DATABASE PERTAMA KALI DIBUAT
     */
    private static List<String> getSeedStatements() {
        // Seed data dipakai agar aplikasi langsung punya akun, kategori, barang contoh, dan pengaturan toko.
        return List.of(
                "INSERT OR IGNORE INTO user (id_user, nama_lengkap, user_password, role) VALUES ('KSR001', 'Kasir Utama', 'EZAK123', 'kasir')",
                "INSERT OR IGNORE INTO user (id_user, nama_lengkap, user_password, role) VALUES ('PMK001', 'Pemilik Toko', 'EZAK321', 'pemilik')",
                "INSERT OR IGNORE INTO kategori (id_kategori, nama_kategori) VALUES ('DLL000', 'LAIN LAIN')",
                "INSERT OR IGNORE INTO kategori (id_kategori, nama_kategori) VALUES ('ESK000', 'ES_KRIM')",
                "INSERT OR IGNORE INTO kategori (id_kategori, nama_kategori) VALUES ('MKM000', 'MAKANAN')",
                "INSERT OR IGNORE INTO kategori (id_kategori, nama_kategori) VALUES ('MIM000', 'MINUMAN')",
                "INSERT OR IGNORE INTO kategori (id_kategori, nama_kategori) VALUES ('PBR000', 'PEMBERSIH')",
                "INSERT OR IGNORE INTO kategori (id_kategori, nama_kategori) VALUES ('RKK000', 'ROKOK')",
                "INSERT OR IGNORE INTO kategori (id_kategori, nama_kategori) VALUES ('SBK000', 'SEMBAKO')",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('DLL001', 'BENSIN 1L', 'DLL000', 5, 'Liter', 10000.00, 12000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('ESK001', 'PADDLEPOP', 'ESK000', 10, 'Pcs', 5000.00, 6000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('ESK002', 'KOCNETTO COKLAT', 'ESK000', 29, 'Pcs', 9000.00, 10000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('ESK003', 'MAGNUM CLASSIS', 'ESK000', 20, 'Pcs', 18000.00, 22000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('ESK004', 'ICED MOCHI', 'ESK000', 10, 'Pcs', 3000.00, 4000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('ESK005', 'WALLS STROBERRY VANILA', 'ESK000', 0, 'Pcs', 4000.00, 5000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('MIM001', 'MINERAL', 'MIM000', 30, 'Liter', 8000.00, 9000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('MIM002', 'AQUA', 'MIM000', 30, 'Liter', 7000.00, 8000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('MIM003', 'KOPI GOLDA', 'MIM000', 30, 'Pcs', 2500.00, 3000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('MIM004', 'UTRAMILK', 'MIM000', 20, 'Pcs', 4000.00, 5000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('MIM005', 'TEH BOTOL SOSRO LESS SUGAR', 'MIM000', 10, 'Pcs', 3000.00, 4990.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('MKM001', 'MIE GORENG', 'MKM000', 30, 'Pcs', 3000.00, 3500.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('MKM002', 'MIE SEDAP GORANG', 'MKM000', 30, 'Pcs', 3000.00, 3500.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('MKM003', 'BISKUIT ROMAH', 'MKM000', 30, 'Pcs', 8000.00, 10000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('MKM004', 'OREO', 'MKM000', 20, 'Pcs', 1500.00, 2000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('MKM005', 'BETTER', 'MKM000', 12, 'Pcs', 1000.00, 2000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('PBR001', 'SUNLIGHT', 'PBR000', 30, 'Pcs', 4000.00, 5000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('PBR002', 'PESODENT', 'PBR000', 30, 'Pcs', 6000.00, 7000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('PBR005', 'CLEAR', 'PBR000', 12, 'Pcs', 500.00, 1000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('RKK001', 'G.A', 'RKK000', 12, 'Pcs', 18000.00, 20000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('RKK002', 'URBANMILK', 'RKK000', 12, 'Pcs', 25000.00, 30000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('RKK003', 'BRAND JATI', 'RKK000', 12, 'Pcs', 19000.00, 20000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('RKK004', 'ESSE DOUBLE CLIK', 'RKK000', 15, 'Pcs', 35000.00, 40000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('RKK005', 'D''TE', 'RKK000', 14, 'Pcs', 19000.00, 20000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('SBK001', 'TELUR', 'SBK000', 30, 'Butir', 2000.00, 2500.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('SBK002', 'BERAS DIAMOND', 'SBK000', 30, 'Kg', 300000.00, 346000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('SBK003', 'GULA PASIR 1 KG', 'SBK000', 20, 'Kg', 16000.00, 18000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('SBK004', 'MINYAK GORENG KITA', 'SBK000', 10, 'Liter', 12000.00, 14000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('SBK005', 'TEPUNG TERIGU', 'SBK000', 30, 'Kg', 4000.00, 5000.00)",
                "INSERT OR IGNORE INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES ('SBK006', 'GARAM', 'SBK000', 12, 'Pcs', 4000.00, 5000.00)",
                "INSERT OR IGNORE INTO transaksi (id_transaksi, tgl_transaksi, id_user, total) VALUES ('TRK001', '2026-04-01 22:13:37', 'KSR001', 300000.00)",
                "INSERT OR IGNORE INTO detail_transaksi (id_detail, id_transaksi, id_barang, jumlah, harga_satuan) VALUES ('DTL001', 'TRK001', 'ESK005', 60, 5000.00)",
                "INSERT OR IGNORE INTO pengeluaran (id_pengeluaran, tgl_pengeluaran, nominal, jenis, id_user) VALUES ('PGN001', '2026-04-01', 100000.00, 'PLN', 'PMK001')",
                "INSERT OR IGNORE INTO pengeluaran (id_pengeluaran, tgl_pengeluaran, nominal, jenis, id_user) VALUES ('PGN002', '2026-04-01', 326000.00, 'AIR', 'PMK001')",
                "INSERT OR IGNORE INTO pengeluaran (id_pengeluaran, tgl_pengeluaran, nominal, jenis, id_user) VALUES ('PGN003', '2026-04-01', 30000.00, 'PLASTIK KATONGAN', 'PMK001')",
                "INSERT OR IGNORE INTO pengaturan_toko (id, nama_toko, nomor_telepon, alamat, email) VALUES (1, 'Toko Zikry', '081234567890', 'Alamat belum diatur', 'tokozikry@example.com')"
        );
    }
}
