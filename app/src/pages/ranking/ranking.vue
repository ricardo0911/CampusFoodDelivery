<template>
  <view class="page">
    <!-- 自定义导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <view class="back-icon"></view>
      </view>
      <text class="nav-title">美食排行榜</text>
      <view class="nav-placeholder"></view>
    </view>

    <!-- 标签切换 -->
    <view class="tabs">
      <view
        class="tab"
        :class="{ active: activeTab === 'dishes' }"
        @click="activeTab = 'dishes'"
      >
        <text>热销菜品</text>
      </view>
      <view
        class="tab"
        :class="{ active: activeTab === 'shops' }"
        @click="activeTab = 'shops'"
      >
        <text>人气店铺</text>
      </view>
      <view
        class="tab"
        :class="{ active: activeTab === 'new' }"
        @click="activeTab = 'new'"
      >
        <text>新品推荐</text>
      </view>
    </view>

    <!-- 热销菜品 -->
    <view class="list" v-if="activeTab === 'dishes'">
      <view
        class="rank-item"
        v-for="(dish, index) in hotDishes"
        :key="dish.id"
      >
        <view class="rank-badge" :class="getRankClass(index)">
          {{ index + 1 }}
        </view>
        <image class="item-image" :src="dish.image || '/static/food.png'" mode="aspectFill" />
        <view class="item-info">
          <text class="item-name">{{ dish.name }}</text>
          <text class="item-desc">{{ dish.description || '美味可口' }}</text>
          <view class="item-meta">
            <text class="item-price">¥{{ dish.price }}</text>
            <text class="item-sales">月售{{ dish.sales }}份</text>
          </view>
        </view>
      </view>
      <view class="empty" v-if="hotDishes.length === 0">
        <text>暂无数据</text>
      </view>
    </view>

    <!-- 人气店铺 -->
    <view class="list" v-if="activeTab === 'shops'">
      <view
        class="rank-item shop-item"
        v-for="(shop, index) in hotShops"
        :key="shop.id"
        @click="goShop(shop.id)"
      >
        <view class="rank-badge" :class="getRankClass(index)">
          {{ index + 1 }}
        </view>
        <image class="item-image" :src="shop.logo || '/static/shop.png'" mode="aspectFill" />
        <view class="item-info">
          <text class="item-name">{{ shop.name }}</text>
          <view class="shop-tags">
            <view class="tag">评分{{ shop.rating }}</view>
            <view class="tag">起送¥{{ shop.minOrderAmount }}</view>
          </view>
          <view class="item-meta">
            <text class="item-sales">月售{{ shop.monthlySales }}单</text>
          </view>
        </view>
      </view>
      <view class="empty" v-if="hotShops.length === 0">
        <text>暂无数据</text>
      </view>
    </view>

    <!-- 新品推荐 -->
    <view class="list" v-if="activeTab === 'new'">
      <view
        class="rank-item new-item"
        v-for="(dish, index) in newDishes"
        :key="dish.id"
      >
        <view class="new-badge">NEW</view>
        <image class="item-image" :src="dish.image || '/static/food.png'" mode="aspectFill" />
        <view class="item-info">
          <text class="item-name">{{ dish.name }}</text>
          <text class="item-desc">{{ dish.description || '新品上市' }}</text>
          <view class="item-meta">
            <text class="item-price">¥{{ dish.price }}</text>
          </view>
        </view>
      </view>
      <view class="empty" v-if="newDishes.length === 0">
        <text>暂无新品</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const activeTab = ref('dishes')
const hotDishes = ref([])
const hotShops = ref([])
const newDishes = ref([])

const goBack = () => {
  uni.navigateBack()
}

const goShop = (id) => {
  uni.navigateTo({ url: `/pages/shop/shop?id=${id}` })
}

const getRankClass = (index) => {
  if (index === 0) return 'gold'
  if (index === 1) return 'silver'
  if (index === 2) return 'bronze'
  return ''
}

const loadData = async () => {
  try {
    // 加载热销菜品
    const dishRes = await uni.request({
      url: 'http://localhost:8080/api/customer/ranking/hot-dishes?limit=10',
      method: 'GET'
    })
    if (dishRes.data.code === 200) {
      hotDishes.value = dishRes.data.data || []
    }

    // 加载热门店铺
    const shopRes = await uni.request({
      url: 'http://localhost:8080/api/customer/ranking/hot-shops?limit=10',
      method: 'GET'
    })
    if (shopRes.data.code === 200) {
      hotShops.value = shopRes.data.data || []
    }

    // 加载新品
    const newRes = await uni.request({
      url: 'http://localhost:8080/api/customer/ranking/new-dishes?limit=10',
      method: 'GET'
    })
    if (newRes.data.code === 200) {
      newDishes.value = newRes.data.data || []
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(loadData)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
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

.tabs {
  display: flex;
  background: #fff;
  padding: 20rpx;
  gap: 20rpx;
}

.tab {
  flex: 1;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 36rpx;
  font-size: 28rpx;
  color: #666;
  transition: all 0.3s;
}

.tab.active {
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  color: #fff;
  font-weight: 600;
}

.list {
  padding: 20rpx;
}

.rank-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  position: relative;
}

.rank-badge {
  width: 48rpx;
  height: 48rpx;
  background: #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 700;
  color: #fff;
  margin-right: 20rpx;
}

.rank-badge.gold {
  background: linear-gradient(135deg, #FFD700, #FFA500);
}

.rank-badge.silver {
  background: linear-gradient(135deg, #C0C0C0, #A0A0A0);
}

.rank-badge.bronze {
  background: linear-gradient(135deg, #CD7F32, #B87333);
}

.new-badge {
  position: absolute;
  top: 16rpx;
  left: 16rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  color: #fff;
  font-size: 20rpx;
  font-weight: 600;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  z-index: 1;
}

.new-item {
  padding-left: 24rpx;
}

.item-image {
  width: 140rpx;
  height: 140rpx;
  border-radius: 16rpx;
  margin-right: 20rpx;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 8rpx;
}

.item-desc {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.item-price {
  font-size: 32rpx;
  font-weight: 700;
  color: #ff6b35;
}

.item-sales {
  font-size: 24rpx;
  color: #999;
}

.shop-tags {
  display: flex;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.tag {
  font-size: 22rpx;
  color: #ff6b35;
  background: rgba(255, 107, 53, 0.1);
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 100rpx;
  color: #999;
  font-size: 28rpx;
}
</style>
