package util;

import model.Toko;
import model.Transaksi;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class StrukPdfUtil {
    private static final Locale LOCALE_ID = new Locale("id", "ID");
    private static final NumberFormat RUPIAH_FORMAT = NumberFormat.getCurrencyInstance(LOCALE_ID);
    private static final DateTimeFormatter TRANSACTION_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", LOCALE_ID);

    private static final float RECEIPT_WIDTH = 226.77f; // 80mm in PDF points
    private static final float MARGIN = 12f;
    private static final float CONTENT_WIDTH = RECEIPT_WIDTH - (MARGIN * 2);
    private static final float LINE_HEIGHT = 10.5f;
    private static final float NORMAL_FONT_SIZE = 8.5f;
    private static final float SMALL_FONT_SIZE = 7.5f;
    private static final float TITLE_FONT_SIZE = 11f;
    private static final float LABEL_WIDTH = 45f;
    private static final String SEPARATOR_TEXT = "----------------------------------------";

    private StrukPdfUtil() {
    }

    public static void exportToPdf(File file, Toko toko, Transaksi transaksi, List<StrukItem> items,
                                   String kasirName, double nominalBayar, double kembalian) throws IOException {
        if (file == null || transaksi == null || items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Data struk tidak lengkap.");
        }

        RUPIAH_FORMAT.setMaximumFractionDigits(0);
        RUPIAH_FORMAT.setMinimumFractionDigits(0);

        float pageHeight = calculateReceiptHeight(toko, items);
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(RECEIPT_WIDTH, pageHeight));
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float y = pageHeight - MARGIN;
                y = drawHeader(content, toko, y);
                y = drawTransactionInfo(content, transaksi, kasirName, y);
                y = drawItems(content, items, y);
                y = drawPaymentSummary(content, transaksi.getTotal(), nominalBayar, kembalian, y);
                drawFooter(content, y);
            }

            document.save(file);
        }
    }

    private static float drawHeader(PDPageContentStream content, Toko toko, float y) throws IOException {
        String storeName = safeText(toko != null ? toko.getNamaToko() : null, "Toko Zikry");
        drawCentered(content, "TOKO ZIKRY", PDType1Font.HELVETICA_BOLD, TITLE_FONT_SIZE, y);
        y -= LINE_HEIGHT + 1;
        drawCentered(content, storeName, PDType1Font.HELVETICA, NORMAL_FONT_SIZE, y);
        y -= LINE_HEIGHT + 2;

        String alamat = getPrintableAddress(toko, storeName);
        String telepon = toko != null ? toko.getNomorTelepon() : null;
        y = drawCenteredWrapped(content, alamat, SMALL_FONT_SIZE, y, CONTENT_WIDTH);
        y = drawCenteredWrapped(content, telepon, SMALL_FONT_SIZE, y, CONTENT_WIDTH);

        y -= 4;
        drawSeparator(content, y);
        return y - LINE_HEIGHT - 2;
    }

    private static float drawTransactionInfo(PDPageContentStream content, Transaksi transaksi,
                                             String kasirName, float y) throws IOException {
        y = drawLabelValue(content, "No", safeText(transaksi.getIdTransaksi(), "-"), y);
        String transactionTime = transaksi.getTglTransaksi() == null
                ? "-"
                : transaksi.getTglTransaksi().format(TRANSACTION_TIME_FORMAT);
        y = drawLabelValue(content, "Tanggal", transactionTime, y);
        y = drawLabelValue(content, "Kasir", safeText(kasirName, "-"), y);
        y -= 3;
        drawSeparator(content, y);
        return y - LINE_HEIGHT - 2;
    }

    private static float drawItems(PDPageContentStream content, List<StrukItem> items, float y) throws IOException {
        for (StrukItem item : items) {
            List<String> nameLines = wrapText(safeText(item.getNamaBarang(), "-"), PDType1Font.HELVETICA_BOLD,
                    NORMAL_FONT_SIZE, CONTENT_WIDTH);
            for (String line : nameLines) {
                drawText(content, line, MARGIN, y, PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
                y -= LINE_HEIGHT;
            }

            String qtyPrice = item.getJumlah() + " x " + formatRupiah(item.getHargaSatuan());
            drawText(content, qtyPrice, MARGIN + 8, y, PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
            drawRight(content, formatRupiah(item.getSubtotal()), RECEIPT_WIDTH - MARGIN, y,
                    PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
            y -= LINE_HEIGHT + 4;
        }

        drawSeparator(content, y);
        return y - LINE_HEIGHT - 2;
    }

    private static float drawPaymentSummary(PDPageContentStream content, double total, double nominalBayar,
                                            double kembalian, float y) throws IOException {
        y = drawSummaryValue(content, "TOTAL", formatRupiah(total), y);
        y = drawSummaryValue(content, "BAYAR", formatRupiah(nominalBayar), y);
        y = drawSummaryValue(content, "KEMBALI", formatRupiah(kembalian), y);
        y -= 3;
        drawSeparator(content, y);
        return y - LINE_HEIGHT - 2;
    }

    private static void drawFooter(PDPageContentStream content, float y) throws IOException {
        drawCentered(content, "Terima kasih telah berbelanja", PDType1Font.HELVETICA, SMALL_FONT_SIZE, y);
        y -= LINE_HEIGHT;
        drawCenteredWrapped(content, "Barang yang sudah dibeli tidak dapat dikembalikan",
                SMALL_FONT_SIZE, y, CONTENT_WIDTH);
    }

    private static float drawKeyValue(PDPageContentStream content, String key, String value, float y)
            throws IOException {
        return drawKeyValue(content, key, value, y, false);
    }

    private static float drawKeyValue(PDPageContentStream content, String key, String value, float y, boolean bold)
            throws IOException {
        PDType1Font font = bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA;
        drawText(content, key, MARGIN, y, font, NORMAL_FONT_SIZE);
        drawRight(content, value, RECEIPT_WIDTH - MARGIN, y, font, NORMAL_FONT_SIZE);
        return y - LINE_HEIGHT;
    }

    private static float drawLabelValue(PDPageContentStream content, String key, String value, float y)
            throws IOException {
        drawText(content, key, MARGIN, y, PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
        drawText(content, ":", MARGIN + LABEL_WIDTH, y, PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
        drawText(content, value, MARGIN + LABEL_WIDTH + 8, y, PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
        return y - LINE_HEIGHT;
    }

    private static float drawSummaryValue(PDPageContentStream content, String key, String value, float y)
            throws IOException {
        drawText(content, key, MARGIN, y, PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
        drawText(content, ":", MARGIN + LABEL_WIDTH, y, PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
        drawRight(content, value, RECEIPT_WIDTH - MARGIN, y, PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
        return y - LINE_HEIGHT;
    }

    private static void drawSeparator(PDPageContentStream content, float y) throws IOException {
        drawText(content, SEPARATOR_TEXT, MARGIN, y, PDType1Font.COURIER, SMALL_FONT_SIZE);
    }

    private static void drawText(PDPageContentStream content, String text, float x, float y,
                                 PDType1Font font, float fontSize) throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(safePdfText(text));
        content.endText();
    }

    private static void drawRight(PDPageContentStream content, String text, float rightX, float y,
                                  PDType1Font font, float fontSize) throws IOException {
        float width = stringWidth(text, font, fontSize);
        drawText(content, text, rightX - width, y, font, fontSize);
    }

    private static void drawCentered(PDPageContentStream content, String text, PDType1Font font,
                                     float fontSize, float y) throws IOException {
        float width = stringWidth(text, font, fontSize);
        drawText(content, text, (RECEIPT_WIDTH - width) / 2, y, font, fontSize);
    }

    private static float drawCenteredWrapped(PDPageContentStream content, String text, float fontSize,
                                             float y, float maxWidth) throws IOException {
        if (isBlank(text)) {
            return y;
        }

        List<String> lines = wrapText(text, PDType1Font.HELVETICA, fontSize, maxWidth);
        for (String line : lines) {
            drawCentered(content, line, PDType1Font.HELVETICA, fontSize, y);
            y -= LINE_HEIGHT;
        }
        return y;
    }

    private static List<String> wrapText(String text, PDType1Font font, float fontSize, float maxWidth)
            throws IOException {
        List<String> lines = new ArrayList<>();
        if (isBlank(text)) {
            return lines;
        }

        String[] words = safePdfText(text).split("\\s+");
        String current = "";
        for (String word : words) {
            word = shortenToWidth(word, font, fontSize, maxWidth);
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (stringWidth(candidate, font, fontSize) <= maxWidth) {
                current = candidate;
            } else {
                if (!current.isEmpty()) {
                    lines.add(current);
                }
                current = word;
            }
        }

        if (!current.isEmpty()) {
            lines.add(current);
        }
        return lines;
    }

    private static String shortenToWidth(String text, PDType1Font font, float fontSize, float maxWidth)
            throws IOException {
        if (stringWidth(text, font, fontSize) <= maxWidth) {
            return text;
        }

        String suffix = "...";
        String shortened = text;
        while (shortened.length() > 1 && stringWidth(shortened + suffix, font, fontSize) > maxWidth) {
            shortened = shortened.substring(0, shortened.length() - 1);
        }
        return shortened + suffix;
    }

    private static float calculateReceiptHeight(Toko toko, List<StrukItem> items) throws IOException {
        float height = 242f;
        String storeName = safeText(toko != null ? toko.getNamaToko() : null, "Toko Zikry");
        height += countWrappedLines(getPrintableAddress(toko, storeName), SMALL_FONT_SIZE, CONTENT_WIDTH) * LINE_HEIGHT;
        height += countWrappedLines(toko != null ? toko.getNomorTelepon() : null, SMALL_FONT_SIZE, CONTENT_WIDTH) * LINE_HEIGHT;

        for (StrukItem item : items) {
            int itemNameLines = Math.max(1, countWrappedLines(item.getNamaBarang(), NORMAL_FONT_SIZE, CONTENT_WIDTH));
            height += (itemNameLines + 1) * LINE_HEIGHT + 3;
        }
        return Math.max(height, 330f);
    }

    private static int countWrappedLines(String text, float fontSize, float maxWidth) throws IOException {
        return wrapText(text, PDType1Font.HELVETICA, fontSize, maxWidth).size();
    }

    private static String formatRupiah(double value) {
        return RUPIAH_FORMAT.format(value).replace("Rp", "Rp").replace(",00", "");
    }

    private static String getPrintableAddress(Toko toko, String storeName) {
        if (toko == null || isBlank(toko.getAlamat())) {
            return "Samarinda";
        }

        String alamat = toko.getAlamat().trim();
        if ("alamat belum diatur".equalsIgnoreCase(alamat) || alamat.equalsIgnoreCase(safeText(storeName, ""))) {
            return "Samarinda";
        }
        return alamat;
    }

    private static float stringWidth(String text, PDType1Font font, float fontSize) throws IOException {
        return font.getStringWidth(safePdfText(text)) / 1000f * fontSize;
    }

    private static String safeText(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String safePdfText(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^\\x20-\\x7E]", " ");
    }

    public static final class StrukItem {
        private final String namaBarang;
        private final int jumlah;
        private final double hargaSatuan;
        private final double subtotal;

        public StrukItem(String namaBarang, int jumlah, double hargaSatuan, double subtotal) {
            this.namaBarang = namaBarang;
            this.jumlah = jumlah;
            this.hargaSatuan = hargaSatuan;
            this.subtotal = subtotal;
        }

        public String getNamaBarang() {
            return namaBarang;
        }

        public int getJumlah() {
            return jumlah;
        }

        public double getHargaSatuan() {
            return hargaSatuan;
        }

        public double getSubtotal() {
            return subtotal;
        }
    }
}
