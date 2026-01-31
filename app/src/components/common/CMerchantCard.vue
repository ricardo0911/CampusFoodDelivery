<template>
  <view class="c-merchant-card" :class="{ 'c-merchant-card--clickable': clickable }" @click="handleClick">
    <image
      class="c-merchant-card__image"
      :src="image"
      mode="aspectFill"
      @error="handleImageError"
    />
    
    <view class="c-merchant-card__content">
      <view class="c-merchant-card__header">
        <text class="c-merchant-card__name">{{ name }}</text>
        <view v-if="isBrand" class="c-merchant-card__brand-tag">品牌</view>
      </view>
      
      <view class="c-merchant-card__meta">
        <view class="c-merchant-card__rating">
          <text class="c-merchant-card__rating-score">{{ rating }}</text>
          <view class="c-merchant-card__stars">
            <text v-for="n in 5" :key="n" class="c-merchant-card__star" :class="{ 'is-filled': n <= Math.round(rating) }">★</text>
          </view>
        </view>
        <text class="c-merchant-card__sales">月售{{ formatSales }}</text>
        <text class="c-merchant-card__time">{{ deliveryTime }}分钟</text>
      </view>
      
      <view class="c-merchant-card__delivery">
        <text class="c-merchant-card__delivery-text">
          起送¥{{ minOrderAmount }}
          <text class="c-merchant-card__divider">|</text>
          <text :class="{ 'c-merchant-card__delivery-free': deliveryFee === 0 }">
            {{ deliveryFee === 0 ? '免配送费' : `配送¥${deliveryFee}` }}
          </text>
        </text>
      </view>
      
      <view v-if="activities && activities.length > 0" class="c-merchant-card__activities">
        <view
          v-for="(activity, index) in displayedActivities"
          :key="index"
          class="c-merchant-card__activity"
        >
          <CTag :type="activity.type" size="sm">{{ activity.label }}</CTag>
          <text class="c-merchant-card__activity-text">{{ activity.text }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import CTag from './CTag.vue'

const props = defineProps({
  id: {
    type: [String, Number],
    required: true
  },
  name: {
    type: String,
    required: true
  },
  image: {
    type: String,
    default: '/static/default-shop.jpg'
  },
  rating: {
    type: Number,
    default: 4.5
  },
  monthlySales: {
    type: Number,
    default: 0
  },
  deliveryTime: {
    type: Number,
    default: 30
  },
  minOrderAmount: {
    type: Number,
    default: 15
  },
  deliveryFee: {
    type: Number,
    default: 3
  },
  isBrand: {
    type: Boolean,
    default: false
  },
  isNew: {
    type: Boolean,
    default: false
  },
  activities: {
    type: Array,
    default: () => []
  },
  clickable: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['click', 'imageError'])

const formatSales = computed(() => {
  if (props.monthlySales >= 10000) {
    return (props.monthlySales / 10000).toFixed(1) + '万'
  }
  return props.monthlySales + '+'
})

const displayedActivities = computed(() => {
  return props.activities.slice(0, 3)
})

const handleClick = () => {
  if (props.clickable) {
    emit('click', props.id)
  }
}

const handleImageError = () => {
  emit('imageError', props.id)
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-system.scss';

.c-merchant-card {
  display: flex;
  gap: $space-md;
  padding: $space-md;
  background: $color-surface;
  border-radius: $radius-md;
  
  &--clickable {
    cursor: pointer;
    @include tap-highlight;
  }
  
  &__image {
    width: 160rpx;
    height: 160rpx;
    border-radius: $radius-md;
    object-fit: cover;
    flex-shrink: 0;
  }
  
  &__content {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-width: 0;
  }
  
  &__header {
    display: flex;
    align-items: center;
    gap: $space-sm;
    margin-bottom: $space-xs;
  }
  
  &__name {
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    @include text-ellipsis;
  }
  
  &__brand-tag {
    flex-shrink: 0;
    background: linear-gradient(135deg, #FFD700, #FFA500);
    color: #8B4513;
    font-size: $font-size-xs;
    padding: 2rpx 8rpx;
    border-radius: $radius-sm;
    font-weight: $font-weight-semibold;
  }
  
  &__meta {
    display: flex;
    align-items: center;
    gap: $space-sm;
    margin-bottom: $space-xs;
  }
  
  &__rating {
    display: flex;
    align-items: center;
    gap: $space-xs;
    
    &-score {
      color: $color-primary;
      font-weight: $font-weight-bold;
      font-size: $font-size-sm;
    }
  }
  
  &__stars {
    display: flex;
    gap: 2rpx;
  }
  
  &__star {
    font-size: $font-size-xs;
    color: #ddd;
    
    &.is-filled {
      color: #FFC107;
    }
  }
  
  &__sales,
  &__time {
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
  
  &__delivery {
    margin-bottom: $space-xs;
    
    &-text {
      font-size: $font-size-sm;
      color: $color-text-secondary;
    }
  }
  
  &__divider {
    margin: 0 8rpx;
    color: $color-border;
  }
  
  &__delivery-free {
    color: $color-success;
    font-weight: $font-weight-medium;
  }
  
  &__activities {
    display: flex;
    flex-direction: column;
    gap: $space-xs;
  }
  
  &__activity {
    display: flex;
    align-items: center;
    gap: $space-sm;
  }
  
  &__activity-text {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    @include text-ellipsis;
  }
}
</style>
