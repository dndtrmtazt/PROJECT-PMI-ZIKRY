package model;

public class Kategori {
    private String idKategori;
    private String namaKategori;

    public Kategori() {
    }

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
        return "Kategori{" +
                "idKategori='" + idKategori + '\'' +
                ", namaKategori='" + namaKategori + '\'' +
                '}';
    }
}
