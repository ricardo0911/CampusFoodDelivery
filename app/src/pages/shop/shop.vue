<template>
  <view class="page">
    <!-- 店铺信息头部 -->
    <view class="shop-header">
      <image class="shop-banner" :src="shop.banner || shop.logo || '/static/default-shop.png'" mode="aspectFill" />
      <view class="shop-overlay"></view>
      
      <!-- 返回按钮 -->
      <view class="back-btn" @click="goBack">
        <text>←</text>
      </view>
      
      <!-- 店铺信息卡片 -->
      <view class="shop-card">
        <view class="shop-main">
          <image class="shop-avatar" :src="shop.logo || '/static/default-shop.png'" mode="aspectFill" />
          <view class="shop-info">
            <text class="shop-name">{{ shop.name || '美味餐厅' }}</text>
            <view class="shop-rating">
              <text class="rating-score">{{ shop.rating || 4.8 }}分</text>
              <text class="rating-text">月售{{ shop.monthlySales || 999 }}+</text>
            </view>
          </view>
          <view class="shop-badge">
            <text>品质商家</text>
          </view>
        </view>
        
        <view class="shop-delivery-info">
          <view class="delivery-tag">
            <text class="tag-icon">🚀</text>
            <text>{{ shop.deliveryTime || 30 }}分钟送达</text>
          </view>
          <view class="delivery-tag">
            <text class="tag-icon">💰</text>
            <text>起送¥{{ shop.minOrderAmount || 15 }}</text>
          </view>
          <view class="delivery-tag">
            <text class="tag-icon">🛵</text>
            <text>配送¥{{ shop.deliveryFee || 3 }}</text>
          </view>
        </view>
        
        <view class="promo-banner" v-if="shop.hasPromo">
          <text class="promo-icon">🎁</text>
          <text class="promo-text">满30减5，满50减10</text>
        </view>
      </view>
    </view>
    
    <!-- 菜品分类和列表 -->
    <view class="menu-container">
      <!-- 左侧分类 -->
      <scroll-view class="category-sidebar" scroll-y>
        <view 
          v-for="cat in categories" 
          :key="cat.id" 
          class="category-tab" 
          :class="{ active: currentCategory === cat.id }"
          @click="currentCategory = cat.id"
        >
          <view class="category-indicator" v-if="currentCategory === cat.id"></view>
          <text class="category-name">{{ cat.name }}</text>
          <text class="category-count">{{ getCategoryCount(cat.id) }}</text>
        </view>
      </scroll-view>
      
      <!-- 右侧菜品 -->
      <scroll-view class="dish-container" scroll-y>
        <view class="dish-section">
          <text class="section-title">{{ currentCategoryName }}</text>
          
          <view v-for="dish in filteredDishes" :key="dish.id" class="dish-card">
            <image class="dish-image" :src="dish.image || '/static/default-dish.png'" mode="aspectFill" />
            
            <view class="dish-content">
              <view class="dish-header">
                <text class="dish-name">{{ dish.name }}</text>
                <view class="dish-tags">
                  <text class="hot-tag" v-if="dish.isHot">🔥爆</text>
                  <text class="new-tag" v-if="dish.isNew">新</text>
                </view>
              </view>
              
              <text class="dish-desc">{{ dish.description || '精选食材，美味可口' }}</text>
              
              <view class="dish-sales">
                <text>月售{{ dish.monthlySales || 100 }}</text>
                <text class="dish-like">👍{{ dish.likeRate || 95 }}%</text>
              </view>
              
              <view class="dish-footer">
                <view class="price-area">
                  <text class="price-symbol">¥</text>
                  <text class="price-value">{{ dish.price }}</text>
                  <text class="original-price" v-if="dish.originalPrice">¥{{ dish.originalPrice }}</text>
                </view>
                
                <view class="quantity-control">
                  <view class="minus-btn" v-if="getCartQuantity(dish.id) > 0" @click="decreaseCart(dish)">
                    <text>−</text>
                  </view>
                  <text class="quantity-text" v-if="getCartQuantity(dish.id) > 0">{{ getCartQuantity(dish.id) }}</text>
                  <view class="plus-btn" @click="addToCart(dish)">
                    <text>+</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
    
    <!-- 底部购物车栏 -->
    <view class="cart-bar">
      <view class="cart-left" @click="toggleCartDetail">
        <view class="cart-icon-wrapper">
          <text class="cart-icon">🛒</text>
          <view class="cart-badge" v-if="cartCount > 0">{{ cartCount }}</view>
        </view>
        <view class="cart-price-info">
          <text class="cart-total" v-if="cartTotal > 0">¥{{ cartTotal.toFixed(2) }}</text>
          <text class="cart-empty" v-else>购物车是空的</text>
          <text class="cart-delivery">配送费¥{{ shop.deliveryFee || 3 }}</text>
        </view>
      </view>
      
      <view 
        class="checkout-btn" 
        :class="{ disabled: cartTotal < (shop.minOrderAmount || 15) }"
        @click="goCheckout"
      >
        <text v-if="cartTotal >= (shop.minOrderAmount || 15)">去结算</text>
        <text v-else>差¥{{ ((shop.minOrderAmount || 15) - cartTotal).toFixed(0) }}起送</text>
      </view>
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
const cartItems = ref({}) // { dishId: quantity }

