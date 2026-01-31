<template>
  <view class="c-load-more">
    <view v-if="status === 'loading'" class="c-load-more__loading">
      <view class="c-load-more__spinner" />
      <text class="c-load-more__text">{{ loadingText }}</text>
    </view>
    
    <view
      v-else-if="status === 'more'"
      class="c-load-more__more"
      @click="handleLoadMore"
    >
      <text class="c-load-more__text">{{ loadMoreText }}</text>
      <view class="c-load-more__arrow" />
    </view>
    
    <view v-else-if="status === 'noMore'" class="c-load-more__nomore">
      <text class="c-load-more__text">{{ noMoreText }}</text>
    </view>
    
    <view
      v-else-if="status === 'error'"
      class="c-load-more__error"
      @click="handleRetry"
    >
      <text class="c-load-more__text">{{ errorText }}</text>
      <text class="c-load-more__retry">点击重试</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  status: {
    type: String,
    default: 'more',
    validator: (value) => ['loading', 'more', 'noMore', 'error'].includes(value)
  },
  loadingText: {
    type: String,
    default: '加载中...'
  },
  loadMoreText: {
    type: String,
    default: '上拉加载更多'
  },
  noMoreText: {
    type: String,
    default: '没有更多了'
  },
  errorText: {
    type: String,
    default: '加载失败'
  }
})

const emit = defineEmits(['loadMore', 'retry'])

const handleLoadMore = () => {
  if (props.status === 'more') {
    emit('loadMore')
  }
}

const handleRetry = () => {
  emit('retry')
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-system.scss';

.c-load-more {
  @include flex-center;
  padding: $space-lg $space-md;
  
  &__loading,
  &__more,
  &__nomore,
  &__error {
    @include flex-center;
    gap: $space-sm;
  }
  
  &__loading {
    .c-load-more__spinner {
      width: 32rpx;
      height: 32rpx;
      border: 3rpx solid $color-border;
      border-top-color: $color-primary;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
  }
  
  &__more {
    cursor: pointer;
    @include tap-highlight;
    
    .c-load-more__arrow {
      width: 0;
      height: 0;
      border-left: 8rpx solid transparent;
      border-right: 8rpx solid transparent;
      border-top: 10rpx solid $color-text-tertiary;
      transition: transform 200ms;
    }
    
    &:active {
      .c-load-more__arrow {
        transform: translateY(4rpx);
      }
    }
  }
  
  &__error {
    cursor: pointer;
    @include tap-highlight;
    
    .c-load-more__retry {
      color: $color-primary;
      font-weight: $font-weight-medium;
    }
  }
  
  &__text {
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
}
</style>
