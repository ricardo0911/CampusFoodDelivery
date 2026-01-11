<template>
  <view class="page">
    <!-- 顶部渐变头部 -->
    <view class="header">
      <view class="location-bar" @click="chooseLocation">
        <text class="location-icon">📍</text>
        <text class="location-text">{{ locationName }}</text>
        <text class="arrow">▼</text>
      </view>
      
      <!-- 搜索栏 -->
      <view class="search-bar">
        <text class="search-icon">🔍</text>
        <input class="search-input" placeholder="搜索店铺或菜品" v-model="keyword" @confirm="search" />
      </view>
      
      <!-- 分类导航 -->
      <scroll-view class="category-nav" scroll-x>
        <view 
          class="category-item" 
          :class="{ active: activeCategory === '' }" 
          @click="selectCategory('')"
        >
          <view class="category-icon">🍽️</view>
          <text class="category-name">全部</text>
        </view>
        <view 
          class="category-item" 
          :class="{ active: activeCategory === '快餐' }" 
          @click="selectCategory('快餐')"
        >
          <view class="category-icon">🍔</view>
          <text class="category-name">快餐</text>
        </view>
        <view 
          class="category-item" 
          :class="{ active: activeCategory === '中餐' }" 
          @click="selectCategory('中餐')"
        >
          <view class="category-icon">🍜</view>
          <text class="category-name">中餐</text>
        </view>
        <view 
          class="category-item" 
          :class="{ active: activeCategory === '饮品' }" 
          @click="selectCategory('饮品')"
        >
          <view class="category-icon">🧋</view>
          <text class="category-name">饮品</text>
        </view>
        <view 
          class="category-item" 
          :class="{ active: activeCategory === '甜点' }" 
          @click="selectCategory('甜点')"
        >
          <view class="category-icon">🍰</view>
          <text class="category-name">甜点</text>
        </view>
      </scroll-view>
    </view>
    
    <!-- 店铺列表 -->
    <scroll-view class="shop-list" scroll-y>
      <!-- 推荐标签 -->
      <view class="section-title">
        <text class="title-text">附近商家</text>
        <text class="title-sub">为您精选优质店铺</text>
      </view>
      
      <view class="shop-card" v-for="shop in shopList" :key="shop.id" @click="goShop(shop.id)">
        <!-- 店铺图片 -->
        <image class="shop-cover" :src="shop.logo" mode="aspectFill" />
        
        <!-- 店铺信息 -->
        <view class="shop-content">
          <view class="shop-header">
            <text class="shop-name">{{ shop.name }}</text>
            <view class="shop-badge" v-if="shop.rating >= 4.5">
              <text>品质</text>
            </view>
          </view>
          
          <view class="shop-rating">
            <view class="rating-stars">
              <text class="star filled" v-for="n in Math.floor(shop.rating || 0)" :key="'f'+n">★</text>
              <text class="star" v-for="n in (5 - Math.floor(shop.rating || 0))" :key="'e'+n">☆</text>
            </view>
            <text class="rating-score">{{ shop.rating || 4.5 }}</text>
            <text class="sales-text">月售{{ shop.monthlySales || 999 }}+</text>
          </view>
          
          <view class="shop-delivery">
            <view class="delivery-item">
              <text class="delivery-label">起送</text>
              <text class="delivery-value">¥{{ shop.minOrderAmount || 15 }}</text>
            </view>
            <view class="delivery-divider"></view>
            <view class="delivery-item">
              <text class="delivery-label">配送</text>
              <text class="delivery-value">¥{{ shop.deliveryFee || 3 }}</text>
            </view>
            <view class="delivery-divider"></view>
            <view class="delivery-item">
              <text class="delivery-label">时长</text>
              <text class="delivery-value">{{ shop.deliveryTime || 30 }}分钟</text>
            </view>
          </view>
          
          <view class="shop-tags">
            <text class="promo-tag" v-if="shop.hasPromo">满减</text>
            <text class="new-tag" v-if="shop.isNew">新店</text>
            <text class="feature-tag">准时达</text>
          </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view class="empty-state" v-if="shopList.length === 0">
        <view class="empty-icon">🏪</view>
        <text class="empty-text">暂无营业中的店铺</text>
        <text class="empty-hint">换个时间再来看看吧</text>
      </view>
      
      <!-- 底部安全区 -->
      <view class="safe-bottom"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get } from '@/utils/request'

const keyword = ref('')
const shopList = ref([])
const activeCategory = ref('')
const locationName = ref('点击选择位置')
const latitude = ref(0)
const longitude = ref(0)

// 获取当前位置后自动打开地图选择
const getLocation = () => {
  uni.getLocation({
    type: 'gcj02',
    isHighAccuracy: true,
    success: (res) => {
      latitude.value = res.latitude
      longitude.value = res.longitude
      // 自动打开地图选择器获取详细地址
      openLocationPicker()
    },
    fail: (err) => {
      console.log('定位失败:', err)
      // 直接打开地图选择器
      openLocationPicker()
    }
  })
}

// 打开地图选择器（支持全国任意位置）
const openLocationPicker = () => {
  uni.chooseLocation({
    latitude: latitude.value || undefined,
    longitude: longitude.value || undefined,
    success: (res) => {
      // 显示用户选择的地址名称
      locationName.value = res.name || res.address || '已选位置'
      latitude.value = res.latitude
      longitude.value = res.longitude
    },
    fail: (err) => {
      console.log('选择位置失败:', err)
      if (latitude.value && longitude.value) {
        locationName.value = '当前位置'
      }
    }
  })
}

