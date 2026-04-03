package com.shop.service;

import com.shop.model.Category;
import com.shop.repository.CategoryRepository;
import com.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service xử lý logic quản lý danh mục sản phẩm
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    /**
     * Lấy tất cả danh mục
     */
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    /**
     * Lấy danh mục theo ID
     */
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    /**
     * Lưu danh mục mới hoặc cập nhật
     */
    @Transactional
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * Xóa danh mục theo ID
     */
    @Transactional
    public void deleteById(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy danh mục!");
        }
        if (productRepository.countForCategoryId(id) > 0) {
            throw new RuntimeException("Không thể xóa danh mục đang có sản phẩm!");
        }
        categoryRepository.deleteById(id);
    }

    /**
     * Kiểm tra tên danh mục đã tồn tại chưa
     */
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}
