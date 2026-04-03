package com.shop.service;

import com.shop.model.*;
import com.shop.repository.OrderRepository;
import com.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service xử lý logic đặt hàng và quản lý đơn hàng
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final BillEmailService billEmailService;

    /**
     * Tạo đơn hàng mới từ giỏ hàng
     * @param user   người dùng đặt hàng
     * @param address địa chỉ giao hàng
     * @param phone   số điện thoại
     * @param note    ghi chú
     * @return Order đã được tạo
     */
    @Transactional
    public Order createOrder(User user, String address, String phone, String note) {
        // Lấy giỏ hàng của user
        List<CartItem> cartItems = cartService.getCartItems(user);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống!");
        }

        // Tạo đơn hàng mới
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(address);
        order.setPhone(phone);
        order.setNote(note);
        order.setStatus(Order.OrderStatus.PENDING);

        // Tạo danh sách OrderItems từ CartItems
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // Kiểm tra tồn kho
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Sản phẩm '" + product.getName() + "' không đủ số lượng!");
            }

            // Tạo OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice()); // Lưu giá tại thời điểm đặt hàng
            orderItems.add(orderItem);

            // Trừ tồn kho
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            // Cộng vào tổng tiền
            totalPrice = totalPrice.add(orderItem.getSubTotal());
        }

        order.setTotalPrice(totalPrice);
        order.setOrderItems(orderItems);

        // Lưu đơn hàng
        Order savedOrder = orderRepository.save(order);

        // Xóa giỏ hàng sau khi đặt hàng thành công
        cartService.clearCart(user);

        // Gửi email bill (không làm fail đơn hàng nếu gửi mail lỗi)
        try {
            billEmailService.sendOrderBill(savedOrder);
        } catch (Exception e) {
            log.warn("Không thể gửi email bill cho đơn #{}: {}", savedOrder.getId(), e.getMessage());
        }

        return savedOrder;
    }

    /**
     * Lấy lịch sử đơn hàng của user
     */
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Lấy chi tiết đơn hàng theo ID
     */
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * Lấy tất cả đơn hàng (cho admin)
     */
    public List<Order> findAll() {
        return orderRepository.findAll(
                org.springframework.data.domain.Sort.by("createdAt").descending());
    }

    /**
     * Đơn mới nhất (dashboard)
     */
    @Transactional(readOnly = true)
    public List<Order> findRecentOrders(int limit) {
        int safe = Math.min(Math.max(limit, 1), 50);
        Pageable p = PageRequest.of(0, safe, Sort.by("createdAt").descending());
        return orderRepository.findAll(p).getContent();
    }

    /**
     * Danh sách đơn admin có phân trang và lọc trạng thái (enum name, ví dụ PENDING).
     */
    @Transactional(readOnly = true)
    public Page<Order> findOrdersForAdmin(String statusFilter, Pageable pageable) {
        if (statusFilter == null || statusFilter.isBlank()) {
            return orderRepository.findAll(pageable);
        }
        try {
            Order.OrderStatus st = Order.OrderStatus.valueOf(statusFilter.trim().toUpperCase());
            return orderRepository.findByStatus(st, pageable);
        } catch (IllegalArgumentException e) {
            return orderRepository.findAll(pageable);
        }
    }

    /**
     * Cập nhật trạng thái đơn hàng (admin)
     */
    @Transactional
    public Order updateStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    /**
     * Tổng doanh thu (cho dashboard admin)
     */
    public BigDecimal calculateTotalRevenue() {
        return orderRepository.calculateTotalRevenue();
    }

    /**
     * Đếm tổng số đơn hàng (cho dashboard admin)
     */
    public long countOrders() {
        return orderRepository.count();
    }

    /**
     * Đếm đơn hàng theo trạng thái
     */
    public long countByStatus(Order.OrderStatus status) {
        return orderRepository.countByStatus(status);
    }
}
