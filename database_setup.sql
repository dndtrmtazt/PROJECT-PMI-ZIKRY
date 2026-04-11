/*
 Navicat Premium Dump SQL
 Source Server: localhost:3306
 Source Database: umkm
 MySQL Version: 8.0.30
 File Encoding: utf8mb4
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ================================
-- Buat database jika belum ada
-- ================================
CREATE DATABASE IF NOT EXISTS umkm;
USE umkm;

-- ================================
-- Table structure for user
-- ================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role` enum('kasir','pemilik') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id_user`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ================================
-- Records of user
-- ================================
INSERT INTO `user` VALUES ('KSR001', 'EZAK123', 'kasir');
INSERT INTO `user` VALUES ('PMK001', 'EZAK321', 'pemilik');

-- ================================
-- Table structure for kategori
-- ================================
DROP TABLE IF EXISTS `kategori`;
CREATE TABLE `kategori` (
  `id_kategori` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `nama_kategori` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id_kategori`) USING BTREE,
  UNIQUE INDEX `nama_kategori`(`nama_kategori` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ================================
-- Records of kategori
-- ================================
INSERT INTO `kategori` VALUES ('DLL000', 'LAIN LAIN');
INSERT INTO `kategori` VALUES ('ESK000', 'ES_KRIM');
INSERT INTO `kategori` VALUES ('MKM000', 'MAKANAN');
INSERT INTO `kategori` VALUES ('MIM000', 'MINUMAN');
INSERT INTO `kategori` VALUES ('PBR000', 'PEMBERSIH');
INSERT INTO `kategori` VALUES ('RKK000', 'ROKOK');
INSERT INTO `kategori` VALUES ('SBK000', 'SEMBAKO');

-- ================================
-- Table structure for barang
-- ================================
DROP TABLE IF EXISTS `barang`;
CREATE TABLE `barang` (
  `id_barang` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `nama_barang` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `id_kategori` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `stok` int NULL DEFAULT 0,
  `harga_beli` decimal(12, 2) NOT NULL,
  `harga_jual` decimal(12, 2) NOT NULL,
  PRIMARY KEY (`id_barang`) USING BTREE,
  INDEX `idx_barang_kategori`(`id_kategori` ASC) USING BTREE,
  CONSTRAINT `barang_ibfk_1` FOREIGN KEY (`id_kategori`) REFERENCES `kategori` (`id_kategori`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ================================
-- Records of barang
-- ================================
INSERT INTO `barang` VALUES ('DLL001', 'BENSIN 1L', 'DLL000', 5, 10000.00, 12000.00);
INSERT INTO `barang` VALUES ('ESK001', 'PADDLEPOP', 'ESK000', 10, 5000.00, 6000.00);
INSERT INTO `barang` VALUES ('ESK002', 'KOCNETTO COKLAT', 'ESK000', 29, 9000.00, 10000.00);
INSERT INTO `barang` VALUES ('ESK003', 'MAGNUM CLASSIS', 'ESK000', 20, 18000.00, 22000.00);
INSERT INTO `barang` VALUES ('ESK004', 'ICED MOCHI', 'ESK000', 10, 3000.00, 4000.00);
INSERT INTO `barang` VALUES ('ESK005', 'WALLS STROBERRY VANILA', 'ESK000', -48, 4000.00, 5000.00);
INSERT INTO `barang` VALUES ('MIM001', 'MINERAL', 'MIM000', 30, 8000.00, 9000.00);
INSERT INTO `barang` VALUES ('MIM002', 'AQUA', 'MIM000', 30, 7000.00, 8000.00);
INSERT INTO `barang` VALUES ('MIM003', 'KOPI GOLDA', 'MIM000', 30, 2500.00, 3000.00);
INSERT INTO `barang` VALUES ('MIM004', 'UTRAMILK', 'MIM000', 20, 4000.00, 5000.00);
INSERT INTO `barang` VALUES ('MIM005', 'TEH BOTOL SOSRO LESS SUGAR', 'MIM000', 10, 3000.00, 4990.00);
INSERT INTO `barang` VALUES ('MKM001', 'MIE GORENG', 'MKM000', 30, 3000.00, 3500.00);
INSERT INTO `barang` VALUES ('MKM002', 'MIE SEDAP GORANG', 'MKM000', 30, 3000.00, 3500.00);
INSERT INTO `barang` VALUES ('MKM003', 'BISKUIT ROMAH', 'MKM000', 30, 8000.00, 10000.00);
INSERT INTO `barang` VALUES ('MKM004', 'OREO', 'MKM000', 20, 1500.00, 2000.00);
INSERT INTO `barang` VALUES ('MKM005', 'BETTER', 'MKM000', 12, 1000.00, 2000.00);
INSERT INTO `barang` VALUES ('PBR001', 'SUNLIGHT', 'PBR000', 30, 4000.00, 5000.00);
INSERT INTO `barang` VALUES ('PBR002', 'PESODENT', 'PBR000', 30, 6000.00, 7000.00);
INSERT INTO `barang` VALUES ('PBR005', 'CLEAR', 'PBR000', 12, 500.00, 1000.00);
INSERT INTO `barang` VALUES ('RKK001', 'G.A', 'RKK000', 12, 18000.00, 20000.00);
INSERT INTO `barang` VALUES ('RKK002', 'URBANMILK', 'RKK000', 12, 25000.00, 30000.00);
INSERT INTO `barang` VALUES ('RKK003', 'BRAND JATI', 'RKK000', 12, 19000.00, 20000.00);
INSERT INTO `barang` VALUES ('RKK004', 'ESSE DOUBLE CLIK', 'RKK000', 15, 35000.00, 40000.00);
INSERT INTO `barang` VALUES ('RKK005', 'D\'TE', 'RKK000', 14, 19000.00, 20000.00);
INSERT INTO `barang` VALUES ('SBK001', 'TELUR', 'SBK000', 30, 2000.00, 2500.00);
INSERT INTO `barang` VALUES ('SBK002', 'BERAS DIAMOND', 'SBK000', 30, 300000.00, 346000.00);
INSERT INTO `barang` VALUES ('SBK003', 'GULA PASIR 1 KG', 'SBK000', 20, 16000.00, 18000.00);
INSERT INTO `barang` VALUES ('SBK004', 'MINYAK GORENG KITA', 'SBK000', 10, 12000.00, 14000.00);
INSERT INTO `barang` VALUES ('SBK005', 'TEPUNG TERIGU', 'SBK000', 30, 4000.00, 5000.00);
INSERT INTO `barang` VALUES ('SBK006', 'GARAM', 'SBK000', 12, 4000.00, 5000.00);

-- ================================
-- Table structure for transaksi
-- ================================
DROP TABLE IF EXISTS `transaksi`;
CREATE TABLE `transaksi` (
  `id_transaksi` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tgl_transaksi` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `id_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `total` decimal(12, 2) NULL DEFAULT 0.00,
  PRIMARY KEY (`id_transaksi`) USING BTREE,
  INDEX `idx_transaksi_user`(`id_user` ASC) USING BTREE,
  CONSTRAINT `transaksi_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `user` (`id_user`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ================================
-- Records of transaksi
-- ================================
INSERT INTO `transaksi` VALUES ('TRK001', '2026-04-01 22:13:37', 'KSR001', 300000.00);

-- ================================
-- Table structure for detail_transaksi
-- ================================
DROP TABLE IF EXISTS `detail_transaksi`;
CREATE TABLE `detail_transaksi` (
  `id_detail` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `id_transaksi` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `id_barang` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `jumlah` int NOT NULL,
  `harga_satuan` decimal(12, 2) NOT NULL,
  `subtotal` decimal(12, 2) NULL DEFAULT NULL,
  PRIMARY KEY (`id_detail`) USING BTREE,
  INDEX `idx_detail_transaksi`(`id_transaksi` ASC) USING BTREE,
  INDEX `idx_detail_barang`(`id_barang` ASC) USING BTREE,
  CONSTRAINT `detail_transaksi_ibfk_1` FOREIGN KEY (`id_transaksi`) REFERENCES `transaksi` (`id_transaksi`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `detail_transaksi_ibfk_2` FOREIGN KEY (`id_barang`) REFERENCES `barang` (`id_barang`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ================================
-- Records of detail_transaksi
-- ================================
INSERT INTO `detail_transaksi` VALUES ('DTL001', 'TRK001', 'ESK005', 60, 5000.00, 300000.00);

-- ================================
-- Table structure for pengeluaran
-- ================================
DROP TABLE IF EXISTS `pengeluaran`;
CREATE TABLE `pengeluaran` (
  `id_pengeluaran` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tgl_pengeluaran` date NOT NULL,
  `nominal` decimal(12, 2) NOT NULL,
  `jenis` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `id_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id_pengeluaran`) USING BTREE,
  INDEX `id_user`(`id_user` ASC) USING BTREE,
  CONSTRAINT `pengeluaran_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `user` (`id_user`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ================================
-- Records of pengeluaran
-- ================================
INSERT INTO `pengeluaran` VALUES ('PGN001', '2026-04-01', 100000.00, 'PLN', 'PMK001');
INSERT INTO `pengeluaran` VALUES ('PGN002', '2026-04-01', 326000.00, 'AIR', 'PMK001');
INSERT INTO `pengeluaran` VALUES ('PGN003', '2026-04-01', 30000.00, 'PLASTIK KATONGAN', 'PMK001');

-- ================================
-- Triggers structure for table detail_transaksi
-- ================================
DROP TRIGGER IF EXISTS `trg_subtotal`;
DELIMITER ;;
CREATE TRIGGER `trg_subtotal` BEFORE INSERT ON `detail_transaksi` FOR EACH ROW BEGIN
    SET NEW.subtotal = NEW.jumlah * NEW.harga_satuan;
END
;;
DELIMITER ;

DROP TRIGGER IF EXISTS `trg_update_total`;
DELIMITER ;;
CREATE TRIGGER `trg_update_total` AFTER INSERT ON `detail_transaksi` FOR EACH ROW BEGIN
    UPDATE transaksi
    SET total = (
        SELECT SUM(subtotal)
        FROM detail_transaksi
        WHERE id_transaksi = NEW.id_transaksi
    )
    WHERE id_transaksi = NEW.id_transaksi;
END
;;
DELIMITER ;

DROP TRIGGER IF EXISTS `trg_kurangi_stok`;
DELIMITER ;;
CREATE TRIGGER `trg_kurangi_stok` AFTER INSERT ON `detail_transaksi` FOR EACH ROW BEGIN
    UPDATE barang
    SET stok = stok - NEW.jumlah
    WHERE id_barang = NEW.id_barang;
END
;;
DELIMITER ;

SET FOREIGN_KEY_CHECKS = 1;

