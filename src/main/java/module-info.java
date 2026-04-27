module com.example.pmitokozikry {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires java.desktop;
    requires org.xerial.sqlitejdbc;
    requires org.apache.pdfbox;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    exports app;
    exports model; // Pastikan ini ada agar module lain bisa lihat class Barang
    exports Controller;
    exports util;

    opens app to javafx.fxml;
    opens Controller to javafx.fxml;

    // --- INI KUNCINYA: Tambahkan javafx.base di sini ---
    opens model to javafx.fxml, javafx.base;

    opens config to javafx.fxml;
}
