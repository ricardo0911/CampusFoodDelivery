<template>
  <view class="page">
    <!-- 店铺信息头部 -->
    <view class="shop-header">
      <image class="shop-banner" :src="shop.banner || shop.logo || '/static/default-shop.jpg'" mode="aspectFill" />
      <view class="shop-overlay"></view>
      
      <!-- 返回按钮 -->
      <view class="back-btn" @click="goBack">
        <view class="back-arrow"></view>
      </view>
      
      <!-- 店铺信息卡片 -->
      <view class="shop-card">
        <view class="shop-main">
          <image class="shop-avatar" :src="shop.logo || '/static/default-shop.jpg'" mode="aspectFill" />
          <view class="shop-info">
            <text class="shop-name">{{ shop.name || '美味餐厅' }}</text>
            <view class="shop-rating">
              <view class="rating-stars">
                <view class="star filled" v-for="n in 5" :key="n"></view>
              </view>
              <text class="rating-score">{{ shop.rating || 4.8 }}分</text>
              <text class="rating-text">月售{{ shop.monthlySales || 999 }}+</text>
            </view>
          </view>
          <view class="shop-badge">
            <text>品质商家</text>
          </view>
        </view>
        
        <!-- 配送信息 - 使用CSS图标 -->
        <view class="shop-delivery-info">
          <view class="delivery-tag">
            <view class="icon-clock"></view>
            <text>{{ shop.deliveryTime || 30 }}分钟送达</text>
          </view>
          <view class="delivery-tag">
            <view class="icon-yen"></view>
            <text>起送¥{{ shop.minOrderAmount || 15 }}</text>
          </view>
          <view class="delivery-tag">
            <view class="icon-scooter"></view>
            <text>配送¥{{ shop.deliveryFee || 3 }}</text>
          </view>
        </view>
        
        <!-- 优惠活动 -->
        <view class="promo-banner" v-if="shop.hasPromo">
          <view class="promo-icon">
            <view class="icon-gift"></view>
          </view>
          <view class="promo-list">
            <view class="promo-item">
              <text class="promo-label">满减</text>
              <text class="promo-text">满30减5，满50减10</text>
            </view>
            <view class="promo-item">
              <text class="promo-label">首单</text>
              <text class="promo-text">新客立减3元</text>
            </view>
          </view>
          <view class="promo-arrow"></view>
        </view>
      </view>
    </view>
    
    <!-- 公告栏 -->
    <view class="notice-bar" v-if="shop.notice">
      <view class="notice-icon"></view>
      <text class="notice-text">{{ shop.notice }}</text>
    </view>
    
    <!-- 菜品分类和列表 -->
    <view class="menu-container">
      <!-- 左侧分类导航 -->
      <scroll-view class="category-sidebar" scroll-y :scroll-into-view="'cat-' + currentCategory" scroll-with-animation>
        <view 
          v-for="cat in categories" 
          :key="cat.id"
          :id="'cat-' + cat.id"
          class="category-tab" 
          :class="{ active: currentCategory === cat.id }"
          @click="switchCategory(cat.id)"
        >
          <view class="category-indicator"></view>
          <text class="category-name">{{ cat.name }}</text>
          <view class="category-badge" v-if="getCategoryCartCount(cat.id) > 0">
            {{ getCategoryCartCount(cat.id) }}
          </view>
        </view>
      </scroll-view>
      
      <!-- 右侧菜品列表 -->
      <scroll-view 
        class="dish-container" 
        scroll-y 
        :scroll-into-view="'section-' + currentCategory"
        @scroll="onDishScroll"
        scroll-with-animation
      >
        <view v-for="cat in categories" :key="cat.id" :id="'section-' + cat.id" class="dish-section">
          <view class="section-header">
            <text class="section-title">{{ cat.name }}</text>
            <text class="section-subtitle">{{ cat.description || '美味推荐' }}</text>
          </view>
          
          <CDishCard
            v-for="dish in getDishesByCategory(cat.id)"
            :key="dish.id"
            :id="dish.id"
            :name="dish.name"
            :description="dish.description || '精选食材，美味可口'"
            :image="dish.image || '/static/default-dish.jpg'"
            :price="dish.price"
            :original-price="dish.originalPrice || 0"
            :monthly-sales="dish.monthlySales || 100"
            :rating="dish.likeRate || 95"
            :count="getCartQuantity(dish.id)"
            :show-add-btn="true"
            @add="() => addToCart(dish)"
            @change="(data) => handleDishCountChange(dish, data.count)"
          />
        </view>
        
        <!-- 底部占位 -->
        <view class="dish-section-bottom"></view>
      </scroll-view>
    </view>
    
    <!-- 购物车浮球 -->
    <CCartFloat
      :total-count="cartCount"
      :total-price="cartTotal"
      :offset-y="200"
      @click="toggleCartDetail"
    />
    
    <!-- 购物车详情弹窗 -->
    <view class="cart-detail-mask" v-if="cartExpanded" @click="toggleCartDetail"></view>
    <view class="cart-detail" v-if="cartExpanded">
      <view class="cart-detail-header">
        <text class="cart-detail-title">已选商品</text>
        <view class="clear-cart" @click="clearCart">
          <view class="trash-icon"></view>
          <text>清空</text>
        </view>
      </view>
      <scroll-view class="cart-detail-list" scroll-y>
        <view v-for="item in cartDetailList" :key="item.dishId" class="cart-detail-item">
          <image class="cart-item-image" :src="item.dishImage" mode="aspectFill" />
          <view class="cart-item-info">
            <text class="cart-item-name">{{ item.dishName }}</text>
            <text class="cart-item-price">¥{{ item.unitPrice }}</text>
          </view>
          <view class="quantity-control small">
            <view class="btn-minus" @click="decreaseCartById(item.dishId)">
              <view class="minus-icon"></view>
            </view>
            <text class="quantity-text">{{ item.quantity }}</text>
            <view class="btn-plus" @click="addToCartById(item.dishId)">
              <view class="plus-icon"></view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
    
    <!-- 添加商品动画 -->
    <view 
      class="fly-item" 
      v-if="flyItem.show"
      :style="{
        left: flyItem.x + 'px',
        top: flyItem.y + 'px'
      }"
    >
      <view class="fly-ball"></view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { get, post } from '@/utils/request'
