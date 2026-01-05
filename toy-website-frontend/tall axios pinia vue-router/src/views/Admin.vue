<template>
  <div class="admin-page">
    <div class="container">
      <div class="header">
        <div>
          <h1>Admin</h1>
          <p class="subtitle">Manage products, orders, and customers</p>
        </div>
        <div class="header-actions">
          <button class="btn-secondary" :disabled="isLoading" @click="refresh">
            {{ isLoading ? 'Refreshing...' : 'Refresh' }}
          </button>
        </div>
      </div>

      <div class="tabs">
        <button class="tab" :class="{ active: tab === 'products' }" @click="tab = 'products'">Products</button>
        <button class="tab" :class="{ active: tab === 'orders' }" @click="tab = 'orders'">Orders</button>
        <button class="tab" :class="{ active: tab === 'users' }" @click="tab = 'users'">Users</button>
      </div>

      <div v-if="error" class="card">
        <p class="error">{{ error }}</p>
        <button class="btn-primary" @click="refresh">Retry</button>
      </div>

      <!-- Products -->
      <div v-else-if="tab === 'products'" class="grid">
        <section class="card">
          <h2>Create Product</h2>
          <div class="form-grid">
            <label class="field">
              <span>Name</span>
              <input v-model="newProduct.name" />
            </label>
            <label class="field">
              <span>Category</span>
              <input v-model="newProduct.category" />
            </label>
            <label class="field">
              <span>Price</span>
              <input v-model="newProduct.price" type="number" step="0.01" min="0" />
            </label>
            <label class="field">
              <span>Stock</span>
              <input v-model="newProduct.stock" type="number" step="1" min="0" />
            </label>
            <label class="field full">
              <span>Image URL</span>
              <input v-model="newProduct.imageUrl" />
            </label>
            <label class="field full">
              <span>Description</span>
              <textarea v-model="newProduct.description" rows="4" />
            </label>
          </div>
          <div class="actions">
            <button class="btn-primary" :disabled="isSaving" @click="createProduct">
              {{ isSaving ? 'Saving...' : 'Create' }}
            </button>
          </div>
        </section>

        <section class="card">
          <h2>Products</h2>
          <div class="table">
            <div class="trow thead">
              <div>Name</div>
              <div>Category</div>
              <div class="right">Price</div>
              <div class="right">Stock</div>
              <div class="right">Actions</div>
            </div>
            <div v-for="p in products" :key="p.id" class="trow">
              <div>
                <input class="inline" v-model="p._edit.name" />
              </div>
              <div>
                <input class="inline" v-model="p._edit.category" />
              </div>
              <div class="right">
                <input class="inline right" v-model="p._edit.price" type="number" step="0.01" min="0" />
              </div>
              <div class="right">
                <input class="inline right" v-model="p._edit.stock" type="number" step="1" min="0" />
              </div>
              <div class="right actions-cell">
                <button class="btn-link" :disabled="isSaving" @click="saveProduct(p)">Save</button>
                <button class="btn-danger-outline" :disabled="isSaving" @click="deleteProduct(p)">Delete</button>
              </div>
            </div>
          </div>
        </section>
      </div>

      <!-- Orders -->
      <div v-else-if="tab === 'orders'" class="card">
        <h2>Orders</h2>
        <div class="table">
          <div class="trow thead orders">
            <div>Order #</div>
            <div>User</div>
            <div>Status</div>
            <div class="right">Total</div>
            <div>Created</div>
            <div class="right">Actions</div>
          </div>
          <div v-for="o in orders" :key="o.id" class="trow orders">
            <div>{{ o.orderNumber }}</div>
            <div>{{ o.user?.email || '-' }}</div>
            <div>
              <span class="badge" :class="statusClass(o.status)">{{ displayStatus(o.status) }}</span>
            </div>
            <div class="right">${{ formatMoney(o.total) }}</div>
            <div>{{ formatDateTime(o.createdAt) }}</div>
            <div class="right actions-cell">
              <select v-model="o._nextStatus" class="select">
                <option value="">Change...</option>
                <option value="CANCELLED">CANCELLED</option>
                <option value="FULFILLED">SUCCESS</option>
              </select>
              <button class="btn-link" :disabled="isSaving || !o._nextStatus" @click="updateOrderStatus(o)">
                Apply
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Users -->
      <div v-else class="card">
        <h2>Users</h2>
        <p class="muted">Only CUSTOMER accounts can be deleted here.</p>
        <div class="table">
          <div class="trow thead users">
            <div>Email</div>
            <div>Name</div>
            <div>Role</div>
            <div>Created</div>
            <div class="right">Actions</div>
          </div>
          <div v-for="u in users" :key="u.id" class="trow users">
            <div>{{ u.email }}</div>
            <div>{{ u.name }}</div>
            <div><span class="badge" :class="u.role === 'ADMIN' ? 'ok' : 'warn'">{{ u.role }}</span></div>
            <div>{{ formatDateTime(u.createdAt) }}</div>
            <div class="right actions-cell">
              <button
                class="btn-danger-outline"
                :disabled="isSaving || u.role === 'ADMIN'"
                @click="deleteUser(u)"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="isLoading" class="hint muted">Loading data…</div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { adminApi } from '@/api/admin'

