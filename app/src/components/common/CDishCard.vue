<template>
  <view class="c-dish-card" :class="{ 'c-dish-card--clickable': clickable }" @click="handleClick">
    <image
      class="c-dish-card__image"
      :src="image"
      mode="aspectFill"
      @error="handleImageError"
    />
    
    <view class="c-dish-card__info">
      <text class="c-dish-card__name">{{ name }}</text>
      <text v-if="description" class="c-dish-card__desc">{{ description }}</text>
      
      <view class="c-dish-card__meta">
        <text class="c-dish-card__sales">月售{{ monthlySales }}</text>
        <text v-if="rating > 0" class="c-dish-card__rating">好评率{{ rating }}%</text>
      </view>
      
      <view class="c-dish-card__footer">
        <view class="c-dish-card__price-wrap">
          <text class="c-dish-card__price">
            <text class="c-dish-card__price-symbol">¥</text>
            {{ formatPrice }}
          </text>
          <text v-if="originalPrice > price" class="c-dish-card__price-original">
            ¥{{ formatOriginalPrice }}
          </text>
        </view>
        
        <view v-if="showAddBtn" class="c-dish-card__action">
          <CStepper
            v-if="count > 0"
            v-model:value="localCount"
            :min="0"
            @change="handleCountChange"
          />
          <button
            v-else
            class="c-dish-card__add-btn"
            @click.stop="handleAdd"
          >
            <text class="c-dish-card__add-icon">+</text>
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import CStepper from './CStepper.vue'

const props = defineProps({
  id: {
    type: [String, Number],
    required: true
  },
  name: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  },
  image: {
    type: String,
    default: '/static/default-dish.jpg'
  },
  price: {
    type: Number,
    required: true
  },
  originalPrice: {
    type: Number,
    default: 0
  },
  monthlySales: {
    type: Number,
    default: 0
  },
  rating: {
    type: Number,
    default: 0
  },
  count: {
    type: Number,
    default: 0
  },
  showAddBtn: {
    type: Boolean,
    default: true
  },
  clickable: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['click', 'add', 'change', 'imageError'])

const localCount = ref(props.count)

watch(() => props.count, (newVal) => {
  localCount.value = newVal
})

const formatPrice = computed(() => {
  return props.price.toFixed(2)
})

const formatOriginalPrice = computed(() => {
  return props.originalPrice.toFixed(2)
})

const handleClick = () => {
  if (props.clickable) {
    emit('click', props.id)
  }
}

const handleAdd = () => {
  localCount.value = 1
  emit('add', props.id)
  emit('change', { id: props.id, count: 1 })
}

const handleCountChange = (newCount) => {
  emit('change', { id: props.id, count: newCount })
}

const handleImageError = () => {
  emit('imageError', props.id)
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-system.scss';

.c-dish-card {
  display: flex;
  gap: $space-md;
  padding: $space-md;
  background: $color-surface;
  border-radius: $radius-md;
  
  &--clickable {
    cursor: pointer;
    
    &:active {
      background: darken($color-surface, 2%);
    }
  }
  
  &__image {
    width: 200rpx;
    height: 200rpx;
    border-radius: $radius-md;
    object-fit: cover;
    flex-shrink: 0;
  }
  
  &__info {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;
  }
  
  &__name {
    font-size: $font-size-md;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
    @include text-ellipsis-multi(2);
    margin-bottom: $space-xs;
    line-height: 1.4;
  }
  
  &__desc {
    font-size: $font-size-sm;
    color: $color-text-tertiary;
    @include text-ellipsis;
    margin-bottom: $space-sm;
  }
  
  &__meta {
    display: flex;
    gap: $space-md;
    margin-bottom: $space-sm;
  }
  
  &__sales,
  &__rating {
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
  
  &__footer {
    @include flex-between;
    margin-top: auto;
  }
  
  &__price-wrap {
    @include flex-center;
    gap: $space-sm;
  }
  
  &__price {
    @include price-style;
    display: flex;
    align-items: flex-start;
    
    &-symbol {
      font-size: $font-size-sm;
      margin-right: 2rpx;
    }
  }
  
  &__price-original {
    font-size: $font-size-sm;
    color: $color-price-original;
    text-decoration: line-through;
  }
  
  &__action {
    @include flex-center;
  }
  
  &__add-btn {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    background: $gradient-primary;
    color: #fff;
    @include flex-center;
    box-shadow: 0 4rpx 16rpx rgba(255, 107, 0, 0.3);
    transition: all $duration-fast $ease-out;
    
    &:active {
      transform: scale(0.9);
    }
  }
  
  &__add-icon {
    font-size: 40rpx;
    font-weight: $font-weight-medium;
    line-height: 1;
  }
}
</style>
