package com.shop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Entity đại diện cho danh mục sản phẩm
 * Ví dụ: Áo, Quần, Váy, Phụ kiện...
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên danh mục */
    @NotBlank(message = "Tên danh mục không được để trống")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /** Mô tả danh mục */
    @Column(length = 255)
    private String description;

    // Quan hệ 1-nhiều với Products
    @JsonIgnore
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;
}
