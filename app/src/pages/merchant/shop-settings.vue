<template>
  <view class="page">
    <!-- 头部 -->
    <view class="header">
      <view class="shop-preview">
        <image class="shop-logo" :src="shopInfo.logo || '/static/shop1.jpg'" mode="aspectFill" @click="chooseLogo" />
        <view class="shop-meta">
          <text class="shop-name">{{ shopInfo.name || '店铺名称' }}</text>
          <text class="shop-hint">点击头像更换Logo</text>
        </view>
      </view>
    </view>

    <!-- 表单区域 -->
    <scroll-view class="form-container" scroll-y>
      <!-- 基本信息 -->
      <view class="section">
        <view class="section-title">基本信息</view>

        <view class="form-item">
          <text class="form-label">店铺名称</text>
          <input class="form-input" placeholder="请输入店铺名称" v-model="shopInfo.name" />
        </view>

        <view class="form-item">
          <text class="form-label">店铺描述</text>
          <textarea class="form-textarea" placeholder="请输入店铺描述" v-model="shopInfo.description" />
        </view>

        <view class="form-item">
          <text class="form-label">店铺公告</text>
          <textarea class="form-textarea" placeholder="请输入店铺公告（显示在店铺首页）" v-model="shopInfo.notice" />
        </view>
      </view>

      <!-- 营业设置 -->
      <view class="section">
        <view class="section-title">营业设置</view>

        <view class="form-item">
          <text class="form-label">营业状态</text>
          <view class="form-switch">
            <text class="switch-text">{{ shopInfo.status === 1 ? '营业中' : '已打烊' }}</text>
            <switch :checked="shopInfo.status === 1" @change="toggleStatus" color="#667eea" />
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">营业时间</text>
          <view class="time-range">
            <picker mode="time" :value="shopInfo.openTime" @change="onOpenTimeChange">
              <view class="time-picker">
                <text class="time-value">{{ shopInfo.openTime || '09:00' }}</text>
                <text class="time-arrow">▼</text>
              </view>
            </picker>
            <text class="time-separator">至</text>
            <picker mode="time" :value="shopInfo.closeTime" @change="onCloseTimeChange">
              <view class="time-picker">
                <text class="time-value">{{ shopInfo.closeTime || '22:00' }}</text>
                <text class="time-arrow">▼</text>
              </view>
            </picker>
          </view>
        </view>
      </view>

      <!-- 配送设置 -->
      <view class="section">
        <view class="section-title">配送设置</view>

        <view class="form-item">
          <text class="form-label">起送金额</text>
          <view class="form-input-wrap">
            <text class="input-prefix">¥</text>
            <input class="form-input with-prefix" type="digit" placeholder="0" v-model="shopInfo.minOrderAmount" />
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">配送费用</text>
          <view class="form-input-wrap">
            <text class="input-prefix">¥</text>
            <input class="form-input with-prefix" type="digit" placeholder="0" v-model="shopInfo.deliveryFee" />
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">配送时长</text>
          <view class="form-input-wrap">
            <input class="form-input with-suffix" type="number" placeholder="30" v-model="shopInfo.deliveryTime" />
            <text class="input-suffix">分钟</text>
          </view>
        </view>
      </view>

      <!-- 联系信息 -->
      <view class="section">
        <view class="section-title">联系信息</view>

        <view class="form-item">
          <text class="form-label">联系电话</text>
          <input class="form-input" type="number" placeholder="请输入联系电话" v-model="shopInfo.phone" />
        </view>

        <view class="form-item">
          <text class="form-label">店铺地址</text>
          <textarea class="form-textarea" placeholder="请输入店铺地址" v-model="shopInfo.address" />
        </view>
      </view>

      <!-- 保存按钮 -->
      <view class="section">
        <button class="btn-save" :loading="saving" @click="saveShopInfo">保存设置</button>
      </view>

      <view class="safe-bottom"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getShopInfo, updateShopInfo } from '@/api/merchant'

