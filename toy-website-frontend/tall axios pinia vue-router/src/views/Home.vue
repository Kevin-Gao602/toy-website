<template>
  <div class="home">
    <div class="container">
      <!-- 搜索和筛选区域 -->
      <div class="filters">
        <div class="search-box">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Search products..."
            class="search-input"
            @input="handleSearch"
          />
        </div>
        
        <div class="category-filter">
          <label>Category:</label>
          <select v-model="selectedCategory" @change="handleFilter" class="category-select">
            <option value="">All Categories</option>
            <option v-for="category in categories" :key="category" :value="category">
              {{ category }}
            </option>
          </select>
        </div>
      </div>

      <!-- 加载状态 - 显示骨架屏 -->
      <div v-if="productsStore.isLoading" class="products-grid">
        <ProductCardSkeleton v-for="n in 8" :key="`skeleton-${n}`" />
      </div>

      <!-- 错误提示 -->
      <div v-else-if="error" class="error-message">
        <p>{{ error }}</p>
        <button @click="loadProducts" class="btn-retry">Retry</button>
      </div>

      <!-- 产品列表 -->
      <div v-else>
        <div v-if="productsStore.products.length === 0" class="no-products">
          <p>No products found.</p>
        </div>

        <div v-else class="products-grid">
          <ProductCard
            v-for="product in productsStore.products"
            :key="product.id"
            :product="product"
            @click="goToProductDetail(product.id)"
          />
        </div>

        <!-- 分页 -->
        <div v-if="productsStore.totalPages > 1" class="pagination">
          <button
            @click="goToPage(currentPage - 1)"
            :disabled="currentPage === 0"
            class="btn-page"
          >
            Previous
          </button>
          <span class="page-info">
            Page {{ currentPage + 1 }} of {{ productsStore.totalPages }}
          </span>
          <button
            @click="goToPage(currentPage + 1)"
            :disabled="currentPage >= productsStore.totalPages - 1"
            class="btn-page"
          >
            Next
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useProductsStore } from '@/stores/products'
import ProductCard from '@/components/ProductCard.vue'
import ProductCardSkeleton from '@/components/ProductCardSkeleton.vue'
import LoadingSpinner from '@/components/LoadingSpinner.vue'

const router = useRouter()
const productsStore = useProductsStore()

const searchQuery = ref('')
const selectedCategory = ref('')
const error = ref(null)
const currentPage = ref(0)

// 从产品列表中提取唯一分类
const categories = ref([])

async function loadProducts() {
  try {
    error.value = null
    console.log('🔄 开始加载产品...', {
      page: currentPage.value,
      size: 20,
      search: searchQuery.value,
      category: selectedCategory.value
    })
    
    await productsStore.fetchProducts({
      page: currentPage.value,
      size: 20,
      search: searchQuery.value,
      category: selectedCategory.value
    })
    
    console.log('✅ 产品加载完成:', {
      productsCount: productsStore.products.length,
      isLoading: productsStore.isLoading,
      totalElements: productsStore.totalElements
    })
    
    // 提取分类（如果后端不提供，从前端数据提取）
    if (productsStore.products.length > 0) {
      const uniqueCategories = [...new Set(productsStore.products.map(p => p.category).filter(Boolean))]
      if (uniqueCategories.length > 0) {
        categories.value = uniqueCategories
      }
    } else {
      console.warn('⚠️ 产品列表为空')
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load products'
    console.error('❌ 加载产品失败:', err)
    console.error('错误详情:', {
      message: err.message,
      response: err.response,
      status: err.response?.status,
      data: err.response?.data
    })
  }
}

function handleSearch() {
  currentPage.value = 0
  loadProducts()
}

function handleFilter() {
  currentPage.value = 0
  loadProducts()
}

function goToPage(page) {
  currentPage.value = page
  loadProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function goToProductDetail(id) {
  router.push(`/product/${id}`)
}

onMounted(() => {
  console.log('🏠 Home组件已挂载，开始加载产品')
  console.log('Products Store状态:', {
    isLoading: productsStore.isLoading,
    productsCount: productsStore.products.length
  })
  loadProducts()
})
</script>

<style scoped>
.home {
  min-height: calc(100vh - 200px);
  padding: 2rem 0;
  background: #f5f7fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.filters {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  align-items: center;
}

.search-box {
  flex: 1;
  min-width: 250px;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
}

.category-filter {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.category-filter label {
  font-weight: 600;
  color: #333;
}

.category-select {
  padding: 0.75rem 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  background: white;
  cursor: pointer;
  transition: border-color 0.2s;
}

.category-select:focus {
  outline: none;
  border-color: #667eea;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.error-message {
  text-align: center;
  padding: 2rem;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.error-message p {
  color: #e74c3c;
  margin-bottom: 1rem;
}

.btn-retry {
  background: #667eea;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 5px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-retry:hover {
  background: #5568d3;
}

.no-products {
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.no-products p {
  color: #666;
  font-size: 1.1rem;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
  padding: 1rem;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.btn-page {
  background: #667eea;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 5px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-page:hover:not(:disabled) {
  background: #5568d3;
}

.btn-page:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.page-info {
  font-weight: 600;
  color: #333;
}
</style>