import { CDishCard, CCartFloat, CTag } from '@/components/common'

const shopId = ref('')
const shop = ref({})
const categories = ref([])
const dishes = ref([])
const currentCategory = ref(null)
const cartItems = ref({})
const cartExpanded = ref(false)
const animatingPlus = ref(null)
const animatingMinus = ref(null)
const cartShaking = ref(false)
const cartPulse = ref(false)

// 飞入动画
const flyItem = ref({
  show: false,
  x: 0,
  y: 0,
  targetX: 0,
  targetY: 0
})

const cartCount = computed(() => Object.values(cartItems.value).reduce((a, b) => a + b, 0))

const cartTotal = computed(() => {
  let total = 0
  for (const [dishId, qty] of Object.entries(cartItems.value)) {
    const dish = dishes.value.find(d => d.id == dishId)
    if (dish) total += dish.price * qty
  }
  return total
})

const cartDetailList = computed(() => {
  const list = []
  for (const [dishId, qty] of Object.entries(cartItems.value)) {
    const dish = dishes.value.find(d => d.id == dishId)
    if (dish && qty > 0) {
      list.push({
        dishId: dish.id,
        dishName: dish.name,
        dishImage: dish.image || '/static/default-dish.jpg',
        unitPrice: dish.price,
        quantity: qty
      })
    }
  }
  return list
})

const getDishesByCategory = (catId) => {
  return dishes.value.filter(d => d.categoryId === catId)
}

const getCategoryCartCount = (catId) => {
  const catDishes = dishes.value.filter(d => d.categoryId === catId)
  let count = 0
  catDishes.forEach(d => {
    count += cartItems.value[d.id] || 0
  })
  return count
}

