package org.example.toywebsitebackend.repository;

import org.example.toywebsitebackend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // 按分类查询
    Page<Product> findByCategory(String category, Pageable pageable);
    
    // 搜索：名称或描述包含关键词
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);
    
    // 按分类和关键词搜索
    @Query("SELECT p FROM Product p WHERE " +
           "p.category = :category AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> findByCategoryAndKeyword(
        @Param("category") String category,
        @Param("keyword") String keyword,
        Pageable pageable
    );
}

