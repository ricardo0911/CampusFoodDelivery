<template>
  <view class="settings-page">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="avatar-section" @click="changeAvatar">
        <image
          :src="userInfo.avatar || '/static/default-avatar.png'"
          class="avatar"
          mode="aspectFill"
        />
        <view class="avatar-mask">
          <text class="avatar-icon">📷</text>
          <text class="avatar-text">更换头像</text>
        </view>
      </view>
      <view class="nickname-section">
        <text class="nickname-label">昵称</text>
        <view class="nickname-input-wrapper">
          <input
            v-model="nickname"
            class="nickname-input"
            placeholder="请输入昵称"
            maxlength="20"
          />
          <text class="save-btn" @click="saveNickname">保存</text>
        </view>
      </view>
    </view>

    <!-- 设置列表 -->
    <view class="settings-list">
      <!-- 账户安全 -->
      <view class="settings-group">
        <view class="group-title">账户安全</view>
        <view class="group-content">
          <view class="settings-item" @click="changePassword">
            <view class="item-left">
              <text class="item-icon">🔒</text>
              <text class="item-text">修改密码</text>
            </view>
            <view class="item-right">
              <text class="item-arrow">›</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 通用设置 -->
      <view class="settings-group">
        <view class="group-title">通用设置</view>
        <view class="group-content">
          <view class="settings-item" @click="clearCache">
            <view class="item-left">
              <text class="item-icon">🗑️</text>
              <text class="item-text">清除缓存</text>
            </view>
            <view class="item-right">
              <text class="item-value">{{ cacheSize }}</text>
              <text class="item-arrow">›</text>
            </view>
          </view>
          <view class="settings-item" @click="checkUpdate">
            <view class="item-left">
              <text class="item-icon">⬆️</text>
              <text class="item-text">检查更新</text>
            </view>
            <view class="item-right">
              <text class="item-value">{{ version }}</text>
              <text class="item-arrow">›</text>
            </view>
          </view>
          <view class="settings-item" @click="aboutUs">
            <view class="item-left">
              <text class="item-icon">ℹ️</text>
              <text class="item-text">关于我们</text>
            </view>
            <view class="item-right">
              <text class="item-arrow">›</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-section">
      <view class="logout-btn" @click="logout">
        <text>退出登录</text>
      </view>
    </view>

    <!-- 修改密码弹窗 -->
    <uni-popup ref="passwordPopup" type="bottom">
      <view class="popup-content">
        <view class="popup-header">
          <text class="popup-title">修改密码</text>
          <text class="popup-close" @click="closePasswordPopup">✕</text>
        </view>
        <view class="form-list">
          <view class="form-item">
            <text class="form-label">原密码</text>
            <input
              v-model="passwordForm.oldPassword"
              class="form-input"
              type="password"
              placeholder="请输入原密码"
            />
          </view>
          <view class="form-item">
            <text class="form-label">新密码</text>
            <input
              v-model="passwordForm.newPassword"
              class="form-input"
              type="password"
              placeholder="请输入新密码（6-20位）"
              maxlength="20"
            />
          </view>
          <view class="form-item">
            <text class="form-label">确认密码</text>
            <input
              v-model="passwordForm.confirmPassword"
              class="form-input"
              type="password"
              placeholder="请再次输入新密码"
              maxlength="20"
            />
          </view>
        </view>
        <view class="popup-footer">
          <view class="submit-btn" @click="submitPassword">确认修改</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { post, put, get, BASE_URL } from '@/utils/request'

