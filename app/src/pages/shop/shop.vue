<template>
  <view class="container">
    <!-- 店铺信息 -->
    <view class="shop-header">
      <image class="shop-banner" :src="shop.banner || '/static/default-banner.png'" mode="aspectFill" />
      <view class="shop-info">
        <text class="shop-name">{{ shop.name }}</text>
        <text class="shop-desc">{{ shop.description }}</text>
        <view class="shop-meta">
          <text>⭐ {{ shop.rating }}</text>
          <text>月售{{ shop.monthlySales }}</text>
          <text>起送¥{{ shop.minOrderAmount }}</text>
        </view>
      </view>
    </view>
    
    <!-- 菜品分类和列表 -->
    <view class="menu-container">
      <scroll-view class="category-list" scroll-y>
        <view v-for="cat in categories" :key="cat.id" 
              class="category-item" :class="{ active: currentCategory === cat.id }"
              @click="currentCategory = cat.id">
          {{ cat.name }}
        </view>
      </scroll-view>
      
      <scroll-view class="dish-list" scroll-y>
        <view v-for="dish in filteredDishes" :key="dish.id" class="dish-item">
          <image class="dish-image" :src="dish.image || '/static/default-dish.png'" mode="aspectFill" />
          <view class="dish-info">
            <text class="dish-name">{{ dish.name }}</text>
            <text class="dish-desc">{{ dish.description }}</text>
            <view class="dish-bottom">
              <text class="dish-price">¥{{ dish.price }}</text>
              <view class="add-btn" @click="addToCart(dish)">+</view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
    
    <!-- 购物车浮层 -->
    <view class="cart-bar" v-if="cartTotal > 0" @click="goCart">
      <view class="cart-info">
        <text class="cart-count">{{ cartCount }}件</text>
        <text class="cart-total">¥{{ cartTotal }}</text>
      </view>
      <view class="checkout-btn">去结算</view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { get, post } from '@/utils/request'

const shopId = ref('')
const shop = ref({})
const categories = ref([])
const dishes = ref([])
const currentCategory = ref(null)
const cartCount = ref(0)
const cartTotal = ref(0)

const filteredDishes = computed(() => {
  if (!currentCategory.value) return dishes.value
  return dishes.value.filter(d => d.categoryId === currentCategory.value)
})

const loadData = async () => {
  try {
    const res = await get(`/public/shop/${shopId.value}/menu`)
    shop.value = res.data.shop
    categories.value = res.data.categories
    dishes.value = res.data.dishes
    if (categories.value.length > 0) {
      currentCategory.value = categories.value[0].id
    }
  } catch (e) { console.error(e) }
}

const addToCart = async (dish) => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  try {
    await post('/customer/cart/add', { dishId: dish.id, quantity: 1 })
    cartCount.value++
    cartTotal.value += Number(dish.price)
    uni.showToast({ title: '已加入购物车', icon: 'success' })
  } catch (e) { console.error(e) }
}

const goCart = () => { uni.switchTab({ url: '/pages/cart/cart' }) }

onMounted(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  shopId.value = page.options?.id
  if (shopId.value) loadData()
})
</script>

<style scoped>
.shop-header { background: #fff; }
.shop-banner { width: 100%; height: 300rpx; }
.shop-info { padding: 20rpx; }
.shop-name { font-size: 36rpx; font-weight: bold; }
.shop-desc { font-size: 24rpx; color: #666; margin-top: 10rpx; }
.shop-meta { display: flex; gap: 20rpx; margin-top: 10rpx; font-size: 24rpx; color: #999; }
.menu-container { display: flex; height: calc(100vh - 450rpx); }
.category-list { width: 180rpx; background: #f5f5f5; }
.category-item { padding: 30rpx 20rpx; font-size: 28rpx; text-align: center; }
.category-item.active { background: #fff; color: #667eea; font-weight: bold; }
.dish-list { flex: 1; background: #fff; padding: 20rpx; }
.dish-item { display: flex; padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.dish-image { width: 140rpx; height: 140rpx; border-radius: 12rpx; margin-right: 20rpx; }
.dish-info { flex: 1; }
.dish-name { font-size: 30rpx; font-weight: bold; }
.dish-desc { font-size: 24rpx; color: #999; margin-top: 8rpx; }
.dish-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 16rpx; }
.dish-price { font-size: 32rpx; color: #ff4444; font-weight: bold; }
.add-btn {
  width: 50rpx; height: 50rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 36rpx;
}
.cart-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  height: 100rpx; background: #333;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 30rpx;
}
.cart-info { color: #fff; }
.cart-count { margin-right: 20rpx; }
.cart-total { font-size: 36rpx; font-weight: bold; }
.checkout-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; padding: 16rpx 40rpx;
  border-radius: 40rpx; font-size: 28rpx;
}
</style>
