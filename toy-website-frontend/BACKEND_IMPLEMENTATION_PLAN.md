# 后端实现建议 (Backend Implementation Plan)

基于 PRD_Toy_Shop.md 的 Spring Boot 后端实现建议

## 📋 技术栈

- **框架**: Spring Boot 3.x
- **数据库**: H2 (开发) / PostgreSQL (生产)
- **ORM**: Spring Data JPA
- **安全**: Spring Security + JWT
- **构建工具**: Maven / Gradle
- **测试**: JUnit 5 + Mockito

---

## 🏗️ 项目结构建议

```
toy-website-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── toyshop/
│   │   │           ├── ToyShopApplication.java
│   │   │           │
│   │   │           ├── config/              # 配置类
│   │   │           │   ├── SecurityConfig.java
│   │   │           │   ├── JwtConfig.java
│   │   │           │   └── WebConfig.java   # CORS 配置
│   │   │           │
│   │   │           ├── entity/              # 实体类
│   │   │           │   ├── User.java
│   │   │           │   ├── Product.java
│   │   │           │   ├── Cart.java
│   │   │           │   ├── CartItem.java
│   │   │           │   ├── Order.java
│   │   │           │   ├── OrderItem.java
│   │   │           │   └── ShippingAddress.java
│   │   │           │
│   │   │           ├── repository/          # 数据访问层
│   │   │           │   ├── UserRepository.java
│   │   │           │   ├── ProductRepository.java
│   │   │           │   ├── CartRepository.java
│   │   │           │   ├── CartItemRepository.java
│   │   │           │   ├── OrderRepository.java
│   │   │           │   └── OrderItemRepository.java
│   │   │           │
│   │   │           ├── dto/                 # 数据传输对象
│   │   │           │   ├── request/
│   │   │           │   │   ├── LoginRequest.java
│   │   │           │   │   ├── RegisterRequest.java
│   │   │           │   │   ├── AddToCartRequest.java
│   │   │           │   │   └── CreateOrderRequest.java
│   │   │           │   └── response/
│   │   │           │       ├── AuthResponse.java
│   │   │           │       ├── UserResponse.java
│   │   │           │       ├── ProductResponse.java
│   │   │           │       ├── CartResponse.java
│   │   │           │       └── OrderResponse.java
│   │   │           │
│   │   │           ├── service/             # 业务逻辑层
│   │   │           │   ├── AuthService.java
│   │   │           │   ├── UserService.java
│   │   │           │   ├── ProductService.java
│   │   │           │   ├── CartService.java
│   │   │           │   └── OrderService.java
│   │   │           │
│   │   │           ├── controller/          # 控制器层
│   │   │           │   ├── AuthController.java
│   │   │           │   ├── ProductController.java
│   │   │           │   ├── CartController.java
│   │   │           │   └── OrderController.java
│   │   │           │
│   │   │           ├── security/           # 安全相关
│   │   │           │   ├── JwtTokenProvider.java
│   │   │           │   ├── JwtAuthenticationFilter.java
│   │   │           │   └── UserDetailsServiceImpl.java
│   │   │           │
│   │   │           ├── exception/           # 异常处理
│   │   │           │   ├── GlobalExceptionHandler.java
│   │   │           │   ├── ResourceNotFoundException.java
│   │   │           │   ├── BadRequestException.java
│   │   │           │   └── InsufficientStockException.java
│   │   │           │
│   │   │           └── util/                # 工具类
│   │   │               └── OrderNumberGenerator.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── data.sql                    # 初始数据（可选）
│   │
│   └── test/                                # 测试
│       └── java/
│           └── com/toyshop/
│               ├── controller/
│               ├── service/
│               └── repository/
│
├── pom.xml                                 # Maven 配置
└── README.md
```

---

## 🗄️ 数据库设计

### ER 图概览

```
User (用户)
├── id (PK)
├── email (UNIQUE)
├── password (BCrypt)
├── name
├── role (CUSTOMER/ADMIN)
└── createdAt

Product (产品)
├── id (PK)
├── name
├── description
├── price
├── category
├── stock
├── imageUrl
└── createdAt

Cart (购物车)
├── id (PK)
├── userId (FK -> User)
└── createdAt

CartItem (购物车项)
├── id (PK)
├── cartId (FK -> Cart)
├── productId (FK -> Product)
├── quantity
└── createdAt

Order (订单)
├── id (PK)
├── orderNumber (UNIQUE)
├── userId (FK -> User)
├── status (AWAITING_PAYMENT/CANCELLED/FULFILLED)
├── shippingAddress (JSON 或单独表)
├── shippingMethod
├── shippingFee
├── subtotal
├── total
└── createdAt

OrderItem (订单项)
├── id (PK)
├── orderId (FK -> Order)
├── productId (FK -> Product)
├── productName (快照)
├── productPrice (快照)
├── quantity
└── subtotal
```

