package com.shop.repository;

import com.shop.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository cho OrderItem - thao tác với bảng order_items
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Spring Data JPA cung cấp đủ các phương thức CRUD cơ bản
    // Có thể thêm custom query nếu cần
}
