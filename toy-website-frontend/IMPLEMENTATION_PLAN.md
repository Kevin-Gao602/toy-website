# 实现建议 (Implementation Plan)

基于 PRD_Toy_Shop.md 的详细实现建议

## 📋 项目概览

**技术栈：**
- 前端：Vue 3 + Pinia + Vue Router + Axios
- 后端：Spring Boot (需要单独实现)
- 状态管理：Pinia
- HTTP 客户端：Axios

---

## 🏗️ 项目结构建议

```
tall axios pinia vue-router/
├── src/
│   ├── api/              # API 调用封装
│   │   ├── index.js      # Axios 实例配置
│   │   ├── auth.js       # 认证相关 API
│   │   ├── products.js   # 产品相关 API
│   │   ├── cart.js       # 购物车 API
│   │   └── orders.js     # 订单相关 API
│   │
│   ├── stores/           # Pinia stores
│   │   ├── auth.js       # 认证状态管理
│   │   ├── cart.js       # 购物车状态管理
│   │   └── products.js   # 产品状态管理（可选）
│   │
│   ├── router/           # 路由配置
│   │   ├── index.js      # 主路由配置
│   │   └── guards.js     # 路由守卫（认证保护）
│   │
│   ├── views/            # 页面组件
│   │   ├── Home.vue      # 首页/产品目录
│   │   ├── ProductDetail.vue
│   │   ├── Cart.vue
│   │   ├── Checkout.vue
│   │   ├── OrderConfirmation.vue
│   │   ├── MyOrders.vue
│   │   ├── OrderDetail.vue
│   │   ├── Login.vue
│   │   └── Register.vue
│   │
│   ├── components/       # 可复用组件
│   │   ├── ProductCard.vue
│   │   ├── ProductList.vue
│   │   ├── CartItem.vue
│   │   ├── LoadingSpinner.vue
│   │   ├── ErrorMessage.vue
│   │   └── NavBar.vue
│   │
│   ├── composables/      # 组合式函数
│   │   ├── useAuth.js
│   │   └── useCart.js
│   │
│   ├── utils/            # 工具函数
│   │   ├── format.js     # 格式化函数（价格、日期等）
│   │   └── validation.js # 表单验证
│   │
│   ├── App.vue
│   └── main.js
│
└── package.json
```

---

## 🚀 实施步骤

### Phase 1: 基础设置 (Foundation)

#### 1.1 安装依赖
```bash
npm install axios
npm install pinia-plugin-persistedstate  # 用于持久化 token
```

#### 1.2 配置 Axios
- 创建 `src/api/index.js`
- 配置 baseURL（指向 Spring Boot 后端）
- 设置请求/响应拦截器
  - 请求拦截器：自动添加 JWT token
  - 响应拦截器：处理错误（401 跳转登录等）

#### 1.3 配置路由守卫
- 实现认证保护
- 未登录用户访问受保护路由时重定向到登录页

---

### Phase 2: 认证模块 (Authentication)

#### 2.1 创建 Auth Store (`src/stores/auth.js`)
**状态：**
- `token`: JWT token（持久化到 localStorage）
- `user`: 当前用户信息（name, email, role）
- `isAuthenticated`: 计算属性

**方法：**
- `login(email, password)`: 调用登录 API，保存 token
- `register(name, email, password)`: 调用注册 API
- `logout()`: 清除 token 和用户信息
- `checkAuth()`: 从 localStorage 恢复认证状态

#### 2.2 创建认证 API (`src/api/auth.js`)
- `login(email, password)`
- `register(name, email, password)`
- `getCurrentUser()`: 获取当前用户信息

#### 2.3 创建登录/注册页面
- `Login.vue`: 表单 + 错误提示
- `Register.vue`: 表单 + 验证

---

### Phase 3: 产品目录 (Product Catalog)

#### 3.1 创建产品 API (`src/api/products.js`)
- `getProducts(params)`: 获取产品列表（支持分页、搜索、分类过滤）
- `getProduct(id)`: 获取产品详情
- `searchProducts(keyword)`: 搜索产品

