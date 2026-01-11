<template>
  <view class="container">
    <view class="logo-area">
      <view class="logo-icon">🍜</view>
      <text class="logo-title">校园订餐</text>
      <text class="logo-subtitle">美食触手可及</text>
    </view>
    
    <view class="login-box">
      <button class="wechat-btn" @click="wechatLogin">
        <text class="wechat-icon">💚</text>
        <text>微信一键登录</text>
      </button>
      
      <view class="divider">
        <view class="divider-line"></view>
        <text class="divider-text">或</text>
        <view class="divider-line"></view>
      </view>
      
      <view class="form">
        <view class="input-group">
          <text class="input-icon">👤</text>
          <input class="input" v-model="form.username" placeholder="请输入用户名" />
        </view>
        <view class="input-group">
          <text class="input-icon">🔒</text>
          <input class="input" v-model="form.password" type="password" placeholder="请输入密码" />
        </view>
        <view class="login-btn" @click="handleLogin">登 录</view>
      </view>
      
      <text class="agreement">登录即表示同意《用户协议》和《隐私政策》</text>
    </view>
  </view>
</template>

<script setup>
import { reactive } from 'vue'
import { post } from '@/utils/request'

const form = reactive({ username: '', password: '' })

const handleLogin = async () => {
  if (!form.username || !form.password) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  try {
    const res = await post('/auth/login', { ...form, userType: 'user' })
    uni.setStorageSync('token', res.data.token)
    uni.setStorageSync('userInfo', res.data.user)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 1000)
  } catch (e) { console.error(e) }
}

const wechatLogin = () => {
  uni.setStorageSync('token', 'wx-mock-token-123')
  uni.setStorageSync('userInfo', { nickname: '微信用户', phone: '138****8888' })
  uni.showToast({ title: '登录成功', icon: 'success' })
  setTimeout(() => {
    uni.switchTab({ url: '/pages/index/index' })
  }, 1000)
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 50%, #ffc107 100%);
  padding: 40rpx;
}
.logo-area { display: flex; flex-direction: column; align-items: center; margin-bottom: 60rpx; }
.logo-icon { font-size: 100rpx; margin-bottom: 20rpx; }
.logo-title { font-size: 56rpx; font-weight: bold; color: #fff; }
.logo-subtitle { font-size: 28rpx; color: rgba(255,255,255,0.9); margin-top: 10rpx; }
.login-box { width: 100%; background: #fff; border-radius: 32rpx; padding: 50rpx 40rpx; box-shadow: 0 20rpx 60rpx rgba(0,0,0,0.15); }
.wechat-btn { width: 100%; height: 100rpx; background: linear-gradient(135deg, #07c160, #10b981); border-radius: 50rpx; color: #fff; font-size: 34rpx; font-weight: bold; display: flex; align-items: center; justify-content: center; border: none; }
.wechat-btn::after { border: none; }
.wechat-icon { font-size: 40rpx; margin-right: 16rpx; }
.divider { display: flex; align-items: center; margin: 40rpx 0; }
.divider-line { flex: 1; height: 1rpx; background: #e0e0e0; }
.divider-text { padding: 0 30rpx; color: #999; font-size: 26rpx; }
.form { margin-top: 20rpx; }
.input-group { display: flex; align-items: center; background: #f8f8f8; border-radius: 50rpx; padding: 0 30rpx; margin-bottom: 24rpx; }
.input-icon { font-size: 32rpx; margin-right: 20rpx; }
.input { flex: 1; height: 90rpx; font-size: 28rpx; background: transparent; }
.login-btn { width: 100%; height: 90rpx; background: linear-gradient(135deg, #ff6b35, #f7931e); border-radius: 45rpx; color: #fff; font-size: 32rpx; font-weight: bold; display: flex; align-items: center; justify-content: center; margin-top: 20rpx; }
.agreement { display: block; text-align: center; font-size: 22rpx; color: #999; margin-top: 30rpx; }
</style>
