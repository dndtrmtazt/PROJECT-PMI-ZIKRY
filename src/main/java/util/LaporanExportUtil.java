package util;

import model.Laporan;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

// Utility untuk export laporan penjualan ke PDF dan Excel.
public final class LaporanExportUtil {
    private static final String REPORT_TITLE = "Laporan Penjualan Toko Zikry";
    private static final String STORE_NAME = "Toko Zikry";
    private static final Locale LOCALE_ID = new Locale("id", "ID");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(LOCALE_ID);
    private static final DateTimeFormatter PRINT_TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", LOCALE_ID);
    private static final DateTimeFormatter SIGN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy", LOCALE_ID);
    private static final DateTimeFormatter REPORT_NUMBER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
    private static final float PDF_MARGIN = 42f;
    private static final float PDF_ROW_HEIGHT = 26f;
    private static final float PDF_SIGNATURE_HEIGHT = 96f;

    private LaporanExportUtil() {
    }

    // Membuat file PDF laporan dari data yang sedang tampil di halaman laporan.
    public static void exportToPdf(File file, List<Laporan> data, String periode) throws IOException {
        CURRENCY_FORMAT.setMaximumFractionDigits(0);
        CURRENCY_FORMAT.setMinimumFractionDigits(0);
        // Ringkasan dihitung dari data tabel yang sedang ditampilkan, lalu dipakai di header laporan.
        ReportSummary summary = calculateSummary(data);
        LocalDateTime printTime = LocalDateTime.now();
        String printedAt = printTime.format(PRINT_TIME_FORMAT);
        String signDate = printTime.format(SIGN_DATE_FORMAT);
        String reportNumber = "LP-" + printTime.format(REPORT_NUMBER_FORMAT);

        try (PDDocument document = new PDDocument()) {
            PDPage page = createLandscapePage();
            document.addPage(page);
            PDImageXObject logo = loadPdfLogo(document);

            PDPageContentStream content = new PDPageContentStream(document, page);
            float y = drawPdfHeader(content, page, logo, periode, printedAt, reportNumber);
            y = drawPdfSummary(content, y, summary);
            y -= 18;
            y = drawPdfTableHeader(content, y);

            for (int i = 0; i < data.size(); i++) {
                // Jika halaman hampir penuh, lanjutkan tabel ke halaman PDF baru.
                if (y < PDF_MARGIN + 42) {
                    drawPdfFooter(content, page, printedAt);
                    content.close();

                    page = createLandscapePage();
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    y = drawPdfHeader(content, page, logo, periode, printedAt, reportNumber);
                    y = drawPdfTableHeader(content, y - 8);
                }

                drawPdfTableRow(content, y, data.get(i), i % 2 == 0);
                y -= PDF_ROW_HEIGHT;
            }

            if (y < PDF_MARGIN + PDF_SIGNATURE_HEIGHT + PDF_ROW_HEIGHT) {
                drawPdfFooter(content, page, printedAt);
                content.close();

                page = createLandscapePage();
                document.addPage(page);
                content = new PDPageContentStream(document, page);
                y = drawPdfHeader(content, page, logo, periode, printedAt, reportNumber);
                y = drawPdfTableHeader(content, y - 8);
            }

            drawPdfTotalRow(content, y, summary);
            y -= PDF_ROW_HEIGHT;
            drawPdfSignature(content, page, y - 18, signDate);

            drawPdfFooter(content, page, printedAt);
            content.close();
            document.save(file);
        }
    }

