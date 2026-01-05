import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/Home.vue')
    },
    {
      path: '/product/:id',
      name: 'ProductDetail',
      component: () => import('@/views/ProductDetail.vue')
    },
    {
      path: '/cart',
      name: 'Cart',
      component: () => import('@/views/Cart.vue'),
      meta: { requiresAuth: true, requiresCustomer: true }
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue')
    },
    {
      path: '/checkout',
      name: 'Checkout',
      component: () => import('@/views/Checkout.vue'),
      meta: { requiresAuth: true, requiresCustomer: true }
    },
    {
      path: '/checkout/payment',
      name: 'Payment',
      component: () => import('@/views/Payment.vue'),
      meta: { requiresAuth: true, requiresCustomer: true }
    },
    {
      path: '/orders',
      name: 'MyOrders',
      component: () => import('@/views/MyOrders.vue'),
      meta: { requiresAuth: true, requiresCustomer: true }
    },
    {
      path: '/orders/:id',
      name: 'OrderDetail',
      component: () => import('@/views/OrderDetail.vue'),
      meta: { requiresAuth: true, requiresCustomer: true }
    },
    {
      path: '/admin',
      name: 'Admin',
      component: () => import('@/views/Admin.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  ;(async () => {
    // If we have a token but user isn't loaded yet, load it for role-based routing.
    if (authStore.isAuthenticated && !authStore.user) {
      await authStore.fetchUser()
    }

    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }

    if (to.meta.requiresAdmin) {
      if (!authStore.isAuthenticated) {
        next({ name: 'Login', query: { redirect: to.fullPath } })
        return
      }
      if (authStore.user?.role !== 'ADMIN') {
        next({ name: 'Home' })
        return
      }
    }

    if (to.meta.requiresCustomer) {
      if (!authStore.isAuthenticated) {
        next({ name: 'Login', query: { redirect: to.fullPath } })
        return
      }
      if (authStore.user?.role === 'ADMIN') {
        next({ name: 'Admin' })
        return
      }
    }

    next()
  })()
})

export default router
