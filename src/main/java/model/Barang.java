package model;

// Model barang yang mewakili data produk di tabel barang.
public class Barang {
    private String idBarang;
    private String namaBarang;
    private String idKategori;
    private String namaKategori; // Tambahan: buat nyimpan nama kategori hasil JOIN
    private int stok;
    private String satuan;       // Tambahan: buat nyimpan satuan (Pcs, Kg, dll)
    private double hargaBeli;
    private double hargaJual;

    public Barang() {
    }

    // Constructor lengkap (sudah ditambah satuan dan namaKategori)
    public Barang(String idBarang, String namaBarang, String idKategori, String namaKategori, int stok, String satuan, double hargaBeli, double hargaJual) {
        this.idBarang = idBarang;
        this.namaBarang = namaBarang;
        this.idKategori = idKategori;
        this.namaKategori = namaKategori;
        this.stok = stok;
        this.satuan = satuan;
        this.hargaBeli = hargaBeli;
        this.hargaJual = hargaJual;
    }

    // Getter dan Setter
    public String getIdBarang() { return idBarang; }
    public void setIdBarang(String idBarang) { this.idBarang = idBarang; }

    public String getNamaBarang() { return namaBarang; }
    public void setNamaBarang(String namaBarang) { this.namaBarang = namaBarang; }

    public String getIdKategori() { return idKategori; }
    public void setIdKategori(String idKategori) { this.idKategori = idKategori; }

    public String getNamaKategori() { return namaKategori; }
    public void setNamaKategori(String namaKategori) { this.namaKategori = namaKategori; }

    public int getStok() { return stok; }
    public void setStok(int stok) { this.stok = stok; }

    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }

    public double getHargaBeli() { return hargaBeli; }
    public void setHargaBeli(double hargaBeli) { this.hargaBeli = hargaBeli; }

    public double getHargaJual() { return hargaJual; }
    public void setHargaJual(double hargaJual) { this.hargaJual = hargaJual; }

    @Override
    public String toString() {
        // Format ini memudahkan barang ditampilkan di ComboBox atau log.
        return idBarang + " - " + namaBarang;
    }
}
