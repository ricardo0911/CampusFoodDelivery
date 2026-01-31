<template>
  <view class="page">
    <!-- 自定义导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <view class="back-icon"></view>
      </view>
      <text class="nav-title">美食盲盒</text>
      <view class="nav-placeholder"></view>
    </view>

    <!-- 头部介绍 -->
    <view class="header">
      <view class="header-bg"></view>
      <view class="header-content">
        <text class="header-title">惊喜美食盲盒</text>
        <text class="header-desc">随机搭配，超值享受</text>
      </view>
    </view>

    <!-- 盲盒列表 -->
    <view class="box-list">
      <view
        class="box-item"
        v-for="box in boxes"
        :key="box.id"
        @click="selectBox(box)"
      >
        <view class="box-type" :class="getTypeClass(box.type)">
          {{ getTypeName(box.type) }}
        </view>
        <view class="box-icon">
          <view class="gift-box">
            <view class="box-lid"></view>
            <view class="box-body"></view>
            <view class="box-ribbon"></view>
          </view>
        </view>
        <text class="box-name">{{ box.name }}</text>
        <text class="box-desc">{{ box.description || `包含${box.dishCount}道美食` }}</text>
        <view class="box-price">
          <text class="price-current">¥{{ box.price }}</text>
          <text class="price-original">¥{{ box.originalPrice }}</text>
        </view>
        <view class="box-btn" @click.stop="openBox(box)">
          开盲盒
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty" v-if="boxes.length === 0">
      <text>暂无盲盒活动</text>
    </view>

    <!-- 预览弹窗 -->
    <view class="modal" v-if="showPreview" @click="closePreview">
      <view class="modal-content" @click.stop>
        <text class="modal-title">可能获得的美食</text>
        <view class="preview-list">
          <view class="preview-item" v-for="dish in previewDishes" :key="dish.id">
            <image class="preview-image" :src="dish.image || '/static/food.png'" mode="aspectFill" />
            <text class="preview-name">{{ dish.name }}</text>
          </view>
        </view>
        <view class="modal-btn" @click="closePreview">知道了</view>
      </view>
    </view>

    <!-- 开盒结果弹窗 -->
    <view class="modal" v-if="showResult" @click="closeResult">
      <view class="modal-content result" @click.stop>
        <view class="result-header">
          <view class="confetti"></view>
          <text class="result-title">恭喜获得</text>
        </view>
        <view class="result-list">
          <view class="result-item" v-for="dish in resultDishes" :key="dish.id">
            <image class="result-image" :src="dish.image || '/static/food.png'" mode="aspectFill" />
            <view class="result-info">
              <text class="result-name">{{ dish.name }}</text>
              <text class="result-price">¥{{ dish.price }}</text>
            </view>
          </view>
        </view>
        <view class="result-total">
          <text>总价值</text>
          <text class="total-value">¥{{ totalValue }}</text>
        </view>
        <view class="modal-btn" @click="addToCart">加入购物车</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const boxes = ref([])
const showPreview = ref(false)
const showResult = ref(false)
const previewDishes = ref([])
const resultDishes = ref([])
const selectedBox = ref(null)

const totalValue = computed(() => {
  return resultDishes.value.reduce((sum, dish) => sum + parseFloat(dish.price), 0).toFixed(2)
})

const goBack = () => {
  uni.navigateBack()
}

const getTypeName = (type) => {
  const types = { 1: '早餐', 2: '午餐', 3: '下午茶', 4: '晚餐' }
  return types[type] || '美食'
}

const getTypeClass = (type) => {
  const classes = { 1: 'breakfast', 2: 'lunch', 3: 'tea', 4: 'dinner' }
  return classes[type] || ''
}

const loadData = async () => {
  try {
    const res = await uni.request({
      url: 'http://localhost:8080/api/customer/mystery-box/list',
      method: 'GET'
    })

    if (res.data.code === 200) {
      boxes.value = res.data.data || []
    }
  } catch (e) {
    console.error(e)
  }
}