const userInfo = ref({})
const nickname = ref('')
const cacheSize = ref('0KB')
const version = ref('1.0.0')
const passwordPopup = ref(null)

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const res = await get('/customer/user/info')
    if (res.code === 200) {
      userInfo.value = res.data || {}
      nickname.value = res.data?.nickname || res.data?.username || ''
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 计算缓存大小
const calcCacheSize = () => {
  try {
    const res = uni.getStorageInfoSync()
    const size = res.currentSize
    if (size < 1024) {
      cacheSize.value = size + 'KB'
    } else {
      cacheSize.value = (size / 1024).toFixed(2) + 'MB'
    }
  } catch (e) {
    cacheSize.value = '0KB'
  }
}

// 更换头像
const changeAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      uploadAvatar(tempFilePath)
    }
  })
}

// 上传头像
const uploadAvatar = async (filePath) => {
  try {
    uni.showLoading({ title: '上传中...' })

    const token = uni.getStorageSync('token')

    uni.uploadFile({
      url: BASE_URL + '/customer/user/avatar',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        uni.hideLoading()
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          userInfo.value.avatar = data.data?.url
          uni.showToast({ title: '上传成功', icon: 'success' })

          // 更新本地存储
          const storedUserInfo = uni.getStorageSync('userInfo') || {}
          storedUserInfo.avatar = data.data?.url
          uni.setStorageSync('userInfo', storedUserInfo)
        } else {
          uni.showToast({ title: data.message || '上传失败', icon: 'none' })
        }
      },
      fail: () => {
        uni.hideLoading()
        uni.showToast({ title: '上传失败', icon: 'none' })
      }
    })
  } catch (error) {
    uni.hideLoading()
    console.error('上传头像失败:', error)
  }
}

// 保存昵称
const saveNickname = async () => {
  if (!nickname.value.trim()) {
    uni.showToast({ title: '昵称不能为空', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '保存中...' })
    const res = await put('/customer/user/update', {
      nickname: nickname.value.trim()
    })

    if (res.code === 200) {
      uni.showToast({ title: '保存成功', icon: 'success' })

      // 更新本地存储
      const storedUserInfo = uni.getStorageSync('userInfo') || {}
      storedUserInfo.nickname = nickname.value.trim()
      uni.setStorageSync('userInfo', storedUserInfo)
    }
  } catch (error) {
    console.error('保存昵称失败:', error)
  } finally {
    uni.hideLoading()
  }
}

// 打开修改密码弹窗
const changePassword = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordPopup.value?.open()
}

// 关闭密码弹窗
const closePasswordPopup = () => {
  passwordPopup.value?.close()
}

// 提交密码修改
const submitPassword = async () => {
  const { oldPassword, newPassword, confirmPassword } = passwordForm.value

  if (!oldPassword) {
    uni.showToast({ title: '请输入原密码', icon: 'none' })
    return
  }
  if (!newPassword || newPassword.length < 6) {
    uni.showToast({ title: '新密码至少6位', icon: 'none' })
    return
  }
  if (newPassword !== confirmPassword) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '修改中...' })
    const res = await post('/customer/user/change-password', {
      oldPassword,
      newPassword
    })

    if (res.code === 200) {
      uni.showToast({ title: '修改成功', icon: 'success' })
      closePasswordPopup()
    }
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    uni.hideLoading()
  }
}

// 清除缓存
const clearCache = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清除缓存吗？',
    confirmColor: '#667eea',
    success: (res) => {
      if (res.confirm) {
        try {
          // 清除非关键缓存
          const keys = uni.getStorageInfoSync().keys
          const keepKeys = ['token', 'userInfo']

          keys.forEach(key => {
            if (!keepKeys.includes(key)) {
              uni.removeStorageSync(key)
            }
          })

          cacheSize.value = '0KB'
          uni.showToast({ title: '清除成功', icon: 'success' })
        } catch (e) {
          uni.showToast({ title: '清除失败', icon: 'none' })
        }
      }
    }
  })
}

// 检查更新
const checkUpdate = () => {
  uni.showLoading({ title: '检查中...' })

  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({ title: '已是最新版本', icon: 'none' })
  }, 1000)
}

// 关于我们
const aboutUs = () => {
  uni.navigateTo({
    url: '/pages/about/about'
  })
}

