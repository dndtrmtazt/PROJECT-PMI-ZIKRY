package model;

// Model kategori barang, misalnya MAKANAN, MINUMAN, atau ES_KRIM.
public class Kategori {
    private String idKategori;
    private String namaKategori;

    // Constructor kosong dibutuhkan saat objek dibuat dulu lalu diisi lewat setter.
    public Kategori() {
    }

    // Constructor lengkap dipakai saat data kategori sudah tersedia dari database.
    public Kategori(String idKategori, String namaKategori) {
        this.idKategori = idKategori;
        this.namaKategori = namaKategori;
    }

    public String getIdKategori() {
        return idKategori;
    }

    public void setIdKategori(String idKategori) {
        this.idKategori = idKategori;
    }

    public String getNamaKategori() {
        return namaKategori;
    }

    public void setNamaKategori(String namaKategori) {
        this.namaKategori = namaKategori;
    }

    @Override
    public String toString() {
        // Membantu saat objek kategori dicetak ke console untuk debugging.
        return "Kategori{" +
                "idKategori='" + idKategori + '\'' +
                ", namaKategori='" + namaKategori + '\'' +
                '}';
    }
}
