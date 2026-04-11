package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class koneksi {
    private static Connection mysqlkonek;

    public static Connection koneksiDB() throws SQLException {
        if (mysqlkonek == null || mysqlkonek.isClosed()) {
            try {
                String DB = "jdbc:mysql://localhost:3306/umkm"; // ganti dengan nama database Anda
                String user = "root"; // user database
                String pass = ""; // password database
                
                // DriverManager.getConnection otomatis mendeteksi driver jika library sudah di-load
                mysqlkonek = DriverManager.getConnection(DB, user, pass);
                System.out.println("Koneksi Database Berhasil!");
            } catch (SQLException e) {
                System.err.println("Koneksi Database Gagal: " + e.getMessage());
                throw e; // Tetap melempar error agar program utama tahu
            }
        }
        return mysqlkonek;
    }
}