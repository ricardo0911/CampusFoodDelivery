<template>
  <view class="container">
    <!-- 用户信息 -->
    <view class="user-card">
      <image class="avatar" :src="userInfo.avatar || '/static/default-avatar.png'" />
      <view class="user-info" v-if="isLogin">
        <text class="nickname">{{ userInfo.nickname }}</text>
        <text class="phone">{{ userInfo.phone }}</text>
      </view>
      <view class="user-info" v-else @click="goLogin">
        <text class="nickname">点击登录</text>
      </view>
    </view>
    
    <!-- 菜单列表 -->
    <view class="menu-list">
      <view class="menu-item" @click="goOrders">
        <text>📋 我的订单</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="goAddress">
        <text>📍 收货地址</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="goFavorite">
        <text>❤️ 我的收藏</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="goHelp">
        <text>❓ 帮助中心</text>
        <text class="arrow">›</text>
      </view>
    </view>
    
    <view class="logout-btn" v-if="isLogin" @click="handleLogout">退出登录</view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const userInfo = ref({})

const isLogin = computed(() => !!uni.getStorageSync('token'))

const loadUserInfo = () => {
  userInfo.value = uni.getStorageSync('userInfo') || {}
}

const goLogin = () => { uni.navigateTo({ url: '/pages/login/login' }) }
const goOrders = () => { uni.switchTab({ url: '/pages/order/order' }) }
const goAddress = () => { uni.showToast({ title: '功能开发中', icon: 'none' }) }
const goFavorite = () => { uni.showToast({ title: '功能开发中', icon: 'none' }) }
const goHelp = () => { uni.showToast({ title: '功能开发中', icon: 'none' }) }

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
.container { padding: 20rpx; }
.user-card {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
}
.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  margin-right: 30rpx;
  background: #fff;
}
.nickname { font-size: 36rpx; font-weight: bold; color: #fff; }
.phone { font-size: 26rpx; color: rgba(255,255,255,0.8); margin-top: 10rpx; }
.menu-list { background: #fff; border-radius: 24rpx; }
.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  font-size: 30rpx;
}
.menu-item:last-child { border-bottom: none; }
.arrow { color: #999; font-size: 36rpx; }
.logout-btn {
  margin-top: 40rpx;
  text-align: center;
  padding: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  color: #ff4444;
  font-size: 30rpx;
}
</style>
