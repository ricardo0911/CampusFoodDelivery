<template>
  <button
    :class="buttonClasses"
    :disabled="disabled || loading"
    :aria-label="ariaLabel"
    @click="handleClick"
  >
    <view v-if="loading" class="c-btn__loading"></view>
    <slot />
  </button>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'secondary', 'text', 'danger', 'success'].includes(value)
  },
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg'].includes(value)
  },
  block: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  ariaLabel: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['click'])

const buttonClasses = computed(() => {
  const classes = ['c-btn', `c-btn--${props.type}`, `c-btn--${props.size}`]
  if (props.block) classes.push('c-btn--block')
  if (props.disabled) classes.push('c-btn--disabled')
  if (props.loading) classes.push('c-btn--loading')
  return classes
})

const handleClick = (e) => {
  if (!props.disabled && !props.loading) {
    emit('click', e)
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-system.scss';

.c-btn {
  @include button-base;
  padding: 0 $space-lg;
  
  &--sm {
    min-height: 64rpx;
    padding: 0 $space-md;
    font-size: $font-size-sm;
  }
  
  &--lg {
    min-height: 96rpx;
    padding: 0 $space-xl;
    font-size: $font-size-md;
  }
  
  &--primary {
    background: $gradient-primary;
    color: #fff;
    box-shadow: $shadow-primary;
  }
  
  &--secondary {
    background: transparent;
    color: $color-primary;
    border: 2rpx solid $color-primary;
  }
  
  &--text {
    background: transparent;
    color: $color-text-secondary;
    box-shadow: none;
    min-height: auto;
    padding: $space-sm;
  }
  
  &--danger {
    background: $color-error;
    color: #fff;
  }
  
  &--success {
    background: $color-success;
    color: #fff;
  }
  
  &--block {
    width: 100%;
  }
  
  &--disabled {
    opacity: 0.5;
    pointer-events: none;
  }
  
  &--loading {
    pointer-events: none;
    .c-btn__loading {
      width: 28rpx;
      height: 28rpx;
      margin-right: $space-sm;
      border: 3rpx solid currentColor;
      border-top-color: transparent;
      border-radius: 50%;
      animation: btn-loading 1s linear infinite;
    }
  }
}

@keyframes btn-loading {
  to { transform: rotate(360deg); }
}
</style>