const getCartQuantity = (dishId) => cartItems.value[dishId] || 0

const switchCategory = (catId) => {
  currentCategory.value = catId
}

const onDishScroll = (e) => {
  // 简化处理，实际应该计算每个section的位置
}

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
    shop.value = { 
      name: '美味餐厅', 
      rating: 4.8, 
      monthlySales: 1234, 
      minOrderAmount: 15, 
      deliveryFee: 3, 
      deliveryTime: 25, 
      hasPromo: true,
      notice: '欢迎光临本店，我们承诺30分钟内送达！'
    }
    categories.value = [
      { id: 1, name: '热销推荐', description: '本店最受欢迎' },
      { id: 2, name: '优惠套餐', description: '超值组合' },
      { id: 3, name: '精品主食', description: '美味主食' },
      { id: 4, name: '特色小吃', description: '地方风味' },
      { id: 5, name: '清凉饮品', description: '解腻爽口' },
      { id: 6, name: '美味汤品', description: '暖胃暖心' },
    ]
    dishes.value = [
      { id: 1, categoryId: 1, name: '招牌黄焖鸡米饭', price: 28, originalPrice: 35, monthlySales: 356, isHot: true, likeRate: 98, description: '秘制酱料，鲜香入味' },
      { id: 2, categoryId: 1, name: '香辣鸡腿堡套餐', price: 18, monthlySales: 234, isNew: true, likeRate: 96, description: '酥脆多汁，搭配可乐' },
      { id: 3, categoryId: 1, name: '经典红烧肉', price: 32, monthlySales: 189, isHot: true, likeRate: 95, description: '肥而不腻，入口即化' },
      { id: 4, categoryId: 2, name: '超值双人套餐', price: 49, originalPrice: 68, monthlySales: 89, likeRate: 94, description: '两荤两素，超值享受' },
      { id: 5, categoryId: 2, name: '家庭三人套餐', price: 78, originalPrice: 98, monthlySales: 67, isHot: true, likeRate: 97, description: '三荤三素，全家分享' },
      { id: 6, categoryId: 3, name: '扬州炒饭', price: 15, monthlySales: 421, likeRate: 93, description: '粒粒分明，香气四溢' },
      { id: 7, categoryId: 3, name: '牛肉盖浇饭', price: 22, monthlySales: 312, isNew: true, likeRate: 95, description: '鲜嫩牛肉，酱汁浓郁' },
      { id: 8, categoryId: 4, name: '香酥鸡米花', price: 12, monthlySales: 532, likeRate: 94, description: '外酥里嫩，一口一个' },
      { id: 9, categoryId: 4, name: '黄金薯条', price: 8, monthlySales: 445, likeRate: 92, description: '精选土豆，炸至金黄' },
      { id: 10, categoryId: 5, name: '冰镇可乐', price: 5, monthlySales: 678, likeRate: 96, description: '清爽解渴，气泡十足' },
      { id: 11, categoryId: 5, name: '鲜榨橙汁', price: 12, monthlySales: 234, isNew: true, likeRate: 95, description: '100%鲜榨，维C满满' },
      { id: 12, categoryId: 6, name: '紫菜蛋花汤', price: 6, monthlySales: 389, likeRate: 91, description: '清淡鲜美，营养丰富' },
    ]
    currentCategory.value = 1
  }
}

const addToCart = async (dish, event) => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  
  // 触发按钮动画
  animatingPlus.value = dish.id
  setTimeout(() => animatingPlus.value = null, 200)
  
  // 购物车脉冲动画
  cartPulse.value = true
  setTimeout(() => cartPulse.value = false, 300)
  
  // 购物车抖动动画（首次添加）
  if (cartCount.value === 0) {
    cartShaking.value = true
    setTimeout(() => cartShaking.value = false, 500)
  }
  
  cartItems.value[dish.id] = (cartItems.value[dish.id] || 0) + 1
  saveCartToStorage()
  
  // 显示添加提示
  uni.showToast({ title: '已加入购物车', icon: 'none', duration: 800 })
}

