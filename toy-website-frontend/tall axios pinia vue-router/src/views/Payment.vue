<template>
  <div class="payment-page">
    <div class="container">
      <div class="header">
        <h1>Payment</h1>
        <p class="subtitle">Complete your purchase (payment integration coming soon)</p>
      </div>

      <div v-if="isBootstrapping" class="card">
        <p>Preparing your order...</p>
      </div>

      <div v-else-if="pageError" class="card">
        <p class="error">{{ pageError }}</p>
        <button class="btn-primary" @click="bootstrap">Retry</button>
      </div>

      <div v-else class="layout">
        <section class="card">
          <div class="order-header">
            <div class="order-title">
              <h2>Order</h2>
              <div class="muted small">#{{ orderDetail?.orderNumber }}</div>
            </div>
            <div class="meta">
              <span class="pill status">{{ statusLabel }}</span>
              <span v-if="statusLabel === 'PENDING'" class="pill timer">Expires in {{ countdown }}</span>
            </div>
          </div>

          <h2>Shipping</h2>
          <pre class="address">{{ checkoutStore.shippingAddressText }}</pre>
          <div class="meta">
            <span class="pill">{{ shippingLabel }}</span>
            <span class="pill">Shipping ${{ formatMoney(checkoutStore.shippingFee) }}</span>
          </div>

          <div class="divider" />

          <h2>Payment Method</h2>
          <p class="muted">
            Order is already created. Status stays <strong>PENDING</strong> for 5 minutes; payment integration will turn it <strong>SUCCESS</strong>.
          </p>

          <button class="btn-primary" disabled title="Coming soon">
            Pay ${{ formatMoney(orderDetail?.total ?? totalFallback) }}
          </button>

          <div class="actions">
            <router-link to="/orders" class="btn-link">Pay later</router-link>
            <router-link :to="{ path: '/checkout', query: { step: 3 } }" class="btn-link">
              Back to Review
            </router-link>
          </div>
        </section>

        <aside class="card summary">
          <h2>Order Summary</h2>

          <div class="summary-row">
            <span>Subtotal</span>
            <span>${{ formatMoney(orderDetail?.subtotal ?? subtotalFallback) }}</span>
          </div>
          <div class="summary-row">
            <span>Shipping</span>
            <span>${{ formatMoney(orderDetail?.shippingFee ?? checkoutStore.shippingFee) }}</span>
          </div>
          <div class="summary-row total">
            <span>Total</span>
            <span>${{ formatMoney(orderDetail?.total ?? totalFallback) }}</span>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { useCheckoutStore, SHIPPING_METHODS } from '@/stores/checkout'
import { orderApi } from '@/api/orders'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()
const checkoutStore = useCheckoutStore()

const isBootstrapping = ref(true)
const pageError = ref('')
const orderDetail = ref(null)
const now = ref(Date.now())
let tick = null

function formatMoney(value) {
  return Number(value || 0).toFixed(2)
}

function parseLocalDateTime(value) {
  if (!value) return null
  if (typeof value === 'string') {
    const d = new Date(value)
    return Number.isNaN(d.getTime()) ? null : d
  }
  if (Array.isArray(value)) {
    const [y, m, d, hh = 0, mm = 0, ss = 0, nanos = 0] = value
    const ms = Math.floor(Number(nanos || 0) / 1e6)
    const date = new Date(Number(y), Number(m) - 1, Number(d), Number(hh), Number(mm), Number(ss), ms)
    return Number.isNaN(date.getTime()) ? null : date
  }
  return null
}

const subtotalFallback = computed(() => Number(cartStore.subtotal || 0))
const totalFallback = computed(() => Number(cartStore.subtotal || 0) + Number(checkoutStore.shippingFee || 0))

const shippingLabel = computed(() => {
  return checkoutStore.shippingMethod === SHIPPING_METHODS.EXPRESS.code
    ? SHIPPING_METHODS.EXPRESS.label
    : SHIPPING_METHODS.STANDARD.label
})

