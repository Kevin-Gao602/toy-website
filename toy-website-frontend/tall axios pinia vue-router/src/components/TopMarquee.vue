<template>
  <div class="top-marquee" aria-label="Promotion banner">
    <div class="top-marquee__track">
      <div class="top-marquee__content">
        <span v-for="n in repeat" :key="n" class="top-marquee__item">
          {{ text }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  text: { type: String, required: true },
  repeat: { type: Number, default: 2 }
})
</script>

<style scoped>
.top-marquee {
  height: 28px;
  background: #cfeee4;
  color: #1d3b34;
  display: flex;
  align-items: center;
  overflow: hidden;
  border-bottom: 1px solid rgba(29, 59, 52, 0.12);
}

.top-marquee__track {
  width: 100%;
  overflow: hidden;
}

.top-marquee__content {
  display: inline-flex;
  gap: 80rem;
  padding-left: 100%;
  white-space: nowrap;
  animation: marquee 28s linear infinite;
}

.top-marquee__item {
  font-size: 0.9rem;
  font-weight: 600;
  letter-spacing: 0.02em;
}

@keyframes marquee {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-100%);
  }
}

@media (prefers-reduced-motion: reduce) {
  .top-marquee__content {
    animation: none;
    padding-left: 0;
    justify-content: center;
    width: 100%;
  }
}
</style>



