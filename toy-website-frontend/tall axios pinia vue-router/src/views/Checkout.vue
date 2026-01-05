<template>
  <div class="checkout-page">
    <div class="container">
      <div class="header">
        <h1>Checkout</h1>
        <p class="subtitle">Address → Shipping → Review</p>
      </div>

      <div v-if="isBootstrapping" class="card center">
        <p>Loading checkout...</p>
      </div>

      <div v-else class="layout">
        <section class="card">
          <div class="stepper">
            <button
              class="step"
              :class="{ active: currentStep === 1 }"
              @click="goToStep(1)"
            >
              1. Address
            </button>
            <button
              class="step"
              :class="{ active: currentStep === 2 }"
              @click="goToStep(2)"
              :disabled="!checkoutStore.isAddressValid"
              :title="checkoutStore.isAddressValid ? '' : 'Fill address first'"
            >
              2. Shipping
            </button>
            <button
              class="step"
              :class="{ active: currentStep === 3 }"
              @click="goToStep(3)"
              :disabled="!canEnterReview"
              :title="canEnterReview ? '' : 'Complete address and shipping first'"
            >
              3. Review
            </button>
          </div>

          <div v-if="pageError" class="alert error">{{ pageError }}</div>

          <!-- Step 1: Address -->
          <div v-if="currentStep === 1" class="step-body">
            <h2>Shipping Address</h2>
            <p class="muted">All fields are required.</p>

            <div class="form-grid">
              <label class="field">
                <span>Full name</span>
                <input v-model="fullName" type="text" autocomplete="name" />
              </label>
              <label class="field">
                <span>Phone</span>
                <input v-model="phone" type="tel" autocomplete="tel" />
              </label>
              <label class="field full">
                <span>Address</span>
                <input v-model="line1" type="text" autocomplete="street-address" />
              </label>
              <label class="field">
                <span>City</span>
                <input v-model="city" type="text" autocomplete="address-level2" />
              </label>
              <label class="field">
                <span>Postal code</span>
                <input v-model="postalCode" type="text" autocomplete="postal-code" />
              </label>
            </div>

            <div v-if="showAddressError" class="alert error">
              Please fill all address fields.
            </div>

            <div class="actions">
              <router-link to="/cart" class="btn-secondary">Back to Cart</router-link>
              <button class="btn-primary" @click="nextFromAddress">
                Continue
              </button>
            </div>
          </div>

          <!-- Step 2: Shipping method -->
          <div v-else-if="currentStep === 2" class="step-body">
            <h2>Shipping Method</h2>
            <p class="muted">Flat fees, hardcoded.</p>

            <div class="options">
              <label class="option">
                <input
                  type="radio"
                  name="shipping"
                  :value="SHIPPING_METHODS.STANDARD.code"
                  v-model="shippingMethod"
                />
                <div class="option-body">
                  <div class="option-title">Standard</div>
                  <div class="option-sub">$5.00</div>
                </div>
              </label>
              <label class="option">
                <input
                  type="radio"
                  name="shipping"
                  :value="SHIPPING_METHODS.EXPRESS.code"
                  v-model="shippingMethod"
                />
                <div class="option-body">
                  <div class="option-title">Express</div>
                  <div class="option-sub">$15.00</div>
                </div>
              </label>
            </div>

            <div class="actions">
              <button class="btn-secondary" @click="goToStep(1)">Back</button>
              <button class="btn-primary" @click="goToStep(3)" :disabled="!canEnterReview">
                Review Order
              </button>
            </div>
          </div>

          <!-- Step 3: Review -->
          <div v-else class="step-body">
            <h2>Review Order</h2>

            <div class="review-block">
              <h3>Shipping</h3>
              <pre class="address">{{ checkoutStore.shippingAddressText }}</pre>
              <div class="pill-row">
                <span class="pill">{{ shippingLabel }}</span>
                <span class="pill">Shipping ${{ formatMoney(checkoutStore.shippingFee) }}</span>
              </div>
            </div>

            <div class="review-block">
              <h3>Items</h3>
              <div class="items">
                <div v-for="item in cartStore.items" :key="item.productId" class="item">
                  <div class="item-main">
                    <div class="item-name">{{ item.productName }}</div>
                    <div class="item-meta">
                      ${{ formatMoney(item.price) }} × {{ item.quantity }}
                    </div>
                  </div>
                  <div class="item-total">
                    ${{ formatMoney(item.price * item.quantity) }}
                  </div>
                </div>
              </div>
            </div>

            <div v-if="isValidatingStock" class="alert info">
              Validating stock...
            </div>
            <div v-else-if="stockIssues.length" class="alert error">
              <div><strong>Some items exceed available stock:</strong></div>
              <ul class="issues">
                <li v-for="issue in stockIssues" :key="issue.productId">
                  {{ issue.message }}
                </li>
              </ul>
              <router-link to="/cart" class="btn-link">Fix in cart</router-link>
            </div>

            <div class="actions">
              <button class="btn-secondary" @click="goToStep(2)">Back</button>
              <button
                class="btn-primary"
                @click="continueToPayment"
                :disabled="cartStore.isEmpty || isValidatingStock"
              >
                Continue to Payment
              </button>
            </div>
          </div>
        </section>

        <aside class="card summary">
          <h2>Totals</h2>
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
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { useCheckoutStore, SHIPPING_METHODS } from '@/stores/checkout'
import { productApi } from '@/api/products'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const cartStore = useCartStore()
const checkoutStore = useCheckoutStore()

