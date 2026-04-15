package model;

import javafx.beans.property.*;

public class Laporan {
    private final StringProperty tanggal;
    private final DoubleProperty totalPenjualan;
    private final DoubleProperty totalPengeluaran;
    private final IntegerProperty jumlahTransaksi;

    public Laporan(String tanggal, double totalPenjualan, double totalPengeluaran, int jumlahTransaksi) {
        this.tanggal = new SimpleStringProperty(tanggal);
        this.totalPenjualan = new SimpleDoubleProperty(totalPenjualan);
        this.totalPengeluaran = new SimpleDoubleProperty(totalPengeluaran);
        this.jumlahTransaksi = new SimpleIntegerProperty(jumlahTransaksi);
    }

    public String getTanggal() { return tanggal.get(); }
    public StringProperty tanggalProperty() { return tanggal; }

    public double getTotalPenjualan() { return totalPenjualan.get(); }
    public DoubleProperty totalPenjualanProperty() { return totalPenjualan; }

    public double getTotalPengeluaran() { return totalPengeluaran.get(); }
    public DoubleProperty totalPengeluaranProperty() { return totalPengeluaran; }

    public int getJumlahTransaksi() { return jumlahTransaksi.get(); }
    public IntegerProperty jumlahTransaksiProperty() { return jumlahTransaksi; }
}
