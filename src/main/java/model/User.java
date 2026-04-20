package model;

public class User {
    private String idUser;      // Mapping ke kolom 'id_user'
    private String namaLengkap; // Mapping ke kolom 'nama_lengkap'
    private String role;        // Mapping ke kolom 'role'
    private String password;    // Mapping ke kolom 'user_password'

    // Konstruktor Kosong
    public User() {
    }

    // Konstruktor Lengkap (Gunakan ini saat mengambil data dari DB)
    public User(String idUser, String namaLengkap, String role, String password) {
        this.idUser = idUser;
        this.namaLengkap = namaLengkap;
        this.role = role;
        this.password = password;
    }

    // Getter dan Setter untuk idUser
    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    // Getter dan Setter untuk namaLengkap (PENTING: Menghilangkan error merah)
    public String getNamaLengkap() {
        return namaLengkap;
    }

    public void setNamaLengkap(String namaLengkap) {
        this.namaLengkap = namaLengkap;
    }

    // Getter dan Setter untuk role
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Getter dan Setter untuk password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "idUser='" + idUser + '\'' +
                ", namaLengkap='" + namaLengkap + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}