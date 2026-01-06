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

        <div class="sort-filter">
          <label>Sort:</label>
          <select v-model="selectedSort" @change="handleFilter" class="sort-select">
            <option value="">Recommended</option>
            <option value="createdAt:desc">Newest</option>
            <option value="price:asc">Price: Low → High</option>
            <option value="price:desc">Price: High → Low</option>
            <option value="name:asc">Name: A → Z</option>
          </select>
        </div>

        <div class="price-filter" v-if="maxCatalogPrice > 0">
          <label>Price:</label>
          <div class="price-chips">
            <button
              v-for="b in priceBuckets"
              :key="b.key"
              class="chip"
              :class="{ active: selectedPriceBucket === b.key }"
              @click="selectBucket(b.key)"
              type="button"
            >
              {{ b.label }}
            </button>
          </div>

          <div class="range-wrap" v-if="selectedPriceBucket === 'custom'">
            <div class="range-values">
              <span class="range-pill">${{ displayMin }}</span>
              <span class="range-sep">–</span>
              <span class="range-pill">${{ displayMax }}</span>
            </div>

            <div class="dual-range">
              <input
                class="range range-min"
                type="range"
                :min="minCatalogPrice"
                :max="maxCatalogPrice"
                :step="1"
                v-model.number="priceMin"
                @change="handleFilter"
              />
              <input
                class="range range-max"
                type="range"
                :min="minCatalogPrice"
                :max="maxCatalogPrice"
                :step="1"
                v-model.number="priceMax"
                @change="handleFilter"
              />
            </div>
          </div>
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
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useProductsStore } from '@/stores/products'
import { productApi } from '@/api/products'
import ProductCard from '@/components/ProductCard.vue'
import ProductCardSkeleton from '@/components/ProductCardSkeleton.vue'

const router = useRouter()
const productsStore = useProductsStore()

const searchQuery = ref('')
const selectedCategory = ref('')
const selectedSort = ref('')
const error = ref(null)
const currentPage = ref(0)

// 从产品列表中提取唯一分类
const categories = ref([])

const minCatalogPrice = ref(0)
const maxCatalogPrice = ref(0)
const selectedPriceBucket = ref('all') // all | bucket-* | custom
const priceMin = ref(0)
const priceMax = ref(0)

const priceBuckets = [
  { key: 'all', label: 'All' },
  { key: 'bucket-25', label: 'Under $25' },
  { key: 'bucket-25-50', label: '$25–$50' },
  { key: 'bucket-50-100', label: '$50–$100' },
  { key: 'bucket-100', label: '$100+' },
  { key: 'custom', label: 'Custom' }
]

const sortBy = computed(() => (selectedSort.value ? selectedSort.value.split(':')[0] : ''))
const sortDir = computed(() => (selectedSort.value ? selectedSort.value.split(':')[1] : ''))

const normalizedMin = computed(() => Math.min(Number(priceMin.value || 0), Number(priceMax.value || 0)))
const normalizedMax = computed(() => Math.max(Number(priceMin.value || 0), Number(priceMax.value || 0)))

const displayMin = computed(() => Math.max(minCatalogPrice.value, Math.floor(normalizedMin.value)))
const displayMax = computed(() => Math.min(maxCatalogPrice.value, Math.ceil(normalizedMax.value)))

function selectBucket(key) {
  selectedPriceBucket.value = key

  const min = minCatalogPrice.value
  const max = maxCatalogPrice.value

  if (key === 'all') {
    priceMin.value = min
    priceMax.value = max
  } else if (key === 'bucket-25') {
    priceMin.value = min
    priceMax.value = Math.min(25, max)
  } else if (key === 'bucket-25-50') {
    priceMin.value = Math.min(25, max)
    priceMax.value = Math.min(50, max)
  } else if (key === 'bucket-50-100') {
    priceMin.value = Math.min(50, max)
    priceMax.value = Math.min(100, max)
  } else if (key === 'bucket-100') {
    priceMin.value = Math.min(100, max)
    priceMax.value = max
  } else if (key === 'custom') {
    // Keep current values
    priceMin.value = priceMin.value || min
    priceMax.value = priceMax.value || max
  }

  currentPage.value = 0
  loadProducts()
}

function extractPriceNumber(p) {
  const v = p?.price
  if (v == null) return null
  const n = typeof v === 'number' ? v : Number(String(v))
  return Number.isFinite(n) ? n : null
}