### 实体关系

- **User 1:1 Cart** - 每个用户有一个购物车
- **Cart 1:N CartItem** - 购物车有多个购物车项
- **CartItem N:1 Product** - 购物车项关联产品
- **User 1:N Order** - 用户有多个订单
- **Order 1:N OrderItem** - 订单有多个订单项
- **OrderItem N:1 Product** - 订单项关联产品（但保存快照）

---

## 📝 实体类设计

### User.java

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password; // BCrypt 加密
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.CUSTOMER;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Cart cart;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // Getters, Setters, Constructors
}

enum Role {
    CUSTOMER, ADMIN
}
```

### Product.java

```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    private String category;
    
    @Column(nullable = false)
    private Integer stock = 0;
    
    private String imageUrl;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // Getters, Setters, Constructors
}
```

### Cart.java

```java
@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // Getters, Setters, Constructors
}
```

### CartItem.java

```java
@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // Getters, Setters, Constructors
}
```

### Order.java

```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String orderNumber;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.AWAITING_PAYMENT;
    
    @Embedded
    private ShippingAddress shippingAddress;
    
    @Column(nullable = false)
    private String shippingMethod;
    
    @Column(nullable = false)
    private BigDecimal shippingFee;
    
    @Column(nullable = false)
    private BigDecimal subtotal;
    
    @Column(nullable = false)
    private BigDecimal total;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // Getters, Setters, Constructors
}

enum OrderStatus {
    AWAITING_PAYMENT, CANCELLED, FULFILLED
}

@Embeddable
public class ShippingAddress {
    private String recipientName;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String phone;
    
    // Getters, Setters
}
```

### OrderItem.java

```java
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false)
    private String productName; // 快照
    
    @Column(nullable = false)
    private BigDecimal productPrice; // 快照
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private BigDecimal subtotal;
    
    // Getters, Setters, Constructors
}
```

---

## 🔌 API 接口设计

### 1. 认证接口 (AuthController)

```
POST   /api/auth/register
Body: { name, email, password }
Response: { token, user: { id, name, email, role } }

POST   /api/auth/login
Body: { email, password }
Response: { token, user: { id, name, email, role } }

GET    /api/auth/me
Headers: Authorization: Bearer <token>
Response: { id, name, email, role }
```

### 2. 产品接口 (ProductController)

```
GET    /api/products
Query: ?page=0&size=20&search=keyword&category=category
Response: { content: [...], totalElements, totalPages, ... }

GET    /api/products/{id}
Response: { id, name, description, price, category, stock, imageUrl }

POST   /api/products (Admin only)
Body: { name, description, price, category, stock, imageUrl }
Response: { id, ... }

PUT    /api/products/{id} (Admin only)
Body: { name, description, price, category, stock, imageUrl }
Response: { id, ... }

DELETE /api/products/{id} (Admin only)
Response: 204 No Content
```

### 3. 购物车接口 (CartController)

```
GET    /api/cart
Headers: Authorization: Bearer <token>
Response: { 
  items: [{ productId, productName, price, quantity, subtotal }],
  subtotal
}

POST   /api/cart/items
Headers: Authorization: Bearer <token>
Body: { productId, quantity }
Response: { message, cart: {...} }

PUT    /api/cart/items/{productId}
Headers: Authorization: Bearer <token>
Body: { quantity }
Response: { message, cart: {...} }

DELETE /api/cart/items/{productId}
Headers: Authorization: Bearer <token>
Response: 204 No Content

DELETE /api/cart
Headers: Authorization: Bearer <token>
Response: 204 No Content
```

### 4. 订单接口 (OrderController)

```
POST   /api/orders
Headers: Authorization: Bearer <token>
Body: {
  shippingAddress: { recipientName, street, city, state, zipCode, phone },
  shippingMethod: "STANDARD" | "EXPRESS"
}
Response: {
  id, orderNumber, status, items, subtotal, shippingFee, total, createdAt
}

GET    /api/orders
Headers: Authorization: Bearer <token>
Response: [{ id, orderNumber, status, total, createdAt }, ...]

GET    /api/orders/{id}
Headers: Authorization: Bearer <token>
Response: {
  id, orderNumber, status, shippingAddress, shippingMethod,
  items: [...], subtotal, shippingFee, total, createdAt
}

PUT    /api/orders/{id}/cancel (Optional)
Headers: Authorization: Bearer <token>
Response: { id, status: "CANCELLED", ... }
```

---

## 🔐 安全配置

### SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/products/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### JwtTokenProvider.java

```java
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

