package model;

import java.time.LocalDateTime;

// Model header transaksi, berisi informasi utama dari satu penjualan.
public class Transaksi {
    private String idTransaksi;
    private LocalDateTime tglTransaksi;
    private String idUser;
    private double total;

    public Transaksi() {
    }

    public Transaksi(String idTransaksi, LocalDateTime tglTransaksi, String idUser, double total) {
        this.idTransaksi = idTransaksi;
        this.tglTransaksi = tglTransaksi;
        this.idUser = idUser;
        this.total = total;
    }

    public String getIdTransaksi() {
        return idTransaksi;
    }

    public void setIdTransaksi(String idTransaksi) {
        this.idTransaksi = idTransaksi;
    }

    public LocalDateTime getTglTransaksi() {
        return tglTransaksi;
    }

    public void setTglTransaksi(LocalDateTime tglTransaksi) {
        this.tglTransaksi = tglTransaksi;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    @Override
    public String toString() {
        // Format teks sederhana untuk membantu pengecekan data transaksi saat debugging.
        return "Transaksi{" +
                "idTransaksi='" + idTransaksi + '\'' +
                ", tglTransaksi=" + tglTransaksi +
                ", idUser='" + idUser + '\'' +
                ", total=" + total +
                '}';
    }
}
