<template>
  <view class="container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <input class="search-input" placeholder="搜索店铺或菜品" v-model="keyword" @confirm="search" />
    </view>
    
    <!-- 店铺列表 -->
    <view class="shop-list">
      <view class="shop-item" v-for="shop in shopList" :key="shop.id" @click="goShop(shop.id)">
        <image class="shop-logo" :src="shop.logo || '/static/default-shop.png'" mode="aspectFill" />
        <view class="shop-info">
          <text class="shop-name">{{ shop.name }}</text>
          <view class="shop-meta">
            <text class="rating">⭐ {{ shop.rating }}</text>
            <text class="sales">月售{{ shop.monthlySales }}</text>
          </view>
          <view class="shop-tags">
            <text class="tag">起送¥{{ shop.minOrderAmount }}</text>
            <text class="tag">配送¥{{ shop.deliveryFee }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <view class="empty" v-if="shopList.length === 0">
      <text>暂无营业中的店铺</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get } from '@/utils/request'

const keyword = ref('')
const shopList = ref([])

const loadShops = async () => {
  try {
    const res = await get('/public/shop/list', { keyword: keyword.value })
    shopList.value = res.data.records || []
  } catch (e) { console.error(e) }
}

const search = () => { loadShops() }

const goShop = (id) => {
  uni.navigateTo({ url: `/pages/shop/shop?id=${id}` })
}

onMounted(loadShops)
</script>

<style scoped>
.container { padding: 20rpx; }
.search-bar { margin-bottom: 20rpx; }
.search-input {
  width: 100%;
  height: 80rpx;
  background: #fff;
  border-radius: 40rpx;
  padding: 0 30rpx;
  font-size: 28rpx;
}
.shop-item {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}
.shop-logo {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  margin-right: 20rpx;
}
.shop-info { flex: 1; }
.shop-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}
.shop-meta {
  display: flex;
  gap: 20rpx;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #666;
}
.rating { color: #ff9500; }
.shop-tags {
  display: flex;
  gap: 16rpx;
  margin-top: 10rpx;
}
.tag {
  font-size: 22rpx;
  color: #999;
  background: #f5f5f5;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}
.empty {
  text-align: center;
  padding: 100rpx;
  color: #999;
}
</style>
