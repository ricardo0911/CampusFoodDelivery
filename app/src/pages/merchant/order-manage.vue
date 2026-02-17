<template>
  <view class="page">
    <!-- 头部状态筛选 -->
    <view class="header">
      <scroll-view class="status-bar" scroll-x>
        <view
          class="status-item"
          :class="{ active: currentStatus === '' }"
          @click="setStatus('')"
        >
          <text class="status-name">全部</text>
          <text class="status-count" v-if="statusCounts.all > 0">{{ statusCounts.all }}</text>
        </view>
        <view
          class="status-item"
          :class="{ active: currentStatus === 'PENDING' }"
          @click="setStatus('PENDING')"
        >
          <text class="status-name">待接单</text>
          <text class="status-count" v-if="statusCounts.pending > 0">{{ statusCounts.pending }}</text>
        </view>
        <view
          class="status-item"
          :class="{ active: currentStatus === 'ACCEPTED' }"
          @click="setStatus('ACCEPTED')"
        >
          <text class="status-name">制作中</text>
          <text class="status-count" v-if="statusCounts.accepted > 0">{{ statusCounts.accepted }}</text>
        </view>
        <view
          class="status-item"
          :class="{ active: currentStatus === 'COMPLETED' }"
          @click="setStatus('COMPLETED')"
        >
          <text class="status-name">已完成</text>
        </view>
        <view
          class="status-item"
          :class="{ active: currentStatus === 'CANCELLED' }"
          @click="setStatus('CANCELLED')"
        >
          <text class="status-name">已取消</text>
        </view>
      </scroll-view>

      <!-- 实时刷新开关 -->
      <view class="refresh-bar">
        <text class="refresh-text">实时刷新</text>
        <switch :checked="autoRefresh" @change="toggleAutoRefresh" color="#667eea" />
      </view>
    </view>

    <!-- 订单列表 -->
    <scroll-view class="order-list" scroll-y @scrolltolower="loadMore" refresher-enabled :refresher-triggered="refreshing" @refresherrefresh="onRefresh">
      <view
        class="order-card"
        v-for="order in orderList"
        :key="order.id"
        :class="getStatusClass(order.status)"
      >
        <!-- 订单头部 -->
        <view class="order-header" @click="toggleExpand(order)">
          <view class="order-info">
            <text class="order-no">订单 #{{ order.orderNo }}</text>
            <text class="order-time">{{ formatTime(order.createTime) }}</text>
          </view>
          <view class="order-status" :class="getStatusClass(order.status)">
            {{ getStatusText(order.status) }}
          </view>
        </view>

        <!-- 订单内容 -->
        <view class="order-content">
          <view class="order-items">
            <view class="item" v-for="(item, index) in order.items" :key="index">
              <text class="item-name">{{ item.dishName }} x{{ item.quantity }}</text>
              <text class="item-price">¥{{ item.price * item.quantity }}</text>
            </view>
          </view>

          <!-- 展开详情 -->
          <view class="order-detail" v-if="expandedOrderId === order.id">
            <view class="detail-row">
              <text class="detail-label">配送地址</text>
              <text class="detail-value">{{ order.address || '到店自取' }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">联系电话</text>
              <text class="detail-value">{{ order.phone || '--' }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">备注</text>
              <text class="detail-value">{{ order.remark || '无' }}</text>
            </view>
          </view>

          <view class="order-summary">
            <view class="expand-hint" @click="toggleExpand(order)">
              <text>{{ expandedOrderId === order.id ? '收起' : '展开详情' }}</text>
              <text class="expand-icon" :class="{ expanded: expandedOrderId === order.id }">▼</text>
            </view>
            <view class="order-total">
              <text class="total-label">合计</text>
              <text class="total-value">¥{{ order.totalAmount }}</text>
            </view>
          </view>
        </view>

        <!-- 订单操作 -->
        <view class="order-actions" v-if="order.status === 'PENDING'">
          <button class="btn-reject" @click="rejectOrder(order)">拒单</button>
          <button class="btn-accept" @click="acceptOrder(order.id)">接单</button>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="orderList.length > 0">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="orderList.length === 0 && !loading">
        <view class="empty-icon">📋</view>
        <text class="empty-text">暂无订单</text>
        <text class="empty-hint">{{ currentStatus ? '该状态下没有订单' : '订单会在这里显示' }}</text>
      </view>

      <view class="safe-bottom"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import {
  getMerchantOrderList,
  acceptOrder as acceptOrderApi,
  rejectOrder as rejectOrderApi
} from '@/api/merchant'

const orderList = ref([])
const currentStatus = ref('')
const loading = ref(false)
const refreshing = ref(false)
const page = ref(1)
const size = ref(10)
const noMore = ref(false)
const expandedOrderId = ref(null)
const autoRefresh = ref(true)
let refreshTimer = null

const statusCounts = ref({
  all: 0,
  pending: 0,
  accepted: 0
})

// 加载订单列表
const loadOrderList = async (isRefresh = true) => {
  if (loading.value) return
  loading.value = true

  if (isRefresh) {
    page.value = 1
    noMore.value = false
  }

  try {
    const params = {
      page: page.value,
      size: size.value
    }
    if (currentStatus.value) {
      params.status = currentStatus.value
    }

    const res = await getMerchantOrderList(params)
    if (res.data && res.data.records) {
      if (isRefresh) {
        orderList.value = res.data.records
      } else {
        orderList.value = [...orderList.value, ...res.data.records]
      }

      if (res.data.records.length < size.value) {
        noMore.value = true
      }

      // 更新状态统计
      updateStatusCounts(res.data)
    }
  } catch (e) {
    console.error('获取订单列表失败:', e)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 更新状态统计
const updateStatusCounts = (data) => {
  // 这里简化处理，实际应该从后端获取
  statusCounts.value.all = data.total || 0
}

// 加载更多
const loadMore = () => {
  if (!noMore.value && !loading.value) {
    page.value++
    loadOrderList(false)
  }
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  loadOrderList(true)
}

// 设置状态筛选
const setStatus = (status) => {
  currentStatus.value = status
  loadOrderList(true)
}

// 展开/收起详情
const toggleExpand = (order) => {
  if (expandedOrderId.value === order.id) {
    expandedOrderId.value = null
  } else {
    expandedOrderId.value = order.id
  }
}

// 接单
const acceptOrder = async (orderId) => {
  try {
    await acceptOrderApi(orderId)
    uni.showToast({ title: '接单成功', icon: 'success' })
    loadOrderList(true)
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

// 拒单
const rejectOrder = (order) => {
  uni.showModal({
    title: '确认拒单',
    editable: true,
    placeholderText: '请输入拒单原因（选填）',
    success: async (res) => {
      if (res.confirm) {
        try {
          await rejectOrderApi(order.id, res.content || '商家原因')
          uni.showToast({ title: '已拒单', icon: 'success' })
          loadOrderList(true)
        } catch (e) {
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待接单',
    'ACCEPTED': '制作中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

// 获取状态样式类
const getStatusClass = (status) => {
  const classMap = {
    'PENDING': 'pending',
    'ACCEPTED': 'accepted',
    'COMPLETED': 'completed',
    'CANCELLED': 'cancelled'
  }
  return classMap[status] || ''
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// 切换自动刷新
const toggleAutoRefresh = (e) => {
  autoRefresh.value = e.detail.value
  if (autoRefresh.value) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
}

// 开始自动刷新
const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    loadOrderList(true)
  }, 15000) // 15秒刷新一次
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

onMounted(() => {
  loadOrderList()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* 头部 */
.header {
  background: linear-gradient(135deg, #1a1a2e 0%, #2d2d44 100%);
  padding: 30rpx;
  padding-top: calc(var(--status-bar-height) + 20rpx);
}

.status-bar {
  white-space: nowrap;
  margin-bottom: 20rpx;
}

.status-item {
  display: inline-flex;
  align-items: center;
  padding: 16rpx 32rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 32rpx;
  margin-right: 16rpx;
  transition: all 0.3s;
}

.status-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.status-name {
  color: rgba(255, 255, 255, 0.9);
  font-size: 26rpx;
}

.status-item.active .status-name {
  color: #fff;
  font-weight: 500;
}

.status-count {
  min-width: 32rpx;
  height: 32rpx;
  background: #ff6b35;
  border-radius: 16rpx;
  color: #fff;
  font-size: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 12rpx;
  padding: 0 8rpx;
}

.refresh-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10rpx;
}

.refresh-text {
  color: rgba(255, 255, 255, 0.7);
  font-size: 24rpx;
}

/* 订单列表 */
.order-list {
  flex: 1;
  padding: 20rpx;
}

.order-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
  border-left: 6rpx solid #ddd;
}

.order-card.pending {
  border-left-color: #667eea;
}

.order-card.accepted {
  border-left-color: #f7931e;
}

.order-card.completed {
  border-left-color: #2da44e;
}

.order-card.cancelled {
  border-left-color: #999;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
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
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  background: #f5f5f5;
  color: #666;
}

.order-status.pending {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.order-status.accepted {
  background: rgba(247, 147, 30, 0.1);
  color: #f7931e;
}

.order-status.completed {
  background: rgba(45, 164, 78, 0.1);
  color: #2da44e;
}

.order-status.cancelled {
  background: #f5f5f5;
  color: #999;
}

.order-content {
  padding: 20rpx 0;
  border-top: 1rpx solid #f5f5f5;
  border-bottom: 1rpx solid #f5f5f5;
}

.order-items {
  margin-bottom: 16rpx;
}

.item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
}

.item-name {
  font-size: 26rpx;
  color: #333;
  flex: 1;
}

.item-price {
  font-size: 26rpx;
  color: #666;
}

.order-detail {
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
}

.detail-row {
  display: flex;
  margin-bottom: 12rpx;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  width: 140rpx;
  font-size: 24rpx;
  color: #999;
}

.detail-value {
  flex: 1;
  font-size: 24rpx;
  color: #333;
}

.order-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.expand-hint {
  display: flex;
  align-items: center;
  color: #667eea;
  font-size: 24rpx;
}

.expand-icon {
  margin-left: 8rpx;
  font-size: 20rpx;
  transition: transform 0.3s;
}

.expand-icon.expanded {
  transform: rotate(180deg);
}

.order-total {
  display: flex;
  align-items: baseline;
}

.total-label {
  font-size: 24rpx;
  color: #999;
  margin-right: 12rpx;
}

.total-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #ff6b35;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
  margin-top: 20rpx;
  padding-top: 20rpx;
}

.btn-reject {
  padding: 16rpx 40rpx;
  background: #f5f5f5;
  color: #666;
  font-size: 28rpx;
  border-radius: 12rpx;
  border: none;
}

.btn-accept {
  padding: 16rpx 40rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 28rpx;
  border-radius: 12rpx;
  border: none;
}

/* 加载更多 */
.load-more {
  text-align: center;
  padding: 30rpx;
  color: #999;
  font-size: 24rpx;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #333;
  margin-bottom: 12rpx;
}

.empty-hint {
  font-size: 26rpx;
  color: #999;
}

.safe-bottom {
  height: 40rpx;
}
</style>
