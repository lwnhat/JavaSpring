package com.shop.service;

import com.shop.model.Category;
import com.shop.model.Product;
import com.shop.repository.CategoryRepository;
import com.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Service xử lý logic liên quan đến sản phẩm
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // Thư mục lưu ảnh upload
    private static final String UPLOAD_DIR = "uploads/products/";

    /**
     * Lấy tất cả sản phẩm đang active với phân trang
     */
    public Page<Product> findAllActive(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findByActiveTrue(pageable);
    }

    /**
     * Tìm kiếm sản phẩm theo keyword và category với phân trang
     */
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String keyword, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (categoryId != null && categoryId > 0) {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category == null) return Page.empty();

            if (keyword != null && !keyword.trim().isEmpty()) {
                // Tìm theo cả keyword và category
                return productRepository.findByNameContainingIgnoreCaseAndCategoryAndActiveTrue(
                        keyword, category, pageable);
            } else {
                // Chỉ tìm theo category
                return productRepository.findByCategoryAndActiveTrue(category, pageable);
            }
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            // Chỉ tìm theo keyword
            return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword, pageable);
        }

        // Không có filter - trả về tất cả
        return productRepository.findByActiveTrue(pageable);
    }

    /**
     * Lấy sản phẩm theo ID
     */
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Lấy sản phẩm nổi bật
     */
    public List<Product> findFeatured() {
        return productRepository.findByFeaturedTrueAndActiveTrue();
    }

    /**
     * Lấy sản phẩm mới nhất
     */
    public List<Product> findNewest() {
        return productRepository.findTop8ByActiveTrueOrderByCreatedAtDesc();
    }

    /**
     * Lấy tất cả sản phẩm (cho admin)
     */
    public List<Product> findAll() {
        return productRepository.findAll(Sort.by("createdAt").descending());
    }

    /**
     * Danh sách sản phẩm đang hiển thị trong admin (đã loại bỏ sản phẩm đã xóa mềm)
     */
    public List<Product> findAllActiveForAdmin() {
        return productRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    /**
     * Lưu hoặc cập nhật sản phẩm
     * Xử lý cả upload ảnh
     */
    @Transactional
    public Product save(Product product, MultipartFile imageFile) throws IOException {
        // Nếu có file ảnh mới được upload
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageName = uploadImage(imageFile);
            product.setImage(imageName);
        }
        return productRepository.save(product);
    }

    /**
     * Xóa sản phẩm (soft delete - chỉ đặt active = false)
     */
    @Transactional
    public void deleteById(Long id) {
        productRepository.findById(id).ifPresent(product -> {
            product.setActive(false);
            productRepository.save(product);
        });
    }

    /**
     * Upload ảnh sản phẩm lên server
     * @param file file ảnh từ form
     * @return tên file đã được lưu
     */
    public String uploadImage(MultipartFile file) throws IOException {
        // Tạo thư mục nếu chưa có
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Tạo tên file unique để tránh trùng
        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".jpg";
        String fileName = UUID.randomUUID().toString() + extension;

        // Lưu file vào thư mục
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    /**
     * Đếm sản phẩm đang bán (cho dashboard admin; khớp với danh sách sau khi xóa mềm)
     */
    public long countProducts() {
        return productRepository.countByActiveTrue();
    }

    /**
     * Lấy sản phẩm theo danh mục
     */
    public List<Product> findByCategory(Category category) {
        return productRepository.findByCategoryAndActiveTrue(category);
    }
}