#### 3.2 创建产品 Store（可选，或直接在组件中调用 API）
- 缓存产品列表
- 管理加载状态

#### 3.3 创建产品组件
- `ProductCard.vue`: 产品卡片（显示名称、价格、图片、库存状态）
- `ProductList.vue`: 产品列表容器

#### 3.4 创建页面
- `Home.vue`: 
  - 产品列表
  - 搜索框
  - 分类筛选
  - 分页
- `ProductDetail.vue`:
  - 产品详细信息
  - 数量选择器（1 到库存上限）
  - "添加到购物车" 按钮

---

### Phase 4: 购物车 (Cart)

#### 4.1 创建购物车 Store (`src/stores/cart.js`)
**状态：**
- `items`: 购物车商品数组
- `isLoading`: 加载状态

**方法：**
- `fetchCart()`: 从服务器获取购物车
- `addItem(productId, quantity)`: 添加商品
- `updateQuantity(productId, quantity)`: 更新数量
- `removeItem(productId)`: 移除商品
- `clearCart()`: 清空购物车
- `getTotal()`: 计算总价

#### 4.2 创建购物车 API (`src/api/cart.js`)
- `getCart()`: 获取购物车
- `addToCart(productId, quantity)`
- `updateCartItem(productId, quantity)`
- `removeCartItem(productId)`
- `clearCart()`

#### 4.3 创建购物车页面 (`Cart.vue`)
- 显示购物车商品列表
- 数量调整
- 删除商品
- 显示小计
- "前往结账" 按钮（购物车为空时禁用）

---

### Phase 5: 结账流程 (Checkout)

#### 5.1 创建结账页面 (`Checkout.vue`)
**步骤 1: 配送地址**
- 表单字段：姓名、地址、城市、邮编、电话
- 验证：所有字段必填

**步骤 2: 配送方式**
- 标准配送：$5
- 快速配送：$15
- 单选按钮或卡片选择

**步骤 3: 订单确认**
- 显示商品列表
- 显示配送地址摘要
- 显示配送方式
- 显示小计、配送费、总计
- "提交订单" 按钮

**实现方式：**
- 使用 `v-if` 或组件切换显示不同步骤
- 使用 Pinia store 管理结账状态

#### 5.2 创建订单 API (`src/api/orders.js`)
- `createOrder(orderData)`: 创建订单
  - 包含：配送地址、配送方式、购物车商品
- `getOrders()`: 获取用户订单列表
- `getOrder(id)`: 获取订单详情

#### 5.3 创建订单确认页面 (`OrderConfirmation.vue`)
- 显示订单号
- 显示订单状态：`AWAITING_PAYMENT`
- 显示订单摘要
- 提示："Payment not supported in this toy app"

---

### Phase 6: 订单历史 (Order History)

#### 6.1 创建订单列表页面 (`MyOrders.vue`)
- 显示订单列表（订单号、日期、状态、总价）
- 点击订单跳转到订单详情

#### 6.2 创建订单详情页面 (`OrderDetail.vue`)
- 显示订单详细信息
- 商品列表
- 配送地址
- 配送方式
- 总计

---

### Phase 7: UI/UX 优化

#### 7.1 创建通用组件
- `LoadingSpinner.vue`: 加载动画
- `ErrorMessage.vue`: 错误提示
- `NavBar.vue`: 导航栏（显示登录状态、购物车数量）

#### 7.2 添加加载状态
- 所有 API 调用显示加载动画
- 使用 Pinia store 管理加载状态

#### 7.3 错误处理
- 友好的错误提示
- 网络错误处理
- 401 自动跳转登录

---

## 🔧 技术实现细节

### API 配置示例

```javascript
// src/api/index.js
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：添加 token
api.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

// 响应拦截器：处理错误
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      // 重定向到登录页
    }
    return Promise.reject(error)
  }
)

export default api
```