const decreaseCart = (dish) => {
  if (cartItems.value[dish.id] > 0) {
    animatingMinus.value = dish.id
    setTimeout(() => animatingMinus.value = null, 200)
    
    cartItems.value[dish.id]--
    if (cartItems.value[dish.id] === 0) delete cartItems.value[dish.id]
    saveCartToStorage()
  }
}

// 处理菜品数量变化（来自 CDishCard 组件）
const handleDishCountChange = (dish, count) => {
  if (count === 0) {
    delete cartItems.value[dish.id]
  } else {
    cartItems.value[dish.id] = count
  }
  saveCartToStorage()
}

const addToCartById = (dishId) => {
  const dish = dishes.value.find(d => d.id == dishId)
  if (dish) addToCart(dish)
}

const decreaseCartById = (dishId) => {
  const dish = dishes.value.find(d => d.id == dishId)
  if (dish) decreaseCart(dish)
}

const clearCart = () => {
  uni.showModal({
    title: '提示',
    content: '确定清空购物车吗？',
    success: (res) => {
      if (res.confirm) {
        cartItems.value = {}
        saveCartToStorage()
        cartExpanded.value = false
      }
    }
  })
}

const saveCartToStorage = () => {
  const cartData = []
  for (const [dishId, qty] of Object.entries(cartItems.value)) {
    const dish = dishes.value.find(d => d.id == dishId)
    if (dish && qty > 0) {
      cartData.push({
        id: Date.now() + parseInt(dishId),
        dishId: dish.id,
        dishName: dish.name,
        dishImage: dish.image || '/static/default-dish.jpg',
        unitPrice: dish.price,
        quantity: qty,
        selected: true,
        shopId: shopId.value,
        shopName: shop.value.name
      })
    }
  }
  uni.setStorageSync('cartList', cartData)
}

const toggleCartDetail = () => {
  if (cartCount.value > 0) {
    cartExpanded.value = !cartExpanded.value
  }
}

const goBack = () => uni.navigateBack()

const goCheckout = () => {
  if (cartTotal.value >= (shop.value.minOrderAmount || 15)) {
    saveCartToStorage()
    uni.switchTab({ url: '/pages/cart/cart' })
  }
}

onMounted(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  shopId.value = page.options?.id
  if (shopId.value) loadData()
  else loadData()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f8f9fa;
  position: relative;
}

/* ========== 店铺头部 ========== */
.shop-header {
  position: relative;
  height: 380rpx;
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
  background: linear-gradient(180deg, rgba(0,0,0,0.4) 0%, rgba(0,0,0,0.1) 50%, rgba(0,0,0,0) 100%);
}

/* 返回按钮 */
.back-btn {
  position: absolute;
  top: calc(var(--status-bar-height) + 20rpx);
  left: 24rpx;
  width: 64rpx;
  height: 64rpx;
  background: rgba(0,0,0,0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10rpx);
}

.back-arrow {
  width: 20rpx;
  height: 20rpx;
  border-left: 4rpx solid #fff;
  border-bottom: 4rpx solid #fff;
  transform: rotate(45deg);
  margin-left: 6rpx;
}

/* 店铺信息卡片 */
.shop-card {
  position: absolute;
  bottom: -100rpx;
  left: 24rpx;
  right: 24rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 8rpx 40rpx rgba(0,0,0,0.12);
  z-index: 10;
}

.shop-main {
  display: flex;
  align-items: center;
}

.shop-avatar {
  width: 110rpx;
  height: 110rpx;
  border-radius: 16rpx;
  margin-right: 24rpx;
  border: 2rpx solid #f0f0f0;
}

.shop-info {
  flex: 1;
}

.shop-name {
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.3;
}

.shop-rating {
  display: flex;
  align-items: center;
  margin-top: 12rpx;
}

.rating-stars {
  display: flex;
  margin-right: 12rpx;
}

