<template>
  <div class="orders-page">
    <div class="container">
      <div class="header">
        <h1>My Orders</h1>
        <div class="header-actions">
          <button class="btn-secondary" :disabled="isLoading" @click="loadOrders">
            {{ isLoading ? 'Refreshing...' : 'Refresh' }}
          </button>
          <router-link to="/" class="btn-secondary">Back to Home</router-link>
        </div>
      </div>

      <div v-if="isLoading" class="card center">
        <p>Loading orders...</p>
      </div>

      <div v-else-if="error" class="card">
        <p class="error">{{ error }}</p>
        <button class="btn-primary" @click="loadOrders">Retry</button>
      </div>

      <div v-else-if="orders.length === 0" class="card center">
        <p>You have no orders yet.</p>
        <router-link to="/" class="btn-primary">Shop now</router-link>
      </div>

      <div v-else class="orders-grid">
        <div v-for="order in orders" :key="order.id" class="card order-card">
          <div class="order-top">
            <div>
              <div class="order-number">{{ order.orderNumber }}</div>
              <div class="order-meta">
                <span class="muted">Placed:</span> {{ formatDateTime(order.createdAt) }}
              </div>
            </div>
            <div class="badges">
              <span class="badge" :class="statusClass(order)">{{ statusLabel(order) }}</span>
              <span v-if="showCountdown(order)" class="badge timer">
                {{ remainingLabel(order) }}
              </span>
            </div>
          </div>

          <div class="divider" />

          <div class="rows">
            <div class="row">
              <span class="muted">Items</span>
              <span>{{ (order.items || []).length }}</span>
            </div>
            <div class="row">
              <span class="muted">Total</span>
              <span class="total">${{ formatMoney(order.total) }}</span>
            </div>
          </div>

          <div v-if="(order.items || []).length" class="items">
            <div v-for="item in order.items.slice(0, 3)" :key="item.id" class="item">
              <div class="item-name">{{ item.productName }}</div>
              <div class="item-meta">
                ${{ formatMoney(item.productPrice) }} × {{ item.quantity }}
              </div>
            </div>
            <div v-if="order.items.length > 3" class="muted small">+{{ order.items.length - 3 }} more...</div>
          </div>

          <div class="actions">
            <router-link :to="`/orders/${order.id}`" class="btn-link">View detail</router-link>
            <button
              v-if="canCancel(order)"
              class="btn-danger"
              :disabled="isCancellingId === order.id"
              @click="cancel(order)"
            >
              {{ isCancellingId === order.id ? 'Cancelling...' : 'Delete Order' }}
            </button>
            <button
              v-if="canDeleteRecord(order)"
              class="btn-danger-outline"
              :disabled="isDeletingId === order.id"
              @click="deleteRecord(order)"
            >
              {{ isDeletingId === order.id ? 'Deleting...' : 'Delete Record' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { orderApi } from '@/api/orders'
import { useCheckoutStore } from '@/stores/checkout'

const router = useRouter()
const authStore = useAuthStore()
const checkoutStore = useCheckoutStore()

const orders = ref([])
const isLoading = ref(false)
const error = ref('')
const now = ref(Date.now())
const isCancellingId = ref(null)
const isDeletingId = ref(null)
let timer = null
let refreshTimer = null

function parseLocalDateTime(value) {
  // Supports:
  // - ISO string: "2026-01-05T12:34:56.789"
  // - Array: [yyyy, mm, dd, HH, MM, SS, nanos]
  if (!value) return null
  if (typeof value === 'string') {
    const d = new Date(value)
    return Number.isNaN(d.getTime()) ? null : d
  }
  if (Array.isArray(value)) {
    const [y, m, d, hh = 0, mm = 0, ss = 0, nanos = 0] = value
    // JS month is 0-based; backend month is 1-based
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

function remainingMs(order) {
  const exp = parseLocalDateTime(order.expiresAt)
  if (!exp) return 0
  return exp.getTime() - now.value
}

function isExpired(order) {
  return order.status === 'AWAITING_PAYMENT' && remainingMs(order) <= 0
}

function showCountdown(order) {
  return order.status === 'AWAITING_PAYMENT'
}

function remainingLabel(order) {
  const ms = Math.max(0, remainingMs(order))
  const totalSec = Math.floor(ms / 1000)
  const min = String(Math.floor(totalSec / 60)).padStart(2, '0')
  const sec = String(totalSec % 60).padStart(2, '0')
  return `${min}:${sec}`
}

function statusLabel(order) {
  if (order.status === 'CANCELLED') return 'CANCELLED'
  if (order.status === 'FULFILLED') return 'SUCCESS'
  if (isExpired(order)) return 'EXPIRED'
  return 'PENDING'
}

function statusClass(order) {
  const s = statusLabel(order)
  if (s === 'SUCCESS') return 'ok'
  if (s === 'CANCELLED' || s === 'EXPIRED') return 'bad'
  return 'warn'
}

async function loadOrders() {
  error.value = ''
  isLoading.value = true
  try {
    const res = await orderApi.getOrders()
    orders.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Failed to load orders'
  } finally {
    isLoading.value = false
  }
}

function canCancel(order) {
  // "Delete" means set status to CANCELLED (soft delete).
  // Only allow for pending orders that haven't expired.
  return order.status === 'AWAITING_PAYMENT' && remainingMs(order) > 0
}

async function cancel(order) {
  if (!confirm(`Delete order ${order.orderNumber}? This will cancel it.`)) return
  if (isCancellingId.value) return
  isCancellingId.value = order.id
  try {
    await orderApi.cancelOrder(order.id)
    await loadOrders()
  } catch (e) {
    alert(e?.response?.data?.message || e?.message || 'Failed to cancel order')
  } finally {
    isCancellingId.value = null
  }
}

function canDeleteRecord(order) {
  // Only allow hard delete for orders that are no longer pending.
  // (Pending orders should be cancelled first to avoid confusing UX.)
  return order.status === 'CANCELLED' || isExpired(order)
}

async function deleteRecord(order) {
  if (!confirm(`Permanently delete order record ${order.orderNumber}? This cannot be undone.`)) return
  if (isDeletingId.value) return
  isDeletingId.value = order.id
  try {
    await orderApi.deleteOrder(order.id)
    if (checkoutStore.order?.orderId === order.id) {
      checkoutStore.clearOrder()
    }
    await loadOrders()
  } catch (e) {
    alert(e?.response?.data?.message || e?.message || 'Failed to delete order record')
  } finally {
    isDeletingId.value = null
  }
}

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }

  await loadOrders()

  timer = setInterval(() => {
    now.value = Date.now()
  }, 1000)

  // Periodically refresh from backend so auto-cancelled orders update status in UI
  refreshTimer = setInterval(() => {
    loadOrders()
  }, 30000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped>
.orders-page {
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

.header-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

h1 {
  margin: 0;
  color: #333;
}

.orders-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1rem;
}

.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 1.25rem;
}

.center {
  text-align: center;
}

.order-top {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: flex-start;
}

.order-number {
  font-weight: 900;
  color: #333;
}

.order-meta {
  margin-top: 0.25rem;
  color: #666;
  font-size: 0.95rem;
}

.badges {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.badge {
  padding: 0.25rem 0.55rem;
  border-radius: 999px;
  font-size: 0.85rem;
  font-weight: 800;
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

.badge.timer {
  background: #eef2ff;
  border-color: #dbe1ff;
  color: #3b4cc0;
}

.divider {
  height: 1px;
  background: #eee;
  margin: 1rem 0;
}

.rows {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

.row {
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
}

.total {
  font-weight: 900;
  color: #333;
}

.items {
  margin-top: 0.9rem;
  display: grid;
  gap: 0.5rem;
}

.item-name {
  font-weight: 800;
  color: #333;
}

.item-meta {
  color: #666;
  font-size: 0.95rem;
}

.muted {
  color: #666;
}

.small {
  font-size: 0.9rem;
}

.error {
  color: #9b1c1c;
  font-weight: 700;
}

.actions {
  margin-top: 0.75rem;
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  align-items: center;
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

.btn-primary:hover {
  background: #5568d3;
}

.btn-secondary {
  background: white;
  color: #333;
  border: 1px solid #e0e0e0;
}

.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 800;
}

.btn-danger {
  border: none;
  padding: 0.6rem 0.9rem;
  border-radius: 8px;
  font-weight: 900;
  cursor: pointer;
  background: #e74c3c;
  color: white;
}

.btn-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-danger-outline {
  background: transparent;
  color: #e74c3c;
  border: 2px solid #e74c3c;
  padding: 0.55rem 0.85rem;
  border-radius: 8px;
  font-weight: 900;
  cursor: pointer;
}

.btn-danger-outline:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>