// 点击位置栏时调用
const chooseLocation = () => {
  if (latitude.value && longitude.value) {
    // 已有位置，直接打开选择器
    openLocationPicker()
  } else {
    // 没有位置，先定位再选择
    getLocation()
  }
}

const loadShops = async () => {
  try {
    const res = await get('/public/shop/list', { keyword: keyword.value })
    shopList.value = res.data.records || []
  } catch (e) { 
    console.error(e)
    // 添加模拟数据以便展示 UI - 使用真实图片
    shopList.value = [
      { id: 1, name: '黄焖鸡米饭', logo: '/static/shop-huangmenji.jpg', rating: 4.8, monthlySales: 1234, minOrderAmount: 15, deliveryFee: 3, deliveryTime: 25, hasPromo: true },
      { id: 2, name: '兰州拉面馆', logo: '/static/shop-lamian.jpg', rating: 4.6, monthlySales: 856, minOrderAmount: 12, deliveryFee: 2, deliveryTime: 20, isNew: true },
      { id: 3, name: '麻辣香锅', logo: '/static/shop-malaguo.jpg', rating: 4.9, monthlySales: 2341, minOrderAmount: 25, deliveryFee: 4, deliveryTime: 35, hasPromo: true },
    ]
  }
}

const selectCategory = (category) => {
  activeCategory.value = category
  loadShops()
}

const search = () => { loadShops() }

const goShop = (id) => {
  uni.navigateTo({ url: `/pages/shop/shop?id=${id}` })
}

onMounted(() => {
  getLocation()
  loadShops()
})
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f5f6fa;
}

/* 顶部头部 - 温暖的食物主题色 */
.header {
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 50%, #ffc107 100%);
  padding: 30rpx;
  padding-top: calc(var(--status-bar-height) + 20rpx);
  padding-bottom: 40rpx;
  border-radius: 0 0 40rpx 40rpx;
  box-shadow: 0 8rpx 30rpx rgba(255, 107, 53, 0.3);
}

.location-bar {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.location-icon {
  font-size: 32rpx;
}

.location-text {
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  margin-left: 8rpx;
  max-width: 400rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.arrow {
  color: rgba(255,255,255,0.8);
  font-size: 20rpx;
  margin-left: 8rpx;
}

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 40rpx;
  padding: 20rpx 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.1);
}

.search-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

/* 分类导航 */
.category-nav {
  display: flex;
  white-space: nowrap;
  margin-top: 30rpx;
  padding-bottom: 20rpx;
}

.category-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  margin-right: 36rpx;
  transition: all 0.3s;
}

.category-item.active {
  transform: scale(1.05);
}

.category-icon {
  width: 90rpx;
  height: 90rpx;
  background: rgba(255,255,255,0.95);
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.1);
}

.category-item.active .category-icon {
  background: #fff;
  box-shadow: 0 6rpx 20rpx rgba(102, 126, 234, 0.3);
}

.category-name {
  color: rgba(255,255,255,0.9);
  font-size: 24rpx;
  margin-top: 12rpx;
}

.category-item.active .category-name {
  color: #fff;
  font-weight: bold;
}

/* 店铺列表 */
.shop-list {
  flex: 1;
  padding: 30rpx;
  padding-top: 30rpx;
}

.section-title {
  margin-bottom: 24rpx;
}

.title-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #1a1a2e;
}

.title-sub {
  font-size: 24rpx;
  color: #999;
  margin-left: 16rpx;
}

/* 店铺卡片 */
.shop-card {
  background: #fff;
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05);
}

.shop-cover {
  width: 100%;
  height: 240rpx;
}

.shop-content {
  padding: 24rpx;
}

.shop-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.shop-name {
  font-size: 34rpx;
  font-weight: bold;
  color: #1a1a2e;
}

.shop-badge {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: #fff;
  font-size: 20rpx;
  padding: 6rpx 14rpx;
  border-radius: 20rpx;
}

.shop-rating {
  display: flex;
  align-items: center;
  margin-top: 16rpx;
}

.rating-stars {
  display: flex;
}

.star {
  font-size: 24rpx;
  color: #ddd;
}

.star.filled {
  color: #ffc107;
}

.rating-score {
  font-size: 26rpx;
  font-weight: bold;
  color: #ff6b35;
  margin-left: 10rpx;
}

.sales-text {
  font-size: 24rpx;
  color: #999;
  margin-left: 20rpx;
}

.shop-delivery {
  display: flex;
  align-items: center;
  margin-top: 20rpx;
  padding: 16rpx 0;
  border-top: 1rpx solid #f0f0f0;
}

.delivery-item {
  display: flex;
  align-items: center;
}

.delivery-label {
  font-size: 22rpx;
  color: #999;
  margin-right: 8rpx;
}

.delivery-value {
  font-size: 24rpx;
  color: #333;
  font-weight: 500;
}

.delivery-divider {
  width: 1rpx;
  height: 24rpx;
  background: #e0e0e0;
  margin: 0 24rpx;
}

.shop-tags {
  display: flex;
  gap: 12rpx;
  margin-top: 16rpx;
}

.promo-tag {
  font-size: 20rpx;
  color: #ff6b35;
  background: #fff5f0;
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
  border: 1rpx solid #ffcdb8;
}

.new-tag {
  font-size: 20rpx;
  color: #52c41a;
  background: #f6ffed;
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
  border: 1rpx solid #b7eb8f;
}

.feature-tag {
  font-size: 20rpx;
  color: #ff6b35;
  background: #f0f3ff;
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
  border: 1rpx solid #c4d0ff;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #333;
  margin-bottom: 12rpx;
}

.empty-hint {
  font-size: 26rpx;
  color: #999;
}

.safe-bottom {
  height: 120rpx;
}
</style>