const cartCount = computed(() => Object.values(cartItems.value).reduce((a, b) => a + b, 0))
const cartTotal = computed(() => {
  let total = 0
  for (const [dishId, qty] of Object.entries(cartItems.value)) {
    const dish = dishes.value.find(d => d.id == dishId)
    if (dish) total += dish.price * qty
  }
  return total
})

const currentCategoryName = computed(() => {
  const cat = categories.value.find(c => c.id === currentCategory.value)
  return cat?.name || '全部菜品'
})

const filteredDishes = computed(() => {
  if (!currentCategory.value) return dishes.value
  return dishes.value.filter(d => d.categoryId === currentCategory.value)
})

const getCategoryCount = (catId) => {
  return dishes.value.filter(d => d.categoryId === catId).length
}

const getCartQuantity = (dishId) => cartItems.value[dishId] || 0

const loadData = async () => {
  try {
    const res = await get(`/public/shop/${shopId.value}/menu`)
    shop.value = res.data.shop || {}
    categories.value = res.data.categories || []
    dishes.value = res.data.dishes || []
    if (categories.value.length > 0) {
      currentCategory.value = categories.value[0].id
    }
  } catch (e) {
    console.error(e)
    // 模拟数据
    shop.value = { name: '美味餐厅', rating: 4.8, monthlySales: 1234, minOrderAmount: 15, deliveryFee: 3, deliveryTime: 25, hasPromo: true }
    categories.value = [
      { id: 1, name: '热销' },
      { id: 2, name: '套餐' },
      { id: 3, name: '主食' },
      { id: 4, name: '小吃' },
      { id: 5, name: '饮品' },
    ]
    dishes.value = [
      { id: 1, categoryId: 1, name: '招牌黄焖鸡', price: 28, monthlySales: 356, isHot: true },
      { id: 2, categoryId: 1, name: '香辣鸡腿堡', price: 18, monthlySales: 234, isNew: true },
      { id: 3, categoryId: 2, name: '超值双人套餐', price: 49, originalPrice: 68, monthlySales: 189 },
      { id: 4, categoryId: 3, name: '扬州炒饭', price: 15, monthlySales: 421 },
      { id: 5, categoryId: 4, name: '香酥鸡米花', price: 12, monthlySales: 532 },
    ]
    currentCategory.value = 1
  }
}

const addToCart = async (dish) => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  cartItems.value[dish.id] = (cartItems.value[dish.id] || 0) + 1
  uni.showToast({ title: '已添加', icon: 'none', duration: 500 })
}

const decreaseCart = (dish) => {
  if (cartItems.value[dish.id] > 0) {
    cartItems.value[dish.id]--
    if (cartItems.value[dish.id] === 0) delete cartItems.value[dish.id]
  }
}

const goBack = () => uni.navigateBack()
const toggleCartDetail = () => {}
const goCheckout = () => {
  if (cartTotal.value >= (shop.value.minOrderAmount || 15)) {
    uni.switchTab({ url: '/pages/cart/cart' })
  }
}

onMounted(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  shopId.value = page.options?.id
  if (shopId.value) loadData()
  else loadData() // 仍加载模拟数据
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 140rpx;
}

/* 店铺头部 */
.shop-header {
  position: relative;
  height: 400rpx;
}

.shop-banner {
  width: 100%;
  height: 100%;
}

.shop-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.3) 0%, rgba(0,0,0,0.1) 100%);
}

.back-btn {
  position: absolute;
  top: calc(var(--status-bar-height) + 20rpx);
  left: 30rpx;
  width: 60rpx;
  height: 60rpx;
  background: rgba(0,0,0,0.4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 32rpx;
}

.shop-card {
  position: absolute;
  bottom: -80rpx;
  left: 24rpx;
  right: 24rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 8rpx 30rpx rgba(0,0,0,0.1);
  z-index: 10;
}

.shop-main {
  display: flex;
  align-items: center;
}

.shop-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 20rpx;
  margin-right: 20rpx;
}

