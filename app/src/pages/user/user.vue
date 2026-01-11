<template>
  <view class="page">
    <!-- 用户头部 -->
    <view class="user-header">
      <view class="header-bg"></view>
      
      <view class="user-card">
        <view class="avatar-wrapper">
          <image class="avatar" :src="userInfo.avatar || '/static/default-avatar.png'" mode="aspectFill" />
          <view class="vip-badge" v-if="isLogin">VIP</view>
        </view>
        
        <view class="user-info" v-if="isLogin">
          <text class="nickname">{{ userInfo.nickname || '美食达人' }}</text>
          <text class="phone">{{ formatPhone(userInfo.phone) }}</text>
        </view>
        <view class="user-info login-tip" v-else @click="goLogin">
          <text class="nickname">点击登录</text>
          <text class="phone">登录享更多优惠</text>
        </view>
        
        <view class="settings-btn" @click="goSettings">
          <text>⚙️</text>
        </view>
      </view>
    </view>
    
    <!-- 订单快捷入口 -->
    <view class="order-shortcuts">
      <view class="shortcut-title">
        <text class="title-text">我的订单</text>
        <text class="title-link" @click="goOrders">全部订单 ></text>
      </view>
      <view class="shortcut-grid">
        <view class="shortcut-item" @click="goOrdersByStatus(0)">
          <view class="shortcut-icon">💳</view>
          <text class="shortcut-text">待支付</text>
          <view class="shortcut-badge" v-if="orderCount.unpaid > 0">{{ orderCount.unpaid }}</view>
        </view>
        <view class="shortcut-item" @click="goOrdersByStatus(1)">
          <view class="shortcut-icon">👨‍🍳</view>
          <text class="shortcut-text">待接单</text>
          <view class="shortcut-badge" v-if="orderCount.pending > 0">{{ orderCount.pending }}</view>
        </view>
        <view class="shortcut-item" @click="goOrdersByStatus(3)">
          <view class="shortcut-icon">🛵</view>
          <text class="shortcut-text">配送中</text>
          <view class="shortcut-badge" v-if="orderCount.delivering > 0">{{ orderCount.delivering }}</view>
        </view>
        <view class="shortcut-item" @click="goReview">
          <view class="shortcut-icon">⭐</view>
          <text class="shortcut-text">待评价</text>
          <view class="shortcut-badge" v-if="orderCount.toReview > 0">{{ orderCount.toReview }}</view>
        </view>
      </view>
    </view>
    
    <!-- 会员卡片 -->
    <view class="member-card" v-if="isLogin">
      <view class="member-info">
        <text class="member-title">🎖️ 校园美食会员</text>
        <text class="member-desc">享专属优惠，积分当钱花</text>
      </view>
      <view class="member-points">
        <text class="points-value">{{ userInfo.points || 128 }}</text>
        <text class="points-label">积分</text>
      </view>
    </view>
    
    <!-- 功能菜单 -->
    <view class="menu-section">
      <view class="menu-group">
        <view class="menu-item" @click="goAddress">
          <view class="menu-icon">📍</view>
          <text class="menu-text">收货地址</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goFavorite">
          <view class="menu-icon">❤️</view>
          <text class="menu-text">我的收藏</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goCoupon">
          <view class="menu-icon">🎫</view>
          <text class="menu-text">优惠券</text>
          <view class="menu-tag">3张可用</view>
          <text class="menu-arrow">›</text>
        </view>
      </view>
      
      <view class="menu-group">
        <view class="menu-item" @click="goHelp">
          <view class="menu-icon">❓</view>
          <text class="menu-text">帮助中心</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goFeedback">
          <view class="menu-icon">📝</view>
          <text class="menu-text">意见反馈</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goAbout">
          <view class="menu-icon">ℹ️</view>
          <text class="menu-text">关于我们</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>
    
    <!-- 退出按钮 -->
    <view class="logout-section" v-if="isLogin">
      <view class="logout-btn" @click="handleLogout">退出登录</view>
    </view>
    
    <view class="safe-bottom"></view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const userInfo = ref({})
const orderCount = ref({ unpaid: 0, pending: 0, delivering: 0, toReview: 0 })

const isLogin = computed(() => !!uni.getStorageSync('token'))

const formatPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const loadUserInfo = () => {
  userInfo.value = uni.getStorageSync('userInfo') || { nickname: '美食达人', phone: '13800138000', points: 128 }
  // 模拟订单数量
  orderCount.value = { unpaid: 1, pending: 0, delivering: 1, toReview: 2 }
}

