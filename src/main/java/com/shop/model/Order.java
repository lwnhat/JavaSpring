package com.shop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho đơn hàng
 * Mỗi đơn hàng thuộc về một người dùng
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Quan hệ nhiều-1 với User */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Tổng tiền đơn hàng */
    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    /**
     * Trạng thái đơn hàng:
     * PENDING    - Chờ xác nhận
     * CONFIRMED  - Đã xác nhận
     * SHIPPING   - Đang giao hàng
     * DELIVERED  - Đã giao hàng
     * CANCELLED  - Đã hủy
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    /** Địa chỉ giao hàng */
    @Column(name = "shipping_address", length = 255)
    private String shippingAddress;

    /** Số điện thoại nhận hàng */
    @Column(name = "phone", length = 15)
    private String phone;

    /** Ghi chú đơn hàng */
    @Column(name = "note", length = 500)
    private String note;

    /** Thời gian đặt hàng */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Quan hệ 1-nhiều với OrderItems */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Enum trạng thái đơn hàng
     */
    public enum OrderStatus {
        PENDING("Chờ xác nhận"),
        CONFIRMED("Đã xác nhận"),
        SHIPPING("Đang giao hàng"),
        DELIVERED("Đã giao hàng"),
        CANCELLED("Đã hủy");

        private final String displayName;

        OrderStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
