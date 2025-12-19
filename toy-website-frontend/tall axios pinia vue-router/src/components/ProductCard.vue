<template>
  <div class="product-card">
    <div class="product-image">
      <img 
        :src="product.imageUrl || product.image_url || '/placeholder-product.svg'" 
        :alt="product.name"
        @error="handleImageError"
        @load="() => console.log('Image loaded successfully:', product.imageUrl || product.image_url)"
      />
      <div v-if="product.stock === 0" class="out-of-stock-badge">Out of Stock</div>
    </div>
    
    <div class="product-info">
      <h3 class="product-name">{{ product.name }}</h3>
      <p class="product-category">{{ product.category }}</p>
      <p class="product-price">${{ formatPrice(product.price) }}</p>
      
      <button 
        @click="handleAddToCart"
        :disabled="product.stock === 0 || isAdding"
        class="btn-add-to-cart"
      >
        <span v-if="isAdding">Adding...</span>
        <span v-else>Add to Cart</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()
const isAdding = ref(false)

// 调试：检查产品数据和图片元素
onMounted(() => {
  if (import.meta.env.DEV) {
    console.log('ProductCard - Product data:', props.product)
    console.log('ProductCard - imageUrl:', props.product.imageUrl)
    console.log('ProductCard - image_url:', props.product.image_url)
    
    // 检查DOM元素
    setTimeout(() => {
      const imgElement = document.querySelector(`[alt="${props.product.name}"]`)
      if (imgElement) {
        console.log('✅ 图片元素存在:', imgElement)
        console.log('图片src:', imgElement.src)
        console.log('图片宽度:', imgElement.offsetWidth)
        console.log('图片高度:', imgElement.offsetHeight)
        console.log('图片是否加载:', imgElement.complete)
      } else {
        console.warn('⚠️ 图片元素未找到')
      }
      
      const imageContainer = document.querySelector('.product-image')
      if (imageContainer) {
        console.log('✅ 图片容器存在')
        console.log('容器宽度:', imageContainer.offsetWidth)
        console.log('容器高度:', imageContainer.offsetHeight)
      }
    }, 100)
  }
})

function formatPrice(price) {
  return Number(price).toFixed(2)
}

function handleImageError(event) {
  console.error('Image load error:', {
    attemptedUrl: event.target.src,
    productId: props.product.id,
    productName: props.product.name,
    imageUrl: props.product.imageUrl,
    image_url: props.product.image_url
  })
  // Use local fallback to avoid external host availability issues.
  if (!event?.target?.src?.includes('/placeholder-product.svg')) {
    event.target.src = '/placeholder-product.svg'
  }
}

async function handleAddToCart() {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }

  if (props.product.stock === 0) return

  try {
    isAdding.value = true
    await cartStore.addItem(props.product.id, 1)
    // 可以添加成功提示
  } catch (error) {
    alert(error.response?.data?.message || 'Failed to add item to cart')
  } finally {
    isAdding.value = false
  }
}
</script>

<style scoped>
.product-card {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 2px solid #e0e0e0;
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  border-color: #667eea;
}

.product-image {
  position: relative;
  width: 100%;
  height: 200px;
  min-height: 200px; /* 确保最小高度 */
  overflow: hidden;
  background: #f5f5f5;
  display: flex; /* 确保容器正确显示 */
  align-items: center;
  justify-content: center;
  border-bottom: 2px solid #e0e0e0;
}

.product-image img {
  width: 100%;
  height: 100%;
  min-height: 200px; /* 确保图片有高度 */
  object-fit: cover;
  display: block; /* 确保图片作为块元素显示 */
}

.out-of-stock-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 5px;
  font-size: 0.875rem;
  font-weight: 600;
}

.product-info {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  flex: 1;
}

.product-name {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
}

.product-category {
  margin: 0;
  font-size: 0.875rem;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.product-price {
  margin: 0;
  font-size: 1.25rem;
  font-weight: bold;
  color: #667eea;
}

.btn-add-to-cart {
  margin-top: auto;
  background: #667eea;
  color: white;
  border: none;
  padding: 0.75rem;
  border-radius: 5px;
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
</style>

