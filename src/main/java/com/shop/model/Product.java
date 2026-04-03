package com.shop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Entity đại diện cho sản phẩm quần áo
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {

    /** Tên file do admin upload (UUID + đuôi) — phục vụ phân biệt với ảnh đặt sẵn trong /images/products/ */
    private static final Pattern UPLOADED_IMAGE_NAME = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.[^/]+$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên sản phẩm */
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 200, message = "Tên sản phẩm tối đa 200 ký tự")
    @Column(nullable = false, length = 200)
    private String name;

    /** Mô tả chi tiết sản phẩm */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Giá sản phẩm */
    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** Tên file ảnh (lưu trong thư mục uploads) */
    @Column(name = "image", length = 255)
    private String image;

    /** Số lượng tồn kho */
    @Min(value = 0, message = "Số lượng phải >= 0")
    @Column(nullable = false)
    private Integer stock = 0;

    /** Size có sẵn (ví dụ: S, M, L, XL, XXL) */
    @Column(name = "sizes", length = 100)
    private String sizes;

    /** Màu sắc */
    @Column(name = "colors", length = 100)
    private String colors;

    /** Trạng thái: true = đang bán, false = ngừng bán */
    @Column(nullable = false)
    private boolean active = true;

    /** Sản phẩm nổi bật không */
    @Column(name = "featured")
    private boolean featured = false;

    /** Thời gian thêm sản phẩm */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Quan hệ nhiều-1 với Category */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /** Quan hệ 1-nhiều với OrderItems */
    @JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    /** Quan hệ 1-nhiều với CartItems */
    @JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartItem> cartItems;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Trả về đường dẫn ảnh để hiển thị trên web
     * - URL tuyệt đối hoặc bắt đầu bằng / : dùng nguyên giá trị
     * - Tên file upload (UUID...): /uploads/products/
     * - Tên file thường (ảnh đặt trong static/images/products): /images/products/
     * - Không có ảnh: ảnh mặc định /images/products/t1.png … t12.png (xoay vòng theo id sản phẩm)
     */
    public String getImageUrl() {
        if (image != null && !image.isBlank()) {
            String img = image.trim();
            if (img.startsWith("http://") || img.startsWith("https://") || img.startsWith("/")) {
                return img;
            }
            if (UPLOADED_IMAGE_NAME.matcher(img).matches()) {
                return "/uploads/products/" + img;
            }
            return "/images/products/" + img;
        }

        long pid = (id != null && id > 0) ? id : 1L;
        int slot = (int) ((pid - 1) % 12 + 1);
        return "/images/products/t" + slot + ".png";
    }
}
