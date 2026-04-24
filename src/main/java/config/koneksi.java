package config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class koneksi {
    private static final Path DATABASE_PATH = resolveDatabasePath();

    private static Path resolveDatabasePath() {
        Path currentPath = Paths.get("").toAbsolutePath().normalize();

        for (Path path = currentPath; path != null; path = path.getParent()) {
            Path databaseDirectory = path.resolve("database");
            if (Files.exists(path.resolve("pom.xml")) || Files.exists(databaseDirectory)) {
                return databaseDirectory.resolve("umkm.db");
            }
        }

        return currentPath.resolve("database").resolve("umkm.db");
    }

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