### 路由配置示例

```javascript
// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue') },
  { path: '/product/:id', name: 'ProductDetail', component: () => import('@/views/ProductDetail.vue') },
  { path: '/cart', name: 'Cart', component: () => import('@/views/Cart.vue') },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue') },
  // 需要认证的路由
  {
    path: '/checkout',
    name: 'Checkout',
    component: () => import('@/views/Checkout.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'MyOrders',
    component: () => import('@/views/MyOrders.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
```

### Pinia Store 示例

```javascript
// src/stores/auth.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getCurrentUser } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || null)
  const user = ref(null)

  const isAuthenticated = computed(() => !!token.value)

  async function loginUser(email, password) {
    try {
      const response = await login(email, password)
      token.value = response.data.token
      localStorage.setItem('token', token.value)
      await fetchUser()
      return response
    } catch (error) {
      throw error
    }
  }

  async function registerUser(name, email, password) {
    try {
      const response = await register(name, email, password)
      token.value = response.data.token
      localStorage.setItem('token', token.value)
      await fetchUser()
      return response
    } catch (error) {
      throw error
    }
  }

  async function fetchUser() {
    try {
      const response = await getCurrentUser()
      user.value = response.data
    } catch (error) {
      console.error('Failed to fetch user:', error)
    }
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
  }

  // 初始化时恢复用户信息
  if (token.value) {
    fetchUser()
  }

  return {
    token,
    user,
    isAuthenticated,
    loginUser,
    registerUser,
    logout,
    fetchUser
  }
})
```

---

## 📝 环境变量配置

创建 `.env` 文件：
```
VITE_API_BASE_URL=http://localhost:8080/api
```

---

## ✅ 检查清单

### MVP 功能
- [ ] 用户注册/登录
- [ ] 产品目录浏览（分页、搜索、筛选）
- [ ] 产品详情页
- [ ] 购物车管理（添加、更新、删除）
- [ ] 结账流程（地址、配送方式、确认）
- [ ] 订单创建
- [ ] 订单历史查看
- [ ] 订单详情查看

### 技术实现
- [ ] Axios 配置和拦截器
- [ ] Pinia stores（auth, cart）
- [ ] 路由配置和守卫
- [ ] 加载状态管理
- [ ] 错误处理
- [ ] Token 持久化

### UI/UX
- [ ] 响应式设计
- [ ] 加载动画
- [ ] 错误提示
- [ ] 导航栏
- [ ] 购物车图标显示数量

---

## 🎯 优先级建议

1. **高优先级（MVP 核心）**
   - 认证模块
   - 产品目录
   - 购物车
   - 结账流程
   - 订单创建

2. **中优先级（用户体验）**
   - 加载状态
   - 错误处理
   - UI 美化

3. **低优先级（可选功能）**
   - 管理员功能
   - 图片上传
   - 库存警告

---

## 💡 额外建议

1. **使用 TypeScript**（可选）：提供更好的类型安全
2. **使用 UI 框架**（可选）：如 Element Plus、Vuetify 或 Tailwind CSS
3. **单元测试**：使用 Vitest 测试关键功能
4. **代码规范**：已配置 ESLint 和 Prettier，保持代码风格一致

---

## 🔗 后端 API 接口预期

确保后端提供以下接口：

### 认证
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

### 产品
- `GET /api/products` (支持 query: page, size, search, category)
- `GET /api/products/:id`

### 购物车
- `GET /api/cart`
- `POST /api/cart/items`
- `PUT /api/cart/items/:productId`
- `DELETE /api/cart/items/:productId`
- `DELETE /api/cart`

### 订单
- `POST /api/orders`
- `GET /api/orders`
- `GET /api/orders/:id`

---

## 📚 参考资源

- [Vue 3 文档](https://vuejs.org/)
- [Pinia 文档](https://pinia.vuejs.org/)
- [Vue Router 文档](https://router.vuejs.org/)
- [Axios 文档](https://axios-http.com/)

