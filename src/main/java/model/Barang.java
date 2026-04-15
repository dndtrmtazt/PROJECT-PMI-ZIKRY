package model;

public class Barang {
    private String idBarang;
    private String namaBarang;
    private String idKategori;
    private int stok;
    private double hargaBeli;
    private double hargaJual;

    public Barang() {
    }

    public Barang(String idBarang, String namaBarang, String idKategori, int stok, double hargaBeli, double hargaJual) {
        this.idBarang = idBarang;
        this.namaBarang = namaBarang;
        this.idKategori = idKategori;
        this.stok = stok;
        this.hargaBeli = hargaBeli;
        this.hargaJual = hargaJual;
    }

    public String getIdBarang() {
        return idBarang;
    }

    public void setIdBarang(String idBarang) {
        this.idBarang = idBarang;
    }

    public String getNamaBarang() {
        return namaBarang;
    }

    public void setNamaBarang(String namaBarang) {
        this.namaBarang = namaBarang;
    }

    public String getIdKategori() {
        return idKategori;
    }

    public void setIdKategori(String idKategori) {
        this.idKategori = idKategori;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }

    public double getHargaBeli() {
        return hargaBeli;
    }

    public void setHargaBeli(double hargaBeli) {
        this.hargaBeli = hargaBeli;
    }

    public double getHargaJual() {
        return hargaJual;
    }

    public void setHargaJual(double hargaJual) {
        this.hargaJual = hargaJual;
    }

    @Override
    public String toString() {
        return idBarang + " - " + namaBarang;
    }
}