const isBootstrapping = ref(true)
const pageError = ref('')

const currentStep = ref(1)
const showAddressError = ref(false)

const isValidatingStock = ref(false)
const stockIssues = ref([])

function formatMoney(value) {
  return Number(value || 0).toFixed(2)
}

const total = computed(() => Number(cartStore.subtotal) + Number(checkoutStore.shippingFee))

const canEnterReview = computed(() => checkoutStore.isAddressValid && !cartStore.isEmpty)

const shippingLabel = computed(() => {
  return checkoutStore.shippingMethod === SHIPPING_METHODS.EXPRESS.code
    ? SHIPPING_METHODS.EXPRESS.label
    : SHIPPING_METHODS.STANDARD.label
})

const fullName = computed({
  get: () => checkoutStore.address.fullName,
  set: (v) => checkoutStore.setAddressField('fullName', v)
})
const phone = computed({
  get: () => checkoutStore.address.phone,
  set: (v) => checkoutStore.setAddressField('phone', v)
})
const line1 = computed({
  get: () => checkoutStore.address.line1,
  set: (v) => checkoutStore.setAddressField('line1', v)
})
const city = computed({
  get: () => checkoutStore.address.city,
  set: (v) => checkoutStore.setAddressField('city', v)
})
const postalCode = computed({
  get: () => checkoutStore.address.postalCode,
  set: (v) => checkoutStore.setAddressField('postalCode', v)
})

const shippingMethod = computed({
  get: () => checkoutStore.shippingMethod,
  set: (v) => checkoutStore.setShippingMethod(v)
})

function normalizeStep(value) {
  const n = Number(value)
  if (Number.isNaN(n)) return 1
  return Math.min(3, Math.max(1, n))
}

function goToStep(step) {
  const next = normalizeStep(step)

  // guard steps
  if (next === 2 && !checkoutStore.isAddressValid) {
    currentStep.value = 1
    router.replace({ path: '/checkout', query: { step: 1 } })
    return
  }
  if (next === 3 && !canEnterReview.value) {
    currentStep.value = checkoutStore.isAddressValid ? 2 : 1
    router.replace({ path: '/checkout', query: { step: currentStep.value } })
    return
  }

  currentStep.value = next
  router.replace({ path: '/checkout', query: { step: next } })
}

function nextFromAddress() {
  showAddressError.value = false
  if (!checkoutStore.isAddressValid) {
    showAddressError.value = true
    return
  }
  goToStep(2)
}

async function validateStock() {
  stockIssues.value = []
  isValidatingStock.value = true
  try {
    const issues = []
    for (const item of cartStore.items) {
      try {
        const res = await productApi.getProduct(item.productId)
        const stock = Number(res?.data?.stock ?? 0)
        if (Number(item.quantity) > stock) {
          issues.push({
            productId: item.productId,
            message: `${item.productName}: quantity ${item.quantity} exceeds stock ${stock}`
          })
        }
      } catch (e) {
        issues.push({
          productId: item.productId,
          message: `${item.productName}: cannot validate stock (product lookup failed)`
        })
      }
    }
    stockIssues.value = issues
    return issues.length === 0
  } finally {
    isValidatingStock.value = false
  }
}

