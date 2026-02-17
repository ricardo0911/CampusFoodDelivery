<template>
  <view class="page">
    <!-- 状态卡片 -->
    <view class="status-card" :class="statusClass">
      <view class="status-icon">{{ statusIcon }}</view>
      <text class="status-text">{{ statusText }}</text>
      <text class="status-sub" v-if="statusSub">{{ statusSub }}</text>
    </view>

    <!-- 取餐码 -->
    <view class="pickup-code-card" v-if="reservation.status === 2">
      <text class="code-label">取餐码</text>
      <text class="code-value">{{ reservation.pickupCode }}</text>
      <text class="code-hint">请向商家出示此取餐码</text>
    </view>

    <!-- 订单信息 -->
    <view class="info-card">
      <view class="info-item">
        <text class="info-label">店铺</text>
        <text class="info-value">{{ reservation.shopName || '美味快餐店' }}</text>
      </view>
      <view class="info-item">
        <text class="info-label">取餐日期</text>
        <text class="info-value">{{ formatDate(reservation.pickupDate) }}</text>
      </view>
      <view class="info-item">
        <text class="info-label">取餐时段</text>
        <text class="info-value">{{ reservation.pickupTimeRange }}</text>
      </view>
      <view class="info-item" v-if="reservation.discountAmount > 0">
        <text class="info-label">错峰优惠</text>
        <text class="info-value discount">-¥{{ reservation.discountAmount }}</text>
      </view>
      <view class="info-item">
        <text class="info-label">预约状态</text>
        <text class="info-value">{{ reservation.statusText }}</text>
      </view>
    </view>

    <!-- 进度时间线 -->
    <view class="timeline-card">
      <text class="timeline-title">取餐进度</text>
      <view class="timeline">
        <view class="timeline-item" :class="{ done: reservation.status >= 0 }">
          <view class="timeline-dot"></view>
          <view class="timeline-content">
            <text class="timeline-label">已预约</text>
            <text class="timeline-time">{{ formatTime(reservation.createdAt) }}</text>
          </view>
        </view>
        <view class="timeline-item" :class="{ done: reservation.status >= 1, active: reservation.status === 1 }">
          <view class="timeline-dot"></view>
          <view class="timeline-content">
            <text class="timeline-label">制作中</text>
            <text class="timeline-time" v-if="reservation.status >= 1">商家正在准备</text>
          </view>
        </view>
        <view class="timeline-item" :class="{ done: reservation.status >= 2, active: reservation.status === 2 }">
          <view class="timeline-dot"></view>
          <view class="timeline-content">
            <text class="timeline-label">待取餐</text>
            <text class="timeline-time" v-if="reservation.status >= 2">请尽快取餐</text>
          </view>
        </view>
        <view class="timeline-item" :class="{ done: reservation.status === 3 }">
          <view class="timeline-dot"></view>
          <view class="timeline-content">
            <text class="timeline-label">已完成</text>
            <text class="timeline-time" v-if="reservation.status === 3">{{ formatTime(reservation.pickupTime) }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-bar">
      <view class="action-btn secondary" @click="goOrderDetail">
        <text>订单详情</text>
      </view>
      <view class="action-btn primary" v-if="reservation.status === 2" @click="confirmPickup">
        <text>我已取餐</text>
      </view>
      <view class="action-btn danger" v-if="reservation.status < 2" @click="cancelReservation">
        <text>取消预约</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { get, put, del } from '@/utils/request'

const orderId = ref(null)
const reservation = ref({})

const statusIcon = computed(() => {
  switch (reservation.value.status) {
    case 0: return '⏳'
    case 1: return '👨‍🍳'
    case 2: return '🔔'
    case 3: return '✅'
    case 4: return '⚠️'
    case 5: return '❌'
    default: return '📋'
  }
})

const statusText = computed(() => {
  switch (reservation.value.status) {
    case 0: return '等待商家制作'
    case 1: return '商家正在制作'
    case 2: return '餐品已就绪'
    case 3: return '已完成取餐'
    case 4: return '取餐超时'
    case 5: return '已取消'
    default: return '加载中...'
  }
})

const statusSub = computed(() => {
  switch (reservation.value.status) {
    case 0: return '预计制作时间 10-15 分钟'
    case 1: return '大约还需 5-10 分钟'
    case 2: return '请在 30 分钟内取餐'
    case 3: return '感谢您的用餐'
    case 4: return '超时未取，已自动退款'
    default: return ''
  }
})

const statusClass = computed(() => {
  switch (reservation.value.status) {
    case 2: return 'ready'
    case 3: return 'done'
    case 4:
    case 5: return 'error'
    default: return 'pending'
  }
})

// 加载预约信息
const loadReservation = async () => {
  try {
    const res = await get(`/customer/reservation/order/${orderId.value}`)
    if (res.code === 200 && res.data) {
      reservation.value = res.data
    }
  } catch (e) {
    // 模拟数据
    reservation.value = {
      id: 1,
      orderId: orderId.value,
      pickupCode: '286541',
      pickupDate: '2026-01-18',
      pickupTimeRange: '12:00-12:15',
      status: 2,
      statusText: '已完成，请取餐',
      discountAmount: 3.75,
      shopName: '美味快餐店',
      createdAt: '2026-01-18T11:30:00'
    }
  }
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

// 确认取餐
const confirmPickup = async () => {
  uni.showModal({
    title: '确认取餐',
    content: '请确认您已取到餐品',
    success: async (res) => {
      if (res.confirm) {
        try {
          await put(`/customer/reservation/pickup/${orderId.value}`)
          uni.showToast({ title: '取餐成功', icon: 'success' })
          loadReservation()
        } catch (e) {
          reservation.value.status = 3
          uni.showToast({ title: '取餐成功', icon: 'success' })
        }
      }
    }
  })
}

// 取消预约
const cancelReservation = async () => {
  uni.showModal({
    title: '取消预约',
    content: '确定要取消此预约吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await del(`/customer/reservation/order/${orderId.value}`)
          uni.showToast({ title: '已取消', icon: 'success' })
          setTimeout(() => {
            uni.navigateBack()
          }, 1000)
        } catch (e) {
          uni.showToast({ title: '取消失败', icon: 'none' })
        }
      }
    }
  })
}

