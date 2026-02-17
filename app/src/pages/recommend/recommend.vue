<template>
  <view class="page">
    <!-- 顶部标题 -->
    <view class="header">
      <text class="header-title">🤖 为你推荐</text>
      <text class="header-sub">基于你的口味偏好智能推荐</text>
    </view>

    <!-- 推荐列表 -->
    <scroll-view class="recommend-list" scroll-y @scrolltolower="loadMore">
      <!-- 推荐卡片 -->
      <view class="dish-card" v-for="dish in recommendations" :key="dish.id" @click="goDish(dish)">
        <image class="dish-image" :src="dish.image || '/static/food1.jpg'" mode="aspectFill" />
        
        <view class="dish-content">
          <view class="dish-header">
            <text class="dish-name">{{ dish.name }}</text>
            <view class="dish-tags">
              <text class="tag" v-for="tag in dish.recommendTags" :key="tag">{{ tag }}</text>
            </view>
          </view>
          
          <text class="dish-desc">{{ dish.description }}</text>
          
          <!-- 营养信息 -->
          <view class="nutrition-info" v-if="dish.nutrition">
            <text class="calorie">🔥 {{ dish.nutrition.calories }}千卡</text>
            <text class="nutrition-tags">{{ dish.nutrition.tags }}</text>
          </view>
          
          <view class="dish-footer">
            <view class="shop-info">
              <text class="shop-name">{{ dish.shopName }}</text>
            </view>
            <view class="price-info">
              <text class="price">¥{{ dish.price }}</text>
              <text class="sales">月售{{ dish.sales || 0 }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="recommendations.length === 0 && !loading">
        <view class="empty-icon">🍽️</view>
        <text class="empty-text">正在学习你的口味偏好</text>
        <text class="empty-hint">多点几次餐，推荐会更准哦</text>
      </view>

      <!-- 加载状态 -->
      <view class="loading-state" v-if="loading">
        <text class="loading-text">🔍 正在为你挑选美食...</text>
      </view>

      <!-- 底部安全区 -->
      <view class="safe-bottom"></view>
    </scroll-view>

    <!-- 健康数据入口 -->
    <view class="health-entry" @click="goNutrition">
      <text class="health-icon">📊</text>
      <view class="health-info">
        <text class="health-title">今日营养</text>
        <text class="health-data">{{ todayCalories }}/{{ targetCalories }} 千卡</text>
      </view>
      <text class="health-arrow">›</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get, post } from '@/utils/request'

const recommendations = ref([])
const loading = ref(false)
const logId = ref(null)
const todayCalories = ref(0)
const targetCalories = ref(2000)

// 加载推荐
const loadRecommendations = async () => {
  loading.value = true
  try {
    const res = await get('/customer/recommend/home', { limit: 20 })
    if (res.code === 200 && res.data) {
      recommendations.value = res.data.dishes || []
      logId.value = res.data.logId
    }
  } catch (e) {
    // 使用模拟数据
    loadMockData()
  } finally {
    loading.value = false
  }
}

// 模拟数据
const loadMockData = () => {
  recommendations.value = [
    { id: 1, name: '宫保鸡丁', description: '经典川菜，鸡肉嫩滑', price: 18, sales: 520, shopName: '美味快餐店', recommendTags: ['你常点', '热销'], nutrition: { calories: 350, tags: '高蛋白' } },
    { id: 2, name: '番茄炒蛋', description: '家常经典，营养美味', price: 12, sales: 650, shopName: '美味快餐店', recommendTags: ['低脂'], nutrition: { calories: 180, tags: '低脂' } },
    { id: 3, name: '牛肉拉面', description: '手工拉面配秘制牛肉', price: 22, sales: 450, shopName: '幸福面馆', recommendTags: ['热销'], nutrition: { calories: 450, tags: '高蛋白' } },
    { id: 4, name: '青椒肉丝', description: '青椒爽脆，肉丝入味', price: 15, sales: 420, shopName: '美味快餐店', recommendTags: ['均衡'], nutrition: { calories: 260, tags: '均衡' } },
    { id: 5, name: '酸梅汤', description: '自制酸梅汤，消暑解腻', price: 5, sales: 200, shopName: '幸福面馆', recommendTags: ['低热量'], nutrition: { calories: 60, tags: '低热量' } },
  ]
}

// 加载今日营养
const loadTodayNutrition = async () => {
  try {
    const res = await get('/customer/nutrition/today')
    if (res.code === 200 && res.data) {
      todayCalories.value = res.data.consumedCalories || 0
      targetCalories.value = res.data.targetCalories || 2000
    }
  } catch (e) {
    console.log('营养数据加载失败')
  }
}

// 点击菜品
const goDish = async (dish) => {
  // 记录点击
  if (logId.value) {
    post('/customer/recommend/click', null, { logId: logId.value, dishId: dish.id })
  }
  // 跳转到店铺
  uni.navigateTo({ url: `/pages/shop/shop?id=${dish.shopId}` })
}

// 跳转营养报告
const goNutrition = () => {
  uni.navigateTo({ url: '/pages/nutrition/nutrition' })
}

// 加载更多
const loadMore = () => {
  // 可以实现分页加载
}

onMounted(() => {
  loadRecommendations()
  loadTodayNutrition()
})
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: linear-gradient(180deg, #fff5f0 0%, #f5f5f5 100%);
}

.header {
  padding: 40rpx 30rpx;
  padding-top: calc(var(--status-bar-height) + 20rpx);
}

.header-title {
  font-size: 44rpx;
  font-weight: bold;
  color: #1a1a2e;
}

.header-sub {
  font-size: 26rpx;
  color: #999;
  margin-left: 16rpx;
}

.recommend-list {
  flex: 1;
  padding: 0 30rpx;
  padding-bottom: 160rpx;
}

/* 菜品卡片 */
.dish-card {
  display: flex;
  background: #fff;
  border-radius: 20rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05);
}

.dish-image {
  width: 200rpx;
  height: 200rpx;
  flex-shrink: 0;
}

.dish-content {
  flex: 1;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
}

.dish-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.dish-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.dish-tags {
  display: flex;
  gap: 8rpx;
}

.tag {
  font-size: 20rpx;
  color: #ff6b35;
  background: #fff0eb;
  padding: 4rpx 10rpx;
  border-radius: 6rpx;
}

.dish-desc {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nutrition-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-top: 12rpx;
}

.calorie {
  font-size: 22rpx;
  color: #ff6b35;
}

.nutrition-tags {
  font-size: 20rpx;
  color: #2da44e;
  background: #e6ffed;
  padding: 4rpx 10rpx;
  border-radius: 6rpx;
}

.dish-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.shop-name {
  font-size: 22rpx;
  color: #666;
}

.price-info {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
}

.price {
  font-size: 32rpx;
  font-weight: bold;
  color: #ff6b35;
}

.sales {
  font-size: 20rpx;
  color: #999;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #333;
}

.empty-hint {
  font-size: 26rpx;
  color: #999;
  margin-top: 12rpx;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 60rpx 0;
}

.loading-text {
  font-size: 28rpx;
  color: #999;
}

/* 健康入口 */
.health-entry {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  background: #fff;
  padding: 30rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05);
}

.health-icon {
  font-size: 48rpx;
}

.health-info {
  flex: 1;
  margin-left: 20rpx;
}

.health-title {
  font-size: 28rpx;
  color: #333;
  display: block;
}

.health-data {
  font-size: 24rpx;
  color: #ff6b35;
}

.health-arrow {
  font-size: 40rpx;
  color: #ccc;
}

.safe-bottom {
  height: 160rpx;
}
</style>
