<template>
  <div class="payment-page">
    <div class="container">
      <div class="header">
        <h1>Payment</h1>
        <p class="subtitle">Complete your purchase (payment integration coming soon)</p>
      </div>

      <div v-if="cartStore.isLoading" class="card">
        <p>Loading...</p>
      </div>

      <div v-else-if="cartStore.isEmpty" class="card">
        <p>Your cart is empty.</p>
        <router-link to="/cart" class="btn-secondary">Back to Cart</router-link>
      </div>

      <div v-else class="layout">
        <section class="card">
          <h2>Shipping</h2>
          <pre class="address">{{ checkoutStore.shippingAddressText }}</pre>
          <div class="meta">
            <span class="pill">{{ shippingLabel }}</span>
            <span class="pill">Shipping ${{ formatMoney(checkoutStore.shippingFee) }}</span>
          </div>

          <div class="divider" />

          <h2>Payment Method</h2>
          <p class="muted">
            This is the pre-payment milestone. Add Stripe/PayPal later — for now we stop here.
          </p>

          <button class="btn-primary" disabled title="Coming soon">
            Pay ${{ formatMoney(total) }}
          </button>

          <div class="actions">
            <router-link :to="{ path: '/checkout', query: { step: 3 } }" class="btn-link">
              Back to Review
            </router-link>
          </div>
        </section>

        <aside class="card summary">
          <h2>Order Summary</h2>

          <div class="summary-row">
            <span>Subtotal</span>
            <span>${{ formatMoney(cartStore.subtotal) }}</span>
          </div>
          <div class="summary-row">
            <span>Shipping</span>
            <span>${{ formatMoney(checkoutStore.shippingFee) }}</span>
          </div>
          <div class="summary-row total">
            <span>Total</span>
            <span>${{ formatMoney(total) }}</span>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { useCheckoutStore, SHIPPING_METHODS } from '@/stores/checkout'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()
const checkoutStore = useCheckoutStore()

function formatMoney(value) {
  return Number(value || 0).toFixed(2)
}

const total = computed(() => Number(cartStore.subtotal) + Number(checkoutStore.shippingFee))

const shippingLabel = computed(() => {
  return checkoutStore.shippingMethod === SHIPPING_METHODS.EXPRESS.code
    ? SHIPPING_METHODS.EXPRESS.label
    : SHIPPING_METHODS.STANDARD.label
})

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }

  // Ensure checkout data exists; if not, send user back to checkout step 1
  if (!checkoutStore.isAddressValid) {
    router.push({ path: '/checkout', query: { step: 1 } })
    return
  }

  await cartStore.fetchCart()
  if (cartStore.isEmpty) {
    router.push('/cart')
  }
})
</script>

<style scoped>
.payment-page {
  min-height: calc(100vh - 200px);
  padding: 2rem 0;
  background: #f5f7fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.header {
  margin-bottom: 1.5rem;
}

.subtitle {
  color: #666;
  margin: 0.25rem 0 0 0;
}

.layout {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 2rem;
}

.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 1.5rem;
}

.summary {
  height: fit-content;
  position: sticky;
  top: 2rem;
}

h1 {
  margin: 0;
  color: #333;
}

h2 {
  margin: 0 0 0.75rem 0;
  color: #333;
  font-size: 1.1rem;
}

.address {
  margin: 0;
  padding: 0.75rem;
  background: #f7f8fc;
  border-radius: 8px;
  white-space: pre-wrap;
  color: #333;
}

.meta {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.75rem;
  flex-wrap: wrap;
}

.pill {
  background: #eef2ff;
  color: #3b4cc0;
  border: 1px solid #dbe1ff;
  padding: 0.25rem 0.5rem;
  border-radius: 999px;
  font-size: 0.9rem;
  font-weight: 600;
}

.divider {
  height: 1px;
  background: #eee;
  margin: 1.25rem 0;
}

.muted {
  color: #666;
  margin: 0 0 1rem 0;
}

.btn-primary {
  width: 100%;
  background: #667eea;
  color: white;
  border: none;
  padding: 0.9rem 1rem;
  border-radius: 8px;
  font-weight: 700;
  cursor: not-allowed;
  opacity: 0.85;
}

.btn-secondary {
  display: inline-block;
  margin-top: 0.75rem;
  background: #667eea;
  color: white;
  padding: 0.75rem 1.25rem;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 700;
}

.actions {
  margin-top: 0.75rem;
}

.btn-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 700;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 0.75rem 0;
  border-bottom: 1px solid #eee;
}

.summary-row.total {
  border-bottom: none;
  border-top: 2px solid #eee;
  margin-top: 0.5rem;
  padding-top: 1rem;
  font-weight: 800;
  color: #667eea;
  font-size: 1.1rem;
}

@media (max-width: 900px) {
  .layout {
    grid-template-columns: 1fr;
  }
  .summary {
    position: static;
  }
}
</style>


