<template>
  <div class="cart-page">
    <div class="container">
      <h1>Shopping Cart</h1>

      <LoadingSpinner v-if="cartStore.isLoading" message="Loading cart..." />

      <div v-else-if="cartStore.isEmpty" class="empty-cart">
        <p>Your cart is empty.</p>
        <router-link to="/" class="btn-primary">Continue Shopping</router-link>
      </div>

      <div v-else class="cart-content">
        <div class="cart-items">
          <div v-for="item in cartStore.items" :key="item.productId" class="cart-item">
            <div class="item-info">
              <h3>{{ item.productName }}</h3>
              <p class="item-price">${{ formatPrice(item.price) }}</p>
            </div>
            <div class="item-actions">
              <div class="quantity-control">
                <button @click="decreaseQuantity(item)" class="btn-quantity">-</button>
                <span class="quantity">{{ item.quantity }}</span>
                <button @click="increaseQuantity(item)" class="btn-quantity">+</button>
              </div>
              <p class="item-subtotal">${{ formatPrice(item.price * item.quantity) }}</p>
              <button @click="removeItem(item.productId)" class="btn-remove">Remove</button>
            </div>
          </div>
        </div>

        <div class="cart-summary">
          <h2>Order Summary</h2>
          <div class="summary-row">
            <span>Subtotal:</span>
            <span>${{ formatPrice(cartStore.subtotal) }}</span>
          </div>
          <div class="summary-row total">
            <span>Total:</span>
            <span>${{ formatPrice(cartStore.subtotal) }}</span>
          </div>
          <router-link to="/checkout" class="btn-checkout">Proceed to Checkout</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'
import LoadingSpinner from '@/components/LoadingSpinner.vue'

const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()

function formatPrice(price) {
  return Number(price).toFixed(2)
}

async function increaseQuantity(item) {
  try {
    await cartStore.updateQuantity(item.productId, item.quantity + 1)
  } catch (error) {
    alert(error.response?.data?.message || 'Failed to update quantity')
  }
}

async function decreaseQuantity(item) {
  if (item.quantity > 1) {
    try {
      await cartStore.updateQuantity(item.productId, item.quantity - 1)
    } catch (error) {
      alert(error.response?.data?.message || 'Failed to update quantity')
    }
  }
}

async function removeItem(productId) {
  if (confirm('Remove this item from cart?')) {
    try {
      await cartStore.removeItem(productId)
    } catch (error) {
      alert(error.response?.data?.message || 'Failed to remove item')
    }
  }
}

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  await cartStore.fetchCart()
})
</script>

<style scoped>
.cart-page {
  min-height: calc(100vh - 200px);
  padding: 2rem 0;
  background: #f5f7fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

h1 {
  margin-bottom: 2rem;
  color: #333;
}

.empty-cart {
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.empty-cart p {
  font-size: 1.2rem;
  color: #666;
  margin-bottom: 1.5rem;
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

.cart-content {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 2rem;
}

.cart-items {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cart-item {
  background: white;
  padding: 1.5rem;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-info h3 {
  margin: 0 0 0.5rem 0;
  color: #333;
}

.item-price {
  color: #667eea;
  font-weight: 600;
  font-size: 1.1rem;
}

.item-actions {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  border: 2px solid #e0e0e0;
  border-radius: 5px;
  padding: 0.25rem;
}

.btn-quantity {
  background: #f5f5f5;
  border: none;
  width: 30px;
  height: 30px;
  border-radius: 3px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s;
}

.btn-quantity:hover {
  background: #e0e0e0;
}

.quantity {
  min-width: 30px;
  text-align: center;
  font-weight: 600;
}

.item-subtotal {
  font-weight: 600;
  font-size: 1.1rem;
  color: #333;
  min-width: 80px;
  text-align: right;
}

.btn-remove {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 5px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s;
}

.btn-remove:hover {
  background: #c0392b;
}

.cart-summary {
  background: white;
  padding: 1.5rem;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  height: fit-content;
  position: sticky;
  top: 2rem;
}

.cart-summary h2 {
  margin: 0 0 1rem 0;
  color: #333;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 0.75rem 0;
  border-bottom: 1px solid #e0e0e0;
}

.summary-row.total {
  border-bottom: none;
  border-top: 2px solid #e0e0e0;
  margin-top: 0.5rem;
  padding-top: 1rem;
  font-size: 1.2rem;
  font-weight: bold;
  color: #667eea;
}

.btn-checkout {
  display: block;
  width: 100%;
  background: #667eea;
  color: white;
  padding: 1rem;
  border-radius: 5px;
  text-decoration: none;
  text-align: center;
  font-weight: 600;
  margin-top: 1rem;
  transition: background 0.2s;
}

.btn-checkout:hover {
  background: #5568d3;
}

@media (max-width: 768px) {
  .cart-content {
    grid-template-columns: 1fr;
  }
}
</style>

