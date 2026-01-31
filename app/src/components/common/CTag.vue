<template>
  <view :class="['c-tag', `c-tag--${type}`, `c-tag--${size}`, { 'c-tag--rounded': rounded }]">
    <slot />
  </view>
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'default',
    validator: (value) => ['primary', 'success', 'warning', 'error', 'default', 'outline'].includes(value)
  },
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg'].includes(value)
  },
  rounded: {
    type: Boolean,
    default: false
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/design-system.scss';

.c-tag {
  display: inline-flex;
  align-items: center;
  padding: 4rpx 12rpx;
  border-radius: $radius-sm;
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;
  line-height: 1.2;
  
  &--primary {
    background: rgba(255, 107, 0, 0.1);
    color: $color-primary;
  }
  
  &--success {
    background: rgba(0, 184, 148, 0.1);
    color: $color-success;
  }
  
  &--warning {
    background: rgba(253, 203, 110, 0.2);
    color: #E67E22;
  }
  
  &--error {
    background: rgba(231, 76, 60, 0.1);
    color: $color-error;
  }
  
  &--default {
    background: $color-background;
    color: $color-text-secondary;
  }
  
  &--outline {
    background: transparent;
    border: 2rpx solid currentColor;
  }
  
  &--sm {
    padding: 2rpx 8rpx;
    font-size: 18rpx;
  }
  
  &--lg {
    padding: 8rpx 16rpx;
    font-size: $font-size-sm;
  }
  
  &--rounded {
    border-radius: $radius-full;
  }
}
</style>
