<template>
  <div class="product-card-skeleton">
    <div class="skeleton-image">
      <div class="skeleton-shimmer"></div>
    </div>
    
    <div class="skeleton-info">
      <div class="skeleton-line skeleton-title"></div>
      <div class="skeleton-line skeleton-category"></div>
      <div class="skeleton-line skeleton-price"></div>
      <div class="skeleton-button"></div>
    </div>
  </div>
</template>

<script setup>
// 骨架屏组件，用于加载时显示占位符
</script>

<style scoped>
.product-card-skeleton {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 2px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.skeleton-image {
  width: 100%;
  height: 200px;
  min-height: 200px;
  background: #f0f0f0;
  position: relative;
  overflow: hidden;
  border-bottom: 2px solid #e0e0e0;
}

.skeleton-shimmer {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.4),
    transparent
  );
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}

.skeleton-info {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.skeleton-line {
  height: 16px;
  background: #f0f0f0;
  border-radius: 4px;
  position: relative;
  overflow: hidden;
}

.skeleton-line::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.4),
    transparent
  );
  animation: shimmer 1.5s infinite;
}

.skeleton-title {
  width: 80%;
  height: 20px;
}

.skeleton-category {
  width: 60%;
  height: 14px;
}

.skeleton-price {
  width: 40%;
  height: 24px;
}

.skeleton-button {
  width: 100%;
  height: 40px;
  background: #f0f0f0;
  border-radius: 5px;
  margin-top: auto;
  position: relative;
  overflow: hidden;
}

.skeleton-button::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.4),
    transparent
  );
  animation: shimmer 1.5s infinite;
}
</style>

