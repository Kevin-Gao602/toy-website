package org.example.toywebsitebackend.controller;

import org.example.toywebsitebackend.model.Product;
import org.example.toywebsitebackend.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 产品Controller
 * 提供产品相关的API接口
 * 
 * 数据流：前端 HTTP 请求 → Controller → Service → Repository → 数据库
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 获取产品列表（支持分页、搜索、分类筛选）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, resolveSort(sortBy, sortDir));
            Page<Product> productPage = productService.queryProducts(category, search, minPrice, maxPrice, pageable);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("content", productPage.getContent());
            response.put("totalElements", productPage.getTotalElements());
            response.put("totalPages", productPage.getTotalPages());
            response.put("size", productPage.getSize());
            response.put("number", productPage.getNumber());
            response.put("first", productPage.isFirst());
            response.put("last", productPage.isLast());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch products");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    private Sort resolveSort(String sortBy, String sortDir) {
        if (sortBy == null || sortBy.trim().isEmpty()) return Sort.unsorted();

        String field = sortBy.trim();
        // Whitelist to avoid invalid property paths
        if (!field.equals("price") && !field.equals("name") && !field.equals("createdAt")) {
            return Sort.unsorted();
        }

        Sort.Direction dir = Sort.Direction.ASC;
        if (sortDir != null && sortDir.equalsIgnoreCase("desc")) dir = Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    /**
     * 获取产品详情
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