async function loadCatalogMeta() {
  try {
    // Always fetch categories from the unfiltered product list (not from the current filtered view)
    const pageSize = 200
    const first = await productApi.getProducts({ page: 0, size: pageSize, search: '', category: '' })
    const data = first.data || {}
    const totalPages = Number(data.totalPages || 1)

    const set = new Set()
    let minP = Number.POSITIVE_INFINITY
    let maxP = 0
    const addFrom = (content) => {
      ;(content || []).forEach((p) => {
        if (p?.category) set.add(p.category)
        const priceNum = extractPriceNumber(p)
        if (priceNum != null) {
          minP = Math.min(minP, priceNum)
          maxP = Math.max(maxP, priceNum)
        }
      })
    }

    addFrom(data.content || data || [])

    // If backend returns paged content, fetch remaining pages to capture all categories.
    // For a toy-shop sized catalog this is fine and avoids the “dropdown shrinks” bug.
    if (totalPages > 1) {
      for (let page = 1; page < totalPages; page += 1) {
        const res = await productApi.getProducts({ page, size: pageSize, search: '', category: '' })
        const d = res.data || {}
        addFrom(d.content || d || [])
      }
    }

    categories.value = [...set].sort((a, b) => String(a).localeCompare(String(b)))

    // Price bounds
    const hasPrice = Number.isFinite(minP) && maxP > 0
    minCatalogPrice.value = hasPrice ? Math.floor(minP) : 0
    maxCatalogPrice.value = hasPrice ? Math.ceil(maxP) : 0
    priceMin.value = minCatalogPrice.value
    priceMax.value = maxCatalogPrice.value
  } catch (e) {
    // Don't block the page if category loading fails; filters will still work.
    console.warn('Failed to load categories:', e)
  }
}

async function loadProducts() {
  try {
    error.value = null
    console.log('🔄 开始加载产品...', {
      page: currentPage.value,
      size: 20,
      search: searchQuery.value,
      category: selectedCategory.value,
      minPrice: effectiveMinPrice.value,
      maxPrice: effectiveMaxPrice.value,
      sortBy: sortBy.value,
      sortDir: sortDir.value
    })
    
    await productsStore.fetchProducts({
      page: currentPage.value,
      size: 20,
      search: searchQuery.value,
      category: selectedCategory.value,
      minPrice: effectiveMinPrice.value,
      maxPrice: effectiveMaxPrice.value,
      sortBy: sortBy.value,
      sortDir: sortDir.value
    })
    
    console.log('✅ 产品加载完成:', {
      productsCount: productsStore.products.length,
      isLoading: productsStore.isLoading,
      totalElements: productsStore.totalElements
    })
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
  loadCatalogMeta()
  loadProducts()
})

const effectiveMinPrice = computed(() => {
  if (maxCatalogPrice.value <= 0) return undefined
  if (selectedPriceBucket.value === 'all') return undefined

  const min = displayMin.value
  // If the slider is set to the full lower bound, don't send it
  return min > minCatalogPrice.value ? min : undefined
})

const effectiveMaxPrice = computed(() => {
  if (maxCatalogPrice.value <= 0) return undefined
  if (selectedPriceBucket.value === 'all') return undefined

  const max = displayMax.value
  // If the slider is set to the full upper bound, don't send it
  return max < maxCatalogPrice.value ? max : undefined
})
</script>

<style scoped>
.home {
  min-height: calc(100vh - 200px);
  padding: 2rem 0;
  background: transparent;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.filters {
  display: flex;
  gap: 0.75rem 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  align-items: center;
  padding: 1rem;
  background: white;
  border-radius: 14px;
  box-shadow: 0 8px 24px rgba(16, 24, 40, 0.08);
  border: 1px solid rgba(102, 126, 234, 0.12);
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

.sort-filter {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.sort-filter label {
  font-weight: 600;
  color: #333;
}

.sort-select {
  padding: 0.75rem 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  background: white;
  cursor: pointer;
  transition: border-color 0.2s;
}

.sort-select:focus {
  outline: none;
  border-color: #667eea;
}

.price-filter {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1 1 520px;
  min-width: 320px;
}

.price-filter label {
  font-weight: 600;
  color: #333;
}

.price-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.chip {
  background: #f3f5ff;
  color: #3b49df;
  border: 1px solid rgba(59, 73, 223, 0.18);
  padding: 0.45rem 0.7rem;
  border-radius: 999px;
  font-weight: 700;
  font-size: 0.9rem;
  cursor: pointer;
  transition: transform 0.12s ease, background 0.12s ease, border-color 0.12s ease;
}

.chip:hover {
  transform: translateY(-1px);
  border-color: rgba(59, 73, 223, 0.28);
}

.chip.active {
  background: #667eea;
  border-color: #667eea;
  color: white;
}

.range-wrap {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  min-width: 260px;
  flex: 1;
}

.range-values {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  justify-content: flex-start;
}

.range-pill {
  background: rgba(44, 62, 80, 0.06);
  color: #2c3e50;
  border: 1px solid rgba(44, 62, 80, 0.12);
  padding: 0.2rem 0.5rem;
  border-radius: 999px;
  font-weight: 700;
  font-size: 0.9rem;
}

.range-sep {
  color: rgba(44, 62, 80, 0.6);
  font-weight: 700;
}

.dual-range {
  position: relative;
  height: 34px;
}

.range {
  -webkit-appearance: none;
  appearance: none;
  width: 100%;
  height: 6px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.25), rgba(102, 126, 234, 0.55));
  outline: none;
  position: absolute;
  left: 0;
  top: 14px;
  pointer-events: none; /* enable only thumbs */
}

.range::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #667eea;
  border: 2px solid white;
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.35);
  cursor: pointer;
  pointer-events: auto;
}

.range::-moz-range-thumb {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #667eea;
  border: 2px solid white;
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.35);
  cursor: pointer;
  pointer-events: auto;
}

.range-min {
  z-index: 2;
}

.range-max {
  z-index: 3;
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

