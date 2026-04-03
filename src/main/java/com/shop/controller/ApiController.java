package com.shop.controller;

import com.shop.model.*;
import com.shop.security.JwtService;
import com.shop.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * REST API Controller cho Android App
 * Tất cả response đều là JSON
 * Base URL: /api
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final OrderService orderService;
    private final CartService cartService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // ==========================================
    // PRODUCT APIs
    // ==========================================

    /**
     * GET /api/health
     * Kiểm tra server & mạng (không cần MySQL) — dùng cho app Android / LDPlayer
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "OK");
        response.put("data", Map.of(
                "app", "clothing-shop",
                "time", System.currentTimeMillis()
        ));
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/products
     * Lấy danh sách sản phẩm với phân trang và tìm kiếm
     */
    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Product> productPage = productService.searchProducts(keyword, categoryId, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", productPage.getContent());
        response.put("currentPage", productPage.getNumber());
        response.put("totalItems", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/products/{id}
     * Lấy chi tiết sản phẩm theo ID
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        return productService.findById(id)
                .map(product -> {
                    response.put("success", true);
                    response.put("data", product);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    response.put("success", false);
                    response.put("message", "Không tìm thấy sản phẩm!");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }

    // ==========================================
    // CATEGORY APIs
    // ==========================================

    /**
     * GET /api/categories
     * Lấy tất cả danh mục
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategories() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", categoryService.findAll());
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // AUTH APIs
    // ==========================================

    /**
     * POST /api/login
     * Đăng nhập từ Android App
     * Body: { "username": "...", "password": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> loginRequest) {

        Map<String, Object> response = new HashMap<>();

        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        if (username == null || password == null) {
            response.put("success", false);
            response.put("message", "Username và password không được để trống!");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Xác thực với Spring Security
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            // Lấy thông tin user
            User user = userService.findByUsername(username).orElseThrow();

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("fullName", user.getFullName());
            userData.put("role", user.getRole());
            userData.put("phone", user.getPhone());
            userData.put("address", user.getAddress());
            userData.put("avatarUrl", user.getAvatarUrl());

            response.put("success", true);
            response.put("message", "Đăng nhập thành công!");
            response.put("data", userData);
            response.put("accessToken", jwtService.generateToken(user));

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            response.put("success", false);
            response.put("message", "Tên đăng nhập hoặc mật khẩu không đúng!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * POST /api/register
     * Đăng ký tài khoản từ Android App
     * Body: { "username": "...", "email": "...", "password": "..." }
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody Map<String, String> registerRequest) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = new User();
            user.setUsername(registerRequest.get("username"));
            user.setEmail(registerRequest.get("email"));
            user.setPassword(registerRequest.get("password"));
            user.setFullName(registerRequest.getOrDefault("fullName", ""));
            user.setPhone(registerRequest.getOrDefault("phone", ""));

            User savedUser = userService.register(user);

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", savedUser.getId());
            userData.put("username", savedUser.getUsername());
            userData.put("email", savedUser.getEmail());
            userData.put("avatarUrl", savedUser.getAvatarUrl());

            response.put("success", true);
            response.put("message", "Đăng ký thành công!");
            response.put("data", userData);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/profile/avatar
     * Đổi avatar (multipart). Form: userId + file
     */
    @PostMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @RequestParam Long userId,
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();
        try {
            User updated = userService.updateAvatar(userId, file);
            response.put("success", true);
            response.put("message", "Đã cập nhật ảnh đại diện!");
            response.put("avatarUrl", updated.getAvatarUrl());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Lỗi lưu file: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==========================================
    // ORDER APIs
    // ==========================================

    /**
     * POST /api/orders
     * Tạo đơn hàng mới từ Android
     * Body: { "userId": 1, "address": "...", "phone": "...", "note": "..." }
     */
    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestBody Map<String, Object> orderRequest) {

        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = Long.valueOf(orderRequest.get("userId").toString());
            String address = (String) orderRequest.get("address");
            String phone = (String) orderRequest.get("phone");
            String note = (String) orderRequest.getOrDefault("note", "");

            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

            // Android app gửi items trực tiếp từ local cart.
            // Đồng bộ các items này vào server cart trước khi gọi luồng tạo đơn hiện có.
            Object rawItems = orderRequest.get("items");
            if (rawItems instanceof List<?> items && !items.isEmpty()) {
                cartService.clearCart(user);
                for (Object itemObj : items) {
                    if (!(itemObj instanceof Map<?, ?> itemMap)) {
                        continue;
                    }

                    int quantity = 1;
                    Object quantityObj = itemMap.get("quantity");
                    if (quantityObj instanceof Number number) {
                        quantity = number.intValue();
                    }

                    Long productId = null;
                    Object productIdObj = itemMap.get("productId");
                    if (productIdObj instanceof Number number) {
                        productId = number.longValue();
                    } else {
                        Object productObj = itemMap.get("product");
                        if (productObj instanceof Map<?, ?> productMap) {
                            Object idObj = productMap.get("id");
                            if (idObj instanceof Number number) {
                                productId = number.longValue();
                            }
                        }
                    }

                    if (productId != null && quantity > 0) {
                        cartService.addToCart(user, productId, quantity);
                    }
                }
            }

            Order order = orderService.createOrder(user, address, phone, note);

            response.put("success", true);
            response.put("message", "Đặt hàng thành công!");
            response.put("orderId", order.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * GET /api/orders/user/{userId}
     * Lấy lịch sử đơn hàng của user
     */
    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<Map<String, Object>> getOrdersByUser(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

            List<Order> orders = orderService.getOrdersByUser(user);

            // Chuyển đổi thành dạng đơn giản hơn để tránh vòng lặp JSON
            List<Map<String, Object>> orderList = orders.stream().map(order -> {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("id", order.getId());
                orderMap.put("totalPrice", order.getTotalPrice());
                orderMap.put("status", order.getStatus().name());
                orderMap.put("statusDisplay", order.getStatus().getDisplayName());
                orderMap.put("createdAt", order.getCreatedAt());
                orderMap.put("shippingAddress", order.getShippingAddress());
                orderMap.put("itemCount", order.getOrderItems().size());
                return orderMap;
            }).toList();

            response.put("success", true);
            response.put("data", orderList);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/cart/add
     * Thêm sản phẩm vào giỏ hàng qua API
     */
    @PostMapping("/cart/add")
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestBody Map<String, Object> cartRequest) {

        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = Long.valueOf(cartRequest.get("userId").toString());
            Long productId = Long.valueOf(cartRequest.get("productId").toString());
            int quantity = Integer.parseInt(cartRequest.getOrDefault("quantity", "1").toString());

            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

            cartService.addToCart(user, productId, quantity);

            response.put("success", true);
            response.put("message", "Đã thêm vào giỏ hàng!");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * GET /api/cart/user/{userId}
     * Lấy giỏ hàng của user
     */
    @GetMapping("/cart/user/{userId}")
    public ResponseEntity<Map<String, Object>> getCart(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

            List<CartItem> cartItems = cartService.getCartItems(user);

            List<Map<String, Object>> items = cartItems.stream().map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("id", item.getId());
                itemMap.put("productId", item.getProduct().getId());
                itemMap.put("productName", item.getProduct().getName());
                itemMap.put("price", item.getProduct().getPrice());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("subTotal", item.getSubTotal());
                itemMap.put("image", item.getProduct().getImageUrl());
                return itemMap;
            }).toList();

            response.put("success", true);
            response.put("data", items);
            response.put("total", cartService.calculateTotal(cartItems));
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