.shop-info {
  flex: 1;
}

.shop-name {
  font-size: 34rpx;
  font-weight: bold;
  color: #1a1a2e;
}

.shop-rating {
  display: flex;
  align-items: center;
  margin-top: 8rpx;
}

.rating-score {
  font-size: 26rpx;
  color: #ff6b35;
  font-weight: bold;
}

.rating-text {
  font-size: 24rpx;
  color: #999;
  margin-left: 16rpx;
}

.shop-badge {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 20rpx;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
}

.shop-delivery-info {
  display: flex;
  justify-content: space-around;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
}

.delivery-tag {
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: #666;
}

.tag-icon {
  margin-right: 6rpx;
}

.promo-banner {
  display: flex;
  align-items: center;
  margin-top: 16rpx;
  padding: 16rpx;
  background: linear-gradient(135deg, #fff5f0, #fff0eb);
  border-radius: 12rpx;
}

.promo-icon {
  margin-right: 10rpx;
}

.promo-text {
  font-size: 24rpx;
  color: #ff6b35;
}

/* 菜单区域 */
.menu-container {
  display: flex;
  margin-top: 100rpx;
  height: calc(100vh - 540rpx);
}

.category-sidebar {
  width: 180rpx;
  background: #fff;
}

.category-tab {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28rpx 16rpx;
  transition: all 0.3s;
}

.category-tab.active {
  background: #f5f6fa;
}

.category-indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 6rpx;
  height: 40rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 0 6rpx 6rpx 0;
}

.category-name {
  font-size: 26rpx;
  color: #333;
}

.category-tab.active .category-name {
  color: #667eea;
  font-weight: bold;
}

.category-count {
  font-size: 20rpx;
  color: #999;
  margin-top: 4rpx;
}

/* 菜品列表 */
.dish-container {
  flex: 1;
  padding: 20rpx;
  background: #f5f6fa;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #1a1a2e;
  margin-bottom: 20rpx;
}

.dish-card {
  display: flex;
  background: #fff;
  border-radius: 20rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.03);
}

.dish-image {
  width: 180rpx;
  height: 180rpx;
  border-radius: 16rpx;
  margin-right: 20rpx;
}

.dish-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.dish-header {
  display: flex;
  align-items: center;
}

.dish-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #1a1a2e;
}

.dish-tags {
  display: flex;
  margin-left: 10rpx;
}

.hot-tag {
  font-size: 18rpx;
  color: #ff4444;
  background: #fff0f0;
  padding: 2rpx 8rpx;
  border-radius: 6rpx;
  margin-right: 6rpx;
}

.new-tag {
  font-size: 18rpx;
  color: #52c41a;
  background: #f6ffed;
  padding: 2rpx 8rpx;
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

.dish-sales {
  display: flex;
  align-items: center;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: #999;
}

.dish-like {
  margin-left: 20rpx;
}

.dish-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.price-area {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 24rpx;
  color: #ff4444;
}

.price-value {
  font-size: 36rpx;
  color: #ff4444;
  font-weight: bold;
}

.original-price {
  font-size: 22rpx;
  color: #999;
  text-decoration: line-through;
  margin-left: 10rpx;
}

.quantity-control {
  display: flex;
  align-items: center;
}

.minus-btn, .plus-btn {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
}

.minus-btn {
  background: #f0f0f0;
  color: #666;
}

.plus-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
}

.quantity-text {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  min-width: 50rpx;
  text-align: center;
}

/* 购物车栏 */
.cart-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 110rpx;
  background: #2a2a2a;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  padding-bottom: env(safe-area-inset-bottom);
}

.cart-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.cart-icon-wrapper {
  position: relative;
  width: 90rpx;
  height: 90rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: -40rpx;
  box-shadow: 0 4rpx 20rpx rgba(102, 126, 234, 0.4);
}

.cart-icon {
  font-size: 40rpx;
}

.cart-badge {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  min-width: 36rpx;
  height: 36rpx;
  background: #ff4444;
  color: #fff;
  font-size: 22rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
}

.cart-price-info {
  display: flex;
  flex-direction: column;
  margin-left: 20rpx;
}

.cart-total {
  font-size: 36rpx;
  color: #fff;
  font-weight: bold;
}

.cart-empty {
  font-size: 28rpx;
  color: #999;
}

.cart-delivery {
  font-size: 22rpx;
  color: #999;
}

.checkout-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  padding: 20rpx 50rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: bold;
}

.checkout-btn.disabled {
  background: #555;
  color: #999;
}
</style>
