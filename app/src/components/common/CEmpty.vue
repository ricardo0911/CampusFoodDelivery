<template>
  <view class="c-empty">
    <image
      v-if="image"
      class="c-empty__image"
      :src="image"
      mode="aspectFit"
    />
    <text v-if="title" class="c-empty__title">{{ title }}</text>
    <text v-if="description" class="c-empty__desc">{{ description }}</text>
    <view v-if="$slots.action" class="c-empty__action">
      <slot name="action" />
    </view>
  </view>
</template>

<script setup>
defineProps({
  image: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    default: '暂无数据'
  },
  description: {
    type: String,
    default: ''
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/design-system.scss';

.c-empty {
  @include flex-center;
  flex-direction: column;
  padding: $space-2xl $space-md;
  
  &__image {
    width: 240rpx;
    height: 240rpx;
    margin-bottom: $space-lg;
    opacity: 0.6;
  }
  
  &__title {
    font-size: $font-size-lg;
    color: $color-text-primary;
    font-weight: $font-weight-medium;
    margin-bottom: $space-sm;
  }
  
  &__desc {
    font-size: $font-size-base;
    color: $color-text-secondary;
    text-align: center;
  }
  
  &__action {
    margin-top: $space-lg;
  }
}
</style>
