package config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class koneksi {
    // [0] Inisialisasi awal: Menentukan lokasi file database saat class dipanggil
    private static final Path DATABASE_PATH = tentukanLokasiDatabase();

    /**
     * ALUR PENCARIAN LOKASI DATABASE:
     */
    private static Path tentukanLokasiDatabase() {
        // 1. Ambil path direktori tempat aplikasi dijalankan saat ini
        Path currentPath = Paths.get("").toAbsolutePath().normalize();

        // 2. Lakukan looping ke atas (parent directory) untuk mencari folder proyek
        for (Path path = currentPath; path != null; path = path.getParent()) {
            Path databaseDirectory = path.resolve("database");
            // 3. Jika ditemukan file 'pom.xml' atau folder 'database', maka itu adalah root proyek
            if (Files.exists(path.resolve("pom.xml")) || Files.exists(databaseDirectory)) {
                return databaseDirectory.resolve("umkm.db");
            }
        }

        // 4. Jika tidak ditemukan, buat folder 'database' di lokasi saat ini sebagai fallback
        return currentPath.resolve("database").resolve("umkm.db");
    }

    /**
     * ALUR PROSES KONEKSI DATABASE (Step-by-Step):
     */
    public static Connection koneksiDB() throws SQLException {
        try {
            // 1. Me-load Driver JDBC SQLite ke dalam memori aplikasi
            Class.forName("org.sqlite.JDBC");
            
            // 2. Cek dan buat folder 'database' secara fisik jika belum ada di komputer
            Files.createDirectories(DATABASE_PATH.getParent());
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite tidak ditemukan.", e);
        } catch (Exception e) {
            throw new SQLException("Gagal menyiapkan folder database SQLite.", e);
        }

        // 3. Membuka koneksi langsung ke file database 'umkm.db'
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_PATH.toAbsolutePath());

        // 4. Mengaktifkan fitur Foreign Key (Penting: Agar relasi antar tabel berfungsi di SQLite)
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        // 5. Menjalankan inisialisasi tabel (membuat tabel jika database masih kosong)
        // DatabaseInitializer juga mengisi seed data awal, jadi aplikasi siap dipakai saat database baru dibuat.
        DatabaseInitializer.initialize(connection);
        
        // 6. Memberikan info bahwa koneksi telah berhasil terhubung
        System.out.println("Koneksi SQLite berhasil: " + DATABASE_PATH.toAbsolutePath());
        
        // 7. Mengembalikan objek koneksi untuk digunakan oleh DAO
        return connection;
    }
}
