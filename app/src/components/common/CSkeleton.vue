<template>
  <view class="c-skeleton" :class="[`c-skeleton--${type}`, { 'c-skeleton--circle': circle }]">
    <view
      v-if="rows > 1"
      v-for="i in rows"
      :key="i"
      class="c-skeleton__row"
      :style="{ width: i === rows && lastRowWidth ? lastRowWidth : '100%' }"
    />
  </view>
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'text',
    validator: (value) => ['text', 'title', 'image', 'avatar', 'button'].includes(value)
  },
  rows: {
    type: Number,
    default: 1
  },
  circle: {
    type: Boolean,
    default: false
  },
  lastRowWidth: {
    type: String,
    default: ''
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/design-system.scss';

.c-skeleton {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: $radius-sm;
  
  &--text {
    height: 32rpx;
    margin-bottom: $space-sm;
  }
  
  &--title {
    height: 40rpx;
    width: 60%;
    margin-bottom: $space-md;
  }
  
  &--image {
    width: 100%;
    padding-bottom: 75%;
  }
  
  &--avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
  }
  
  &--button {
    height: 72rpx;
    width: 200rpx;
    border-radius: $radius-button;
  }
  
  &--circle {
    border-radius: 50%;
  }
  
  &__row {
    height: 32rpx;
    margin-bottom: $space-sm;
    background: inherit;
    border-radius: inherit;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
}
</style>
