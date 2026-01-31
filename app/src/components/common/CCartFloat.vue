<template>
  <view
    v-if="totalCount > 0 || alwaysShow"
    class="c-cart-float"
    :class="{ 'c-cart-float--bounce': isBouncing }"
    :style="positionStyle"
    @click="handleClick"
  >
    <view class="c-cart-float__icon">
      <text class="c-cart-float__icon-text">🛒</text>
    </view>
    <view v-if="totalCount > 0" class="c-cart-float__badge">
      <text class="c-cart-float__badge-text">{{ formatCount }}</text>
    </view>
    <view v-if="totalPrice > 0" class="c-cart-float__price">
      <text class="c-cart-float__price-text">¥{{ formatPrice }}</text>
    </view>
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  totalCount: {
    type: Number,
    default: 0
  },
  totalPrice: {
    type: Number,
    default: 0
  },
  position: {
    type: String,
    default: 'right-bottom',
    validator: (value) => ['right-bottom', 'left-bottom'].includes(value)
  },
  alwaysShow: {
    type: Boolean,
    default: false
  },
  offsetX: {
    type: Number,
    default: 32
  },
  offsetY: {
    type: Number,
    default: 200
  }
})

const emit = defineEmits(['click'])

const isBouncing = ref(false)

// 监听数量变化，触发动画
watch(() => props.totalCount, (newVal, oldVal) => {
  if (newVal > oldVal) {
    isBouncing.value = true
    setTimeout(() => {
      isBouncing.value = false
    }, 300)
  }
})

const formatCount = computed(() => {
  return props.totalCount > 99 ? '99+' : props.totalCount
})

const formatPrice = computed(() => {
  return props.totalPrice.toFixed(2)
})

const positionStyle = computed(() => {
  const style = {
    bottom: `${props.offsetY}rpx`
  }
  
  if (props.position === 'right-bottom') {
    style.right = `${props.offsetX}rpx`
  } else {
    style.left = `${props.offsetX}rpx`
  }
  
  return style
})

const handleClick = () => {
  emit('click')
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-system.scss';

.c-cart-float {
  position: fixed;
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: $gradient-primary;
  box-shadow: $shadow-float;
  @include flex-center;
  z-index: $z-fixed;
  cursor: pointer;
  transition: transform 200ms $ease-out;
  
  &:active {
    transform: scale(0.95);
  }
  
  &--bounce {
    animation: cart-bounce 300ms $ease-bounce;
  }
  
  &__icon {
    @include flex-center;
    
    &-text {
      font-size: 48rpx;
    }
  }
  
  &__badge {
    position: absolute;
    top: -8rpx;
    right: -8rpx;
    min-width: 40rpx;
    height: 40rpx;
    background: $color-error;
    border-radius: 50%;
    @include flex-center;
    box-shadow: 0 4rpx 12rpx rgba(231, 76, 60, 0.4);
    
    &-text {
      color: #fff;
      font-size: $font-size-xs;
      font-weight: $font-weight-bold;
      padding: 0 8rpx;
    }
  }
  
  &__price {
    position: absolute;
    bottom: -16rpx;
    left: 50%;
    transform: translateX(-50%);
    background: $color-surface;
    padding: 4rpx 16rpx;
    border-radius: $radius-full;
    box-shadow: $shadow-sm;
    white-space: nowrap;
    
    &-text {
      font-family: $font-family-price;
      font-size: $font-size-sm;
      font-weight: $font-weight-bold;
      color: $color-price;
    }
  }
}

@keyframes cart-bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.15); }
}
</style>