const tab = ref('products')
const isLoading = ref(false)
const isSaving = ref(false)
const error = ref('')

const products = ref([])
const orders = ref([])
const users = ref([])

const newProduct = reactive({
  name: '',
  description: '',
  category: '',
  price: 0,
  stock: 0,
  imageUrl: ''
})

function formatMoney(v) {
  return Number(v || 0).toFixed(2)
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

function formatDateTime(value) {
  const d = parseLocalDateTime(value)
  return d ? d.toLocaleString() : '-'
}

function displayStatus(status) {
  if (status === 'FULFILLED') return 'SUCCESS'
  if (status === 'AWAITING_PAYMENT') return 'PENDING'
  return status
}

function statusClass(status) {
  if (status === 'FULFILLED') return 'ok'
  if (status === 'CANCELLED') return 'bad'
  return 'warn'
}

function normalizeProducts(list) {
  return (list || []).map((p) => ({
    ...p,
    _edit: {
      name: p.name,
      description: p.description,
      category: p.category,
      price: p.price,
      stock: p.stock,
      imageUrl: p.imageUrl
    }
  }))
}

async function refresh() {
  error.value = ''
  isLoading.value = true
  try {
    const [pRes, oRes, uRes] = await Promise.all([
      adminApi.getProducts({ page: 0, size: 200 }),
      adminApi.getOrders(),
      adminApi.getUsers()
    ])
    products.value = normalizeProducts(pRes.data?.content || [])
    orders.value = (oRes.data || []).map((o) => ({ ...o, _nextStatus: '' }))
    users.value = uRes.data || []
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Failed to load admin data'
  } finally {
    isLoading.value = false
  }
}

async function createProduct() {
  isSaving.value = true
  try {
    await adminApi.createProduct({
      name: newProduct.name,
      description: newProduct.description,
      category: newProduct.category,
      price: newProduct.price,
      stock: newProduct.stock,
      imageUrl: newProduct.imageUrl
    })
    newProduct.name = ''
    newProduct.description = ''
    newProduct.category = ''
    newProduct.price = 0
    newProduct.stock = 0
    newProduct.imageUrl = ''
    await refresh()
  } catch (e) {
    alert(e?.response?.data?.message || e?.message || 'Failed to create product')
  } finally {
    isSaving.value = false
  }
}

async function saveProduct(p) {
  isSaving.value = true
  try {
    await adminApi.updateProduct(p.id, { ...p._edit })
    await refresh()
  } catch (e) {
    alert(e?.response?.data?.message || e?.message || 'Failed to update product')
  } finally {
    isSaving.value = false
  }
}

async function deleteProduct(p) {
  if (!confirm(`Delete product "${p.name}"?`)) return
  isSaving.value = true
  try {
    await adminApi.deleteProduct(p.id)
    await refresh()
  } catch (e) {
    alert(e?.response?.data?.message || e?.message || 'Failed to delete product')
  } finally {
    isSaving.value = false
  }
}

async function updateOrderStatus(o) {
  isSaving.value = true
  try {
    await adminApi.updateOrderStatus(o.id, o._nextStatus)
    await refresh()
  } catch (e) {
    alert(e?.response?.data?.message || e?.message || 'Failed to update order status')
  } finally {
    isSaving.value = false
  }
}

async function deleteUser(u) {
  if (u.role === 'ADMIN') return
  if (!confirm(`Delete user "${u.email}"? This will remove their orders and cart.`)) return
  isSaving.value = true
  try {
    await adminApi.deleteUser(u.id)
    await refresh()
  } catch (e) {
    alert(e?.response?.data?.message || e?.message || 'Failed to delete user')
  } finally {
    isSaving.value = false
  }
}

onMounted(async () => {
  await refresh()
})
</script>

<style scoped>
.admin-page {
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
  margin-bottom: 1rem;
}

h1 {
  margin: 0;
  color: #333;
}

.subtitle {
  margin: 0.25rem 0 0 0;
  color: #666;
}

.tabs {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-bottom: 1rem;
}

.tab {
  border: 1px solid #e6e6e6;
  background: #fafafa;
  padding: 0.5rem 0.75rem;
  border-radius: 999px;
  font-weight: 800;
  cursor: pointer;
}

.tab.active {
  background: #eef2ff;
  border-color: #dbe1ff;
  color: #3b4cc0;
}

.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 1.25rem;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 1rem;
}

