<template>
  <div class="product-detail-page">
    <div class="container">
      <LoadingSpinner v-if="productsStore.isLoading" message="Loading product..." />

      <div v-else-if="productsStore.currentProduct" class="product-detail">
        <div class="product-image-section">
          <img
            :src="productsStore.currentProduct.imageUrl || productsStore.currentProduct.image_url || '/placeholder-product.svg'"
            :alt="productsStore.currentProduct.name"
            @error="handleImageError"
            @load="() => console.log('Product detail image loaded:', productsStore.currentProduct.imageUrl || productsStore.currentProduct.image_url)"
            class="product-image"
          />
        </div>

        <div class="product-info-section">
          <h1>{{ productsStore.currentProduct.name }}</h1>
          <p class="product-category">{{ productsStore.currentProduct.category }}</p>
          <p class="product-price">${{ formatPrice(productsStore.currentProduct.price) }}</p>
          
          <div class="stock-info">
            <span v-if="productsStore.currentProduct.stock > 0" class="in-stock">
              In Stock ({{ productsStore.currentProduct.stock }} available)
            </span>
            <span v-else class="out-of-stock">Out of Stock</span>
          </div>

          <p class="product-description">{{ productsStore.currentProduct.description }}</p>

          <div class="add-to-cart-section">
            <div class="quantity-selector">
              <label>Quantity:</label>
              <div class="quantity-control">
                <button @click="decreaseQuantity" :disabled="quantity <= 1" class="btn-quantity">-</button>
                <input v-model.number="quantity" type="number" :min="1" :max="maxQuantity" class="quantity-input" />
                <button @click="increaseQuantity" :disabled="quantity >= maxQuantity" class="btn-quantity">+</button>
              </div>
            </div>

            <button
              @click="handleAddToCart"
              :disabled="productsStore.currentProduct.stock === 0 || isAdding"
              class="btn-add-to-cart"
            >
              <span v-if="isAdding">Adding...</span>
              <span v-else>Add to Cart</span>
            </button>
          </div>
        </div>
      </div>

      <div v-else class="error-message">
        <p>Product not found.</p>
        <router-link to="/" class="btn-primary">Back to Home</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProductsStore } from '@/stores/products'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'
import LoadingSpinner from '@/components/LoadingSpinner.vue'

const route = useRoute()
const router = useRouter()
const productsStore = useProductsStore()
const cartStore = useCartStore()
const authStore = useAuthStore()

const quantity = ref(1)
const isAdding = ref(false)

const maxQuantity = computed(() => {
  return productsStore.currentProduct?.stock || 0
})

function formatPrice(price) {
  return Number(price).toFixed(2)
}

function handleImageError(event) {
  console.error('Product detail image load error:', {
    attemptedUrl: event.target.src,
    productId: productsStore.currentProduct?.id,
    productName: productsStore.currentProduct?.name,
    imageUrl: productsStore.currentProduct?.imageUrl,
    image_url: productsStore.currentProduct?.image_url
  })
  // Use local fallback to avoid external host availability issues.
  if (!event?.target?.src?.includes('/placeholder-product.svg')) {
    event.target.src = '/placeholder-product.svg'
  }
}

// 调试：检查产品数据
onMounted(() => {
  if (import.meta.env.DEV) {
    console.log('ProductDetail - Current product:', productsStore.currentProduct)
    console.log('ProductDetail - imageUrl:', productsStore.currentProduct?.imageUrl)
    console.log('ProductDetail - image_url:', productsStore.currentProduct?.image_url)
  }
})

function increaseQuantity() {
  if (quantity.value < maxQuantity.value) {
    quantity.value++
  }
}

function decreaseQuantity() {
  if (quantity.value > 1) {
    quantity.value--
  }
}

async function handleAddToCart() {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }

  if (productsStore.currentProduct.stock === 0) return

  try {
    isAdding.value = true
    await cartStore.addItem(productsStore.currentProduct.id, quantity.value)
    alert('Product added to cart!')
    quantity.value = 1
  } catch (error) {
    alert(error.response?.data?.message || 'Failed to add item to cart')
  } finally {
    isAdding.value = false
  }
}

onMounted(async () => {
  const productId = route.params.id
  try {
    await productsStore.fetchProduct(productId)
  } catch (error) {
    console.error('Failed to load product:', error)
  }
})

onUnmounted(() => {
  productsStore.clearCurrentProduct()
})
</script>

<style scoped>
.product-detail-page {
  min-height: calc(100vh - 200px);
  padding: 2rem 0;
  background: #f5f7fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.product-detail {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
  background: white;
  padding: 2rem;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.product-image-section {
  width: 100%;
}

.product-image {
  width: 100%;
  height: auto;
  border-radius: 10px;
  object-fit: cover;
}

.product-info-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.product-info-section h1 {
  margin: 0;
  color: #333;
  font-size: 2rem;
}

.product-category {
  color: #666;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-size: 0.875rem;
}

.product-price {
  font-size: 2rem;
  font-weight: bold;
  color: #667eea;
  margin: 0;
}

.stock-info {
  margin: 1rem 0;
}

.in-stock {
  color: #27ae60;
  font-weight: 600;
}

.out-of-stock {
  color: #e74c3c;
  font-weight: 600;
}

.product-description {
  color: #666;
  line-height: 1.8;
  margin: 1rem 0;
}

.add-to-cart-section {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 2px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.quantity-selector {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.quantity-selector label {
  font-weight: 600;
  color: #333;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  border: 2px solid #e0e0e0;
  border-radius: 5px;
  padding: 0.25rem;
  width: fit-content;
}

.btn-quantity {
  background: #f5f5f5;
  border: none;
  width: 35px;
  height: 35px;
  border-radius: 3px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s;
}

.btn-quantity:hover:not(:disabled) {
  background: #e0e0e0;
}

.btn-quantity:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-input {
  width: 60px;
  text-align: center;
  border: none;
  font-size: 1rem;
  font-weight: 600;
}

.quantity-input:focus {
  outline: none;
}

.btn-add-to-cart {
  background: #667eea;
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 5px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-add-to-cart:hover:not(:disabled) {
  background: #5568d3;
}

.btn-add-to-cart:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.error-message {
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.error-message p {
  color: #e74c3c;
  margin-bottom: 1.5rem;
  font-size: 1.2rem;
}

.btn-primary {
  display: inline-block;
  background: #667eea;
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 5px;
  text-decoration: none;
  font-weight: 600;
  transition: background 0.2s;
}

.btn-primary:hover {
  background: #5568d3;
}

@media (max-width: 768px) {
  .product-detail {
    grid-template-columns: 1fr;
  }
}
</style>

