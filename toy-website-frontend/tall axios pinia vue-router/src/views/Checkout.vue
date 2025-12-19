<template>
  <div class="checkout-page">
    <div class="container">
      <h1>Checkout</h1>
      <p class="coming-soon">Checkout page - Coming soon</p>
      <router-link to="/cart" class="btn-primary">Back to Cart</router-link>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()

onMounted(() => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
  } else if (cartStore.isEmpty) {
    router.push('/cart')
  }
})
</script>

<style scoped>
.checkout-page {
  min-height: calc(100vh - 200px);
  padding: 2rem 0;
  background: #f5f7fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  text-align: center;
}

.coming-soon {
  font-size: 1.2rem;
  color: #666;
  margin: 2rem 0;
}

.btn-primary {
  display: inline-block;
  background: #667eea;
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 5px;
  text-decoration: none;
  font-weight: 600;
}
</style>

