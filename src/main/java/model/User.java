package model;

public class User {
    private String idUser;    // id_user
    private String password;  // user_password
    private String role;      // admin, kasir, pemilik, dll

    public User() {
    }

    public User(String idUser, String password, String role) {
        this.idUser = idUser;
        this.password = password;
        this.role = role;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "idUser='" + idUser + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
