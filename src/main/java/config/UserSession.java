package config;

import model.User;

/**
 * Class UserSession (Pola Singleton)
 * Kegunaan: Menyimpan data pengguna yang sedang login agar bisa diakses di seluruh halaman aplikasi.
 */
public class UserSession {
    // [1] Variabel statis untuk menyimpan satu-satunya instance dari class ini
    private static UserSession instance;
    
    // [2] Variabel untuk menyimpan data objek User yang sedang aktif
    private User currentUser;

    /**
     * [1] Constructor dibuat privat agar tidak bisa di-instansiasi (new UserSession) dari luar.
     */
    private UserSession() {
    }

    /**
     * Method: getInstance()
     * Kegunaan: Mengambil instance tunggal dari UserSession.
     * Alur: 1. Cek jika instance kosong -> 2. Buat objek baru -> 3. Kembalikan instance.
     */
    public static UserSession getInstance() {
        if (instance == null) {
            // [2] Inisialisasi hanya dilakukan sekali (Singleton)
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Method: setCurrentUser(User user)
     * Kegunaan: Mengatur data user saat login berhasil.
     * Alur: 1. Simpan data user ke variabel -> 2. Tampilkan log konfirmasi login.
     */
    public void setCurrentUser(User user) {
        // [1] Menyimpan objek user ke dalam session
        this.currentUser = user;
        // [2] Output debug untuk memastikan user berhasil masuk ke session
        System.out.println("[UserSession] User logged in: " + user.getIdUser() + " (" + user.getRole() + ")");
    }

    /**
     * Method: getCurrentUser()
     * Kegunaan: Mengambil seluruh data objek user yang sedang aktif.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Method: getUserId()
     * Kegunaan: Mengambil ID user secara langsung.
     * Alur: Cek jika user ada -> ambil ID, jika tidak -> null.
     */
    public String getUserId() {
        return currentUser != null ? currentUser.getIdUser() : null;
    }

    /**
     * Method: getUserRole()
     * Kegunaan: Mengambil jabatan/role user.
     */
    public String getUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    /**
     * Method: isKasir()
     * Kegunaan: Pengecekan cepat apakah user adalah Kasir.
     */
    public boolean isKasir() {
        return currentUser != null && "kasir".equalsIgnoreCase(currentUser.getRole());
    }

    /**
     * Method: isPemilik()
     * Kegunaan: Pengecekan cepat apakah user adalah Pemilik/Admin.
     */
    public boolean isPemilik() {
        return currentUser != null && "pemilik".equalsIgnoreCase(currentUser.getRole());
    }

    /**
     * Method: logout()
     * Kegunaan: Menghapus sesi user.
     * Alur: 1. Cetak info logout -> 2. Kosongkan data currentUser (set null).
     */
    public void logout() {
        // [1] Catat info user yang melakukan logout
        System.out.println("[UserSession] User logged out: " + (currentUser != null ? currentUser.getIdUser() : "Unknown"));
        // [2] Menghapus data user dari memori (Sesi berakhir)
        currentUser = null;
    }

    /**
     * Method: isLoggedIn()
     * Kegunaan: Mengecek apakah ada user yang sedang login atau tidak.
     */
    public boolean isLoggedIn() {
        // Mengembalikan true jika currentUser tidak kosong
        return currentUser != null;
    }
}