    // Membuat file Excel laporan lengkap dengan styling dan ringkasan.
    public static void exportToExcel(File file, List<Laporan> data, String periode) throws IOException {
        // Export Excel memakai data yang sama dengan PDF agar angka laporan tetap konsisten.
        ReportSummary summary = calculateSummary(data);
        LocalDateTime printTime = LocalDateTime.now();
        String printedAt = printTime.format(PRINT_TIME_FORMAT);
        String reportNumber = "LP-" + printTime.format(REPORT_NUMBER_FORMAT);

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream outputStream = new FileOutputStream(file)) {
            Sheet sheet = workbook.createSheet("Laporan Penjualan");

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle metaStyle = createMetaStyle(workbook);
            CellStyle metaLabelStyle = createMetaLabelStyle(workbook);
            CellStyle summaryLabelStyle = createSummaryLabelStyle(workbook);
            CellStyle summaryValueStyle = createCurrencyStyle(workbook, true);
            CellStyle summaryNumberStyle = createSummaryNumberStyle(workbook);
            CellStyle tableHeaderStyle = createTableHeaderStyle(workbook);
            CellStyle dateStyle = createBorderedStyle(workbook, false);
            CellStyle currencyStyle = createCurrencyStyle(workbook, false);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle totalLabelStyle = createTotalLabelStyle(workbook);
            CellStyle totalCurrencyStyle = createTotalCurrencyStyle(workbook);
            CellStyle totalNumberStyle = createTotalNumberStyle(workbook);
            CellStyle footerStyle = createMetaStyle(workbook);

            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(28);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(REPORT_TITLE);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

            createLabelValueRow(sheet, 2, "Periode", periode, metaLabelStyle, metaStyle);
            createLabelValueRow(sheet, 3, "Tanggal Cetak", printedAt, metaLabelStyle, metaStyle);
            createLabelValueRow(sheet, 4, "Nama Toko", STORE_NAME, metaLabelStyle, metaStyle);
            createLabelValueRow(sheet, 5, "No. Laporan", reportNumber, metaLabelStyle, metaStyle);

            Row summaryHeader = sheet.createRow(7);
            summaryHeader.setHeightInPoints(24);
            summaryHeader.createCell(0).setCellValue("Total Penjualan");
            summaryHeader.createCell(1).setCellValue("Total Pengeluaran");
            summaryHeader.createCell(2).setCellValue("Jumlah Transaksi");
            for (int i = 0; i <= 2; i++) {
                summaryHeader.getCell(i).setCellStyle(summaryLabelStyle);
            }

            Row summaryValue = sheet.createRow(8);
            summaryValue.setHeightInPoints(26);
            setNumericCell(summaryValue, 0, summary.totalPenjualan, summaryValueStyle);
            setNumericCell(summaryValue, 1, summary.totalPengeluaran, summaryValueStyle);
            setNumericCell(summaryValue, 2, summary.jumlahTransaksi, summaryNumberStyle);

            Row noteRow = sheet.createRow(10);
            Cell noteCell = noteRow.createCell(0);
            noteCell.setCellValue("Ringkasan berdasarkan data yang sedang ditampilkan pada tabel laporan.");
            noteCell.setCellStyle(metaStyle);
            sheet.addMergedRegion(new CellRangeAddress(10, 10, 0, 3));

            int tableHeaderIndex = 12;
            Row tableHeader = sheet.createRow(tableHeaderIndex);
            tableHeader.setHeightInPoints(24);
            String[] headers = {"Tanggal", "Total Penjualan", "Total Pengeluaran", "Jumlah Transaksi"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = tableHeader.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(tableHeaderStyle);
            }
            sheet.createFreezePane(0, tableHeaderIndex + 1);
            sheet.setAutoFilter(new CellRangeAddress(tableHeaderIndex, tableHeaderIndex + data.size(), 0, headers.length - 1));

            int rowIndex = tableHeaderIndex + 1;
            for (Laporan laporan : data) {
                Row row = sheet.createRow(rowIndex++);
                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(laporan.getTanggal());
                dateCell.setCellStyle(dateStyle);
                setNumericCell(row, 1, laporan.getTotalPenjualan(), currencyStyle);
                setNumericCell(row, 2, laporan.getTotalPengeluaran(), currencyStyle);
                setNumericCell(row, 3, laporan.getJumlahTransaksi(), numberStyle);
            }

            Row totalRow = sheet.createRow(rowIndex++);
            totalRow.setHeightInPoints(24);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("TOTAL");
            totalLabelCell.setCellStyle(totalLabelStyle);
            setNumericCell(totalRow, 1, summary.totalPenjualan, totalCurrencyStyle);
            setNumericCell(totalRow, 2, summary.totalPengeluaran, totalCurrencyStyle);
            setNumericCell(totalRow, 3, summary.jumlahTransaksi, totalNumberStyle);

            Row footerRow = sheet.createRow(rowIndex + 2);
            Cell footerCell = footerRow.createCell(0);
            footerCell.setCellValue("Dicetak oleh " + STORE_NAME + " pada " + printedAt);
            footerCell.setCellStyle(footerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIndex + 2, rowIndex + 2, 0, 3));

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.min(Math.max(currentWidth + 900, 4200), 9000));
            }

            workbook.write(outputStream);
        }
    }

    // Membuat halaman PDF landscape agar tabel laporan muat.
    private static PDPage createLandscapePage() {
        return new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
    }

    // Menggambar judul, logo, metadata, dan periode laporan di PDF.
    private static float drawPdfHeader(PDPageContentStream content, PDPage page, PDImageXObject logo,
                                       String periode, String printedAt, String reportNumber) throws IOException {
        // Header PDF berisi identitas laporan, periode, nomor laporan, dan waktu cetak.
        float width = page.getMediaBox().getWidth();
        float height = page.getMediaBox().getHeight();
        content.setNonStrokingColor(new Color(74, 118, 168));
        content.addRect(0, height - 86, width, 86);
        content.fill();

        float textX = PDF_MARGIN;
        if (logo != null) {
            float logoBoxSize = 48f;
            float logoX = PDF_MARGIN;
            float logoY = height - 67f;
            content.setNonStrokingColor(Color.WHITE);
            content.addRect(logoX, logoY, logoBoxSize, logoBoxSize);
            content.fill();

            float imagePadding = 7f;
            content.drawImage(logo, logoX + imagePadding, logoY + imagePadding, logoBoxSize - (imagePadding * 2), logoBoxSize - (imagePadding * 2));
            textX = logoX + logoBoxSize + 14f;
        }

        drawText(content, REPORT_TITLE, textX, height - 36, PDType1Font.HELVETICA_BOLD, 20, Color.WHITE);
        drawText(content, STORE_NAME, textX, height - 56, PDType1Font.HELVETICA_BOLD, 10, new Color(232, 240, 254));
        drawText(content, "Periode: " + periode, textX, height - 72, PDType1Font.HELVETICA, 10, Color.WHITE);
        drawText(content, "No. Laporan: " + reportNumber, width - 250, height - 38, PDType1Font.HELVETICA_BOLD, 10, Color.WHITE);
        drawText(content, "Tanggal Cetak: " + printedAt, width - 250, height - 58, PDType1Font.HELVETICA, 10, Color.WHITE);
        return height - 124;
    }

    // Menggambar kotak ringkasan total penjualan, pengeluaran, dan transaksi.
    private static float drawPdfSummary(PDPageContentStream content, float y, ReportSummary summary) throws IOException {
        // Tiga kartu ringkasan ini memudahkan pembaca melihat penjualan, pengeluaran, dan jumlah transaksi.
        float boxWidth = 230f;
        float boxHeight = 58f;
        float gap = 18f;
        drawPdfSummaryBox(content, PDF_MARGIN, y, boxWidth, boxHeight, "Total Penjualan", CURRENCY_FORMAT.format(summary.totalPenjualan), new Color(232, 245, 233), new Color(46, 125, 50));
        drawPdfSummaryBox(content, PDF_MARGIN + boxWidth + gap, y, boxWidth, boxHeight, "Total Pengeluaran", CURRENCY_FORMAT.format(summary.totalPengeluaran), new Color(255, 235, 238), new Color(183, 28, 28));
        drawPdfSummaryBox(content, PDF_MARGIN + (boxWidth + gap) * 2, y, boxWidth, boxHeight, "Jumlah Transaksi", summary.jumlahTransaksi + " Transaksi", new Color(232, 244, 253), new Color(1, 87, 155));
        float noteY = y - boxHeight - 18;
        drawText(content, "Ringkasan berdasarkan data yang sedang ditampilkan pada tabel laporan.", PDF_MARGIN, noteY, PDType1Font.HELVETICA_OBLIQUE, 9, new Color(71, 85, 105));
        return noteY - 12;
    }

    private static void drawPdfSummaryBox(PDPageContentStream content, float x, float y, float width, float height,
                                          String label, String value, Color background, Color accent) throws IOException {
        content.setNonStrokingColor(background);
        content.addRect(x, y - height, width, height);
        content.fill();
        content.setStrokingColor(new Color(220, 226, 235));
        content.addRect(x, y - height, width, height);
        content.stroke();
        drawText(content, label, x + 14, y - 20, PDType1Font.HELVETICA_BOLD, 10, accent);
        drawText(content, value, x + 14, y - 43, PDType1Font.HELVETICA_BOLD, 16, Color.BLACK);
    }

    // Menggambar header tabel PDF.
    private static float drawPdfTableHeader(PDPageContentStream content, float y) throws IOException {
        float[] widths = getPdfColumnWidths();
        String[] headers = {"Tanggal", "Total Penjualan", "Total Pengeluaran", "Jumlah Transaksi"};
        float x = PDF_MARGIN;
        content.setNonStrokingColor(new Color(74, 118, 168));
        content.addRect(x, y - PDF_ROW_HEIGHT, getPdfTableWidth(), PDF_ROW_HEIGHT);
        content.fill();

        for (int i = 0; i < headers.length; i++) {
            drawPdfCellBorder(content, x, y, widths[i], PDF_ROW_HEIGHT, new Color(74, 118, 168));
            drawText(content, headers[i], x + 8, y - 17, PDType1Font.HELVETICA_BOLD, 10, Color.WHITE);
            x += widths[i];
        }
        return y - PDF_ROW_HEIGHT;
    }

    // Menggambar satu baris data laporan pada tabel PDF.
    private static void drawPdfTableRow(PDPageContentStream content, float y, Laporan laporan, boolean even) throws IOException {
        float[] widths = getPdfColumnWidths();
        String[] values = {
                laporan.getTanggal(),
                CURRENCY_FORMAT.format(laporan.getTotalPenjualan()),
                CURRENCY_FORMAT.format(laporan.getTotalPengeluaran()),
                String.valueOf(laporan.getJumlahTransaksi())
        };
        float x = PDF_MARGIN;
        Color background = even ? new Color(248, 250, 252) : Color.WHITE;
        content.setNonStrokingColor(background);
        content.addRect(x, y - PDF_ROW_HEIGHT, getPdfTableWidth(), PDF_ROW_HEIGHT);
        content.fill();

        for (int i = 0; i < values.length; i++) {
            drawPdfCellBorder(content, x, y, widths[i], PDF_ROW_HEIGHT, new Color(226, 232, 240));
            drawText(content, truncate(values[i], widths[i] - 16, 9), x + 8, y - 17, PDType1Font.HELVETICA, 9, Color.BLACK);
            x += widths[i];
        }
    }

    // Menggambar baris total di akhir tabel PDF.
    private static void drawPdfTotalRow(PDPageContentStream content, float y, ReportSummary summary) throws IOException {
        float[] widths = getPdfColumnWidths();
        String[] values = {
                "TOTAL",
                CURRENCY_FORMAT.format(summary.totalPenjualan),
                CURRENCY_FORMAT.format(summary.totalPengeluaran),
                String.valueOf(summary.jumlahTransaksi)
        };
        float x = PDF_MARGIN;
        content.setNonStrokingColor(new Color(232, 240, 254));
        content.addRect(x, y - PDF_ROW_HEIGHT, getPdfTableWidth(), PDF_ROW_HEIGHT);
        content.fill();

        for (int i = 0; i < values.length; i++) {
            drawPdfCellBorder(content, x, y, widths[i], PDF_ROW_HEIGHT, new Color(191, 219, 254));
            drawText(content, values[i], x + 8, y - 17, PDType1Font.HELVETICA_BOLD, 9, new Color(30, 64, 119));
            x += widths[i];
        }
    }

    // Menggambar area tanda tangan pemilik pada laporan PDF.
    private static void drawPdfSignature(PDPageContentStream content, PDPage page, float y, String signDate) throws IOException {
        float signatureX = page.getMediaBox().getWidth() - PDF_MARGIN - 180f;
        drawText(content, "Samarinda, " + signDate, signatureX, y, PDType1Font.HELVETICA, 10, Color.BLACK);
        drawText(content, "Pemilik Toko", signatureX, y - 18, PDType1Font.HELVETICA, 10, Color.BLACK);
        drawText(content, "(........................)", signatureX, y - 78, PDType1Font.HELVETICA, 10, Color.BLACK);
    }

    private static void drawPdfCellBorder(PDPageContentStream content, float x, float y, float width, float height, Color color) throws IOException {
        content.setStrokingColor(color);
        content.addRect(x, y - height, width, height);
        content.stroke();
    }

    // Menggambar footer waktu cetak pada PDF.
    private static void drawPdfFooter(PDPageContentStream content, PDPage page, String printedAt) throws IOException {
        float y = 28f;
        content.setStrokingColor(new Color(226, 232, 240));
        content.moveTo(PDF_MARGIN, y + 16);
        content.lineTo(page.getMediaBox().getWidth() - PDF_MARGIN, y + 16);
        content.stroke();
        drawText(content, STORE_NAME, PDF_MARGIN, y, PDType1Font.HELVETICA_BOLD, 9, new Color(71, 85, 105));
        drawText(content, "Tanggal cetak: " + printedAt, page.getMediaBox().getWidth() - 210, y, PDType1Font.HELVETICA, 9, new Color(71, 85, 105));
    }

    // Memuat logo toko dari resources jika tersedia.
    private static PDImageXObject loadPdfLogo(PDDocument document) throws IOException {
        try (InputStream inputStream = LaporanExportUtil.class.getResourceAsStream("/Images/LOGO.png")) {
            if (inputStream == null) {
                return null;
            }
            return PDImageXObject.createFromByteArray(document, readAllBytes(inputStream), "logo-toko-zikry");
        }
    }

    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int read;
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        return outputStream.toByteArray();
    }

    private static void drawText(PDPageContentStream content, String text, float x, float y,
                                 PDType1Font font, float size, Color color) throws IOException {
        content.beginText();
        content.setNonStrokingColor(color);
        content.setFont(font, size);
        content.newLineAtOffset(x, y);
        content.showText(sanitizePdfText(text));
        content.endText();
    }

    private static float[] getPdfColumnWidths() {
        return new float[]{132f, 205f, 205f, 168f};
    }

    private static float getPdfTableWidth() {
        float total = 0;
        for (float width : getPdfColumnWidths()) {
            total += width;
        }
        return total;
    }

    // Membersihkan teks agar aman ditulis dengan font PDFBox.
    private static String sanitizePdfText(String text) {
        return text == null ? "" : text.replace('\u2013', '-').replace('\u2014', '-');
    }

    // Memotong teks yang terlalu panjang agar tidak keluar dari kolom PDF.
    private static String truncate(String text, float maxWidth, float fontSize) throws IOException {
        if (PDType1Font.HELVETICA.getStringWidth(sanitizePdfText(text)) / 1000 * fontSize <= maxWidth) {
            return sanitizePdfText(text);
        }
        String ellipsis = "...";
        String value = sanitizePdfText(text);
        while (value.length() > 0 && PDType1Font.HELVETICA.getStringWidth(value + ellipsis) / 1000 * fontSize > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        return value + ellipsis;
    }

    private static void createLabelValueRow(Sheet sheet, int rowIndex, String label, String value, CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowIndex);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(valueStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 3));
    }

    private static CellStyle createTitleStyle(Workbook workbook) {
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 20);
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.CENTER);
        setFillColor(workbook, style, new java.awt.Color(74, 118, 168));
        return style;
    }

    private static CellStyle createMetaStyle(Workbook workbook) {
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static CellStyle createMetaLabelStyle(Workbook workbook) {
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static CellStyle createSummaryLabelStyle(Workbook workbook) {
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        CellStyle style = createBorderedStyle(workbook, true);
        style.setFont(font);
        setFillColor(workbook, style, new java.awt.Color(232, 240, 254));
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle createTableHeaderStyle(Workbook workbook) {
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        CellStyle style = createBorderedStyle(workbook, true);
        style.setFont(font);
        setFillColor(workbook, style, new java.awt.Color(74, 118, 168));
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle createCurrencyStyle(Workbook workbook, boolean summary) {
        CellStyle style = createBorderedStyle(workbook, summary);
        DataFormat dataFormat = workbook.createDataFormat();
        style.setDataFormat(dataFormat.getFormat("[>=1000000]\"Rp\"#\".\"###\".\"###;[>=1000]\"Rp\"#\".\"###;\"Rp\"0"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        if (summary) {
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 12);
            font.setColor(IndexedColors.DARK_BLUE.getIndex());
            style.setFont(font);
            setFillColor(workbook, style, new java.awt.Color(248, 251, 255));
        }
        return style;
    }

    private static CellStyle createTotalLabelStyle(Workbook workbook) {
        CellStyle style = createBorderedStyle(workbook, false);
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        setFillColor(workbook, style, new java.awt.Color(217, 234, 253));
        return style;
    }

    private static CellStyle createTotalCurrencyStyle(Workbook workbook) {
        CellStyle style = createCurrencyStyle(workbook, false);
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        setFillColor(workbook, style, new java.awt.Color(217, 234, 253));
        return style;
    }

    private static CellStyle createTotalNumberStyle(Workbook workbook) {
        CellStyle style = createBorderedStyle(workbook, false);
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        setFillColor(workbook, style, new java.awt.Color(217, 234, 253));
        return style;
    }

    private static CellStyle createSummaryNumberStyle(Workbook workbook) {
        CellStyle style = createBorderedStyle(workbook, true);
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        setFillColor(workbook, style, new java.awt.Color(248, 251, 255));
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = createBorderedStyle(workbook, false);
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private static CellStyle createBorderedStyle(Workbook workbook, boolean centered) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        if (centered) {
            style.setAlignment(HorizontalAlignment.CENTER);
        }
        return style;
    }

    private static void setFillColor(Workbook workbook, CellStyle style, java.awt.Color color) {
        if (workbook instanceof XSSFWorkbook && style instanceof XSSFCellStyle) {
            XSSFColor xssfColor = new XSSFColor(color, ((XSSFWorkbook) workbook).getStylesSource().getIndexedColors());
            ((XSSFCellStyle) style).setFillForegroundColor(xssfColor);
        } else {
            style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    private static void setNumericCell(Row row, int column, double value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    // Menghitung total ringkasan dari seluruh data laporan.
    private static ReportSummary calculateSummary(List<Laporan> data) {
        double totalPenjualan = 0;
        double totalPengeluaran = 0;
        int jumlahTransaksi = 0;
        for (Laporan laporan : data) {
            totalPenjualan += laporan.getTotalPenjualan();
            totalPengeluaran += laporan.getTotalPengeluaran();
            jumlahTransaksi += laporan.getJumlahTransaksi();
        }
        return new ReportSummary(totalPenjualan, totalPengeluaran, jumlahTransaksi);
    }

    // Model kecil khusus untuk menyimpan total ringkasan export.
    private static final class ReportSummary {
        private final double totalPenjualan;
        private final double totalPengeluaran;
        private final int jumlahTransaksi;

        private ReportSummary(double totalPenjualan, double totalPengeluaran, int jumlahTransaksi) {
            this.totalPenjualan = totalPenjualan;
            this.totalPengeluaran = totalPengeluaran;
            this.jumlahTransaksi = jumlahTransaksi;
        }
    }
}