async function continueToPayment() {
  pageError.value = ''
  if (cartStore.isEmpty) {
    router.push('/cart')
    return
  }
  if (!checkoutStore.isAddressValid) {
    goToStep(1)
    return
  }

  const ok = await validateStock()
  if (!ok) return

  router.push('/checkout/payment')
}

onMounted(async () => {
  try {
    isBootstrapping.value = true
    pageError.value = ''

    if (!authStore.isAuthenticated) {
      router.push('/login')
      return
    }

    await cartStore.fetchCart()
    if (cartStore.isEmpty) {
      router.push('/cart')
      return
    }

    // Sync step from query, with guards
    const initial = normalizeStep(route.query.step || 1)
    currentStep.value = initial
    if (initial === 2 && !checkoutStore.isAddressValid) currentStep.value = 1
    if (initial === 3 && !canEnterReview.value) currentStep.value = checkoutStore.isAddressValid ? 2 : 1
    router.replace({ path: '/checkout', query: { step: currentStep.value } })

    // auto-run stock validation when entering review
    if (currentStep.value === 3) {
      await validateStock()
    }
  } catch (e) {
    pageError.value = e?.message || 'Failed to load checkout'
  } finally {
    isBootstrapping.value = false
  }
})

watch(currentStep, async (step) => {
  if (step === 3 && cartStore.items.length) {
    await validateStock()
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
}

.header {
  margin-bottom: 1.5rem;
}

.subtitle {
  color: #666;
  margin: 0.25rem 0 0 0;
}

h1 {
  margin: 0;
  color: #333;
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

.stepper {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-bottom: 1rem;
}

.step {
  border: 1px solid #e6e6e6;
  background: #fafafa;
  padding: 0.5rem 0.75rem;
  border-radius: 999px;
  font-weight: 700;
  cursor: pointer;
}

.step.active {
  background: #eef2ff;
  border-color: #dbe1ff;
  color: #3b4cc0;
}

.step:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.step-body h2 {
  margin: 0 0 0.5rem 0;
  color: #333;
}

.muted {
  color: #666;
  margin: 0 0 1rem 0;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
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
  font-weight: 700;
  color: #333;
  font-size: 0.95rem;
}

input {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 0.7rem 0.8rem;
  font-size: 1rem;
  outline: none;
}

input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

.options {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0.75rem;
}

.option {
  display: flex;
  gap: 0.75rem;
  padding: 0.9rem;
  border: 1px solid #e6e6e6;
  border-radius: 10px;
  cursor: pointer;
}

.option input {
  margin-top: 0.2rem;
}

.option-title {
  font-weight: 800;
  color: #333;
}

.option-sub {
  color: #666;
  margin-top: 0.15rem;
}

.review-block {
  margin-top: 1rem;
}

.review-block h3 {
  margin: 0 0 0.5rem 0;
  color: #333;
  font-size: 1rem;
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

.item-name {
  font-weight: 800;
  color: #333;
}

.item-meta {
  color: #666;
  margin-top: 0.2rem;
}

.item-total {
  font-weight: 800;
  color: #333;
  white-space: nowrap;
}

.actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-top: 1.25rem;
}

.btn-primary {
  background: #667eea;
  color: white;
  border: none;
  padding: 0.8rem 1.1rem;
  border-radius: 8px;
  font-weight: 800;
  cursor: pointer;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: white;
  color: #333;
  border: 1px solid #e0e0e0;
  padding: 0.8rem 1.1rem;
  border-radius: 8px;
  font-weight: 800;
  cursor: pointer;
  text-decoration: none;
  text-align: center;
}

.btn-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 800;
}

.alert {
  border-radius: 10px;
  padding: 0.75rem 0.9rem;
  margin: 0.75rem 0;
  font-weight: 600;
}

.alert.error {
  background: #fff1f1;
  border: 1px solid #ffd0d0;
  color: #9b1c1c;
}

.alert.info {
  background: #f0f6ff;
  border: 1px solid #cfe2ff;
  color: #1e3a8a;
}

.issues {
  margin: 0.5rem 0 0.25rem 1.25rem;
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
  font-weight: 900;
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
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>

