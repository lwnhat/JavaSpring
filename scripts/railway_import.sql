-- ============================================================
-- CLOTHING SHOP DATABASE
-- Đồ Án Đại Học Java Spring MVC
-- Sử dụng với XAMPP MySQL
-- ============================================================

-- ============================================================
-- BẢNG USERS
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
    full_name   VARCHAR(100),
    phone       VARCHAR(20),
    address     TEXT,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    enabled     TINYINT(1)   NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- BẢNG CATEGORIES
-- ============================================================
CREATE TABLE IF NOT EXISTS categories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- BẢNG PRODUCTS
-- ============================================================
CREATE TABLE IF NOT EXISTS products (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    price       DECIMAL(12,2) NOT NULL DEFAULT 0,
    image       VARCHAR(255),
    stock       INT          NOT NULL DEFAULT 0,
    sizes       VARCHAR(255),
    colors      VARCHAR(255),
    active      TINYINT(1)   NOT NULL DEFAULT 1,
    featured    TINYINT(1)   NOT NULL DEFAULT 0,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    category_id BIGINT,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- BẢNG ORDERS
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    total_price     DECIMAL(12,2) NOT NULL DEFAULT 0,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    full_name       VARCHAR(100),
    phone           VARCHAR(20),
    address         TEXT,
    note            TEXT,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- BẢNG ORDER_ITEMS
-- ============================================================
CREATE TABLE IF NOT EXISTS order_items (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id    BIGINT NOT NULL,
    product_id  BIGINT,
    product_name VARCHAR(255),
    size        VARCHAR(20),
    price       DECIMAL(12,2) NOT NULL DEFAULT 0,
    quantity    INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_orderitem_order   FOREIGN KEY (order_id)   REFERENCES orders(id)   ON DELETE CASCADE,
    CONSTRAINT fk_orderitem_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- BẢNG CART_ITEMS
-- ============================================================
CREATE TABLE IF NOT EXISTS cart_items (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    product_id  BIGINT NOT NULL,
    quantity    INT NOT NULL DEFAULT 1,
    size        VARCHAR(20),
    CONSTRAINT fk_cart_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- DỮ LIỆU MẪU
-- ============================================================

-- Tài khoản ADMIN
-- password: admin123  (BCrypt hash)
INSERT INTO users (username, email, password, role, full_name, enabled) VALUES
('admin', 'admin@shop.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'ROLE_ADMIN', 'Administrator', 1);

-- Tài khoản người dùng mẫu
-- password: user123  (BCrypt hash)
INSERT INTO users (username, email, password, role, full_name, phone, address, enabled) VALUES
('nguyenvana', 'vana@example.com',
 '$2a$10$TDSHNBiK5kpQdnr6K7JxMuoLN8PJlO7LpwJ2m3X.5jEfxwBhD5.ru',
 'ROLE_USER', 'Nguyễn Văn A', '0901234561', '123 Nguyễn Huệ, Q1, TP.HCM', 1),
('tranthib', 'thib@example.com',
 '$2a$10$TDSHNBiK5kpQdnr6K7JxMuoLN8PJlO7LpwJ2m3X.5jEfxwBhD5.ru',
 'ROLE_USER', 'Trần Thị B', '0901234562', '456 Lê Lợi, Q3, TP.HCM', 1);

-- Danh mục
INSERT INTO categories (name, description) VALUES
('Áo Nam',    'Các loại áo dành cho nam giới'),
('Quần Nam',  'Các loại quần dành cho nam giới'),
('Áo Nữ',    'Các loại áo dành cho nữ giới'),
('Quần Nữ',  'Các loại quần dành cho nữ giới'),
('Phụ Kiện', 'Mũ, thắt lưng, túi xách và các phụ kiện khác');

-- Sản phẩm mẫu
INSERT INTO products (name, description, price, stock, sizes, colors, active, featured, category_id) VALUES
('Áo Thun Nam Basic',     'Áo thun cotton 100%, thoáng mát',             250000, 100, 'S,M,L,XL,XXL', 'Trắng,Đen,Xám,Navy', 1, 1, 1),
('Áo Polo Nam',           'Áo polo cổ bẻ, vải pique cao cấp',           350000,  80, 'S,M,L,XL,XXL', 'Đen,Trắng,Đỏ,Xanh', 1, 1, 1),
('Áo Sơ Mi Nam Trắng',   'Áo sơ mi công sở, dễ phối đồ',               450000,  60, 'S,M,L,XL',     'Trắng,Xanh nhạt',   1, 0, 1),
('Áo Hoodie Nam',         'Áo hoodie nỉ ấm, có túi kangaroo',           480000,  50, 'S,M,L,XL,XXL', 'Đen,Xám,Navy',      1, 1, 1),
('Quần Jeans Nam Slim',   'Quần jeans co giãn, form slim fit',           550000,  70, '28,29,30,31,32,33', 'Xanh đậm,Đen',  1, 1, 2),
('Quần Kaki Nam',         'Quần kaki công sở, thoáng mát',              420000,  90, '28,29,30,31,32',    'Nâu,Đen,Kem',   1, 0, 2),
('Quần Short Nam',        'Quần short thể thao, vải dù',                280000, 120, 'S,M,L,XL,XXL', 'Đen,Xám,Xanh',     1, 0, 2),
('Áo Thun Nữ Croptop',   'Áo thun nữ ngắn, kiểu dáng trẻ trung',      220000, 100, 'XS,S,M,L',     'Trắng,Hồng,Đen',   1, 1, 3),
('Áo Sơ Mi Nữ',          'Áo sơ mi nữ công sở thanh lịch',             380000,  60, 'XS,S,M,L',     'Trắng,Xanh,Hồng',  1, 0, 3),
('Áo Blazer Nữ',         'Áo vest nữ, phong cách hiện đại',            650000,  40, 'XS,S,M,L',     'Đen,Xám,Kem',       1, 1, 3),
('Quần Skinny Nữ',       'Quần jeans skinny fit cho nữ',               480000,  80, '24,25,26,27,28', 'Đen,Xanh',         1, 1, 4),
('Chân Váy Midi',        'Chân váy dài, thanh lịch dễ phối',           320000,  70, 'S,M,L',        'Đen,Trắng,Hoa',     1, 0, 4),
('Mũ Lưỡi Trai',        'Mũ snapback unisex phong cách',               150000, 200, 'Free Size',    'Đen,Trắng,Navy',    1, 0, 5),
('Thắt Lưng Da',         'Thắt lưng da bò thật, bền đẹp',             280000, 150, 'Free Size',    'Đen,Nâu',           1, 0, 5),
('Túi Tote Nữ',          'Túi tote vải canvas thời trang',            180000, 120, 'Free Size',    'Trắng,Đen,Hồng',    1, 1, 5);
