package com.shop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity đại diện cho chi tiết từng sản phẩm trong đơn hàng
 */
@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Quan hệ nhiều-1 với Order */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** Quan hệ nhiều-1 với Product */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Số lượng đặt */
    @Column(nullable = false)
    private Integer quantity;

    /** Giá tại thời điểm đặt hàng (có thể khác giá hiện tại) */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /**
     * Tính thành tiền của item này
     * @return quantity * price
     */
    public BigDecimal getSubTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
