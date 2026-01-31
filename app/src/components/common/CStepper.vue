<template>
  <view class="c-stepper">
    <button
      class="c-stepper__btn c-stepper__btn--minus"
      :class="{ 'c-stepper__btn--disabled': value <= min }"
      :disabled="value <= min"
      @click="decrease"
    >
      <text class="c-stepper__icon">−</text>
    </button>
    
    <text class="c-stepper__value">{{ value }}</text>
    
    <button
      class="c-stepper__btn c-stepper__btn--plus"
      :class="{ 'c-stepper__btn--disabled': value >= max }"
      :disabled="value >= max"
      @click="increase"
    >
      <text class="c-stepper__icon">+</text>
    </button>
  </view>
</template>

<script setup>
const props = defineProps({
  value: {
    type: Number,
    default: 1
  },
  min: {
    type: Number,
    default: 1
  },
  max: {
    type: Number,
    default: 99
  },
  step: {
    type: Number,
    default: 1
  }
})

const emit = defineEmits(['update:value', 'change'])

const decrease = () => {
  if (props.value > props.min) {
    const newValue = props.value - props.step
    emit('update:value', newValue)
    emit('change', newValue)
  }
}

const increase = () => {
  if (props.value < props.max) {
    const newValue = props.value + props.step
    emit('update:value', newValue)
    emit('change', newValue)
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-system.scss';

.c-stepper {
  @include flex-center;
  
  &__btn {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    @include flex-center;
    transition: all $duration-fast $ease-out;
    
    &--minus {
      background: $color-background;
      color: $color-text-secondary;
      border: 2rpx solid $color-border;
      
      &:active:not(:disabled) {
        background: $color-border;
      }
    }
    
    &--plus {
      background: $gradient-primary;
      color: #fff;
      box-shadow: 0 4rpx 12rpx rgba(255, 107, 0, 0.3);
      
      &:active:not(:disabled) {
        transform: scale(0.9);
      }
    }
    
    &--disabled {
      opacity: 0.5;
      pointer-events: none;
    }
  }
  
  &__icon {
    font-size: 32rpx;
    font-weight: $font-weight-medium;
    line-height: 1;
  }
  
  &__value {
    min-width: 64rpx;
    text-align: center;
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    padding: 0 $space-sm;
  }
}
</style>
