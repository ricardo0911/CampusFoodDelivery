<template>
  <view class="page">
    <!-- 深色头部 -->
    <view class="header">
      <view class="header-top">
        <view class="shop-info">
          <image class="shop-logo" :src="shopInfo.logo || '/static/shop1.jpg'" mode="aspectFill" />
          <view class="shop-meta">
            <text class="shop-name">{{ shopInfo.name || '我的店铺' }}</text>
            <view class="shop-status" :class="{ open: shopInfo.status === 1 }">
              <text class="status-dot"></text>
              <text class="status-text">{{ shopInfo.status === 1 ? '营业中' : '已打烊' }}</text>
            </view>
          </view>
        </view>
        <view class="header-actions">
          <view class="action-btn" @click="toggleShopStatus">
            <text class="action-icon">{{ shopInfo.status === 1 ? '🔴' : '🟢' }}</text>
            <text class="action-text">{{ shopInfo.status === 1 ? '歇业' : '营业' }}</text>
          </view>
        </view>
      </view>

      <!-- 今日数据概览 -->
      <view class="stats-grid">
        <view class="stat-card">
          <text class="stat-value">{{ todayStats.orderCount || 0 }}</text>
          <text class="stat-label">今日订单</text>
        </view>
        <view class="stat-card">
          <text class="stat-value">¥{{ todayStats.revenue || 0 }}</text>
          <text class="stat-label">今日营业额</text>
        </view>
        <view class="stat-card">
          <text class="stat-value">{{ todayStats.newOrderCount || 0 }}</text>
          <text class="stat-label">新订单</text>
          <view class="badge" v-if="todayStats.newOrderCount > 0">{{ todayStats.newOrderCount }}</view>
        </view>
        <view class="stat-card">
          <text class="stat-value">{{ todayStats.pendingCount || 0 }}</text>
          <text class="stat-label">待处理</text>
          <view class="badge warning" v-if="todayStats.pendingCount > 0">{{ todayStats.pendingCount }}</view>
        </view>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="section">
      <view class="section-title">
        <text class="title-text">快捷操作</text>
      </view>
      <view class="quick-actions">
        <view class="action-item" @click="goToPage('dish-manage')">
          <view class="action-icon-wrap dish">
            <text class="action-icon">🍽️</text>
          </view>
          <text class="action-name">菜品管理</text>
        </view>
        <view class="action-item" @click="goToPage('order-manage')">
          <view class="action-icon-wrap order">
            <text class="action-icon">📋</text>
          </view>
          <text class="action-name">订单管理</text>
        </view>
        <view class="action-item" @click="goToPage('shop-settings')">
          <view class="action-icon-wrap setting">
            <text class="action-icon">⚙️</text>
          </view>
          <text class="action-name">店铺设置</text>
        </view>
      </view>
    </view>

    <!-- 待处理订单 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title">
          <text class="title-text">待处理订单</text>
          <text class="title-sub" v-if="pendingOrders.length > 0">{{ pendingOrders.length }}个新订单</text>
        </view>
        <text class="view-all" @click="goToPage('order-manage')">查看全部 ></text>
      </view>

      <view class="order-list" v-if="pendingOrders.length > 0">
        <view class="order-card" v-for="order in pendingOrders" :key="order.id">
          <view class="order-header">
            <view class="order-info">
              <text class="order-no">订单 #{{ order.orderNo }}</text>
              <text class="order-time">{{ formatTime(order.createTime) }}</text>
            </view>
            <view class="order-status pending">待接单</view>
          </view>

          <view class="order-items">
            <text class="items-text">{{ getItemsSummary(order.items) }}</text>
          </view>

          <view class="order-footer">
            <view class="order-amount">
              <text class="amount-label">合计</text>
              <text class="amount-value">¥{{ order.totalAmount }}</text>
            </view>
            <view class="order-actions">
              <button class="btn-reject" @click="rejectOrder(order.id)">拒单</button>
              <button class="btn-accept" @click="acceptOrder(order.id)">接单</button>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-else>
        <view class="empty-icon">📋</view>
        <text class="empty-text">暂无待处理订单</text>
        <text class="empty-hint">新订单会在这里显示</text>
      </view>
    </view>

    <!-- 底部安全区 -->
    <view class="safe-bottom"></view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import {
  getShopInfo,
  updateShopInfo,
  getMerchantOrderList,
  acceptOrder as acceptOrderApi,
  rejectOrder as rejectOrderApi,
  getTodayStats
} from '@/api/merchant'

const shopInfo = ref({})
const todayStats = ref({
  orderCount: 0,
  revenue: 0,
  newOrderCount: 0,
  pendingCount: 0
})
const pendingOrders = ref([])
let refreshTimer = null

// 加载店铺信息
const loadShopInfo = async () => {
  try {
    const res = await getShopInfo()
    if (res.data) {
      shopInfo.value = res.data
    }
  } catch (e) {
    console.error('获取店铺信息失败:', e)
  }
}

// 加载今日统计
const loadTodayStats = async () => {
  try {
    const res = await getTodayStats()
    if (res.data) {
      todayStats.value = res.data
    }
  } catch (e) {
    console.error('获取统计数据失败:', e)
  }
}

// 加载待处理订单
const loadPendingOrders = async () => {
  try {
    const res = await getMerchantOrderList({ status: 'PENDING', page: 1, size: 5 })
    if (res.data && res.data.records) {
      pendingOrders.value = res.data.records
    }
  } catch (e) {
    console.error('获取订单失败:', e)
  }
}

