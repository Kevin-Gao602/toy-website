<template>
  <div class="order-detail-page">
    <div class="container">
      <div class="header">
        <div>
          <h1>Order Detail</h1>
          <p v-if="order" class="subtitle">#{{ order.orderNumber }}</p>
        </div>
        <router-link to="/orders" class="btn-secondary">Back to Orders</router-link>
      </div>

      <div v-if="isLoading" class="card center">
        <p>Loading order...</p>
      </div>

      <div v-else-if="error" class="card">
        <p class="error">{{ error }}</p>
        <button class="btn-primary" @click="load">Retry</button>
      </div>

      <div v-else-if="!order" class="card center">
        <p>Order not found.</p>
      </div>

      <div v-else class="layout">
        <section class="card">
          <div class="top">
            <span class="badge" :class="statusClass">{{ statusLabel }}</span>
            <span class="muted">Placed: {{ formatDateTime(order.createdAt) }}</span>
          </div>

          <div class="section">
            <h2>Shipping</h2>
            <pre class="address">{{ order.shippingAddress }}</pre>
            <div class="pill-row">
              <span class="pill">{{ order.shippingMethod }}</span>
              <span class="pill">Shipping ${{ formatMoney(order.shippingFee) }}</span>
            </div>
          </div>

          <div class="section">
            <h2>Items</h2>
            <div class="items">
              <div v-for="item in order.items" :key="item.id" class="item">
                <div>
                  <div class="name">{{ item.productName }}</div>
                  <div class="muted">
                    ${{ formatMoney(item.productPrice) }} × {{ item.quantity }}
                  </div>
                </div>
                <div class="line-total">${{ formatMoney(item.subtotal) }}</div>
              </div>
            </div>
          </div>
        </section>

        <aside class="card summary">
          <h2>Totals</h2>
          <div class="row">
            <span>Subtotal</span>
            <span>${{ formatMoney(order.subtotal) }}</span>
          </div>
          <div class="row">
            <span>Shipping</span>
            <span>${{ formatMoney(order.shippingFee) }}</span>
          </div>
          <div class="row total">
            <span>Total</span>
            <span>${{ formatMoney(order.total) }}</span>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { orderApi } from '@/api/orders'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isLoading = ref(false)
const error = ref('')
const order = ref(null)

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

function formatMoney(value) {
  return Number(value || 0).toFixed(2)
}

function formatDateTime(value) {
  const d = parseLocalDateTime(value)
  if (!d) return '-'
  return d.toLocaleString()
}

const statusLabel = computed(() => {
  const raw = order.value?.status
  if (raw === 'FULFILLED') return 'SUCCESS'
  if (raw === 'CANCELLED') return 'CANCELLED'
  return 'PENDING'
})

const statusClass = computed(() => {
  if (statusLabel.value === 'SUCCESS') return 'ok'
  if (statusLabel.value === 'CANCELLED') return 'bad'
  return 'warn'
})

async function load() {
  error.value = ''
  isLoading.value = true
  try {
    const id = route.params.id
    const res = await orderApi.getOrder(id)
    order.value = res.data
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Failed to load order'
  } finally {
    isLoading.value = false
  }
}

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  await load()
})
</script>

<style scoped>
.order-detail-page {
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
  margin-bottom: 1.5rem;
}

h1 {
  margin: 0;
  color: #333;
}

.subtitle {
  margin: 0.25rem 0 0 0;
  color: #666;
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

.center {
  text-align: center;
}

.summary {
  height: fit-content;
  position: sticky;
  top: 2rem;
}

.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.badge {
  padding: 0.25rem 0.55rem;
  border-radius: 999px;
  font-size: 0.85rem;
  font-weight: 900;
  border: 1px solid transparent;
}

.badge.warn {
  background: #fff7ed;
  border-color: #fed7aa;
  color: #9a3412;
}

.badge.ok {
  background: #ecfdf5;
  border-color: #bbf7d0;
  color: #166534;
}

.badge.bad {
  background: #fff1f1;
  border-color: #ffd0d0;
  color: #9b1c1c;
}

.section {
  margin-top: 1.25rem;
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

.pill-row {
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
  font-weight: 700;
}

.items {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.item {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.75rem;
  border: 1px solid #eee;
  border-radius: 10px;
}

.name {
  font-weight: 900;
  color: #333;
}

.line-total {
  font-weight: 900;
  color: #333;
  white-space: nowrap;
}

.row {
  display: flex;
  justify-content: space-between;
  padding: 0.75rem 0;
  border-bottom: 1px solid #eee;
}

.row.total {
  border-bottom: none;
  border-top: 2px solid #eee;
  margin-top: 0.5rem;
  padding-top: 1rem;
  font-weight: 900;
  color: #667eea;
  font-size: 1.1rem;
}

.muted {
  color: #666;
}

.error {
  color: #9b1c1c;
  font-weight: 700;
  margin: 0 0 0.75rem 0;
}

.btn-primary,
.btn-secondary {
  border: none;
  padding: 0.75rem 1.25rem;
  border-radius: 8px;
  font-weight: 800;
  cursor: pointer;
  text-decoration: none;
  display: inline-block;
  text-align: center;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-secondary {
  background: white;
  color: #333;
  border: 1px solid #e0e0e0;
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