const selectBox = async (box) => {
  selectedBox.value = box
  try {
    const res = await uni.request({
      url: `http://localhost:8080/api/customer/mystery-box/preview/${box.id}`,
      method: 'GET'
    })

    if (res.data.code === 200) {
      previewDishes.value = res.data.data || []
      showPreview.value = true
    }
  } catch (e) {
    console.error(e)
  }
}

const openBox = async (box) => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }

  try {
    const res = await uni.request({
      url: `http://localhost:8080/api/customer/mystery-box/open/${box.id}`,
      method: 'POST',
      header: { 'Authorization': `Bearer ${token}` }
    })

    if (res.data.code === 200) {
      resultDishes.value = res.data.data || []
      selectedBox.value = box
      showResult.value = true
    } else {
      uni.showToast({ title: res.data.message || '开盒失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '网络错误', icon: 'none' })
  }
}

const closePreview = () => {
  showPreview.value = false
}

const closeResult = () => {
  showResult.value = false
}

const addToCart = () => {
  uni.showToast({ title: '已加入购物车', icon: 'success' })
  showResult.value = false
}

onMounted(loadData)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fff5f0 0%, #f5f5f5 30%);
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
  height: 88rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
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

.header {
  position: relative;
  padding: 40rpx 30rpx;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 0 0 40rpx 40rpx;
}

.header-content {
  position: relative;
  text-align: center;
}

.header-title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: #fff;
  margin-bottom: 12rpx;
}

.header-desc {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

.box-list {
  display: flex;
  flex-wrap: wrap;
  padding: 20rpx;
  gap: 20rpx;
}

.box-item {
  width: calc(50% - 10rpx);
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.box-type {
  position: absolute;
  top: 20rpx;
  left: 20rpx;
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  color: #fff;
}

.box-type.breakfast {
  background: linear-gradient(135deg, #ffa500, #ffcc00);
}

.box-type.lunch {
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
}

.box-type.tea {
  background: linear-gradient(135deg, #9c27b0, #e91e63);
}

.box-type.dinner {
  background: linear-gradient(135deg, #3f51b5, #2196f3);
}

.box-icon {
  width: 160rpx;
  height: 160rpx;
  margin: 20rpx 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.gift-box {
  width: 120rpx;
  height: 120rpx;
  position: relative;
}

.box-lid {
  position: absolute;
  top: 0;
  left: -10rpx;
  right: -10rpx;
  height: 30rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 8rpx;
}

.box-body {
  position: absolute;
  top: 25rpx;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, #ff8f50, #ff6b35);
  border-radius: 8rpx;
}

.box-ribbon {
  position: absolute;
  top: 25rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 20rpx;
  height: 100%;
  background: #ffd700;
}

.box-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 8rpx;
}

.box-desc {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 16rpx;
}

.box-price {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.price-current {
  font-size: 36rpx;
  font-weight: 700;
  color: #ff6b35;
}

.price-original {
  font-size: 24rpx;
  color: #999;
  text-decoration: line-through;
}

.box-btn {
  width: 100%;
  height: 72rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 600;
  color: #fff;
}

.empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 100rpx;
  color: #999;
  font-size: 28rpx;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal-content {
  width: 600rpx;
  background: #fff;
  border-radius: 32rpx;
  padding: 40rpx;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #333;
  text-align: center;
  margin-bottom: 30rpx;
}

.preview-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.preview-item {
  width: calc(33.33% - 14rpx);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.preview-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 16rpx;
  margin-bottom: 8rpx;
}

.preview-name {
  font-size: 24rpx;
  color: #666;
  text-align: center;
}

.modal-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
}

.result-header {
  text-align: center;
  margin-bottom: 30rpx;
}

.result-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #ff6b35;
}

.result-list {
  margin-bottom: 30rpx;
}

.result-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.result-item:last-child {
  border-bottom: none;
}

.result-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 12rpx;
  margin-right: 20rpx;
}

.result-info {
  flex: 1;
}

.result-name {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 8rpx;
}

.result-price {
  font-size: 28rpx;
  color: #ff6b35;
}

.result-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  background: #fff5f0;
  border-radius: 16rpx;
  margin-bottom: 30rpx;
}

.total-value {
  font-size: 40rpx;
  font-weight: 700;
  color: #ff6b35;
}
</style>
