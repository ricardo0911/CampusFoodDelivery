<template>
  <view class="container">
    <view class="login-box">
      <text class="title">校园订餐</text>
      <text class="subtitle">登录后享受便捷订餐服务</text>
      
      <view class="form">
        <view class="input-group">
          <text class="label">用户名</text>
          <input class="input" v-model="form.username" placeholder="请输入用户名" />
        </view>
        <view class="input-group">
          <text class="label">密码</text>
          <input class="input" v-model="form.password" type="password" placeholder="请输入密码" />
        </view>
        
        <view class="login-btn" @click="handleLogin">登 录</view>
        <view class="register-link" @click="goRegister">没有账号？立即注册</view>
      </view>
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

const goRegister = () => {
  uni.showToast({ title: '暂不支持注册，请联系管理员', icon: 'none' })
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx;
}
.login-box {
  width: 100%;
  background: #fff;
  border-radius: 24rpx;
  padding: 60rpx 40rpx;
}
.title {
  font-size: 48rpx;
  font-weight: bold;
  text-align: center;
  color: #333;
}
.subtitle {
  font-size: 26rpx;
  color: #999;
  text-align: center;
  margin-top: 16rpx;
}
.form { margin-top: 60rpx; }
.input-group { margin-bottom: 30rpx; }
.label { font-size: 28rpx; color: #666; margin-bottom: 16rpx; display: block; }
.input {
  width: 100%;
  height: 90rpx;
  background: #f5f5f5;
  border-radius: 45rpx;
  padding: 0 30rpx;
  font-size: 28rpx;
}
.login-btn {
  width: 100%;
  height: 90rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 45rpx;
  color: #fff;
  font-size: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 40rpx;
}
.register-link {
  text-align: center;
  margin-top: 30rpx;
  font-size: 26rpx;
  color: #667eea;
}
</style>
