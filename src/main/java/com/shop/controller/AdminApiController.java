package com.shop.controller;

import com.shop.model.Category;
import com.shop.model.Order;
import com.shop.model.Product;
import com.shop.model.User;
import com.shop.service.CategoryService;
import com.shop.service.OrderService;
import com.shop.service.ProductService;
import com.shop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST JSON cho app quản trị mobile. Bảo vệ: JWT + ROLE_ADMIN ({@code /api/admin/**}).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminApiController {

    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;
    private final CategoryService categoryService;

    @GetMapping("/dashboard/stats")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> dashboardStats() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalProducts", productService.countProducts());
        data.put("totalOrders", orderService.countOrders());
        data.put("totalRevenue", orderService.calculateTotalRevenue());
        data.put("totalUsers", userService.countUsers());
        data.put("pendingOrders", orderService.countByStatus(Order.OrderStatus.PENDING));
        data.put("confirmedOrders", orderService.countByStatus(Order.OrderStatus.CONFIRMED));
        data.put("shippingOrders", orderService.countByStatus(Order.OrderStatus.SHIPPING));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    /** Đặt trước /orders/{id} để không nhầm "recent" là id. */
    @GetMapping("/orders/recent")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> recentOrders(
            @RequestParam(defaultValue = "5") int limit) {
        List<Map<String, Object>> list = orderService.findRecentOrders(limit).stream()
                .map(this::orderToSummaryMap)
                .collect(Collectors.toList());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", list);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        int safeSize = Math.min(Math.max(size, 1), 200);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderService.findOrdersForAdmin(status, pageable);

        List<Map<String, Object>> list = orderPage.getContent().stream()
                .map(this::orderToSummaryMap)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", list);
        response.put("currentPage", orderPage.getNumber());
        response.put("totalItems", orderPage.getTotalElements());
        response.put("totalPages", orderPage.getTotalPages());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> orderDetail(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        return orderService.findById(id)
                .map(order -> {
                    response.put("success", true);
                    response.put("data", orderToDetailMap(order));
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    response.put("success", false);
                    response.put("message", "Không tìm thấy đơn hàng!");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        Map<String, Object> response = new HashMap<>();
        String raw = body.get("status");
        if (raw == null || raw.isBlank()) {
            response.put("success", false);
            response.put("message", "Trường status là bắt buộc");
            return ResponseEntity.badRequest().body(response);
        }
        try {
            Order.OrderStatus status = Order.OrderStatus.valueOf(raw.trim().toUpperCase());
            Order updated = orderService.updateStatus(id, status);
            response.put("success", true);
            response.put("message", "Đã cập nhật trạng thái");
            response.put("data", orderToSummaryMap(updated));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Trạng thái không hợp lệ: " + raw);
            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/products")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> listProducts() {
        List<Map<String, Object>> list = productService.findAllActiveForAdmin().stream()
                .map(this::productToMap)
                .collect(Collectors.toList());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", list);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getProduct(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        return productService.findById(id)
                .map(p -> {
                    response.put("success", true);
                    response.put("data", productToMap(p));
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    response.put("success", false);
                    response.put("message", "Không tìm thấy sản phẩm!");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }

    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<Map<String, Object>> createProduct(
            @RequestParam String name,
            @RequestParam BigDecimal price,
            @RequestParam(defaultValue = "0") int stock,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String sizes,
            @RequestParam(required = false) String colors,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(defaultValue = "false") boolean featured,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        Map<String, Object> response = new HashMap<>();
        try {
            Product product = new Product();
            product.setName(name.trim());
            product.setPrice(price);
            product.setStock(Math.max(stock, 0));
            product.setDescription(description != null ? description.trim() : null);
            product.setSizes(sizes != null ? sizes.trim() : null);
            product.setColors(colors != null ? colors.trim() : null);
            product.setActive(active);
            product.setFeatured(featured);
            if (categoryId != null && categoryId > 0) {
                categoryService.findById(categoryId).ifPresent(product::setCategory);
            }
            Product saved = productService.save(product, imageFile);
            response.put("success", true);
            response.put("message", "Đã tạo sản phẩm");
            response.put("data", productToMap(saved));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Lỗi upload ảnh: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping(value = "/products/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam BigDecimal price,
            @RequestParam(defaultValue = "0") int stock,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String sizes,
            @RequestParam(required = false) String colors,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(defaultValue = "false") boolean featured,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        Map<String, Object> response = new HashMap<>();
        try {
            Product product = productService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
            product.setName(name.trim());
            product.setPrice(price);
            product.setStock(Math.max(stock, 0));
            product.setDescription(description != null ? description.trim() : null);
            product.setSizes(sizes != null ? sizes.trim() : null);
            product.setColors(colors != null ? colors.trim() : null);
            product.setActive(active);
            product.setFeatured(featured);
            if (categoryId != null && categoryId > 0) {
                categoryService.findById(categoryId).ifPresent(product::setCategory);
            } else {
                product.setCategory(null);
            }
            Product saved = productService.save(product, imageFile);
            response.put("success", true);
            response.put("message", "Đã cập nhật sản phẩm");
            response.put("data", productToMap(saved));
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Lỗi upload ảnh: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/products/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            productService.deleteById(id);
            response.put("success", true);
            response.put("message", "Đã ngừng bán sản phẩm");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/categories")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> listCategories() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", categoryService.findAll());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/categories")
    public ResponseEntity<Map<String, Object>> createCategory(@Valid @RequestBody Category body) {
        Map<String, Object> response = new HashMap<>();
        try {
            body.setId(null);
            if (categoryService.existsByName(body.getName())) {
                response.put("success", false);
                response.put("message", "Tên danh mục đã tồn tại!");
                return ResponseEntity.badRequest().body(response);
            }
            Category saved = categoryService.save(body);
            response.put("success", true);
            response.put("message", "Đã tạo danh mục");
            response.put("data", saved);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody Category body) {
        Map<String, Object> response = new HashMap<>();
        try {
            Category existing = categoryService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));
            existing.setName(body.getName());
            existing.setDescription(body.getDescription());
            Category saved = categoryService.save(existing);
            response.put("success", true);
            response.put("message", "Đã cập nhật danh mục");
            response.put("data", saved);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            categoryService.deleteById(id);
            response.put("success", true);
            response.put("message", "Đã xóa danh mục");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/users")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> listUsers() {
        List<User> users = userService.findAll();
        long adminCount = users.stream().filter(u -> "ROLE_ADMIN".equals(u.getRole())).count();
        long userCount = users.stream().filter(u -> "ROLE_USER".equals(u.getRole())).count();
        List<Map<String, Object>> list = users.stream().map(this::userToMap).toList();

        Map<String, Object> data = new HashMap<>();
        data.put("users", list);
        data.put("adminCount", adminCount);
        data.put("userCount", userCount);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> userToMap(User u) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("email", u.getEmail());
        m.put("fullName", u.getFullName());
        m.put("role", u.getRole());
        m.put("enabled", u.isEnabled());
        return m;
    }

    private Map<String, Object> orderToSummaryMap(Order order) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", order.getId());
        m.put("totalPrice", order.getTotalPrice());
        m.put("status", order.getStatus().name());
        m.put("statusDisplay", order.getStatus().getDisplayName());
        m.put("createdAt", order.getCreatedAt());
        m.put("shippingAddress", order.getShippingAddress());
        m.put("phone", order.getPhone());
        m.put("username", order.getUser() != null ? order.getUser().getUsername() : null);
        m.put("userId", order.getUser() != null ? order.getUser().getId() : null);
        m.put("itemCount", order.getOrderItems() != null ? order.getOrderItems().size() : 0);
        return m;
    }

    private Map<String, Object> orderToDetailMap(Order order) {
        Map<String, Object> m = orderToSummaryMap(order);
        m.put("note", order.getNote());
        if (order.getOrderItems() != null) {
            List<Map<String, Object>> items = order.getOrderItems().stream().map(oi -> {
                Map<String, Object> im = new HashMap<>();
                im.put("productId", oi.getProduct().getId());
                im.put("productName", oi.getProduct().getName());
                im.put("quantity", oi.getQuantity());
                im.put("price", oi.getPrice());
                im.put("subTotal", oi.getSubTotal());
                return im;
            }).collect(Collectors.toList());
            m.put("items", items);
        } else {
            m.put("items", List.of());
        }
        return m;
    }

    private Map<String, Object> productToMap(Product p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("name", p.getName());
        m.put("description", p.getDescription());
        m.put("price", p.getPrice());
        m.put("stock", p.getStock());
        m.put("sizes", p.getSizes());
        m.put("colors", p.getColors());
        m.put("active", p.isActive());
        m.put("featured", p.isFeatured());
        m.put("imageUrl", p.getImageUrl());
        if (p.getCategory() != null) {
            m.put("categoryId", p.getCategory().getId());
            m.put("categoryName", p.getCategory().getName());
        } else {
            m.put("categoryId", null);
            m.put("categoryName", null);
        }
        return m;
    }
}
