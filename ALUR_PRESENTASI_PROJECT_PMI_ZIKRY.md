# Alur Presentasi Project PMI Toko Zikry

## 1. Gambaran Singkat Project

**PMI Toko Zikry** adalah aplikasi kasir desktop berbasis **JavaFX** yang dipakai untuk:

- login berdasarkan role
- transaksi kasir
- manajemen barang
- manajemen kategori
- manajemen user
- manajemen pengeluaran
- laporan penjualan
- export laporan ke PDF dan Excel
- cetak struk PDF

Project ini memakai **SQLite** sebagai database lokal, sehingga aplikasi bisa dijalankan tanpa server terpisah.

---

## 2. Analisis Struktur Project

Struktur project ini sudah cukup rapi dan mengikuti pembagian tanggung jawab per lapisan:

```text
src/main/java
|-- app
|-- config
|-- Controller
|-- DAO
|-- model
`-- util

src/main/resources
|-- FXML
|-- CSS
`-- Images

database
`-- umkm.db
```

### Fungsi tiap folder

#### `app`
Berisi entry point aplikasi.

- `MainApplication.java`
  Menjalankan aplikasi dan membuka halaman login pertama kali.

#### `config`
Berisi konfigurasi inti aplikasi.

- `koneksi.java`
  Menghubungkan aplikasi ke database SQLite.
- `DatabaseInitializer.java`
  Membuat tabel, index, dan seed data awal jika database belum siap.
- `UserSession.java`
  Menyimpan informasi user yang sedang login.

#### `Controller`
Berisi logika tiap halaman.

Contoh controller penting:

- `LoginController.java`
  Mengurus login dan perpindahan user ke dashboard sesuai role.
- `MainController.java`
  Menjadi pengatur layout utama admin/pemilik dan navigasi antar halaman.
- `KasirDashboardController.java`
  Mengurus transaksi kasir, keranjang, pembayaran, dan cetak struk.
- `DashboardController.java`
  Menampilkan ringkasan data harian dan stok menipis.
- `LaporanController.java`
  Menampilkan laporan dan export PDF/Excel.

#### `DAO`
Berisi akses data ke database.

Contoh:

- `UserDAO.java`
- `BarangDAO.java`
- `TransaksiDAO.java`
- `PengeluaranDAO.java`
- `LaporanDao.java`

Folder ini penting karena semua query database dipusatkan di sini.

#### `model`
Berisi representasi data dari database.

Contoh:

- `User`
- `Barang`
- `Transaksi`
- `Detail_Transaksi`
- `Pengeluaran`
- `Kategori`
- `Laporan`
- `Toko`

#### `util`
Berisi fungsi bantu.

- `StrukPdfUtil.java`
  Untuk membuat struk PDF.
- `LaporanExportUtil.java`
  Untuk export laporan ke PDF dan Excel.

#### `resources/FXML`
Berisi struktur tampilan JavaFX.

- `FXML/LoginView.fxml`
- `FXML/Admin/...`
- `FXML/Kasir/...`
- `FXML/Dialog/...`

#### `resources/CSS`
Berisi styling antarmuka.

#### `resources/Images`
Berisi asset gambar, icon, screenshot, dan file pendukung lain.

---

## 3. Pola Arsitektur yang Dipakai

Secara praktik, project ini memakai pola yang mirip **MVC + DAO**:

- `FXML` = tampilan
- `Controller` = logika interaksi user
- `model` = representasi data
- `DAO` = komunikasi ke database
- `config` = koneksi, session, dan inisialisasi
- `util` = fitur tambahan seperti export

### Alur hubungan antar layer

```text
User
  -> FXML
  -> Controller
  -> DAO
  -> SQLite Database
  -> hasil kembali ke Controller
  -> ditampilkan lagi ke UI
