<template>
  <view class="demo-page">
    <view class="demo-section">
      <text class="demo-title">按钮组件 (CButton)</text>
      <view class="demo-row">
        <CButton type="primary">主按钮</CButton>
        <CButton type="secondary">次要按钮</CButton>
        <CButton type="text">文字按钮</CButton>
      </view>
      <view class="demo-row">
        <CButton type="primary" size="sm">小按钮</CButton>
        <CButton type="primary" size="lg">大按钮</CButton>
      </view>
      <view class="demo-row">
        <CButton type="primary" block>块级按钮</CButton>
      </view>
      <view class="demo-row">
        <CButton type="primary" loading>加载中</CButton>
        <CButton type="primary" disabled>禁用</CButton>
      </view>
    </view>

    <view class="demo-section">
      <text class="demo-title">标签组件 (CTag)</text>
      <view class="demo-row">
        <CTag type="primary">满减</CTag>
        <CTag type="success">新客</CTag>
        <CTag type="warning">热门</CTag>
        <CTag type="error">限时</CTag>
        <CTag type="default">默认</CTag>
      </view>
      <view class="demo-row">
        <CTag type="primary" rounded>圆角标签</CTag>
        <CTag type="success" size="lg">大标签</CTag>
      </view>
    </view>

    <view class="demo-section">
      <text class="demo-title">步进器组件 (CStepper)</text>
      <view class="demo-row">
        <CStepper v-model:value="count" :min="1" :max="10" />
        <text>当前值: {{ count }}</text>
      </view>
    </view>

    <view class="demo-section">
      <text class="demo-title">空状态组件 (CEmpty)</text>
      <CEmpty
        title="暂无订单"
        description="快去下单吧"
      >
        <template #action>
          <CButton type="primary" size="sm">去逛逛</CButton>
        </template>
      </CEmpty>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { CButton, CTag, CStepper, CEmpty } from '@/components/common'

const count = ref(1)
</script>

<style lang="scss" scoped>
@import '@/styles/design-system.scss';

.demo-page {
  padding: $space-md;
  background: $color-background;
  min-height: 100vh;
}

.demo-section {
  margin-bottom: $space-lg;
  padding: $space-md;
  background: $color-surface;
  border-radius: $radius-md;
}

.demo-title {
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
  margin-bottom: $space-md;
  display: block;
}

.demo-row {
  @include flex-center;
  gap: $space-md;
  margin-bottom: $space-md;
  flex-wrap: wrap;
}
</style>