h2 {
  margin: 0 0 0.75rem 0;
  color: #333;
  font-size: 1.1rem;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.field.full {
  grid-column: 1 / -1;
}

.field span {
  font-weight: 800;
  color: #333;
  font-size: 0.95rem;
}

input,
textarea,
select {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 0.65rem 0.75rem;
  font-size: 0.95rem;
  outline: none;
}

.inline {
  width: 100%;
  padding: 0.4rem 0.5rem;
}

.right {
  text-align: right;
}

.actions {
  margin-top: 0.9rem;
  display: flex;
  justify-content: flex-end;
}

.actions-cell {
  display: inline-flex;
  gap: 0.5rem;
  justify-content: flex-end;
  align-items: center;
}

.table {
  display: grid;
  gap: 0.35rem;
}

.trow {
  display: grid;
  grid-template-columns: 1.4fr 1fr 0.6fr 0.5fr 0.7fr;
  gap: 0.5rem;
  align-items: center;
  padding: 0.5rem 0.5rem;
  border: 1px solid #eee;
  border-radius: 10px;
}

.trow.orders {
  grid-template-columns: 1.2fr 1.2fr 0.8fr 0.6fr 1fr 1fr;
}

.trow.users {
  grid-template-columns: 1.6fr 1fr 0.7fr 1fr 0.6fr;
}

.thead {
  background: #fafafa;
  font-weight: 900;
  color: #333;
}

.badge {
  padding: 0.25rem 0.55rem;
  border-radius: 999px;
  font-size: 0.85rem;
  font-weight: 900;
  border: 1px solid transparent;
  display: inline-block;
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

.btn-primary,
.btn-secondary {
  border: none;
  padding: 0.75rem 1.25rem;
  border-radius: 8px;
  font-weight: 900;
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

.btn-link {
  background: transparent;
  border: none;
  color: #667eea;
  font-weight: 900;
  cursor: pointer;
}

.btn-danger-outline {
  background: transparent;
  border: 2px solid #e74c3c;
  color: #e74c3c;
  border-radius: 8px;
  padding: 0.45rem 0.65rem;
  font-weight: 900;
  cursor: pointer;
}

.select {
  padding: 0.35rem 0.5rem;
}

.muted {
  color: #666;
}

.hint {
  margin-top: 0.75rem;
}

.error {
  color: #9b1c1c;
  font-weight: 800;
}

@media (max-width: 950px) {
  .grid {
    grid-template-columns: 1fr;
  }
  .trow,
  .trow.orders,
  .trow.users {
    grid-template-columns: 1fr;
    gap: 0.35rem;
  }
  .right {
    text-align: left;
  }
  .actions-cell {
    justify-content: flex-start;
  }
}
</style>


