package com.shop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity đại diện cho sản phẩm trong giỏ hàng
 * Giỏ hàng được lưu vào database (persistent cart)
 */
@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Quan hệ nhiều-1 với User */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Quan hệ nhiều-1 với Product */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Số lượng sản phẩm trong giỏ */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Tính thành tiền của item này
     * @return product.price * quantity
     */
    public BigDecimal getSubTotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
