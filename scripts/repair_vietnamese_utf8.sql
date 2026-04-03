-- Backup/repair helper for Vietnamese text corruption in clothing_shop
-- Run with: mysql -u root --default-character-set=utf8mb4 < scripts/repair_vietnamese_utf8.sql

USE clothing_shop;

ALTER DATABASE clothing_shop CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

ALTER TABLE categories CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE products CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE users CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE orders CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE order_items CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE cart_items CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Repair sample categories
UPDATE categories SET
  name = 'Áo Nam',
  description = 'Các loại áo dành cho nam giới'
WHERE id = 1;

UPDATE categories SET
  name = 'Quần Nam',
  description = 'Các loại quần dành cho nam giới'
WHERE id = 2;

UPDATE categories SET
  name = 'Áo Nữ',
  description = 'Các loại áo dành cho nữ giới'
WHERE id = 3;

UPDATE categories SET
  name = 'Quần Nữ',
  description = 'Các loại quần dành cho nữ giới'
WHERE id = 4;

UPDATE categories SET
  name = 'Phụ Kiện',
  description = 'Mũ, thắt lưng, túi xách và các phụ kiện khác'
WHERE id = 5;

-- Repair sample products
UPDATE products SET
  name = 'Áo Thun Nam Basic',
  description = 'Áo thun cotton 100%, thoáng mát',
  sizes = 'S,M,L,XL,XXL',
  colors = 'Trắng,Đen,Xám,Navy'
WHERE id = 1;

UPDATE products SET
  name = 'Áo Polo Nam',
  description = 'Áo polo cổ bẻ, vải pique cao cấp',
  sizes = 'S,M,L,XL,XXL',
  colors = 'Đen,Trắng,Đỏ,Xanh'
WHERE id = 2;

UPDATE products SET
  name = 'Áo Sơ Mi Nam Trắng',
  description = 'Áo sơ mi công sở, dễ phối đồ',
  sizes = 'S,M,L,XL',
  colors = 'Trắng,Xanh nhạt'
WHERE id = 3;

UPDATE products SET
  name = 'Áo Hoodie Nam',
  description = 'Áo hoodie nỉ ấm, có túi kangaroo',
  sizes = 'S,M,L,XL,XXL',
  colors = 'Đen,Xám,Navy'
WHERE id = 4;

UPDATE products SET
  name = 'Quần Jeans Nam Slim',
  description = 'Quần jeans co giãn, form slim fit',
  sizes = '28,29,30,31,32,33',
  colors = 'Xanh đậm,Đen'
WHERE id = 5;

UPDATE products SET
  name = 'Quần Kaki Nam',
  description = 'Quần kaki công sở, thoáng mát',
  sizes = '28,29,30,31,32',
  colors = 'Nâu,Đen,Kem'
WHERE id = 6;

UPDATE products SET
  name = 'Quần Short Nam',
  description = 'Quần short thể thao, vải dù',
  sizes = 'S,M,L,XL,XXL',
  colors = 'Đen,Xám,Xanh'
WHERE id = 7;

UPDATE products SET
  name = 'Áo Thun Nữ Croptop',
  description = 'Áo thun nữ ngắn, kiểu dáng trẻ trung',
  sizes = 'XS,S,M,L',
  colors = 'Trắng,Hồng,Đen'
WHERE id = 8;

UPDATE products SET
  name = 'Áo Sơ Mi Nữ',
  description = 'Áo sơ mi nữ công sở thanh lịch',
  sizes = 'XS,S,M,L',
  colors = 'Trắng,Xanh,Hồng'
WHERE id = 9;

UPDATE products SET
  name = 'Áo Blazer Nữ',
  description = 'Áo vest nữ, phong cách hiện đại',
  sizes = 'XS,S,M,L',
  colors = 'Đen,Xám,Kem'
WHERE id = 10;

UPDATE products SET
  name = 'Quần Skinny Nữ',
  description = 'Quần jeans skinny fit cho nữ',
  sizes = '24,25,26,27,28',
  colors = 'Đen,Xanh'
WHERE id = 11;

UPDATE products SET
  name = 'Chân Váy Midi',
  description = 'Chân váy dài, thanh lịch dễ phối',
  sizes = 'S,M,L',
  colors = 'Đen,Trắng,Hoa'
WHERE id = 12;

UPDATE products SET
  name = 'Mũ Lưỡi Trai',
  description = 'Mũ snapback unisex phong cách',
  sizes = 'Free Size',
  colors = 'Đen,Trắng,Navy'
WHERE id = 13;

UPDATE products SET
  name = 'Thắt Lưng Da',
  description = 'Thắt lưng da bò thật, bền đẹp',
  sizes = 'Free Size',
  colors = 'Đen,Nâu'
WHERE id = 14;

UPDATE products SET
  name = 'Túi Tote Nữ',
  description = 'Túi tote vải canvas thời trang',
  sizes = 'Free Size',
  colors = 'Trắng,Đen,Hồng'
WHERE id = 15;

-- Repair sample users
UPDATE users SET
  full_name = 'Nguyễn Văn A',
  address = '123 Nguyễn Huệ, Q1, TP.HCM'
WHERE username = 'nguyenvana';

UPDATE users SET
  full_name = 'Trần Thị B',
  address = '456 Lê Lợi, Q3, TP.HCM'
WHERE username = 'tranthib';
