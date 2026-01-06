package org.example.toywebsitebackend.service;

import org.example.toywebsitebackend.model.Product;
import org.example.toywebsitebackend.repository.ProductRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Expression;
import java.math.BigDecimal;
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
     * 统一查询：可选 category/search/minPrice/maxPrice + pageable(sort)
     */
    @Transactional(readOnly = true)
    public Page<Product> queryProducts(String category, String search, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Specification<Product> spec = Specification.where(null);

        if (category != null && !category.trim().isEmpty()) {
            String c = category.trim();
            spec = spec.and((root, q, cb) -> cb.equal(root.get("category"), c));
        }

        if (search != null && !search.trim().isEmpty()) {
            String kw = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> {
                Expression<String> name = cb.lower(cb.coalesce(root.get("name"), ""));
                Expression<String> desc = cb.lower(cb.coalesce(root.get("description"), ""));
                return cb.or(cb.like(name, kw), cb.like(desc, kw));
            });
        }

        if (minPrice != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        return productRepository.findAll(spec, pageable);
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

