package com.shop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho người dùng hệ thống
 * Gồm cả khách hàng (ROLE_USER) và admin (ROLE_ADMIN)
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên đăng nhập - phải là duy nhất */
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập từ 3-50 ký tự")
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /** Email - phải là duy nhất */
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    /** Mật khẩu đã được mã hóa (BCrypt) */
    @NotBlank(message = "Mật khẩu không được để trống")
    @Column(nullable = false)
    @JsonIgnore
    private String password;

    /** Vai trò: ROLE_USER hoặc ROLE_ADMIN */
    @Column(nullable = false, length = 20)
    private String role = "ROLE_USER";

    /** Họ tên đầy đủ */
    @Column(name = "full_name", length = 100)
    private String fullName;

    /** Số điện thoại */
    @Column(name = "phone", length = 15)
    private String phone;

    /** Địa chỉ */
    @Column(name = "address", length = 255)
    private String address;

    /** Tên file ảnh avatar (lưu trong uploads/avatars/) */
    @Column(name = "avatar", length = 255)
    private String avatar;

    /** Thời gian tạo tài khoản */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Tài khoản có được kích hoạt không */
    @Column(nullable = false)
    private boolean enabled = true;

    // Quan hệ 1-nhiều với Orders
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

    // Quan hệ 1-nhiều với CartItems
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartItem> cartItems;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * URL hiển thị avatar; nếu chưa có ảnh thì dùng ảnh mặc định.
     */
    public String getAvatarUrl() {
        if (avatar == null || avatar.isBlank()) {
            return "/images/no-image.svg";
        }
        String a = avatar.trim();
        if (a.startsWith("http://") || a.startsWith("https://") || a.startsWith("/")) {
            return a;
        }
        return "/uploads/avatars/" + a;
    }
}
