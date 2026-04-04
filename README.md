# Clothing Shop - Đồ Án Java Spring MVC

Dự án Web Application bán quần áo sử dụng Java Spring Boot, Spring MVC, Thymeleaf, MySQL.

---

## Công Nghệ Sử Dụng

- **Java 17** + **Spring Boot 3.2.0**
- **Spring MVC** + **Spring Security**
- **Spring Data JPA** + **Hibernate**
- **Thymeleaf** (template engine)
- **MySQL** (XAMPP)
- **Bootstrap 5.3.0**
- **Maven**

---

## Cài Đặt & Chạy Dự Án

## Deploy Railway

### 1. Chuẩn bị

1. Push source code lên GitHub (không commit `application-local.properties`).
2. Dự án đã có sẵn `railway.json` và `nixpacks.toml` để Railway build/start đúng Java 21.

### 2. Tạo project trên Railway

1. Vào Railway -> **New Project**.
2. Chọn **Provision MySQL**.
3. Chọn **Deploy from GitHub Repo** và chọn repository của dự án.
4. Mở service app và attach database MySQL vào app service.

### 3. Biến môi trường cần cấu hình

App đã hỗ trợ tự đọc biến `MYSQL*` của Railway, bạn chỉ cần bổ sung các biến ứng dụng sau (nếu cần):

- `SPRING_PROFILES_ACTIVE=prod`
- `APP_LOG_LEVEL=INFO`
- `SECURITY_LOG_LEVEL=INFO`
- `APP_BOOTSTRAP_USERS_ENABLED=true`
- `APP_BOOTSTRAP_ADMIN_USERNAME=admin`
- `APP_BOOTSTRAP_ADMIN_EMAIL=admin@shop.com`
- `APP_BOOTSTRAP_ADMIN_PASSWORD=admin123`
- `APP_BOOTSTRAP_ADMIN_FORCE_RESET_PASSWORD=false` (bật `true` 1 lần nếu cần ép reset mật khẩu admin)
- `GMAIL_APP_PASSWORD=<app-password-gmail>` (nếu dùng gửi mail bill)
- `APP_MAIL_BILL_ENABLED=false` (nếu chưa cấu hình mail)

Ghi chú: nếu DB Railway đã có user cũ với mật khẩu chưa hash BCrypt, app sẽ tự chuyển sang BCrypt lúc khởi động khi `APP_BOOTSTRAP_USERS_ENABLED=true`.

### 4. Import dữ liệu database

1. Vào service MySQL trong Railway.
2. Mở SQL console.
3. Import schema/data từ file `clothing_shop.sql`.

### 5. Truy cập ứng dụng

Sau khi deploy thành công, Railway cấp URL public dạng:

- `https://<your-app>.up.railway.app`

Health check endpoint:

- `/api/health`

### 6. Lưu ý production

Ứng dụng đang lưu ảnh upload vào thư mục local `uploads/`. Trên môi trường container (Railway), dữ liệu filesystem có thể mất sau khi redeploy/restart. Nên chuyển ảnh sang dịch vụ lưu trữ ngoài (S3/Cloudinary/Azure Blob) nếu dùng production.

### 1. Chuẩn Bị Database

1. Khởi động **XAMPP**, bật **Apache** và **MySQL**
2. Mở **phpMyAdmin** (`http://localhost/phpmyadmin`)
3. Import file SQL: **`clothing_shop.sql`** (ở thư mục gốc dự án)

### 2. Kiểm Tra Kết Nối Database

Mở file `src/main/resources/application.properties` và kiểm tra:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/clothing_shop?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=       # mặc định XAMPP không có password
```

### 3. Thêm Thư Mục Upload

Tạo thư mục `uploads/products/` trong thư mục gốc dự án (cùng cấp với `pom.xml`).

### 4. Thêm Ảnh Placeholder

Copy một ảnh tên `no-image.png` vào thư mục:
```
src/main/resources/static/images/no-image.png
```

### 5. Chạy Ứng Dụng

```bash
mvn spring-boot:run
```

Hoặc mở trong **IntelliJ IDEA** / **Eclipse** và chạy class `ClothingShopApplication.java`.

### 6. Truy Cập

| URL | Mô tả |
|-----|-------|
| `http://localhost:8080` | Trang chủ |
| `http://localhost:8080/auth/login` | Đăng nhập |
| `http://localhost:8080/admin` | Trang admin |
| `http://localhost:8080/api/products` | REST API |

---

## Tài Khoản Mẫu

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| User  | `nguyenvana` | `user123` |
| User  | `tranthib` | `user123` |

---

## Cấu Trúc Dự Án

```
src/main/java/com/shop/
├── ClothingShopApplication.java
├── config/
│   ├── SecurityConfig.java
│   └── WebMvcConfig.java
├── controller/
│   ├── HomeController.java
│   ├── AuthController.java
│   ├── ProductController.java
│   ├── CartController.java
│   ├── OrderController.java
│   ├── AdminController.java
│   └── ApiController.java          # REST API cho Android
├── model/
│   ├── User.java
│   ├── Category.java
│   ├── Product.java
│   ├── Order.java
│   ├── OrderItem.java
│   └── CartItem.java
├── repository/
│   ├── UserRepository.java
│   ├── CategoryRepository.java
│   ├── ProductRepository.java
│   ├── OrderRepository.java
│   ├── OrderItemRepository.java
│   └── CartItemRepository.java
└── service/
    ├── UserService.java
    ├── ProductService.java
    ├── CartService.java
    ├── OrderService.java
    └── CategoryService.java

src/main/resources/
├── application.properties
├── static/
│   ├── css/style.css
│   ├── js/main.js
│   └── images/no-image.png        # Cần thêm thủ công
└── templates/
    ├── fragments/
    │   ├── header.html
    │   └── footer.html
    ├── index.html
    ├── auth/login.html
    ├── auth/register.html
    ├── products/list.html
    ├── products/detail.html
    ├── cart/cart.html
    ├── orders/checkout.html
    ├── orders/history.html
    ├── orders/detail.html
    └── admin/
        ├── dashboard.html
        ├── products/list.html
        ├── products/form.html
        ├── categories/list.html
        ├── categories/form.html
        ├── orders/list.html
        ├── orders/detail.html
        └── users/list.html
```

---

## REST API Endpoints (cho Android)

| Method | URL | Mô tả |
|--------|-----|-------|
| GET | `/api/products` | Danh sách sản phẩm (có phân trang, filter) |
| GET | `/api/products/{id}` | Chi tiết sản phẩm |
| GET | `/api/categories` | Danh sách danh mục |
| POST | `/api/login` | Đăng nhập, trả về thông tin user |
| POST | `/api/register` | Đăng ký tài khoản |
| POST | `/api/orders` | Tạo đơn hàng mới |
| GET | `/api/orders/user/{userId}` | Lịch sử đơn hàng |
| POST | `/api/cart/add` | Thêm vào giỏ hàng |
| GET | `/api/cart/user/{userId}` | Xem giỏ hàng |