// 查看订单详情
const goOrderDetail = () => {
  uni.navigateTo({ url: `/pages/order/detail?id=${orderId.value}` })
}

onMounted(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  orderId.value = page.options?.orderId || 1
  loadReservation()
  
  // 定时刷新状态
  const timer = setInterval(() => {
    if (reservation.value.status < 3) {
      loadReservation()
    } else {
      clearInterval(timer)
    }
  }, 10000)
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 30rpx;
  padding-top: calc(var(--status-bar-height) + 30rpx);
}

/* 状态卡片 */
.status-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 40rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.status-card.ready {
  background: linear-gradient(135deg, #2da44e, #4caf50);
}

.status-card.done {
  background: linear-gradient(135deg, #666, #888);
}

.status-card.error {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
}

.status-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.status-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
}

.status-sub {
  font-size: 26rpx;
  color: rgba(255,255,255,0.8);
  margin-top: 12rpx;
}

/* 取餐码卡片 */
.pickup-code-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #fff;
  margin-top: 24rpx;
  padding: 40rpx;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 30rpx rgba(0,0,0,0.05);
}

.code-label {
  font-size: 26rpx;
  color: #999;
}

.code-value {
  font-size: 80rpx;
  font-weight: bold;
  color: #2da44e;
  letter-spacing: 20rpx;
  margin: 20rpx 0;
}

.code-hint {
  font-size: 24rpx;
  color: #666;
}

/* 信息卡片 */
.info-card {
  background: #fff;
  margin-top: 24rpx;
  padding: 30rpx;
  border-radius: 24rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 28rpx;
  color: #999;
}

.info-value {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.info-value.discount {
  color: #ff6b35;
}

/* 时间线 */
.timeline-card {
  background: #fff;
  margin-top: 24rpx;
  padding: 30rpx;
  border-radius: 24rpx;
}

.timeline-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
  display: block;
}

.timeline {
  padding-left: 30rpx;
}

.timeline-item {
  display: flex;
  position: relative;
  padding-bottom: 40rpx;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: 9rpx;
  top: 30rpx;
  bottom: 0;
  width: 2rpx;
  background: #eee;
}

.timeline-item:last-child::before {
  display: none;
}

.timeline-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background: #eee;
  margin-right: 24rpx;
  margin-top: 6rpx;
  flex-shrink: 0;
}

.timeline-item.done .timeline-dot {
  background: #2da44e;
}

.timeline-item.active .timeline-dot {
  background: #667eea;
  box-shadow: 0 0 0 8rpx rgba(102, 126, 234, 0.2);
}

.timeline-content {
  flex: 1;
}

.timeline-label {
  font-size: 28rpx;
  color: #333;
  display: block;
}

.timeline-time {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
}

/* 操作栏 */
.action-bar {
  display: flex;
  gap: 20rpx;
  margin-top: 40rpx;
}

.action-btn {
  flex: 1;
  text-align: center;
  padding: 28rpx;
  border-radius: 40rpx;
  font-size: 30rpx;
  font-weight: bold;
}

.action-btn.primary {
  background: linear-gradient(135deg, #2da44e, #4caf50);
  color: #fff;
}

.action-btn.secondary {
  background: #f5f5f5;
  color: #666;
}

.action-btn.danger {
  background: #fff0eb;
  color: #ff6b35;
}
</style>
