<template>
  <view class="c-cart-bar" :class="{ 'c-cart-bar--expanded': isExpanded }">
    <view class="c-cart-bar__main">
      <view class="c-cart-bar__left">
        <view class="c-cart-bar__select-all" @click="toggleSelectAll">
          <view class="c-cart-bar__checkbox" :class="{ 'is-checked': allSelected }">
            <text v-if="allSelected" class="c-cart-bar__check-icon">✓</text>
          </view>
          <text class="c-cart-bar__select-text">全选</text>
        </view>
        
        <view class="c-cart-bar__total">
          <text class="c-cart-bar__total-label">合计:</text>
          <text class="c-cart-bar__total-price">
            <text class="c-cart-bar__currency">¥</text>
            {{ formatTotalPrice }}
          </text>
        </view>
      </view>
      
      <view class="c-cart-bar__right">
        <CButton
          type="primary"
          size="lg"
          :disabled="selectedCount === 0"
          @click="handleCheckout"
        >
          <text v-if="selectedCount > 0">去结算({{ selectedCount }})</text>
          <text v-else>去结算</text>
        </CButton>
      </view>
    </view>
    
    <!-- 底部安全区域 -->
    <view class="c-cart-bar__safe-area" />
  </view>
</template>

<script setup>
import { computed } from 'vue'
import CButton from './CButton.vue'

const props = defineProps({
  totalPrice: {
    type: Number,
    default: 0
  },
  selectedCount: {
    type: Number,
    default: 0
  },
  totalCount: {
    type: Number,
    default: 0
  },
  allSelected: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggleSelectAll', 'checkout'])

const formatTotalPrice = computed(() => {
  return props.totalPrice.toFixed(2)
})

const isExpanded = computed(() => {
  return props.selectedCount > 0
})

const toggleSelectAll = () => {
  emit('toggleSelectAll')
}

const handleCheckout = () => {
  if (props.selectedCount > 0) {
    emit('checkout')
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-system.scss';

.c-cart-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: $color-surface;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.08);
  z-index: $z-fixed;
  
  &__main {
    @include flex-between;
    padding: $space-sm $space-md;
    height: 100rpx;
  }
  
  &__left {
    @include flex-center;
    gap: $space-md;
    flex: 1;
  }
  
  &__select-all {
    @include flex-center;
    gap: $space-sm;
    cursor: pointer;
    @include tap-highlight;
  }
  
  &__checkbox {
    width: 40rpx;
    height: 40rpx;
    border-radius: 50%;
    border: 2rpx solid $color-border;
    @include flex-center;
    transition: all $duration-fast;
    
    &.is-checked {
      background: $color-primary;
      border-color: $color-primary;
    }
  }
  
  &__check-icon {
    color: #fff;
    font-size: 24rpx;
    font-weight: $font-weight-bold;
  }
  
  &__select-text {
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
  
  &__total {
    @include flex-center;
    gap: $space-xs;
  }
  
  &__total-label {
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
  
  &__total-price {
    font-family: $font-family-price;
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $color-price;
    display: flex;
    align-items: flex-start;
  }
  
  &__currency {
    font-size: $font-size-sm;
    margin-right: 2rpx;
  }
  
  &__right {
    flex-shrink: 0;
  }
  
  &__safe-area {
    @include safe-area-bottom;
    height: constant(safe-area-inset-bottom);
    height: env(safe-area-inset-bottom);
    background: $color-surface;
  }
}
</style>
