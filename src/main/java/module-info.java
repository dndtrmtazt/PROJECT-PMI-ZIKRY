// Konfigurasi module Java yang mendeklarasikan dependency dan akses package.
module com.example.pmitokozikry {
    // Dependency utama aplikasi JavaFX, database, dan export dokumen.
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires java.desktop;
    requires org.xerial.sqlitejdbc;
    requires org.apache.pdfbox;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    // Package yang boleh diakses dari luar module.
    exports app;
    exports model; // Pastikan ini ada agar module lain bisa lihat class Barang
    exports Controller;
    exports util;

    // Package yang perlu dibuka agar FXMLLoader bisa mengakses controller/model lewat reflection.
    opens app to javafx.fxml;
    opens Controller to javafx.fxml;

    // --- INI KUNCINYA: Tambahkan javafx.base di sini ---
    opens model to javafx.fxml, javafx.base;

    opens config to javafx.fxml;
}
