<template>
  <nav class="navbar">
    <div class="nav-container">
      <router-link to="/" class="logo">
        <h1>🎮 Toy Shop</h1>
      </router-link>

      <div class="nav-links">
        <router-link to="/" class="nav-link">Home</router-link>
        
        <router-link to="/cart" class="nav-link cart-link">
          Cart
          <span v-if="cartStore.itemCount > 0" class="cart-badge">
            {{ cartStore.itemCount }}
          </span>
        </router-link>

        <template v-if="authStore.isAuthenticated">
          <router-link to="/orders" class="nav-link">My Orders</router-link>
          <span class="user-info">Hello, {{ authStore.user?.name }}</span>
          <button @click="handleLogout" class="btn-logout">Logout</button>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-link">Login</router-link>
          <router-link to="/register" class="btn-primary">Register</router-link>
        </template>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const cartStore = useCartStore()
const router = useRouter()

function handleLogout() {
  authStore.logout()
  router.push('/')
}
</script>

<style scoped>
.navbar {
  background: linear-gradient(135deg, #667eea 0%,rgb(119, 81, 158) 100%);
  color: white;
  padding: 1rem 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  text-decoration: none;
  color: white;
}

.logo h1 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: bold;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.nav-link {
  color: white;
  text-decoration: none;
  font-weight: 500;
  transition: opacity 0.2s;
  position: relative;
}

.nav-link:hover {
  opacity: 0.8;
}

.cart-link {
  position: relative;
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -12px;
  background: #ff4757;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: bold;
}

.user-info {
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.9rem;
}

.btn-primary {
  background: white;
  color: #667eea;
  padding: 0.5rem 1rem;
  border-radius: 5px;
  text-decoration: none;
  font-weight: 600;
  transition: transform 0.2s;
}

.btn-primary:hover {
  transform: translateY(-2px);
}

.btn-logout {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 0.5rem 1rem;
  border-radius: 5px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.2s;
}

.btn-logout:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style>