const shopInfo = ref({
  name: '',
  description: '',
  notice: '',
  status: 1,
  openTime: '09:00',
  closeTime: '22:00',
  minOrderAmount: 0,
  deliveryFee: 0,
  deliveryTime: 30,
  phone: '',
  address: '',
  logo: ''
})

const saving = ref(false)

// 加载店铺信息
const loadShopInfo = async () => {
  try {
    const res = await getShopInfo()
    if (res.data) {
      shopInfo.value = {
        ...shopInfo.value,
        ...res.data
      }
    }
  } catch (e) {
    console.error('获取店铺信息失败:', e)
    uni.showToast({ title: '获取信息失败', icon: 'none' })
  }
}

// 选择Logo
const chooseLogo = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      // 这里应该上传图片到服务器，简化处理直接使用本地路径
      shopInfo.value.logo = res.tempFilePaths[0]
      uni.showToast({ title: '图片已选择，保存后生效', icon: 'none' })
    }
  })
}

// 切换营业状态
const toggleStatus = (e) => {
  shopInfo.value.status = e.detail.value ? 1 : 0
}

// 营业时间选择
const onOpenTimeChange = (e) => {
  shopInfo.value.openTime = e.detail.value
}

const onCloseTimeChange = (e) => {
  shopInfo.value.closeTime = e.detail.value
}

// 保存店铺信息
const saveShopInfo = async () => {
  if (!shopInfo.value.name) {
    uni.showToast({ title: '请输入店铺名称', icon: 'none' })
    return
  }

  saving.value = true
  try {
    await updateShopInfo(shopInfo.value)
    uni.showToast({ title: '保存成功', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: '保存失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadShopInfo()
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
  padding: 40rpx 30rpx;
  padding-top: calc(var(--status-bar-height) + 40rpx);
}

.shop-preview {
  display: flex;
  align-items: center;
}

.shop-logo {
  width: 120rpx;
  height: 120rpx;
  border-radius: 24rpx;
  margin-right: 30rpx;
  background: #fff;
  border: 4rpx solid rgba(255, 255, 255, 0.2);
}

.shop-meta {
  display: flex;
  flex-direction: column;
}

.shop-name {
  color: #fff;
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 12rpx;
}

.shop-hint {
  color: rgba(255, 255, 255, 0.6);
  font-size: 24rpx;
}

/* 表单区域 */
.form-container {
  flex: 1;
  padding: 20rpx;
}

.section {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #1a1a2e;
  margin-bottom: 30rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 16rpx;
  font-weight: 500;
}

.form-input {
  width: 100%;
  height: 80rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.form-textarea {
  width: 100%;
  height: 160rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.form-switch {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8f9fa;
  padding: 20rpx 24rpx;
  border-radius: 12rpx;
}

.switch-text {
  font-size: 28rpx;
  color: #333;
}

/* 时间选择 */
.time-range {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.time-picker {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  padding: 20rpx 30rpx;
  border-radius: 12rpx;
  min-width: 160rpx;
  justify-content: center;
}

.time-value {
  font-size: 28rpx;
  color: #333;
  margin-right: 12rpx;
}

.time-arrow {
  font-size: 20rpx;
  color: #999;
}

.time-separator {
  font-size: 28rpx;
  color: #666;
}

/* 带前缀/后缀的输入框 */
.form-input-wrap {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 0 24rpx;
}

.input-prefix {
  font-size: 28rpx;
  color: #666;
  margin-right: 12rpx;
}

.input-suffix {
  font-size: 28rpx;
  color: #666;
  margin-left: 12rpx;
}

.form-input.with-prefix,
.form-input.with-suffix {
  flex: 1;
  background: transparent;
  padding: 0;
}

/* 保存按钮 */
.btn-save {
  width: 100%;
  height: 90rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 16rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.safe-bottom {
  height: 40rpx;
}
</style>
