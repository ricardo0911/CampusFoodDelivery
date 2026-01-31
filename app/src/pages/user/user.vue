<template>
  <view class="page">
    <!-- 用户头部 -->
    <view class="user-header">
      <view class="header-bg"></view>
      
      <view class="user-card">
        <view class="avatar-wrapper">
          <image class="avatar" :src="userInfo.avatar || '/static/default-avatar.jpg'" mode="aspectFill" />
          <view class="vip-tag" v-if="isLogin">
            <view class="vip-icon">V</view>
          </view>
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
          <view class="gear-icon">
            <view class="gear-outer">
              <view class="gear-inner"></view>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 订单快捷入口 -->
    <view class="order-card">
      <view class="card-header">
        <text class="header-title">我的订单</text>
        <view class="header-link" @click="goOrders">
          <text>全部订单</text>
          <view class="arrow-icon"></view>
        </view>
      </view>
      
      <view class="shortcut-grid">
        <view class="shortcut-item" @click="goOrdersByStatus(0)">
          <view class="icon-wrapper">
            <view class="icon-wallet">
              <view class="wallet-body"></view>
              <view class="wallet-flap"></view>
            </view>
          </view>
          <text class="shortcut-text">待支付</text>
          <view class="shortcut-badge" v-if="orderCount.unpaid > 0">{{ orderCount.unpaid }}</view>
        </view>
        
        <view class="shortcut-item" @click="goOrdersByStatus(1)">
          <view class="icon-wrapper">
            <view class="icon-chef">
              <view class="chef-hat"></view>
              <view class="chef-band"></view>
            </view>
          </view>
          <text class="shortcut-text">待接单</text>
          <view class="shortcut-badge" v-if="orderCount.pending > 0">{{ orderCount.pending }}</view>
        </view>
        
        <view class="shortcut-item" @click="goOrdersByStatus(3)">
          <view class="icon-wrapper">
            <view class="icon-delivery">
              <view class="box-body"></view>
              <view class="box-handle"></view>
            </view>
          </view>
          <text class="shortcut-text">配送中</text>
          <view class="shortcut-badge" v-if="orderCount.delivering > 0">{{ orderCount.delivering }}</view>
        </view>
        
        <view class="shortcut-item" @click="goReview">
          <view class="icon-wrapper">
            <view class="icon-star">
              <view class="star-shape"></view>
            </view>
          </view>
          <text class="shortcut-text">待评价</text>
          <view class="shortcut-badge" v-if="orderCount.toReview > 0">{{ orderCount.toReview }}</view>
        </view>
      </view>
    </view>
    
    <!-- 会员积分卡片 -->
    <view class="member-card" v-if="isLogin">
      <view class="member-content">
        <view class="member-left">
          <view class="member-badge">
            <view class="medal-icon">
              <view class="medal-ribbon"></view>
              <view class="medal-circle">
                <view class="medal-v">V</view>
              </view>
            </view>
            <text class="badge-text">校园美食会员</text>
          </view>
          <text class="member-desc">享专属优惠，积分当钱花</text>
        </view>
        <view class="member-right">
          <view class="points-display">
            <text class="points-num">{{ userInfo.points || 128 }}</text>
            <text class="points-unit">积分</text>
          </view>
        </view>
      </view>
      <view class="member-wave"></view>
    </view>

    <!-- 签到入口 -->
    <view class="checkin-card" @click="goCheckIn" v-if="isLogin">
      <view class="checkin-left">
        <view class="checkin-icon">
          <view class="calendar-icon">
            <view class="calendar-top"></view>
            <view class="calendar-body">
              <text class="calendar-day">{{ currentDay }}</text>
            </view>
          </view>
        </view>
        <view class="checkin-info">
          <text class="checkin-title">每日签到</text>
          <text class="checkin-desc">连续签到领更多积分</text>
        </view>
      </view>
      <view class="checkin-right">
        <view class="checkin-btn">
          <text>去签到</text>
        </view>
      </view>
    </view>
    
    <!-- 徽章成就卡片 -->
    <view class="badge-card" @click="goBadges">
      <view class="card-header">
        <view class="header-left">
          <view class="trophy-icon">
            <view class="trophy-cup"></view>
            <view class="trophy-base"></view>
          </view>
          <text class="header-title">我的徽章</text>
        </view>
        <view class="header-link">
          <text>查看全部</text>
          <view class="arrow-icon"></view>
        </view>
      </view>
      
      <view class="badge-list">
        <view class="badge-item obtained">
          <view class="badge-icon medal-gold">
            <view class="medal-inner"></view>
          </view>
        </view>
        <view class="badge-item obtained">
          <view class="badge-icon medal-silver">
            <view class="star-badge"></view>
          </view>
        </view>
        <view class="badge-item obtained">
          <view class="badge-icon medal-bronze">
            <view class="coin-badge"></view>
          </view>
        </view>
        <view class="badge-item obtained">
          <view class="badge-icon medal-food">
            <view class="food-badge"></view>
          </view>
        </view>
        <view class="badge-item locked">
          <view class="badge-icon locked-icon">
            <view class="lock-body"></view>
            <view class="lock-shackle"></view>
          </view>
          <view class="lock-overlay"></view>
        </view>
        <view class="badge-item locked">
          <view class="badge-icon locked-icon">
            <view class="lock-body"></view>
            <view class="lock-shackle"></view>
          </view>
          <view class="lock-overlay"></view>
        </view>
      </view>
      
      <view class="progress-section">
        <view class="progress-track">
          <view class="progress-bar" style="width: 37%"></view>
        </view>
        <text class="progress-text">已收集 5/16</text>
      </view>
    </view>
    
    <!-- 功能菜单 -->
    <view class="menu-section">
      <view class="menu-card">
        <view class="menu-item" @click="goAddress">
          <view class="menu-icon">
            <view class="icon-location">
              <view class="location-pin"></view>
              <view class="location-dot"></view>
            </view>
          </view>
          <text class="menu-text">收货地址</text>
          <view class="menu-arrow">
            <view class="arrow-right"></view>
          </view>
        </view>
        
        <view class="menu-item" @click="goFavorite">
          <view class="menu-icon">
            <view class="icon-heart">
              <view class="heart-shape"></view>
            </view>
          </view>
          <text class="menu-text">我的收藏</text>
          <view class="menu-arrow">
            <view class="arrow-right"></view>
          </view>
        </view>
        
        <view class="menu-item" @click="goCoupon">
          <view class="menu-icon">
            <view class="icon-coupon">
              <view class="coupon-body"></view>
              <view class="coupon-notch left"></view>
              <view class="coupon-notch right"></view>
            </view>
          </view>
          <text class="menu-text">优惠券</text>
          <view class="menu-tag">3张可用</view>
          <view class="menu-arrow">
            <view class="arrow-right"></view>
          </view>
        </view>
        
        <view class="menu-item" @click="goBadges">
          <view class="menu-icon">
            <view class="icon-medal">
              <view class="mini-medal"></view>
            </view>
          </view>
          <text class="menu-text">我的徽章</text>
          <view class="menu-tag hot">NEW</view>
          <view class="menu-arrow">
            <view class="arrow-right"></view>
          </view>
        </view>
      </view>
      
      <view class="menu-card">
        <view class="menu-item" @click="goHelp">
          <view class="menu-icon">
            <view class="icon-help">
              <view class="help-circle"></view>
              <view class="help-mark"></view>
            </view>
          </view>
          <text class="menu-text">帮助中心</text>
          <view class="menu-arrow">
            <view class="arrow-right"></view>
          </view>
        </view>
        
        <view class="menu-item" @click="goFeedback">
          <view class="menu-icon">
            <view class="icon-edit">
              <view class="edit-page"></view>
              <view class="edit-line"></view>
              <view class="edit-pen"></view>
            </view>
          </view>
          <text class="menu-text">意见反馈</text>
          <view class="menu-arrow">
            <view class="arrow-right"></view>
          </view>
        </view>
        
        <view class="menu-item" @click="goAbout">
          <view class="menu-icon">
            <view class="icon-info">
              <view class="info-circle"></view>
              <view class="info-i"></view>
            </view>
          </view>
          <text class="menu-text">关于我们</text>
          <view class="menu-arrow">
            <view class="arrow-right"></view>
          </view>
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
const currentDay = computed(() => new Date().getDate())