.star {
  width: 20rpx;
  height: 20rpx;
  margin-right: 4rpx;
  position: relative;
}

.star.filled::before {
  content: '';
  position: absolute;
  width: 0;
  height: 0;
  border-left: 10rpx solid transparent;
  border-right: 10rpx solid transparent;
  border-bottom: 14rpx solid #ffb800;
  transform: rotate(0deg);
}

.star.filled::after {
  content: '';
  position: absolute;
  top: 6rpx;
  left: 0;
  width: 0;
  height: 0;
  border-left: 10rpx solid transparent;
  border-right: 10rpx solid transparent;
  border-top: 14rpx solid #ffb800;
  transform: rotate(0deg);
}

.rating-score {
  font-size: 26rpx;
  color: #ff6b00;
  font-weight: 600;
}

.rating-text {
  font-size: 24rpx;
  color: #999;
  margin-left: 16rpx;
}

.shop-badge {
  background: linear-gradient(135deg, #ff6b35, #ff9500);
  color: #fff;
  font-size: 20rpx;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-weight: 500;
}

/* 配送信息 */
.shop-delivery-info {
  display: flex;
  justify-content: flex-start;
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid #f5f5f5;
  gap: 40rpx;
}

.delivery-tag {
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: #666;
}

/* CSS绘制的图标 */
.icon-clock {
  width: 24rpx;
  height: 24rpx;
  border: 3rpx solid #999;
  border-radius: 50%;
  margin-right: 8rpx;
  position: relative;
}

.icon-clock::before {
  content: '';
  position: absolute;
  width: 2rpx;
  height: 8rpx;
  background: #999;
  top: 4rpx;
  left: 50%;
  transform: translateX(-50%);
}

.icon-clock::after {
  content: '';
  position: absolute;
  width: 6rpx;
  height: 2rpx;
  background: #999;
  top: 50%;
  left: 50%;
  transform: translateY(-50%);
}

.icon-yen {
  width: 24rpx;
  height: 24rpx;
  margin-right: 8rpx;
  position: relative;
}

.icon-yen::before {
  content: '¥';
  font-size: 22rpx;
  color: #ff6b35;
  font-weight: bold;
}

.icon-scooter {
  width: 28rpx;
  height: 20rpx;
  margin-right: 8rpx;
  position: relative;
}

.icon-scooter::before {
  content: '';
  position: absolute;
  width: 20rpx;
  height: 12rpx;
  border: 2rpx solid #999;
  border-radius: 4rpx;
  top: 0;
  left: 0;
}

.icon-scooter::after {
  content: '';
  position: absolute;
  width: 6rpx;
  height: 6rpx;
  border: 2rpx solid #999;
  border-radius: 50%;
  bottom: 0;
  left: 4rpx;
  box-shadow: 10rpx 0 0 -2rpx #999;
}

/* 优惠活动 */
.promo-banner {
  display: flex;
  align-items: flex-start;
  margin-top: 20rpx;
  padding: 20rpx;
  background: linear-gradient(135deg, #fff8f5, #fff0eb);
  border-radius: 16rpx;
}

.promo-icon {
  margin-right: 16rpx;
  flex-shrink: 0;
}

.icon-gift {
  width: 32rpx;
  height: 32rpx;
  position: relative;
}

.icon-gift::before {
  content: '';
  position: absolute;
  width: 24rpx;
  height: 16rpx;
  border: 2rpx solid #ff6b35;
  border-radius: 2rpx;
  top: 12rpx;
  left: 4rpx;
}

.icon-gift::after {
  content: '';
  position: absolute;
  width: 8rpx;
  height: 8rpx;
  border: 2rpx solid #ff6b35;
  border-radius: 50%;
  top: 4rpx;
  left: 12rpx;
}

.promo-list {
  flex: 1;
}

.promo-item {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.promo-item:last-child {
  margin-bottom: 0;
}

.promo-label {
  background: linear-gradient(135deg, #ff6b35, #ff9500);
  color: #fff;
  font-size: 18rpx;
  padding: 4rpx 10rpx;
  border-radius: 6rpx;
  margin-right: 12rpx;
  font-weight: 500;
}

.promo-text {
  font-size: 24rpx;
  color: #666;
}

.promo-arrow {
  width: 12rpx;
  height: 12rpx;
  border-right: 2rpx solid #ccc;
  border-bottom: 2rpx solid #ccc;
  transform: rotate(-45deg);
  margin-left: 16rpx;
  flex-shrink: 0;
}

/* 公告栏 */
.notice-bar {
  margin-top: 120rpx;
  margin-left: 24rpx;
  margin-right: 24rpx;
  background: #fffbe6;
  border: 1rpx solid #ffe58f;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  display: flex;
  align-items: center;
}

.notice-icon {
  width: 24rpx;
  height: 24rpx;
  border: 2rpx solid #faad14;
  border-radius: 50%;
  margin-right: 12rpx;
  position: relative;
  flex-shrink: 0;
}

.notice-icon::before {
  content: '!';
  position: absolute;
  font-size: 16rpx;
  color: #faad14;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-weight: bold;
}

.notice-text {
  font-size: 24rpx;
  color: #ad6800;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ========== 菜单区域 ========== */
.menu-container {
  display: flex;
  margin-top: 20rpx;
  height: calc(100vh - 580rpx);
}

/* 左侧分类导航 */
.category-sidebar {
  width: 190rpx;
  background: #f5f5f5;
}

.category-tab {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32rpx 16rpx;
  transition: all 0.3s ease;
}

.category-tab.active {
  background: #fff;
}

.category-indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 6rpx;
  height: 36rpx;
  background: linear-gradient(180deg, #ff6b35, #ff9500);
  border-radius: 0 6rpx 6rpx 0;
  opacity: 0;
  transition: opacity 0.3s;
}

.category-tab.active .category-indicator {
  opacity: 1;
}

.category-name {
  font-size: 26rpx;
  color: #666;
  transition: all 0.3s;
}

.category-tab.active .category-name {
  color: #1a1a2e;
  font-weight: 600;
}

.category-badge {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  min-width: 28rpx;
  height: 28rpx;
  background: #ff4444;
  color: #fff;
  font-size: 18rpx;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6rpx;
  font-weight: 500;
}

/* 右侧菜品列表 */
.dish-container {
  flex: 1;
  background: #fff;
  padding: 0 24rpx;
}

.dish-section {
  padding-top: 24rpx;
}

.section-header {
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1a2e;
}

.section-subtitle {
  font-size: 22rpx;
  color: #999;
  margin-left: 16rpx;
}

.dish-section-bottom {
  height: 140rpx;
}

/* 菜品卡片 */
.dish-card {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx 0;
  margin-bottom: 16rpx;
  position: relative;
}

.dish-card.dish-hot {
  background: linear-gradient(135deg, #fff, #fff8f5);
}

.dish-image-wrapper {
  position: relative;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.dish-image {
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
  object-fit: cover;
}

.dish-image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0) 60%, rgba(0,0,0,0.3) 100%);
  border-radius: 16rpx;
  display: flex;
  align-items: flex-end;
  justify-content: flex-start;
  padding: 12rpx;
}

.hot-badge {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #ff4444, #ff6b35);
  color: #fff;
  font-size: 20rpx;
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
  font-weight: 500;
}

.fire-icon {
  width: 16rpx;
  height: 16rpx;
  background: linear-gradient(180deg, #ffeb3b, #ff6b35);
  border-radius: 0 50% 50% 50%;
  transform: rotate(45deg);
  margin-right: 6rpx;
}

.dish-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.dish-header {
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 8rpx;
}

.dish-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #1a1a2e;
  line-height: 1.3;
}

.dish-tags {
  display: flex;
  gap: 8rpx;
}

.tag {
  font-size: 18rpx;
  padding: 4rpx 10rpx;
  border-radius: 6rpx;
  font-weight: 500;
}

.tag.hot-tag {
  color: #fff;
  background: linear-gradient(135deg, #ff4444, #ff6b35);
}

.tag.new-tag {
  color: #52c41a;
  background: #f6ffed;
  border: 1rpx solid #b7eb8f;
}

.tag.limit-tag {
  color: #ff6b35;
  background: #fff7e6;
  border: 1rpx solid #ffd591;
}

.dish-desc {
  font-size: 24rpx;
  color: #999;
  margin-top: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-sales {
  display: flex;
  align-items: center;
  margin-top: 12rpx;
  font-size: 22rpx;
  color: #999;
}

.divider {
  margin: 0 12rpx;
  color: #ddd;
}

.dish-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 16rpx;
}

.price-area {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 24rpx;
  color: #ff4444;
  font-weight: 600;
}

.price-value {
  font-size: 40rpx;
  color: #ff4444;
  font-weight: 700;
}

.original-price {
  font-size: 24rpx;
  color: #bbb;
  text-decoration: line-through;
  margin-left: 12rpx;
}

/* 数量控制按钮 */
.quantity-control {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.quantity-control.small {
  gap: 12rpx;
}

.btn-minus {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.btn-minus:active {
  background: #e8e8e8;
  transform: scale(0.95);
}

.btn-minus.animating {
  animation: btnPress 0.2s ease;
}

.minus-icon {
  width: 20rpx;
  height: 3rpx;
  background: #666;
  border-radius: 2rpx;
}

.btn-plus {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b35, #ff9500);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);
  transition: all 0.2s;
}

.btn-plus:active {
  transform: scale(0.95);
  box-shadow: 0 2rpx 8rpx rgba(255, 107, 53, 0.3);
}

.btn-plus.animating {
  animation: btnPress 0.2s ease;
}

.plus-icon {
  width: 20rpx;
  height: 3rpx;
  background: #fff;
  border-radius: 2rpx;
  position: relative;
}

.plus-icon::after {
  content: '';
  position: absolute;
  width: 3rpx;
  height: 20rpx;
  background: #fff;
  border-radius: 2rpx;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.quantity-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  min-width: 40rpx;
  text-align: center;
}

.quantity-control.small .btn-minus,
.quantity-control.small .btn-plus {
  width: 44rpx;
  height: 44rpx;
}

.quantity-control.small .minus-icon {
  width: 16rpx;
}

.quantity-control.small .plus-icon {
  width: 16rpx;
}

.quantity-control.small .plus-icon::after {
  height: 16rpx;
}

.quantity-control.small .quantity-text {
  font-size: 26rpx;
  min-width: 36rpx;
}

@keyframes btnPress {
  0% { transform: scale(1); }
  50% { transform: scale(0.85); }
  100% { transform: scale(1); }
}

/* ========== 购物车栏 ========== */
.cart-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 110rpx;
  background: #2d2d2d;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 100;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.1);
}

.cart-main {
  display: flex;
  align-items: center;
  flex: 1;
  height: 100%;
}

/* 购物车图标 */
.cart-icon-wrapper {
  position: relative;
  width: 100rpx;
  height: 100rpx;
  background: #4a4a4a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: -30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.3);
  transition: all 0.3s;
}

.cart-icon-wrapper.has-items {
  background: linear-gradient(135deg, #ff6b35, #ff9500);
}

.cart-icon-wrapper.shake {
  animation: cartShake 0.5s ease;
}

@keyframes cartShake {
  0%, 100% { transform: rotate(0deg); }
  20% { transform: rotate(-8deg); }
  40% { transform: rotate(8deg); }
  60% { transform: rotate(-4deg); }
  80% { transform: rotate(4deg); }
}

/* CSS绘制的购物车图标 */
.cart-bag {
  position: relative;
  width: 44rpx;
  height: 40rpx;
}

.cart-bag-body {
  position: absolute;
  bottom: 0;
  width: 44rpx;
  height: 32rpx;
  border: 3rpx solid #fff;
  border-radius: 0 0 8rpx 8rpx;
  background: transparent;
}

.cart-bag-handle {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20rpx;
  height: 12rpx;
  border: 3rpx solid #fff;
  border-bottom: none;
  border-radius: 10rpx 10rpx 0 0;
}

.cart-badge {
  position: absolute;
  top: -6rpx;
  right: -6rpx;
  min-width: 36rpx;
  height: 36rpx;
  background: #ff4444;
  color: #fff;
  font-size: 22rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 10rpx;
  font-weight: 600;
  box-shadow: 0 2rpx 8rpx rgba(255, 68, 68, 0.4);
}

.cart-badge-pulse {
  position: absolute;
  top: -6rpx;
  right: -6rpx;
  width: 36rpx;
  height: 36rpx;
  background: #ff4444;
  border-radius: 50%;
  animation: badgePulse 0.3s ease;
}

@keyframes badgePulse {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(1.8); opacity: 0; }
}

.cart-price-info {
  display: flex;
  flex-direction: column;
  margin-left: 24rpx;
}

.cart-price-row {
  display: flex;
  align-items: baseline;
}

.cart-total .price-symbol {
  font-size: 24rpx;
  color: #fff;
  font-weight: 500;
}

.cart-total .price-value {
  font-size: 44rpx;
  color: #fff;
  font-weight: 700;
}

.cart-empty-text {
  font-size: 32rpx;
  color: #999;
  font-weight: 500;
}

.cart-delivery-info {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
}

/* 结算按钮 */
.checkout-btn {
  background: #4a4a4a;
  color: #999;
  padding: 0 40rpx;
  height: 76rpx;
  border-radius: 38rpx;
  font-size: 28rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.checkout-btn.disabled {
  background: #4a4a4a;
  color: #999;
}

.checkout-btn.reachable {
  background: linear-gradient(135deg, #ff6b35, #ff9500);
  color: #fff;
  box-shadow: 0 4rpx 16rpx rgba(255, 107, 53, 0.4);
}

.checkout-btn.reachable:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 12rpx rgba(255, 107, 53, 0.3);
}

.diff-text {
  font-size: 24rpx;
}

.diff-amount {
  font-size: 32rpx;
  font-weight: 700;
  margin: 0 4rpx;
}

/* ========== 购物车详情弹窗 ========== */
.cart-detail-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 98;
}

.cart-detail {
  position: fixed;
  left: 0;
  right: 0;
  bottom: calc(110rpx + env(safe-area-inset-bottom));
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  max-height: 60vh;
  z-index: 99;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.cart-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.cart-detail-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.clear-cart {
  display: flex;
  align-items: center;
  color: #999;
  font-size: 24rpx;
}

.trash-icon {
  width: 24rpx;
  height: 28rpx;
  border: 2rpx solid #999;
  border-radius: 0 0 4rpx 4rpx;
  margin-right: 8rpx;
  position: relative;
}

.trash-icon::before {
  content: '';
  position: absolute;
  top: -6rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 16rpx;
  height: 4rpx;
  border: 2rpx solid #999;
  border-bottom: none;
  border-radius: 4rpx 4rpx 0 0;
}

.cart-detail-list {
  max-height: calc(60vh - 100rpx);
  padding: 20rpx 30rpx;
}

.cart-detail-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.cart-detail-item:last-child {
  border-bottom: none;
}

.cart-item-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 12rpx;
  margin-right: 20rpx;
}

.cart-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.cart-item-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 8rpx;
}

.cart-item-price {
  font-size: 28rpx;
  color: #ff4444;
  font-weight: 600;
}

/* 飞入动画 */
.fly-item {
  position: fixed;
  z-index: 200;
  pointer-events: none;
}

.fly-ball {
  width: 40rpx;
  height: 40rpx;
  background: linear-gradient(135deg, #ff6b35, #ff9500);
  border-radius: 50%;
  animation: flyToCart 0.6s ease-in-out forwards;
}

@keyframes flyToCart {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(0.3);
    opacity: 0;
  }
}
</style>
