package com.shop.service;

import com.shop.model.CartItem;
import com.shop.model.Product;
import com.shop.model.User;
import com.shop.repository.CartItemRepository;
import com.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service xử lý logic giỏ hàng
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    /**
     * Lấy tất cả items trong giỏ hàng của user
     */
    public List<CartItem> getCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     * Nếu sản phẩm đã có trong giỏ thì cộng thêm số lượng
     */
    @Transactional
    public void addToCart(User user, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        // Kiểm tra tồn kho
        if (product.getStock() < quantity) {
            throw new RuntimeException("Số lượng trong kho không đủ!");
        }

        // Kiểm tra xem sản phẩm đã có trong giỏ chưa
        Optional<CartItem> existingItem = cartItemRepository.findByUserAndProduct(user, product);

        if (existingItem.isPresent()) {
            // Cộng thêm số lượng
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            if (newQuantity > product.getStock()) {
                throw new RuntimeException("Số lượng trong kho không đủ!");
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            // Thêm mới vào giỏ
            CartItem cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    @Transactional
    public void updateQuantity(User user, Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy item trong giỏ!"));

        // Kiểm tra quyền sở hữu
        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Không có quyền thao tác!");
        }

        if (quantity <= 0) {
            // Nếu số lượng = 0 thì xóa khỏi giỏ
            cartItemRepository.delete(cartItem);
        } else {
            if (quantity > cartItem.getProduct().getStock()) {
                throw new RuntimeException("Số lượng trong kho không đủ!");
            }
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    @Transactional
    public void removeFromCart(User user, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy item trong giỏ!"));

        // Kiểm tra quyền sở hữu
        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Không có quyền thao tác!");
        }

        cartItemRepository.delete(cartItem);
    }

    /**
     * Xóa toàn bộ giỏ hàng (sau khi đặt hàng)
     */
    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }

    /**
     * Tính tổng tiền giỏ hàng
     */
    public BigDecimal calculateTotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(CartItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Đếm số món trong giỏ hàng
     */
    public long countItems(User user) {
        return cartItemRepository.countByUser(user);
    }
}