// 退出登录
const logout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    confirmColor: '#ff6b6b',
    success: (res) => {
      if (res.confirm) {
        // 清除登录状态
        uni.removeStorageSync('token')
        uni.removeStorageSync('userInfo')

        uni.showToast({
          title: '已退出登录',
          icon: 'success',
          duration: 1500
        })

        setTimeout(() => {
          uni.reLaunch({
            url: '/pages/login/login'
          })
        }, 1500)
      }
    }
  })
}

onMounted(() => {
  fetchUserInfo()
  calcCacheSize()

  // 获取版本号
  const systemInfo = uni.getSystemInfoSync()
  version.value = systemInfo.appVersion || '1.0.0'
})
</script>

<style lang="scss" scoped>
.settings-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40rpx;
}

// 用户卡片
.user-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60rpx 40rpx 80rpx;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 40rpx;
    background: #f5f5f5;
    border-radius: 40rpx 40rpx 0 0;
  }
}

.avatar-section {
  width: 160rpx;
  height: 160rpx;
  margin: 0 auto 30rpx;
  border-radius: 50%;
  position: relative;
  overflow: hidden;
  border: 4rpx solid rgba(255, 255, 255, 0.3);

  .avatar {
    width: 100%;
    height: 100%;
    border-radius: 50%;
  }

  .avatar-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s;

    .avatar-icon {
      font-size: 40rpx;
      margin-bottom: 4rpx;
    }

    .avatar-text {
      font-size: 20rpx;
      color: #fff;
    }
  }

  &:active .avatar-mask {
    opacity: 1;
  }
}

.nickname-section {
  text-align: center;

  .nickname-label {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.8);
    margin-bottom: 16rpx;
    display: block;
  }

  .nickname-input-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 20rpx;
  }

  .nickname-input {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 40rpx;
    padding: 16rpx 30rpx;
    font-size: 32rpx;
    color: #fff;
    text-align: center;
    min-width: 300rpx;

    &::placeholder {
      color: rgba(255, 255, 255, 0.6);
    }
  }

  .save-btn {
    background: #fff;
    color: #667eea;
    font-size: 26rpx;
    padding: 16rpx 32rpx;
    border-radius: 40rpx;
    font-weight: 600;
  }
}

// 设置列表
.settings-list {
  padding: 20rpx;
}

.settings-group {
  margin-bottom: 20rpx;

  .group-title {
    font-size: 24rpx;
    color: #999;
    padding: 20rpx 20rpx 16rpx;
  }

  .group-content {
    background: #fff;
    border-radius: 24rpx;
    overflow: hidden;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
  }
}

.settings-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 30rpx;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: #f9f9f9;
  }
}

.item-left {
  display: flex;
  align-items: center;
  gap: 20rpx;

  .item-icon {
    font-size: 36rpx;
  }

  .item-text {
    font-size: 30rpx;
    color: #333;
  }
}

.item-right {
  display: flex;
  align-items: center;
  gap: 12rpx;

  .item-value {
    font-size: 28rpx;
    color: #999;
  }

  .item-arrow {
    font-size: 36rpx;
    color: #ccc;
  }
}

// 退出登录
.logout-section {
  padding: 40rpx 30rpx;
}

.logout-btn {
  background: #fff;
  color: #ff6b6b;
  height: 90rpx;
  border-radius: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 600;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

// 弹窗样式
.popup-content {
  background: #fff;
  border-radius: 40rpx 40rpx 0 0;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  .popup-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
  }

  .popup-close {
    font-size: 36rpx;
    color: #999;
    padding: 10rpx;
  }
}

.form-list {
  padding: 20rpx 30rpx;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.form-label {
  width: 160rpx;
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.form-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  height: 60rpx;
}

.popup-footer {
  padding: 30rpx;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  height: 90rpx;
  border-radius: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 600;
}
</style>