---

## 💼 服务层实现要点

### OrderService.java (关键业务逻辑)

```java
@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private ProductService productService;
    
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        // 1. 获取用户购物车
        Cart cart = cartService.getCartByUserId(userId);
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }
        
        // 2. 验证库存并创建订单（事务性）
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.AWAITING_PAYMENT);
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingMethod(request.getShippingMethod());
        order.setShippingFee(calculateShippingFee(request.getShippingMethod()));
        
        BigDecimal subtotal = BigDecimal.ZERO;
        
        // 3. 创建订单项并验证库存
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            
            // 验证库存
            if (product.getStock() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                    "Insufficient stock for product: " + product.getName()
                );
            }
            
            // 创建订单项（保存快照）
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubtotal(
                product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );
            
            order.getItems().add(orderItem);
            subtotal = subtotal.add(orderItem.getSubtotal());
            
            // 4. 减少库存
            product.setStock(product.getStock() - cartItem.getQuantity());
            productService.saveProduct(product);
        }
        
        order.setSubtotal(subtotal);
        order.setTotal(subtotal.add(order.getShippingFee()));
        
        // 5. 保存订单
        Order savedOrder = orderRepository.save(order);
        
        // 6. 清空购物车
        cartService.clearCart(userId);
        
        return convertToResponse(savedOrder);
    }
    
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + 
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private BigDecimal calculateShippingFee(String method) {
        return "EXPRESS".equals(method) 
            ? new BigDecimal("15.00") 
            : new BigDecimal("5.00");
    }
}
```

### CartService.java

```java
@Service
@Transactional
public class CartService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    public CartResponse getCart(Long userId) {
        Cart cart = getCartByUserId(userId);
        return convertToResponse(cart);
    }
    
    public CartResponse addItem(Long userId, AddToCartRequest request) {
        Cart cart = getCartByUserId(userId);
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // 验证库存
        if (product.getStock() < request.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock");
        }
        
        // 检查是否已存在
        Optional<CartItem> existingItem = cart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(request.getProductId()))
            .findFirst();
        
        if (existingItem.isPresent()) {
            // 更新数量（或替换，根据需求）
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            if (newQuantity > product.getStock()) {
                throw new InsufficientStockException("Insufficient stock");
            }
            item.setQuantity(newQuantity);
        } else {
            // 添加新项
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(request.getQuantity());
            cart.getItems().add(item);
        }
        
        cartRepository.save(cart);
        return convertToResponse(cart);
    }
    
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                Cart newCart = new Cart();
                newCart.setUser(user);
                return cartRepository.save(newCart);
            });
    }
    
    public void clearCart(Long userId) {
        Cart cart = getCartByUserId(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
```

---

## 📦 依赖配置 (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    <!-- 或 PostgreSQL -->
    <!--
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    -->
    
    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Lombok (可选，简化代码) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## ⚙️ 配置文件 (application.yml)

```yaml
spring:
  application:
    name: toy-shop-backend
  
  datasource:
    url: jdbc:h2:mem:toyshop
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  h2:
    console:
      enabled: true
      path: /h2-console
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  
  # 生产环境使用 PostgreSQL
  # datasource:
  #   url: jdbc:postgresql://localhost:5432/toyshop
  #   username: postgres
  #   password: password

server:
  port: 8080

jwt:
  secret: your-secret-key-change-in-production-min-256-bits
  expiration: 86400000 # 24 hours in milliseconds

logging:
  level:
    com.toyshop: DEBUG
    org.springframework.security: DEBUG
```

---

## 🧪 测试建议

### 单元测试示例

```java
@SpringBootTest
@Transactional
class OrderServiceTest {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Test
    void testCreateOrder_Success() {
        // Arrange
        User user = createTestUser();
        Product product = createTestProduct(10);
        addToCart(user.getId(), product.getId(), 2);
        
        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress(createTestAddress());
        request.setShippingMethod("STANDARD");
        
        // Act
        OrderResponse order = orderService.createOrder(user.getId(), request);
        
        // Assert
        assertNotNull(order);
        assertEquals("AWAITING_PAYMENT", order.getStatus());
        assertEquals(2, order.getItems().size());
        
        // 验证库存已减少
        Product updatedProduct = productRepository.findById(product.getId()).get();
        assertEquals(8, updatedProduct.getStock());
    }
    
    @Test
    void testCreateOrder_InsufficientStock() {
        // Arrange
        User user = createTestUser();
        Product product = createTestProduct(5);
        addToCart(user.getId(), product.getId(), 10); // 超过库存
        
        // Act & Assert
        assertThrows(InsufficientStockException.class, () -> {
            orderService.createOrder(user.getId(), createOrderRequest());
        });
    }
}
```

