<template>
  <view class="page">
    <!-- 自定义导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <view class="back-icon"></view>
      </view>
      <text class="nav-title">限时秒杀</text>
      <view class="nav-placeholder"></view>
    </view>

    <!-- 倒计时头部 -->
    <view class="countdown-header">
      <view class="countdown-info">
        <text class="countdown-label">距离结束还剩</text>
        <view class="countdown-time">
          <view class="time-block">{{ hours }}</view>
          <text class="time-sep">:</text>
          <view class="time-block">{{ minutes }}</view>
          <text class="time-sep">:</text>
          <view class="time-block">{{ seconds }}</view>
        </view>
      </view>
    </view>

    <!-- 进行中的秒杀 -->
    <view class="section" v-if="currentSales.length > 0">
      <view class="section-header">
        <view class="section-icon hot"></view>
        <text class="section-title">正在抢购</text>
      </view>
      <view class="sale-list">
        <view
          class="sale-item"
          v-for="sale in currentSales"
          :key="sale.id"
        >
          <image class="sale-image" :src="sale.dishImage || '/static/food.png'" mode="aspectFill" />
          <view class="sale-info">
            <text class="sale-name">{{ sale.dishName }}</text>
            <view class="sale-price">
              <text class="price-current">¥{{ sale.salePrice }}</text>
              <text class="price-original">¥{{ sale.originalPrice }}</text>
            </view>
            <view class="sale-progress">
              <view class="progress-bar" :style="{ width: getProgress(sale) + '%' }"></view>
            </view>
            <text class="sale-stock">已抢{{ sale.sold }}件，剩{{ sale.stock - sale.sold }}件</text>
          </view>
          <view
            class="grab-btn"
            :class="{ disabled: sale.sold >= sale.stock }"
            @click="handleGrab(sale)"
          >
            {{ sale.sold >= sale.stock ? '已抢光' : '立即抢' }}
          </view>
        </view>
      </view>
    </view>

    <!-- 即将开始 -->
    <view class="section" v-if="upcomingSales.length > 0">
      <view class="section-header">
        <view class="section-icon upcoming"></view>
        <text class="section-title">即将开始</text>
      </view>
      <view class="sale-list">
        <view
          class="sale-item upcoming"
          v-for="sale in upcomingSales"
          :key="sale.id"
        >
          <image class="sale-image" :src="sale.dishImage || '/static/food.png'" mode="aspectFill" />
          <view class="sale-info">
            <text class="sale-name">{{ sale.dishName }}</text>
            <view class="sale-price">
              <text class="price-current">¥{{ sale.salePrice }}</text>
              <text class="price-original">¥{{ sale.originalPrice }}</text>
            </view>
            <text class="sale-time">{{ formatTime(sale.startTime) }} 开抢</text>
          </view>
          <view class="remind-btn" @click="handleRemind(sale)">
            提醒我
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty" v-if="currentSales.length === 0 && upcomingSales.length === 0">
      <view class="empty-icon"></view>
      <text class="empty-text">暂无秒杀活动</text>
      <text class="empty-tip">敬请期待</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const currentSales = ref([])
const upcomingSales = ref([])
const hours = ref('00')
const minutes = ref('00')
const seconds = ref('00')
let timer = null

const goBack = () => {
  uni.navigateBack()
}

