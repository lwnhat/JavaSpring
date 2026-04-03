package com.shop.repository;

import com.shop.model.Order;
import com.shop.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository cho Order - thao tác với bảng orders
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /** Lấy tất cả đơn hàng của một user, sắp xếp mới nhất trước */
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    /** Đếm số đơn hàng theo trạng thái */
    long countByStatus(Order.OrderStatus status);

    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    /** Tính tổng doanh thu (chỉ tính đơn DELIVERED) */
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal calculateTotalRevenue();

    /** Thống kê đơn hàng theo tháng (cho dashboard) */
    @Query("SELECT MONTH(o.createdAt), COUNT(o) FROM Order o " +
           "WHERE YEAR(o.createdAt) = YEAR(CURRENT_DATE) GROUP BY MONTH(o.createdAt)")
    List<Object[]> countOrdersByMonth();
}