---

## 🚀 实施步骤

### Phase 1: 项目初始化
1. 创建 Spring Boot 项目（使用 Spring Initializr）
2. 配置依赖（pom.xml）
3. 配置 application.yml
4. 创建基础包结构

### Phase 2: 实体和 Repository
1. 创建所有实体类
2. 创建 Repository 接口
3. 测试数据库连接和 JPA 映射

### Phase 3: 安全配置
1. 实现 JWT Token Provider
2. 实现 JWT Authentication Filter
3. 配置 Spring Security
4. 实现 UserDetailsService

### Phase 4: 认证模块
1. 实现 AuthService
2. 实现 AuthController
3. 测试注册/登录接口

### Phase 5: 产品模块
1. 实现 ProductService
2. 实现 ProductController
3. 实现分页、搜索、筛选
4. 测试产品接口

### Phase 6: 购物车模块
1. 实现 CartService
2. 实现 CartController
3. 测试购物车操作

### Phase 7: 订单模块（核心）
1. 实现 OrderService（重点：事务处理、库存验证）
2. 实现 OrderController
3. 测试订单创建流程
4. 验证事务回滚（库存不足时）

### Phase 8: 异常处理和优化
1. 实现全局异常处理
2. 添加日志
3. 性能优化
4. 编写测试

### Phase 9: 数据初始化
1. 创建初始数据脚本（data.sql）
2. 创建测试用户（CUSTOMER 和 ADMIN）
3. 创建示例产品

---

## 📝 数据初始化脚本 (data.sql)

```sql
-- 创建管理员用户（密码: admin123）
INSERT INTO users (email, password, name, role, created_at) VALUES
('admin@toyshop.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJ5O', 'Admin User', 'ADMIN', CURRENT_TIMESTAMP);

-- 创建测试客户（密码: user123）
INSERT INTO users (email, password, name, role, created_at) VALUES
('user@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJ5O', 'Test User', 'CUSTOMER', CURRENT_TIMESTAMP);

-- 创建示例产品
INSERT INTO products (name, description, price, category, stock, image_url, created_at) VALUES
('Teddy Bear', 'A soft and cuddly teddy bear', 29.99, 'Stuffed Animals', 50, 'https://example.com/teddy.jpg', CURRENT_TIMESTAMP),
('LEGO Set', 'Building blocks for creative play', 49.99, 'Building Blocks', 30, 'https://example.com/lego.jpg', CURRENT_TIMESTAMP),
('Remote Car', 'Electric remote control car', 39.99, 'Electronics', 25, 'https://example.com/car.jpg', CURRENT_TIMESTAMP),
('Puzzle Game', '1000 piece jigsaw puzzle', 19.99, 'Puzzles', 40, 'https://example.com/puzzle.jpg', CURRENT_TIMESTAMP);
```

**注意**: 密码需要使用 BCrypt 加密。可以使用在线工具或代码生成。

---

## ✅ 检查清单

### 核心功能
- [ ] 用户注册/登录（JWT）
- [ ] 产品 CRUD（分页、搜索、筛选）
- [ ] 购物车管理（添加、更新、删除）
- [ ] 订单创建（事务性、库存验证）
- [ ] 订单查询（列表、详情）
- [ ] 角色权限控制（CUSTOMER/ADMIN）

### 技术实现
- [ ] Spring Security + JWT 配置
- [ ] 数据库实体和关系映射
- [ ] 事务管理（@Transactional）
- [ ] 异常处理（全局异常处理器）
- [ ] CORS 配置
- [ ] 日志记录

### 测试
- [ ] 单元测试（Service 层）
- [ ] 集成测试（Controller 层）
- [ ] 安全测试（认证/授权）

### 文档
- [ ] API 文档（Swagger/OpenAPI）
- [ ] README.md
- [ ] 部署说明

---

## 🔧 额外建议

1. **使用 Swagger/OpenAPI**: 自动生成 API 文档
   ```xml
   <dependency>
       <groupId>org.springdoc</groupId>
       <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
       <version>2.0.2</version>
   </dependency>
   ```

2. **使用 Lombok**: 减少样板代码（@Getter, @Setter, @Builder 等）

3. **使用 MapStruct**: 简化 DTO 转换

4. **添加缓存**: 对产品列表使用 Redis 缓存（可选）

5. **添加监控**: 集成 Actuator 进行健康检查

6. **环境配置**: 分离 dev/test/prod 配置

---

## 📚 参考资源

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Spring Security 文档](https://spring.io/projects/spring-security)
- [Spring Data JPA 文档](https://spring.io/projects/spring-data-jpa)
- [JWT 库文档](https://github.com/jwtk/jjwt)