// 切换营业状态
const toggleShopStatus = async () => {
  const newStatus = shopInfo.value.status === 1 ? 0 : 1
  try {
    await updateShopInfo({ ...shopInfo.value, status: newStatus })
    shopInfo.value.status = newStatus
    uni.showToast({
      title: newStatus === 1 ? '已开始营业' : '已暂停营业',
      icon: 'success'
    })
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

// 接单
const acceptOrder = async (orderId) => {
  try {
    await acceptOrderApi(orderId)
    uni.showToast({ title: '接单成功', icon: 'success' })
    loadPendingOrders()
    loadTodayStats()
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

// 拒单
const rejectOrder = async (orderId) => {
  uni.showModal({
    title: '确认拒单',
    content: '确定要拒绝此订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await rejectOrderApi(orderId, '商家原因')
          uni.showToast({ title: '已拒单', icon: 'success' })
          loadPendingOrders()
        } catch (e) {
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

// 获取订单商品摘要
const getItemsSummary = (items) => {
  if (!items || items.length === 0) return ''
  const names = items.map(item => item.dishName)
  return names.slice(0, 3).join('、') + (names.length > 3 ? ` 等${names.length}件` : '')
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

// 页面跳转
const goToPage = (page) => {
  uni.navigateTo({ url: `/pages/merchant/${page}` })
}

// 自动刷新
const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    loadPendingOrders()
    loadTodayStats()
  }, 30000) // 30秒刷新一次
}

onMounted(() => {
  loadShopInfo()
  loadTodayStats()
  loadPendingOrders()
  startAutoRefresh()
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
}

/* 深色头部 */
.header {
  background: linear-gradient(135deg, #1a1a2e 0%, #2d2d44 100%);
  padding: 30rpx;
  padding-top: calc(var(--status-bar-height) + 20rpx);
  border-radius: 0 0 40rpx 40rpx;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.shop-info {
  display: flex;
  align-items: center;
}

.shop-logo {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  margin-right: 20rpx;
  background: #fff;
}

.shop-meta {
  display: flex;
  flex-direction: column;
}

.shop-name {
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.shop-status {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
}

.shop-status.open {
  background: rgba(45, 164, 78, 0.2);
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #999;
  margin-right: 8rpx;
}

.shop-status.open .status-dot {
  background: #2da44e;
}

.status-text {
  color: rgba(255, 255, 255, 0.8);
  font-size: 22rpx;
}

.header-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  padding: 12rpx 20rpx;
  border-radius: 16rpx;
}

.action-icon {
  font-size: 32rpx;
  margin-bottom: 4rpx;
}

.action-text {
  color: rgba(255, 255, 255, 0.9);
  font-size: 20rpx;
}

/* 数据统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
}

.stat-card {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20rpx;
  padding: 20rpx 10rpx;
  text-align: center;
  position: relative;
}

.stat-value {
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 8rpx;
}

.stat-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 22rpx;
}

.badge {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  min-width: 32rpx;
  height: 32rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 16rpx;
  color: #fff;
  font-size: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
}

.badge.warning {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
}

/* 区块样式 */
.section {
  margin: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.title-text {
  font-size: 32rpx;
  font-weight: bold;
  color: #1a1a2e;
}

.title-sub {
  font-size: 24rpx;
  color: #999;
  margin-left: 16rpx;
}

.view-all {
  font-size: 26rpx;
  color: #667eea;
}

/* 快捷操作 */
.quick-actions {
  display: flex;
  justify-content: space-around;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx;
}

.action-icon-wrap {
  width: 100rpx;
  height: 100rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
}

.action-icon-wrap.dish {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.action-icon-wrap.order {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
}

.action-icon-wrap.setting {
  background: linear-gradient(135deg, #2da44e, #3ddc84);
}

.action-icon-wrap .action-icon {
  font-size: 48rpx;
  margin-bottom: 0;
}

.action-name {
  font-size: 26rpx;
  color: #333;
}

/* 订单列表 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.order-card {
  background: #f8f9fa;
  border-radius: 16rpx;
  padding: 24rpx;
  border-left: 6rpx solid #667eea;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
}

.order-info {
  display: flex;
  flex-direction: column;
}

.order-no {
  font-size: 28rpx;
  font-weight: bold;
  color: #1a1a2e;
  margin-bottom: 8rpx;
}

.order-time {
  font-size: 24rpx;
  color: #999;
}

.order-status {
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
}

.order-status.pending {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.order-items {
  margin-bottom: 16rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #eee;
}

.items-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-amount {
  display: flex;
  align-items: baseline;
}

.amount-label {
  font-size: 24rpx;
  color: #999;
  margin-right: 8rpx;
}

.amount-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #ff6b35;
}

.order-actions {
  display: flex;
  gap: 16rpx;
}

.btn-reject {
  padding: 12rpx 24rpx;
  background: #f5f5f5;
  color: #666;
  font-size: 26rpx;
  border-radius: 8rpx;
  border: none;
}

.btn-accept {
  padding: 12rpx 24rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 26rpx;
  border-radius: 8rpx;
  border: none;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 16rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 8rpx;
}

.empty-hint {
  font-size: 24rpx;
  color: #999;
}

.safe-bottom {
  height: 40rpx;
}
</style>
