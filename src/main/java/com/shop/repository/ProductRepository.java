package com.shop.repository;

import com.shop.model.Category;
import com.shop.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho Product - thao tác với bảng products
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Tìm sản phẩm đang active với phân trang */
    Page<Product> findByActiveTrue(Pageable pageable);

    /** Tìm sản phẩm theo danh mục với phân trang */
    Page<Product> findByCategoryAndActiveTrue(Category category, Pageable pageable);

    /** Tìm kiếm sản phẩm theo tên (không phân biệt hoa thường) với phân trang */
    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    /** Tìm theo tên và danh mục */
    Page<Product> findByNameContainingIgnoreCaseAndCategoryAndActiveTrue(
            String name, Category category, Pageable pageable);

    /** Sản phẩm nổi bật */
    List<Product> findByFeaturedTrueAndActiveTrue();

    /** Sản phẩm mới nhất */
    List<Product> findTop8ByActiveTrueOrderByCreatedAtDesc();

    /** Tìm sản phẩm theo danh mục (không phân trang) */
    List<Product> findByCategoryAndActiveTrue(Category category);

    /** Danh sách sản phẩm đang bán (cho admin) */
    List<Product> findByActiveTrueOrderByCreatedAtDesc();

    /** Đếm sản phẩm đang bán */
    long countByActiveTrue();

    /**
     * Tìm kiếm sản phẩm bằng JPQL
     * Tìm theo tên hoặc mô tả
     */
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countForCategoryId(@Param("categoryId") Long categoryId);
}
