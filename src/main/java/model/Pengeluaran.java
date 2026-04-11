package model;

import java.time.LocalDate;

public class Pengeluaran {
    private String idPengeluaran;
    private LocalDate tglPengeluaran;
    private double nominal;
    private String jenis;
    private String idUser;

    public Pengeluaran() {
    }

    public Pengeluaran(String idPengeluaran, LocalDate tglPengeluaran, double nominal, String jenis, String idUser) {
        this.idPengeluaran = idPengeluaran;
        this.tglPengeluaran = tglPengeluaran;
        this.nominal = nominal;
        this.jenis = jenis;
        this.idUser = idUser;
    }

    public String getIdPengeluaran() {
        return idPengeluaran;
    }

    public void setIdPengeluaran(String idPengeluaran) {
        this.idPengeluaran = idPengeluaran;
    }

    public LocalDate getTglPengeluaran() {
        return tglPengeluaran;
    }

    public void setTglPengeluaran(LocalDate tglPengeluaran) {
        this.tglPengeluaran = tglPengeluaran;
    }

    public double getNominal() {
        return nominal;
    }

    public void setNominal(double nominal) {
        this.nominal = nominal;
    }

    public String getJenis() {
        return jenis;
    }

    public void setJenis(String jenis) {
        this.jenis = jenis;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    @Override
    public String toString() {
        return "Pengeluaran{" +
                "idPengeluaran='" + idPengeluaran + '\'' +
                ", tglPengeluaran=" + tglPengeluaran +
                ", nominal=" + nominal +
                ", jenis='" + jenis + '\'' +
                '}';
    }
}
