package model;

public class Detail_Transaksi {
    private String idDetail;
    private String idTransaksi;
    private String idBarang;
    private int jumlah;
    private double hargaSatuan;
    private double subtotal;

    public Detail_Transaksi() {
    }

    public Detail_Transaksi(String idDetail, String idTransaksi, String idBarang, int jumlah, double hargaSatuan, double subtotal) {
        this.idDetail = idDetail;
        this.idTransaksi = idTransaksi;
        this.idBarang = idBarang;
        this.jumlah = jumlah;
        this.hargaSatuan = hargaSatuan;
        this.subtotal = subtotal;
    }

    public String getIdDetail() {
        return idDetail;
    }

    public void setIdDetail(String idDetail) {
        this.idDetail = idDetail;
    }

    public String getIdTransaksi() {
        return idTransaksi;
    }

    public void setIdTransaksi(String idTransaksi) {
        this.idTransaksi = idTransaksi;
    }

    public String getIdBarang() {
        return idBarang;
    }

    public void setIdBarang(String idBarang) {
        this.idBarang = idBarang;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public double getHargaSatuan() {
        return hargaSatuan;
    }

    public void setHargaSatuan(double hargaSatuan) {
        this.hargaSatuan = hargaSatuan;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return "Detail_Transaksi{" +
                "idDetail='" + idDetail + '\'' +
                ", idTransaksi='" + idTransaksi + '\'' +
                ", idBarang='" + idBarang + '\'' +
                ", jumlah=" + jumlah +
                ", subtotal=" + subtotal +
                '}';
    }
}
