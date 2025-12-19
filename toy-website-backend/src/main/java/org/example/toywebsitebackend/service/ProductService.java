package org.example.toywebsitebackend.service;

import org.example.toywebsitebackend.model.Product;
import org.example.toywebsitebackend.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 产品服务层
 * 处理产品相关的业务逻辑
 */
@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 获取所有产品（分页）
     */
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    /**
     * 按分类获取产品（分页）
     */
    public Page<Product> getProductsByCategory(String category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable);
    }

    /**
     * 搜索产品（分页）
     */
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchProducts(keyword, pageable);
    }

    /**
     * 按分类和关键词搜索产品（分页）
     */
    public Page<Product> getProductsByCategoryAndKeyword(String category, String keyword, Pageable pageable) {
        return productRepository.findByCategoryAndKeyword(category, keyword, pageable);
    }

    /**
     * 根据ID获取产品
     */
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * 获取产品总数
     */
    @Transactional(readOnly = true)
    public long getProductCount() {
        return productRepository.count();
    }
}

