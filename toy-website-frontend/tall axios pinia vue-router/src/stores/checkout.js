import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const SESSION_KEY = 'toyshop.checkout.v1'

function safeParse(json) {
  try {
    return JSON.parse(json)
  } catch {
    return null
  }
}

function readSession() {
  const raw = sessionStorage.getItem(SESSION_KEY)
  if (!raw) return null
  return safeParse(raw)
}

function writeSession(value) {
  sessionStorage.setItem(SESSION_KEY, JSON.stringify(value))
}

export const SHIPPING_METHODS = {
  STANDARD: { code: 'STANDARD', label: 'Standard', fee: 5 },
  EXPRESS: { code: 'EXPRESS', label: 'Express', fee: 15 }
}

export const useCheckoutStore = defineStore('checkout', () => {
  // Address fields are intentionally simple (PRD only requires non-empty validation)
  const address = ref({
    fullName: '',
    phone: '',
    line1: '',
    city: '',
    postalCode: ''
  })

  const shippingMethod = ref(SHIPPING_METHODS.STANDARD.code)

  const shippingFee = computed(() => {
    return shippingMethod.value === SHIPPING_METHODS.EXPRESS.code
      ? SHIPPING_METHODS.EXPRESS.fee
      : SHIPPING_METHODS.STANDARD.fee
  })

  const isAddressValid = computed(() => {
    const a = address.value
    return Boolean(
      a.fullName?.trim() &&
      a.phone?.trim() &&
      a.line1?.trim() &&
      a.city?.trim() &&
      a.postalCode?.trim()
    )
  })

  const shippingAddressText = computed(() => {
    const a = address.value
    return [
      a.fullName,
      a.phone,
      a.line1,
      `${a.city} ${a.postalCode}`.trim()
    ]
      .filter(Boolean)
      .join('\n')
  })

  function hydrateFromSession() {
    const saved = readSession()
    if (!saved) return
    if (saved.address) address.value = { ...address.value, ...saved.address }
    if (saved.shippingMethod) shippingMethod.value = saved.shippingMethod
  }

  function persistToSession() {
    writeSession({
      address: address.value,
      shippingMethod: shippingMethod.value
    })
  }

  function setAddressField(key, value) {
    address.value = { ...address.value, [key]: value }
    persistToSession()
  }

  function setShippingMethod(methodCode) {
    shippingMethod.value = methodCode
    persistToSession()
  }

  function reset() {
    address.value = { fullName: '', phone: '', line1: '', city: '', postalCode: '' }
    shippingMethod.value = SHIPPING_METHODS.STANDARD.code
    sessionStorage.removeItem(SESSION_KEY)
  }

  // auto-hydrate on first use
  hydrateFromSession()

  return {
    address,
    shippingMethod,
    shippingFee,
    isAddressValid,
    shippingAddressText,
    setAddressField,
    setShippingMethod,
    persistToSession,
    hydrateFromSession,
    reset
  }
})


