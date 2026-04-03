package com.shop.repository;

import com.shop.model.CartItem;
import com.shop.model.Product;
import com.shop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho CartItem - thao tác với bảng cart_items
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /** Lấy tất cả items trong giỏ hàng của một user */
    List<CartItem> findByUser(User user);

    /** Tìm cart item theo user và product (để cập nhật số lượng) */
    Optional<CartItem> findByUserAndProduct(User user, Product product);

    /** Xóa toàn bộ giỏ hàng của user (sau khi đặt hàng) */
    void deleteByUser(User user);

    /** Đếm số món trong giỏ hàng của user */
    long countByUser(User user);
}