const goLogin = () => { uni.navigateTo({ url: '/pages/login/login' }) }
const goSettings = () => { uni.showToast({ title: '功能开发中', icon: 'none' }) }
const goOrders = () => { uni.switchTab({ url: '/pages/order/order' }) }
const goOrdersByStatus = (status) => { uni.switchTab({ url: '/pages/order/order' }) }
const goReview = () => { uni.showToast({ title: '功能开发中', icon: 'none' }) }
const goAddress = () => { uni.showToast({ title: '功能开发中', icon: 'none' }) }
const goFavorite = () => { uni.showToast({ title: '功能开发中', icon: 'none' }) }
const goCoupon = () => { uni.showToast({ title: '功能开发中', icon: 'none' }) }
const goHelp = () => { uni.showToast({ title: '功能开发中', icon: 'none' }) }
const goFeedback = () => { uni.showToast({ title: '功能开发中', icon: 'none' }) }
const goAbout = () => { uni.showToast({ title: '功能开发中', icon: 'none' }) }

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定退出登录？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('token')
        uni.removeStorageSync('userInfo')
        userInfo.value = {}
        uni.showToast({ title: '已退出', icon: 'success' })
      }
    }
  })
}

onMounted(loadUserInfo)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f6fa;
}

.user-header {
  position: relative;
  padding-bottom: 60rpx;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 320rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 0 0 40rpx 40rpx;
}

.user-card {
  position: relative;
  display: flex;
  align-items: center;
  padding: 40rpx 30rpx;
  padding-top: calc(var(--status-bar-height) + 60rpx);
}

.avatar-wrapper {
  position: relative;
}

.avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  border: 6rpx solid rgba(255,255,255,0.3);
}

.vip-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  background: linear-gradient(135deg, #ffd700, #ffb700);
  color: #8B4513;
  font-size: 18rpx;
  font-weight: bold;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
}

.user-info {
  flex: 1;
  margin-left: 24rpx;
}

.nickname {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
}

.phone {
  font-size: 26rpx;
  color: rgba(255,255,255,0.8);
  margin-top: 8rpx;
}

.login-tip .nickname {
  font-size: 34rpx;
}

.settings-btn {
  width: 70rpx;
  height: 70rpx;
  background: rgba(255,255,255,0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
}

.order-shortcuts {
  margin: 0 20rpx;
  margin-top: -40rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05);
}

.shortcut-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.title-text {
  font-size: 32rpx;
  font-weight: bold;
  color: #1a1a2e;
}

.title-link {
  font-size: 26rpx;
  color: #999;
}

.shortcut-grid {
  display: flex;
  justify-content: space-around;
}

.shortcut-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.shortcut-icon {
  font-size: 48rpx;
  margin-bottom: 12rpx;
}

.shortcut-text {
  font-size: 24rpx;
  color: #666;
}

.shortcut-badge {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  min-width: 32rpx;
  height: 32rpx;
  background: #ff4444;
  color: #fff;
  font-size: 20rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
}

.member-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20rpx;
  padding: 30rpx;
  background: linear-gradient(135deg, #2c1810, #4a3020);
  border-radius: 24rpx;
}

.member-title {
  font-size: 30rpx;
  color: #ffd700;
  font-weight: bold;
}

.member-desc {
  font-size: 24rpx;
  color: rgba(255,255,255,0.7);
  margin-top: 8rpx;
}

.points-value {
  font-size: 48rpx;
  color: #ffd700;
  font-weight: bold;
}

.points-label {
  font-size: 24rpx;
  color: rgba(255,255,255,0.7);
  margin-left: 8rpx;
}

.menu-section {
  padding: 0 20rpx;
}

.menu-group {
  background: #fff;
  border-radius: 24rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 30rpx 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  font-size: 36rpx;
  margin-right: 20rpx;
}

.menu-text {
  flex: 1;
  font-size: 30rpx;
  color: #333;
}

.menu-tag {
  font-size: 22rpx;
  color: #ff4444;
  background: #fff0f0;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  margin-right: 16rpx;
}

.menu-arrow {
  font-size: 32rpx;
  color: #ccc;
}

.logout-section {
  padding: 40rpx 20rpx;
}

.logout-btn {
  text-align: center;
  padding: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  color: #ff4444;
  font-size: 30rpx;
}

.safe-bottom {
  height: 120rpx;
}
</style>