const isLogin = computed(() => !!uni.getStorageSync('token'))

const formatPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const loadUserInfo = () => {
  userInfo.value = uni.getStorageSync('userInfo') || { nickname: '美食达人', phone: '13800138000', points: 128 }
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
const goBadges = () => { uni.navigateTo({ url: '/pages/badges/badges' }) }
const goCheckIn = () => { uni.navigateTo({ url: '/pages/check-in/check-in' }) }

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
  background: linear-gradient(180deg, #f8f9fa 0%, #f0f2f5 100%);
}

/* ===== 用户头部区域 ===== */
.user-header {
  position: relative;
  padding-bottom: 80rpx;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 360rpx;
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f50 50%, #f7931e 100%);
  border-radius: 0 0 50rpx 50rpx;
}

.user-card {
  position: relative;
  display: flex;
  align-items: center;
  padding: 40rpx 30rpx;
  padding-top: calc(var(--status-bar-height) + 50rpx);
}

.avatar-wrapper {
  position: relative;
}

.avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8rpx 32rpx rgba(255, 107, 53, 0.3);
}

.vip-tag {
  position: absolute;
  bottom: -4rpx;
  right: -4rpx;
  width: 44rpx;
  height: 44rpx;
  background: linear-gradient(135deg, #ffd700, #ffb347);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
}

.vip-icon {
  font-size: 22rpx;
  font-weight: bold;
  color: #8B4513;
}

.user-info {
  flex: 1;
  margin-left: 24rpx;
}

.nickname {
  font-size: 38rpx;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.phone {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 8rpx;
  font-weight: 400;
}

.login-tip .nickname {
  font-size: 36rpx;
  font-weight: 600;
}

/* 设置按钮 - 齿轮图标 */
.settings-btn {
  width: 72rpx;
  height: 72rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10rpx);
}

.gear-icon {
  width: 40rpx;
  height: 40rpx;
  position: relative;
}

.gear-outer {
  width: 100%;
  height: 100%;
  background: #fff;
  border-radius: 50%;
  position: relative;
  clip-path: polygon(
    50% 0%, 61% 0%, 63% 10%, 73% 6%, 78% 16%, 88% 14%, 90% 24%, 100% 26%, 
    100% 38%, 90% 40%, 88% 50%, 100% 54%, 100% 66%, 90% 68%, 88% 78%, 
    78% 76%, 73% 86%, 63% 82%, 61% 92%, 50% 92%, 39% 92%, 37% 82%, 
    27% 86%, 22% 76%, 12% 78%, 10% 68%, 0% 66%, 0% 54%, 10% 50%, 
    12% 40%, 0% 38%, 0% 26%, 10% 24%, 12% 14%, 22% 16%, 27% 6%, 
    37% 10%, 39% 0%
  );
}

.gear-inner {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 20rpx;
  height: 20rpx;
  background: rgba(255, 107, 53, 0.6);
  border-radius: 50%;
}

/* ===== 订单卡片 ===== */
.order-card {
  margin: 0 24rpx;
  margin-top: -60rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 10;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28rpx;
}

.header-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1a2e;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.header-link {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 26rpx;
  color: #999;
}

.arrow-icon {
  width: 12rpx;
  height: 12rpx;
  border-top: 2rpx solid #999;
  border-right: 2rpx solid #999;
  transform: rotate(45deg);
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
  padding: 16rpx 20rpx;
}

.icon-wrapper {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14rpx;
  background: linear-gradient(135deg, #fff5f0 0%, #ffeee5 100%);
}

/* 钱包图标 */
.icon-wallet {
  width: 40rpx;
  height: 32rpx;
  position: relative;
}

.wallet-body {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 6rpx;
  position: relative;
}

.wallet-flap {
  position: absolute;
  top: -6rpx;
  left: 4rpx;
  right: 4rpx;
  height: 10rpx;
  background: linear-gradient(135deg, #ff8f50, #f7931e);
  border-radius: 3rpx 3rpx 0 0;
}

/* 厨师帽图标 */
.icon-chef {
  width: 36rpx;
  height: 40rpx;
  position: relative;
}

.chef-hat {
  width: 100%;
  height: 28rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 18rpx 18rpx 8rpx 8rpx;
  position: relative;
}

.chef-hat::before,
.chef-hat::after {
  content: '';
  position: absolute;
  top: -12rpx;
  width: 14rpx;
  height: 20rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 50%;
}

.chef-hat::before {
  left: 4rpx;
}

.chef-hat::after {
  right: 4rpx;
}

.chef-band {
  position: absolute;
  bottom: 0;
  left: -4rpx;
  right: -4rpx;
  height: 14rpx;
  background: linear-gradient(135deg, #f7931e, #ffb347);
  border-radius: 4rpx;
}

/* 配送箱图标 */
.icon-delivery {
  width: 38rpx;
  height: 34rpx;
  position: relative;
}

.box-body {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 6rpx;
}

.box-handle {
  position: absolute;
  top: -8rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 16rpx;
  height: 12rpx;
  border: 3rpx solid #ff8f50;
  border-bottom: none;
  border-radius: 8rpx 8rpx 0 0;
}

/* 星星图标 */
.icon-star {
  width: 38rpx;
  height: 38rpx;
  position: relative;
}

.star-shape {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  clip-path: polygon(50% 0%, 61% 35%, 98% 35%, 68% 57%, 79% 91%, 50% 70%, 21% 91%, 32% 57%, 2% 35%, 39% 35%);
}

.shortcut-text {
  font-size: 24rpx;
  color: #666;
  font-weight: 500;
}

.shortcut-badge {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  min-width: 32rpx;
  height: 32rpx;
  background: linear-gradient(135deg, #ff4444, #ff6b6b);
  color: #fff;
  font-size: 20rpx;
  font-weight: 600;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 10rpx;
  box-shadow: 0 4rpx 12rpx rgba(255, 68, 68, 0.3);
}

/* ===== 会员卡片 ===== */
.member-card {
  margin: 24rpx;
  background: linear-gradient(135deg, #2c1810 0%, #3d2418 50%, #4a3020 100%);
  border-radius: 24rpx;
  overflow: hidden;
  position: relative;
  box-shadow: 0 8rpx 32rpx rgba(44, 24, 16, 0.3);
}

.member-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  position: relative;
  z-index: 2;
}

.member-left {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.member-badge {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

/* 奖章图标 */
.medal-icon {
  width: 40rpx;
  height: 48rpx;
  position: relative;
}

.medal-ribbon {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20rpx;
  height: 20rpx;
  background: linear-gradient(135deg, #ffd700, #ffb347);
  clip-path: polygon(0 0, 100% 0, 50% 100%);
}

.medal-circle {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 28rpx;
  height: 28rpx;
  background: linear-gradient(135deg, #ffd700, #ffb347);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(255, 215, 0, 0.4);
}

.medal-v {
  font-size: 16rpx;
  font-weight: bold;
  color: #8B4513;
}

.badge-text {
  font-size: 30rpx;
  color: #ffd700;
  font-weight: 700;
}

.member-desc {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.7);
}

.member-right {
  display: flex;
  align-items: center;
}

.points-display {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
  background: rgba(255, 215, 0, 0.15);
  padding: 16rpx 24rpx;
  border-radius: 16rpx;
}

.points-num {
  font-size: 48rpx;
  color: #ffd700;
  font-weight: 700;
}

.points-unit {
  font-size: 24rpx;
  color: rgba(255, 215, 0, 0.8);
}

.member-wave {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40rpx;
  background: linear-gradient(90deg, transparent, rgba(255, 215, 0, 0.1), transparent);
}

/* ===== 签到卡片 ===== */
.checkin-card {
  margin: 24rpx;
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f50 50%, #f7931e 100%);
  border-radius: 24rpx;
  padding: 28rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 8rpx 32rpx rgba(255, 107, 53, 0.3);
}

.checkin-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.checkin-icon {
  width: 72rpx;
  height: 72rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.calendar-icon {
  width: 44rpx;
  height: 48rpx;
  position: relative;
}

.calendar-top {
  position: absolute;
  top: 0;
  left: 4rpx;
  right: 4rpx;
  height: 12rpx;
  background: #fff;
  border-radius: 4rpx 4rpx 0 0;
}

.calendar-body {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 36rpx;
  background: #fff;
  border-radius: 0 0 6rpx 6rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.calendar-day {
  font-size: 24rpx;
  font-weight: bold;
  color: #ff6b35;
}

.checkin-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.checkin-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #fff;
}

.checkin-desc {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);
}

.checkin-right {
  display: flex;
  align-items: center;
}

.checkin-btn {
  background: #fff;
  padding: 16rpx 32rpx;
  border-radius: 40rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.checkin-btn text {
  font-size: 28rpx;
  font-weight: 600;
  color: #ff6b35;
}

/* ===== 徽章卡片 ===== */
.badge-card {
  margin: 24rpx;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #1a1a2e 100%);
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 8rpx 32rpx rgba(26, 26, 46, 0.3);
}

/* 奖杯图标 */
.trophy-icon {
  width: 36rpx;
  height: 40rpx;
  position: relative;
}

.trophy-cup {
  width: 32rpx;
  height: 26rpx;
  background: linear-gradient(135deg, #ffd700, #ffb347);
  border-radius: 0 0 16rpx 16rpx;
  position: relative;
  margin: 0 auto;
}

.trophy-cup::before,
.trophy-cup::after {
  content: '';
  position: absolute;
  top: 8rpx;
  width: 10rpx;
  height: 14rpx;
  background: linear-gradient(135deg, #ffd700, #ffb347);
  border-radius: 0 8rpx 8rpx 0;
}

.trophy-cup::before {
  left: -8rpx;
  border-radius: 8rpx 0 0 8rpx;
}

.trophy-cup::after {
  right: -8rpx;
}

.trophy-base {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20rpx;
  height: 10rpx;
  background: linear-gradient(135deg, #ffd700, #ffb347);
  border-radius: 2rpx;
}

.badge-list {
  display: flex;
  gap: 20rpx;
  margin: 24rpx 0;
}

.badge-item {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.badge-item.obtained {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.2), rgba(255, 107, 107, 0.2));
  box-shadow: 0 4rpx 16rpx rgba(255, 215, 0, 0.2);
}

.badge-item.locked {
  background: rgba(255, 255, 255, 0.08);
}

.badge-icon {
  width: 44rpx;
  height: 44rpx;
  position: relative;
}

/* 金徽章 */
.medal-gold {
  background: linear-gradient(135deg, #ffd700, #ffb347);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.medal-inner {
  width: 24rpx;
  height: 24rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 50%;
}

/* 银徽章 - 星星 */
.medal-silver {
  background: linear-gradient(135deg, #c0c0c0, #e8e8e8);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.star-badge {
  width: 28rpx;
  height: 28rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  clip-path: polygon(50% 0%, 61% 35%, 98% 35%, 68% 57%, 79% 91%, 50% 70%, 21% 91%, 32% 57%, 2% 35%, 39% 35%);
}

/* 铜徽章 - 金币 */
.medal-bronze {
  background: linear-gradient(135deg, #cd7f32, #daa520);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.coin-badge {
  width: 26rpx;
  height: 26rpx;
  border: 3rpx solid #ff6b35;
  border-radius: 50%;
}

/* 食物徽章 */
.medal-food {
  background: linear-gradient(135deg, #ff6b6b, #ee5a5a);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.food-badge {
  width: 20rpx;
  height: 20rpx;
  background: #fff;
  border-radius: 50%;
  position: relative;
}

.food-badge::after {
  content: '';
  position: absolute;
  top: -6rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 8rpx;
  height: 10rpx;
  background: #90EE90;
  border-radius: 4rpx 4rpx 0 0;
}

/* 锁定图标 */
.locked-icon {
  position: relative;
}

.lock-body {
  position: absolute;
  bottom: 8rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 20rpx;
  height: 16rpx;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3rpx;
}

.lock-shackle {
  position: absolute;
  top: 4rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 14rpx;
  height: 12rpx;
  border: 3rpx solid rgba(255, 255, 255, 0.3);
  border-bottom: none;
  border-radius: 10rpx 10rpx 0 0;
}

.lock-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-section {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.progress-track {
  flex: 1;
  height: 8rpx;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 4rpx;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #ffd700, #ff8f50);
  border-radius: 4rpx;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
}

/* ===== 功能菜单 ===== */
.menu-section {
  padding: 0 24rpx;
}

.menu-card {
  background: #fff;
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 28rpx 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
  transition: background 0.2s;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:active {
  background: #f9f9f9;
}

.menu-icon {
  width: 48rpx;
  height: 48rpx;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  background: linear-gradient(135deg, #fff5f0 0%, #ffeee5 100%);
}

/* 位置图标 */
.icon-location {
  width: 24rpx;
  height: 32rpx;
  position: relative;
}

.location-pin {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 50% 50% 50% 0;
  transform: rotate(-45deg);
}

.location-dot {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 8rpx;
  height: 8rpx;
  background: #fff;
  border-radius: 50%;
}

/* 心形图标 */
.icon-heart {
  width: 28rpx;
  height: 26rpx;
  position: relative;
}

.heart-shape {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  position: relative;
  transform: rotate(-45deg);
}

.heart-shape::before,
.heart-shape::after {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 50%;
}

.heart-shape::before {
  top: -50%;
  left: 0;
}

.heart-shape::after {
  top: 0;
  left: 50%;
}

/* 优惠券图标 */
.icon-coupon {
  width: 32rpx;
  height: 24rpx;
  position: relative;
}

.coupon-body {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 4rpx;
}

.coupon-notch {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 10rpx;
  height: 16rpx;
  background: #fff;
  border-radius: 50%;
}

.coupon-notch.left {
  left: -5rpx;
}

.coupon-notch.right {
  right: -5rpx;
}

/* 奖章图标（小） */
.icon-medal {
  width: 28rpx;
  height: 32rpx;
  position: relative;
}

.mini-medal {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  clip-path: polygon(20% 0%, 80% 0%, 100% 35%, 50% 100%, 0% 35%);
}

/* 帮助图标 */
.icon-help {
  width: 28rpx;
  height: 28rpx;
  position: relative;
}

.help-circle {
  width: 100%;
  height: 100%;
  border: 3rpx solid #ff6b35;
  border-radius: 50%;
}

.help-mark {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 18rpx;
  color: #ff6b35;
  font-weight: bold;
}

.help-mark::after {
  content: '?';
  color: #ff6b35;
}

/* 编辑图标 */
.icon-edit {
  width: 28rpx;
  height: 28rpx;
  position: relative;
}

.edit-page {
  width: 100%;
  height: 100%;
  border: 2rpx solid #ff6b35;
  border-radius: 4rpx;
}

.edit-line {
  position: absolute;
  top: 40%;
  left: 20%;
  right: 20%;
  height: 2rpx;
  background: #ff6b35;
}

.edit-pen {
  position: absolute;
  bottom: -2rpx;
  right: -2rpx;
  width: 12rpx;
  height: 12rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 2rpx;
  transform: rotate(-45deg);
}

/* 信息图标 */
.icon-info {
  width: 28rpx;
  height: 28rpx;
  position: relative;
}

.info-circle {
  width: 100%;
  height: 100%;
  border: 3rpx solid #ff6b35;
  border-radius: 50%;
}

.info-i {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 18rpx;
  color: #ff6b35;
  font-weight: bold;
}

.info-i::after {
  content: 'i';
  color: #ff6b35;
  font-style: italic;
}

.menu-text {
  flex: 1;
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
}

.menu-tag {
  font-size: 22rpx;
  color: #ff6b35;
  background: linear-gradient(135deg, #fff5f0, #ffeee5);
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  margin-right: 16rpx;
  font-weight: 500;
}

.menu-tag.hot {
  color: #fff;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);
}

.menu-arrow {
  width: 24rpx;
  height: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.arrow-right {
  width: 10rpx;
  height: 10rpx;
  border-top: 3rpx solid #ccc;
  border-right: 3rpx solid #ccc;
  transform: rotate(45deg);
}

/* ===== 退出登录 ===== */
.logout-section {
  padding: 40rpx 24rpx;
}

.logout-btn {
  text-align: center;
  padding: 28rpx;
  background: #fff;
  border-radius: 24rpx;
  color: #ff6b35;
  font-size: 30rpx;
  font-weight: 500;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
  transition: all 0.2s;
}

.logout-btn:active {
  background: #fff5f0;
  transform: scale(0.98);
}

.safe-bottom {
  height: 140rpx;
}
</style>