const getProgress = (sale) => {
  return Math.min((sale.sold / sale.stock) * 100, 100)
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const updateCountdown = () => {
  if (currentSales.value.length === 0) return

  // 找到最近结束的秒杀
  const now = new Date().getTime()
  let nearestEnd = null

  for (const sale of currentSales.value) {
    const endTime = new Date(sale.endTime).getTime()
    if (!nearestEnd || endTime < nearestEnd) {
      nearestEnd = endTime
    }
  }

  if (!nearestEnd) return

  const diff = nearestEnd - now
  if (diff <= 0) {
    hours.value = '00'
    minutes.value = '00'
    seconds.value = '00'
    loadData()
    return
  }

  const h = Math.floor(diff / (1000 * 60 * 60))
  const m = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const s = Math.floor((diff % (1000 * 60)) / 1000)

  hours.value = String(h).padStart(2, '0')
  minutes.value = String(m).padStart(2, '0')
  seconds.value = String(s).padStart(2, '0')
}

const loadData = async () => {
  try {
    const res = await uni.request({
      url: 'http://localhost:8080/api/customer/flash-sale/list',
      method: 'GET'
    })

    if (res.data.code === 200) {
      currentSales.value = res.data.data.current || []
      upcomingSales.value = res.data.data.upcoming || []
      updateCountdown()
    }
  } catch (e) {
    console.error(e)
  }
}

const handleGrab = async (sale) => {
  if (sale.sold >= sale.stock) {
    uni.showToast({ title: '已抢光', icon: 'none' })
    return
  }

  const token = uni.getStorageSync('token')
  if (!token) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }

  try {
    const res = await uni.request({
      url: `http://localhost:8080/api/customer/flash-sale/grab/${sale.id}`,
      method: 'POST',
      header: { 'Authorization': `Bearer ${token}` }
    })

    if (res.data.code === 200) {
      uni.showToast({ title: '抢购成功！', icon: 'success' })
      loadData()
    } else {
      uni.showToast({ title: res.data.message || '抢购失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '网络错误', icon: 'none' })
  }
}

const handleRemind = (sale) => {
  uni.showToast({ title: '已设置提醒', icon: 'success' })
}

onMounted(() => {
  loadData()
  timer = setInterval(updateCountdown, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
  height: 88rpx;
  background: linear-gradient(135deg, #ff4444, #ff6b6b);
  padding-top: var(--status-bar-height);
}

.nav-back {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  width: 20rpx;
  height: 20rpx;
  border-left: 4rpx solid #fff;
  border-bottom: 4rpx solid #fff;
  transform: rotate(45deg);
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #fff;
}

.nav-placeholder {
  width: 60rpx;
}

.countdown-header {
  background: linear-gradient(135deg, #ff4444, #ff6b6b);
  padding: 30rpx;
  padding-bottom: 50rpx;
}

.countdown-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
}

.countdown-label {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

.countdown-time {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.time-block {
  width: 56rpx;
  height: 56rpx;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 700;
  color: #fff;
}

.time-sep {
  font-size: 32rpx;
  font-weight: 700;
  color: #fff;
}

.section {
  margin: 20rpx;
  margin-top: -30rpx;
}

.section:first-of-type {
  margin-top: -30rpx;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
  padding: 0 10rpx;
}

.section-icon {
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
}

.section-icon.hot {
  background: linear-gradient(135deg, #ff4444, #ff6b6b);
}

.section-icon.upcoming {
  background: linear-gradient(135deg, #ffa500, #ffcc00);
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.sale-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.sale-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
}

.sale-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 16rpx;
  margin-right: 20rpx;
}

.sale-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.sale-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 12rpx;
}

.sale-price {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.price-current {
  font-size: 36rpx;
  font-weight: 700;
  color: #ff4444;
}

.price-original {
  font-size: 24rpx;
  color: #999;
  text-decoration: line-through;
}

.sale-progress {
  width: 100%;
  height: 12rpx;
  background: #f0f0f0;
  border-radius: 6rpx;
  overflow: hidden;
  margin-bottom: 8rpx;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #ff4444, #ff6b6b);
  border-radius: 6rpx;
  transition: width 0.3s;
}

.sale-stock {
  font-size: 22rpx;
  color: #999;
}

.sale-time {
  font-size: 24rpx;
  color: #ffa500;
}

.grab-btn {
  width: 140rpx;
  height: 64rpx;
  background: linear-gradient(135deg, #ff4444, #ff6b6b);
  border-radius: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  font-weight: 600;
  color: #fff;
}

.grab-btn.disabled {
  background: #ccc;
}

.remind-btn {
  width: 140rpx;
  height: 64rpx;
  background: #fff;
  border: 2rpx solid #ffa500;
  border-radius: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  font-weight: 600;
  color: #ffa500;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
}

.empty-icon {
  width: 160rpx;
  height: 160rpx;
  background: #f0f0f0;
  border-radius: 50%;
  margin-bottom: 30rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #333;
  margin-bottom: 12rpx;
}

.empty-tip {
  font-size: 26rpx;
  color: #999;
}
</style>
