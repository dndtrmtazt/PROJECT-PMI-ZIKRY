PRAGMA foreign_keys = OFF;

DROP TABLE IF EXISTS detail_transaksi;
DROP TABLE IF EXISTS transaksi;
DROP TABLE IF EXISTS pengeluaran;
DROP TABLE IF EXISTS barang;
DROP TABLE IF EXISTS kategori;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS pengaturan_toko;

CREATE TABLE user (
  id_user TEXT PRIMARY KEY,
  nama_lengkap TEXT NOT NULL,
  user_password TEXT NOT NULL,
  role TEXT NOT NULL CHECK (lower(role) IN ('kasir', 'pemilik'))
);

CREATE TABLE kategori (
  id_kategori TEXT PRIMARY KEY,
  nama_kategori TEXT NOT NULL UNIQUE
);

CREATE TABLE barang (
  id_barang TEXT PRIMARY KEY,
  nama_barang TEXT NOT NULL,
  id_kategori TEXT,
  stok INTEGER NOT NULL DEFAULT 0,
  satuan TEXT NOT NULL DEFAULT 'Pcs',
  harga_beli NUMERIC NOT NULL,
  harga_jual NUMERIC NOT NULL,
  FOREIGN KEY (id_kategori) REFERENCES kategori(id_kategori) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE transaksi (
  id_transaksi TEXT PRIMARY KEY,
  tgl_transaksi TEXT DEFAULT (datetime('now', 'localtime')),
  id_user TEXT,
  total NUMERIC NOT NULL DEFAULT 0,
  FOREIGN KEY (id_user) REFERENCES user(id_user) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE detail_transaksi (
  id_detail TEXT PRIMARY KEY,
  id_transaksi TEXT NOT NULL,
  id_barang TEXT,
  jumlah INTEGER NOT NULL,
  harga_satuan NUMERIC NOT NULL,
  subtotal NUMERIC GENERATED ALWAYS AS (jumlah * harga_satuan) STORED,
  FOREIGN KEY (id_transaksi) REFERENCES transaksi(id_transaksi) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (id_barang) REFERENCES barang(id_barang) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE pengeluaran (
  id_pengeluaran TEXT PRIMARY KEY,
  tgl_pengeluaran TEXT NOT NULL,
  nominal NUMERIC NOT NULL,
  jenis TEXT,
  id_user TEXT,
  FOREIGN KEY (id_user) REFERENCES user(id_user) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE pengaturan_toko (
  id INTEGER PRIMARY KEY CHECK (id = 1),
  nama_toko TEXT NOT NULL,
  nomor_telepon TEXT,
  alamat TEXT,
  email TEXT
);

CREATE INDEX idx_barang_kategori ON barang(id_kategori);
CREATE INDEX idx_transaksi_user ON transaksi(id_user);
CREATE INDEX idx_detail_transaksi ON detail_transaksi(id_transaksi);
CREATE INDEX idx_detail_barang ON detail_transaksi(id_barang);
CREATE INDEX idx_pengeluaran_user ON pengeluaran(id_user);

INSERT INTO user (id_user, nama_lengkap, user_password, role) VALUES
  ('KSR001', 'Kasir Utama', 'EZAK123', 'kasir'),
  ('PMK001', 'Pemilik Toko', 'EZAK321', 'pemilik');

INSERT INTO kategori (id_kategori, nama_kategori) VALUES
  ('DLL000', 'LAIN LAIN'),
  ('ESK000', 'ES_KRIM'),
  ('MKM000', 'MAKANAN'),
  ('MIM000', 'MINUMAN'),
  ('PBR000', 'PEMBERSIH'),
  ('RKK000', 'ROKOK'),
  ('SBK000', 'SEMBAKO');

INSERT INTO barang (id_barang, nama_barang, id_kategori, stok, satuan, harga_beli, harga_jual) VALUES
  ('DLL001', 'BENSIN 1L', 'DLL000', 5, 'Liter', 10000.00, 12000.00),
  ('ESK001', 'PADDLEPOP', 'ESK000', 10, 'Pcs', 5000.00, 6000.00),
  ('ESK002', 'KOCNETTO COKLAT', 'ESK000', 29, 'Pcs', 9000.00, 10000.00),
  ('ESK003', 'MAGNUM CLASSIS', 'ESK000', 20, 'Pcs', 18000.00, 22000.00),
  ('ESK004', 'ICED MOCHI', 'ESK000', 10, 'Pcs', 3000.00, 4000.00),
  ('ESK005', 'WALLS STROBERRY VANILA', 'ESK000', -48, 'Pcs', 4000.00, 5000.00),
  ('MIM001', 'MINERAL', 'MIM000', 30, 'Liter', 8000.00, 9000.00),
  ('MIM002', 'AQUA', 'MIM000', 30, 'Liter', 7000.00, 8000.00),
  ('MIM003', 'KOPI GOLDA', 'MIM000', 30, 'Pcs', 2500.00, 3000.00),
  ('MIM004', 'UTRAMILK', 'MIM000', 20, 'Pcs', 4000.00, 5000.00),
  ('MIM005', 'TEH BOTOL SOSRO LESS SUGAR', 'MIM000', 10, 'Pcs', 3000.00, 4990.00),
  ('MKM001', 'MIE GORENG', 'MKM000', 30, 'Pcs', 3000.00, 3500.00),
  ('MKM002', 'MIE SEDAP GORANG', 'MKM000', 30, 'Pcs', 3000.00, 3500.00),
  ('MKM003', 'BISKUIT ROMAH', 'MKM000', 30, 'Pcs', 8000.00, 10000.00),
  ('MKM004', 'OREO', 'MKM000', 20, 'Pcs', 1500.00, 2000.00),
  ('MKM005', 'BETTER', 'MKM000', 12, 'Pcs', 1000.00, 2000.00),
  ('PBR001', 'SUNLIGHT', 'PBR000', 30, 'Pcs', 4000.00, 5000.00),
  ('PBR002', 'PESODENT', 'PBR000', 30, 'Pcs', 6000.00, 7000.00),
  ('PBR005', 'CLEAR', 'PBR000', 12, 'Pcs', 500.00, 1000.00),
  ('RKK001', 'G.A', 'RKK000', 12, 'Pcs', 18000.00, 20000.00),
  ('RKK002', 'URBANMILK', 'RKK000', 12, 'Pcs', 25000.00, 30000.00),
  ('RKK003', 'BRAND JATI', 'RKK000', 12, 'Pcs', 19000.00, 20000.00),
  ('RKK004', 'ESSE DOUBLE CLIK', 'RKK000', 15, 'Pcs', 35000.00, 40000.00),
  ('RKK005', 'D''TE', 'RKK000', 14, 'Pcs', 19000.00, 20000.00),
  ('SBK001', 'TELUR', 'SBK000', 30, 'Butir', 2000.00, 2500.00),
  ('SBK002', 'BERAS DIAMOND', 'SBK000', 30, 'Kg', 300000.00, 346000.00),
  ('SBK003', 'GULA PASIR 1 KG', 'SBK000', 20, 'Kg', 16000.00, 18000.00),
  ('SBK004', 'MINYAK GORENG KITA', 'SBK000', 10, 'Liter', 12000.00, 14000.00),
  ('SBK005', 'TEPUNG TERIGU', 'SBK000', 30, 'Kg', 4000.00, 5000.00),
  ('SBK006', 'GARAM', 'SBK000', 12, 'Pcs', 4000.00, 5000.00);

INSERT INTO transaksi (id_transaksi, tgl_transaksi, id_user, total) VALUES
  ('TRK001', '2026-04-01 22:13:37', 'KSR001', 300000.00);

INSERT INTO detail_transaksi (id_detail, id_transaksi, id_barang, jumlah, harga_satuan) VALUES
  ('DTL001', 'TRK001', 'ESK005', 60, 5000.00);

INSERT INTO pengeluaran (id_pengeluaran, tgl_pengeluaran, nominal, jenis, id_user) VALUES
  ('PGN001', '2026-04-01', 100000.00, 'PLN', 'PMK001'),
  ('PGN002', '2026-04-01', 326000.00, 'AIR', 'PMK001'),
  ('PGN003', '2026-04-01', 30000.00, 'PLASTIK KATONGAN', 'PMK001');

INSERT INTO pengaturan_toko (id, nama_toko, nomor_telepon, alamat, email) VALUES
  (1, 'Toko Zikry', '081234567890', 'Alamat belum diatur', 'tokozikry@example.com');

PRAGMA foreign_keys = ON;
