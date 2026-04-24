package config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class koneksi {
    private static final Path DATABASE_PATH = Path.of(
            System.getProperty("user.dir"),
            "database",
            "umkm.db"
    );

    public static Connection koneksiDB() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            Files.createDirectories(DATABASE_PATH.getParent());
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite tidak ditemukan.", e);
        } catch (Exception e) {
            throw new SQLException("Gagal menyiapkan folder database SQLite.", e);
        }

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_PATH.toAbsolutePath());

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        DatabaseInitializer.initialize(connection);
        System.out.println("Koneksi SQLite berhasil: " + DATABASE_PATH.toAbsolutePath());
        return connection;
    }
}