onMounted(async () => {
  await bootstrap()
  tick = setInterval(() => {
    now.value = Date.now()
  }, 1000)
})

onUnmounted(() => {
  if (tick) clearInterval(tick)
})

async function bootstrap() {
  pageError.value = ''
  isBootstrapping.value = true
  try {
    if (!authStore.isAuthenticated) {
      router.push('/login')
      return
    }

    // Ensure we know which user is logged in (used to validate cached checkout order).
    if (!authStore.user) {
      await authStore.fetchUser()
    }

    if (!checkoutStore.isAddressValid) {
      router.push({ path: '/checkout', query: { step: 1 } })
      return
    }

    // If order already created in this checkout session, just fetch detail.
    if (checkoutStore.order?.orderId) {
      // If checkout state belongs to a different user session, clear without hitting API.
      if (checkoutStore.order.userId && authStore.user?.id && checkoutStore.order.userId !== authStore.user.id) {
        checkoutStore.clearOrder()
      } else {
      try {
        const detail = await orderApi.getOrder(checkoutStore.order.orderId, { silent: true })
        orderDetail.value = detail.data
        return
      } catch (e) {
        const status = e?.response?.status
        // Order record may have been deleted; clear cached order and try to create again if possible.
        if (status === 404 || status === 400) {
          checkoutStore.clearOrder()
        } else {
          throw e
        }
      }
      }
    }

    // Otherwise create order now (Payment entrypoint behavior)
    await cartStore.fetchCart()
    if (cartStore.isEmpty) {
      // If cart is empty and we have no existing order, we can't show payment details.
      router.push('/orders')
      return
    }

    const payload = { shippingAddress: checkoutStore.shippingAddressText, shippingMethod: checkoutStore.shippingMethod }
    const created = await orderApi.createOrder(payload)
    checkoutStore.setOrder({
      orderId: created.data?.orderId,
      orderNumber: created.data?.orderNumber,
      status: created.data?.status,
      expiresAt: created.data?.expiresAt,
      userId: authStore.user?.id || null
    })

    // Server clears cart; refresh badge immediately
    await cartStore.fetchCart()

    const detail = await orderApi.getOrder(created.data?.orderId)
    orderDetail.value = detail.data
  } catch (e) {
    pageError.value = e?.response?.data?.message || e?.message || 'Failed to create order'
  } finally {
    isBootstrapping.value = false
  }
}

const statusLabel = computed(() => {
  const raw = orderDetail.value?.status
  if (raw === 'FULFILLED') return 'SUCCESS'
  if (raw === 'CANCELLED') return 'CANCELLED'
  return 'PENDING'
})

const countdown = computed(() => {
  const exp = parseLocalDateTime(orderDetail.value?.expiresAt || checkoutStore.order?.expiresAt)
  if (!exp) return '--:--'
  const ms = Math.max(0, exp.getTime() - now.value)
  const totalSec = Math.floor(ms / 1000)
  const min = String(Math.floor(totalSec / 60)).padStart(2, '0')
  const sec = String(totalSec % 60).padStart(2, '0')
  return `${min}:${sec}`
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

.order-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: flex-start;
  margin-bottom: 0.75rem;
}

.order-title h2 {
  margin: 0;
}

.small {
  font-size: 0.9rem;
}

.pill.status {
  background: #fff7ed;
  border-color: #fed7aa;
  color: #9a3412;
}

.pill.timer {
  background: #eef2ff;
  border-color: #dbe1ff;
  color: #3b4cc0;
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
  display: flex;
  gap: 1rem;
  align-items: center;
}

.btn-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 700;
  background: transparent;
  border: none;
  padding: 0;
  cursor: pointer;
}

.btn-link:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.alert {
  border-radius: 10px;
  padding: 0.75rem 0.9rem;
  margin: 0 0 0.75rem 0;
  font-weight: 600;
}

.alert.error {
  background: #fff1f1;
  border: 1px solid #ffd0d0;
  color: #9b1c1c;
}

.error {
  color: #9b1c1c;
  font-weight: 700;
  margin: 0 0 0.75rem 0;
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

 