```

Ini bagus untuk dipresentasikan karena menunjukkan bahwa kode tidak dicampur jadi satu file besar.

---

## 4. Alur Sistem Secara Keseluruhan

### Alur startup aplikasi

1. `MainApplication` dijalankan.
2. Aplikasi membuka `LoginView.fxml`.
3. User memasukkan username dan password.
4. `LoginController` memanggil `UserDAO.validateUser()`.
5. Jika valid, data user disimpan ke `UserSession`.
6. Jika role `kasir`, user diarahkan ke halaman kasir.
7. Jika role `pemilik/admin`, user diarahkan ke layout admin.

### Alur koneksi database

1. Class `koneksi` mencari lokasi folder `database`.
2. SQLite dibuka lewat `umkm.db`.
3. `DatabaseInitializer.initialize()` memastikan tabel sudah ada.
4. Jika database baru, schema dan seed data langsung dibuat.

### Alur navigasi admin

1. User pemilik masuk ke `MainLayout`.
2. `MainController` mengatur menu sidebar sesuai role.
3. Saat tombol sidebar diklik, `panggilHalaman()` memuat FXML terkait.
4. Controller halaman aktif ditampilkan di area konten utama.

### Alur transaksi kasir

1. Kasir membuka halaman transaksi.
2. Produk dimuat dari `BarangDAO`.
3. Kasir mencari atau memilih barang.
4. Barang masuk ke keranjang.
5. Qty bisa ditambah atau dikurangi.
6. Total belanja dan kembalian dihitung otomatis.
7. Saat checkout, aplikasi validasi nominal bayar.
8. `TransaksiDAO` menyimpan transaksi dan detail transaksi.
9. Stok barang ikut dikurangi.
10. Struk bisa disimpan sebagai PDF.

### Alur laporan

1. Halaman laporan mengambil data dari `LaporanDao`.
2. Data ditampilkan di `TableView`.
3. User bisa filter berdasarkan tanggal.
4. Data yang tampil bisa diexport ke PDF atau Excel.

---

## 5. Kelebihan Struktur Project

- Pemisahan folder sudah jelas antara UI, logic, database, dan utilitas.
- Sudah ada konsep `role-based access` untuk kasir dan pemilik.
- Database lokal SQLite membuat aplikasi ringan dan mudah dipasang.
- Ada `DatabaseInitializer`, jadi database baru bisa otomatis siap pakai.
- Fitur presentable cukup kuat: transaksi, laporan, export, struk, dark mode.
- Theme handling sudah diperhatikan di banyak halaman.

---

## 6. Catatan yang Bisa Disampaikan Saat Presentasi

Kalau ingin presentasi terlihat lebih matang, kamu bisa sampaikan juga beberapa catatan teknis:

- Project ini berbasis desktop, jadi cocok untuk toko kecil atau UMKM yang butuh aplikasi offline/lokal.
- Struktur project menunjukkan pembagian kerja tim yang masuk akal.
- DAO dipakai supaya query database tidak bercampur langsung dengan tampilan.
- Export PDF dan Excel menunjukkan aplikasi tidak hanya untuk input data, tapi juga mendukung kebutuhan operasional dan pelaporan.

Catatan teknis yang bisa disampaikan dengan hati-hati:

- Di `pom.xml`, dependency JavaFX menggunakan versi modern, tetapi konfigurasi compiler masih `source 9` dan `target 9`.
- Beberapa file masih berisi komentar penjelasan yang sangat detail, jadi project ini juga terlihat dipersiapkan untuk pembelajaran atau presentasi akademik.

---

## 7. Saran Urutan Presentasi

Berikut alur presentasi yang enak dibawakan:

### Slide 1 - Judul

Judul yang bisa dipakai:

**PMI Toko Zikry: Aplikasi Kasir Desktop Berbasis JavaFX untuk Pengelolaan Toko**

Isi singkat:

- nama project
- nama tim
- tujuan project

### Slide 2 - Latar Belakang

Poin bicara:

- toko kecil sering mencatat transaksi secara manual
- pencatatan manual rawan salah
- laporan penjualan dan stok sulit dipantau
- dibutuhkan aplikasi kasir sederhana, lokal, dan mudah dipakai

### Slide 3 - Tujuan Project

- mempermudah transaksi penjualan
- mengelola data barang dan stok
- mengelola pengeluaran toko
- membuat laporan harian
- memisahkan hak akses kasir dan pemilik

### Slide 4 - Teknologi yang Digunakan

- Java
- JavaFX
- Maven
- SQLite
- PDFBox
- Apache POI

### Slide 5 - Struktur Project

Tampilkan pembagian:

- `app`
- `config`
- `Controller`
- `DAO`
- `model`
- `util`
- `FXML`
- `CSS`
- `Images`
- `database`

Kalimat presentasi:

"Struktur ini kami buat agar tampilan, logika, dan akses database tidak bercampur."

### Slide 6 - Arsitektur Sistem

Jelaskan:

User -> UI -> Controller -> DAO -> Database -> kembali ke UI

Kalimat presentasi:

"Saat user melakukan aksi, controller menerima input, DAO mengambil atau menyimpan data ke SQLite, lalu hasilnya ditampilkan kembali ke antarmuka."

### Slide 7 - Alur Login dan Hak Akses

Bahas:

- login melalui `LoginController`
- validasi akun melalui `UserDAO`
- session disimpan di `UserSession`
- role menentukan tampilan dashboard

### Slide 8 - Fitur Kasir

Bahas:

- melihat daftar barang
- cari barang
- tambah ke keranjang
- hitung total dan kembalian
- simpan transaksi
- cetak struk PDF

### Slide 9 - Fitur Pemilik/Admin

Bahas:

- dashboard ringkasan
- kelola barang
- kelola kategori
- kelola user
- kelola pengeluaran
- laporan penjualan
- export PDF dan Excel

### Slide 10 - Database dan Seed Data

Bahas:

- database lokal `umkm.db`
- tabel inti dibuat otomatis
- ada seed data awal
- aplikasi tetap bisa dipakai walau database baru dibuat

### Slide 11 - Demo Alur Penggunaan

Urutan demo terbaik:

1. login sebagai kasir
2. lakukan transaksi singkat
3. tampilkan total dan kembalian
4. simpan transaksi dan cetak struk
5. logout
6. login sebagai pemilik
7. buka dashboard
8. buka data barang
9. buka laporan
10. export laporan

### Slide 12 - Kelebihan Project

- desktop dan ringan
- offline
- role-based access
- laporan bisa diexport
- UI sudah mendukung dark mode
- struktur kode cukup rapi

### Slide 13 - Kekurangan / Pengembangan Lanjutan

Bagian ini penting supaya presentasi terlihat jujur dan matang:

- belum berbasis client-server
- keamanan password masih bisa ditingkatkan
- filter laporan masih bisa dibuat lebih fleksibel
- testing otomatis belum terlihat dominan
- packaging installer final bisa ditingkatkan

### Slide 14 - Penutup

Kalimat penutup yang bisa dipakai:

"Melalui PMI Toko Zikry, kami mencoba membangun aplikasi kasir desktop yang sederhana tetapi relevan untuk kebutuhan operasional toko, mulai dari transaksi, pengelolaan stok, hingga pelaporan."

---

## 8. Script Bicara Singkat Saat Presentasi

Kalau kamu mau membawakan dengan runtut, kamu bisa pakai alur ngomong seperti ini:

### Pembukaan

"Project kami bernama PMI Toko Zikry, yaitu aplikasi kasir desktop berbasis JavaFX yang dirancang untuk membantu operasional toko, mulai dari transaksi penjualan, pengelolaan barang, pengeluaran, hingga laporan."

### Saat menjelaskan struktur

"Pada sisi kode, project ini dibagi menjadi beberapa bagian utama. Folder Controller menangani logika halaman, DAO menangani akses database, model merepresentasikan data, config mengatur koneksi dan session, sedangkan FXML dan CSS dipakai untuk tampilan antarmuka."

### Saat menjelaskan alur

"Alur aplikasi dimulai dari login. Setelah user login, sistem memvalidasi akun ke database lalu menyimpan session. Setelah itu user diarahkan ke tampilan sesuai rolenya, yaitu kasir atau pemilik."

### Saat menjelaskan transaksi

"Pada sisi kasir, user dapat memilih barang, menambahkannya ke keranjang, menghitung total belanja dan kembalian, lalu menyimpan transaksi. Setelah transaksi berhasil, struk dapat dicetak dalam bentuk PDF."

### Saat menjelaskan laporan

"Pada sisi pemilik, sistem menyediakan laporan penjualan dan pengeluaran yang bisa difilter berdasarkan tanggal, lalu diexport ke PDF atau Excel."

### Penutup

"Jadi, inti dari project ini adalah bagaimana kami membangun aplikasi kasir yang tidak hanya memproses transaksi, tetapi juga membantu pemilik toko dalam monitoring dan pelaporan."

---

## 9. Alur Demo yang Paling Aman Saat Sidang / Presentasi

Supaya demo lancar, gunakan urutan ini:

1. Buka aplikasi.
2. Login sebagai kasir.
3. Cari 1 sampai 2 barang.
4. Tambahkan ke keranjang.
5. Isi nominal bayar.
6. Tunjukkan total dan kembalian.
7. Simpan transaksi.
8. Tunjukkan proses cetak struk PDF.
9. Logout.
10. Login sebagai pemilik.
11. Tunjukkan dashboard ringkasan.
12. Tunjukkan data barang atau stok menipis.
13. Buka laporan.
14. Lakukan export laporan.

Kenapa urutan ini bagus:

- dimulai dari fitur utama
- alurnya mudah dipahami dosen/penguji
- menunjukkan perbedaan role
- menampilkan fitur transaksi dan laporan sekaligus

---

## 10. Pertanyaan yang Mungkin Muncul dan Jawabannya

### "Kenapa memilih SQLite?"

Karena SQLite ringan, tidak perlu server terpisah, mudah dipakai untuk aplikasi desktop lokal, dan cocok untuk skala UMKM.

### "Kenapa memakai DAO?"

Agar query database terpisah dari tampilan dan controller, sehingga kode lebih rapi dan mudah dirawat.

### "Bagaimana sistem membedakan kasir dan pemilik?"

Saat login, data role dibaca dari tabel user. Setelah itu controller mengarahkan user ke tampilan sesuai hak akses.

### "Apa yang terjadi saat database kosong?"

Class `DatabaseInitializer` akan membuat tabel inti, index, dan seed data awal agar aplikasi bisa langsung dipakai.

### "Bagaimana laporan dibuat?"

Data laporan diambil dari database melalui `LaporanDao`, ditampilkan di tabel, lalu diexport melalui `LaporanExportUtil`.

---

## 11. Kesimpulan Analisis

Secara keseluruhan, struktur project ini **cukup baik untuk project akademik dan demo presentasi** karena:

- modulnya terpisah jelas
- fitur utamanya lengkap
- ada role user
- ada laporan dan export
- ada database initializer
- ada transaksi sampai cetak struk

Kalau dipresentasikan dengan urutan yang rapi, project ini akan terlihat kuat bukan hanya dari sisi tampilan, tapi juga dari sisi arsitektur dan alur sistem.

