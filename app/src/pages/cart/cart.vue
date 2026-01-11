<template>
  <view class="page">
    <!-- 简洁头部 -->
    <view class="header">
      <view class="header-left">
        <text class="header-title">购物车</text>
        <text class="header-count" v-if="cartList.length > 0">({{ cartList.length }})</text>
      </view>
      <text class="header-action" v-if="cartList.length > 0" @click="clearCart">清空</text>
    </view>
    
    <!-- 购物车内容 -->
    <scroll-view class="cart-content" scroll-y v-if="cartList.length > 0">
      <!-- 商品列表 -->
      <view class="cart-list">
        <view class="cart-item" v-for="item in cartList" :key="item.id">
          <!-- 左侧：选择框 + 图片 -->
          <view class="item-left">
            <view class="item-check" :class="{ checked: item.selected }" @click="toggleSelect(item)">
              <text v-if="item.selected">✓</text>
            </view>
            <image class="item-image" :src="item.dishImage || '/static/default-dish.jpg'" mode="aspectFill" />
          </view>
          
          <!-- 右侧：商品信息 -->
          <view class="item-right">
            <view class="item-info">
              <text class="item-name">{{ item.dishName }}</text>
              <text class="item-spec" v-if="item.spec">{{ item.spec }}</text>
            </view>
            
            <view class="item-bottom">
              <view class="price-area">
                <text class="price-symbol">¥</text>
                <text class="price-value">{{ item.unitPrice }}</text>
              </view>
              
              <view class="quantity-control">
                <view class="qty-btn minus" @click="updateQuantity(item, -1)">
                  <text>−</text>
                </view>
                <text class="qty-num">{{ item.quantity }}</text>
                <view class="qty-btn plus" @click="updateQuantity(item, 1)">
                  <text>+</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 配送信息卡片 -->
      <view class="delivery-card">
        <view class="delivery-row">
          <text class="delivery-icon">🚴</text>
          <text class="delivery-text">预计30分钟送达</text>
          <text class="delivery-fee">配送费 ¥3.00</text>
        </view>
      </view>
      
      <!-- 优惠信息 -->
      <view class="promo-card">
        <view class="promo-row" @click="selectCoupon">
          <view class="promo-left">
            <text class="promo-icon">🎫</text>
            <text class="promo-label">优惠券</text>
          </view>
          <view class="promo-right">
            <text class="promo-value">暂无可用</text>
            <text class="promo-arrow">›</text>
          </view>
        </view>
      </view>
      
      <view class="safe-bottom"></view>
    </scroll-view>
    
    <!-- 空购物车 -->
    <view class="empty-state" v-else>
      <image class="empty-image" src="/static/empty-cart.png" mode="aspectFit" />
      <text class="empty-title">购物车是空的</text>
      <text class="empty-desc">去挑选心仪的美食吧</text>
      <view class="empty-btn" @click="goIndex">去首页看看</view>
    </view>
    
    <!-- 底部结算栏 -->
    <view class="checkout-bar" v-if="cartList.length > 0">
      <view class="bar-left" @click="toggleAll">
        <view class="check-box" :class="{ checked: allSelected }">
          <text v-if="allSelected">✓</text>
        </view>
        <text class="select-text">全选</text>
      </view>
      
      <view class="bar-center">
        <text class="total-label">合计</text>
        <view class="total-price">
          <text class="total-symbol">¥</text>
          <text class="total-value">{{ finalAmount }}</text>
        </view>
      </view>
      
      <view class="checkout-btn" :class="{ disabled: selectedCount === 0 }" @click="checkout">
        去结算<text v-if="selectedCount > 0">({{ selectedCount }})</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { get, put, del, post } from '@/utils/request'

const cartList = ref([])
const discount = ref(0)

const totalAmount = computed(() => {
  return cartList.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + item.unitPrice * item.quantity, 0)
    .toFixed(2)
})

const finalAmount = computed(() => {
  const total = parseFloat(totalAmount.value) + 3 - discount.value
  return total.toFixed(2)
})

const selectedCount = computed(() => {
  return cartList.value.filter(item => item.selected).reduce((sum, item) => sum + item.quantity, 0)
})

const allSelected = computed(() => {
  return cartList.value.length > 0 && cartList.value.every(item => item.selected)
})

const loadCart = async () => {
  try {
    const res = await get('/customer/cart/list')
    if (res.data && res.data.length > 0) {
      cartList.value = res.data.map(item => ({ ...item, selected: true }))
    } else {
      loadMockCart()
    }
  } catch (e) {
    console.log('使用演示数据')
    loadMockCart()
  }
}

const loadMockCart = () => {
  cartList.value = [
    { id: 1, dishName: '招牌黄焖鸡', dishImage: '/static/shop1.jpg', unitPrice: 28, quantity: 2, selected: true, spec: '微辣' },
    { id: 2, dishName: '扬州炒饭', dishImage: '/static/shop2.jpg', unitPrice: 15, quantity: 1, selected: true },
    { id: 3, dishName: '冰镇可乐', dishImage: '/static/shop3.jpg', unitPrice: 5, quantity: 2, selected: true },
    { id: 4, dishName: '麻辣香锅', dishImage: '/static/shop1.jpg', unitPrice: 45, quantity: 1, selected: true, spec: '中辣' },
    { id: 5, dishName: '珍珠奶茶', dishImage: '/static/shop2.jpg', unitPrice: 12, quantity: 1, selected: true },
  ]
  uni.showToast({ title: '演示模式', icon: 'none', duration: 1500 })
}

const toggleSelect = (item) => {
  item.selected = !item.selected
}

const toggleAll = () => {
  const newVal = !allSelected.value
  cartList.value.forEach(item => item.selected = newVal)
}

const updateQuantity = async (item, delta) => {
  const newQty = item.quantity + delta
  if (newQty <= 0) {
    cartList.value = cartList.value.filter(i => i.id !== item.id)
    try { await del(`/customer/cart/delete/${item.id}`) } catch (e) {}
  } else {
    item.quantity = newQty
    try { await put(`/customer/cart/update/${item.id}`, { quantity: newQty }) } catch (e) {}
  }
}

const clearCart = async () => {
  uni.showModal({
    title: '确认清空',
    content: '确定要清空购物车吗？',
    success: (res) => {
      if (res.confirm) {
        cartList.value = []
      }
    }
  })
}

const goIndex = () => { uni.switchTab({ url: '/pages/index/index' }) }

const checkout = () => {
  if (selectedCount.value === 0) {
    uni.showToast({ title: '请选择商品', icon: 'none' })
    return
  }
  uni.showModal({
    title: '确认下单',
    content: `订单金额: ¥${finalAmount.value}`,
    success: (res) => {
      if (res.confirm) {
        uni.showToast({ title: '下单成功！', icon: 'success' })
        cartList.value = []
      }
    }
  })
}

onMounted(loadCart)
</script>

<style scoped>
/* ================================
   极简购物车 - 美团风格
   ================================ */

/* 页面容器 */
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 130rpx;
}

/* 简洁头部 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  padding-top: calc(var(--status-bar-height) + 24rpx);
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: baseline;
}

.header-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1a1a;
}

.header-count {
  font-size: 28rpx;
  color: #999;
  margin-left: 8rpx;
}

.header-action {
  font-size: 28rpx;
  color: #666;
}

/* 购物车内容 */
.cart-content {
  padding: 20rpx;
}

/* 商品列表 */
.cart-list {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
}

/* 商品项 */
.cart-item {
  display: flex;
  padding: 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.cart-item:last-child {
  border-bottom: none;
}

/* 左侧区域 */
.item-left {
  display: flex;
  align-items: flex-start;
  margin-right: 16rpx;
}

/* 复选框 */
.item-check, .check-box {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  margin-top: 50rpx;
  font-size: 22rpx;
  color: #fff;
  flex-shrink: 0;
  background: #fff;
  transition: all 0.2s;
}

.item-check.checked, .check-box.checked {
  background: #ff6b35;
  border-color: #ff6b35;
}

/* 商品图片 */
.item-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
  background: #f8f8f8;
}

/* 右侧区域 */
.item-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 160rpx;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 8rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-spec {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 16rpx;
}

/* 底部区域 */
.item-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 价格 */
.price-area {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 24rpx;
  color: #ff6b35;
  font-weight: 500;
}

.price-value {
  font-size: 34rpx;
  color: #ff6b35;
  font-weight: 700;
}

/* 数量控制 */
.quantity-control {
  display: flex;
  align-items: center;
}

.qty-btn {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
}

.qty-btn.minus {
  background: #f5f5f5;
  color: #666;
}

.qty-btn.plus {
  background: #ff6b35;
  color: #fff;
}

.qty-btn:active {
  opacity: 0.8;
}

.qty-num {
  min-width: 56rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1a1a;
}

/* 配送信息卡片 */
.delivery-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.delivery-row {
  display: flex;
  align-items: center;
}

.delivery-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.delivery-text {
  flex: 1;
  font-size: 26rpx;
  color: #666;
}

.delivery-fee {
  font-size: 26rpx;
  color: #999;
}

/* 优惠券卡片 */
.promo-card {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
}

.promo-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 24rpx;
}

.promo-left {
  display: flex;
  align-items: center;
}

.promo-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.promo-label {
  font-size: 28rpx;
  color: #333;
}

.promo-right {
  display: flex;
  align-items: center;
}

.promo-value {
  font-size: 26rpx;
  color: #999;
  margin-right: 8rpx;
}

.promo-arrow {
  font-size: 32rpx;
  color: #ccc;
}

/* 空购物车 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
}

.empty-image {
  width: 240rpx;
  height: 240rpx;
  margin-bottom: 32rpx;
}

.empty-title {
  font-size: 32rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 12rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #999;
  margin-bottom: 48rpx;
}

.empty-btn {
  background: #ff6b35;
  color: #fff;
  padding: 24rpx 64rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 500;
}

/* 底部结算栏 */
.checkout-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 110rpx;
  background: #fff;
  display: flex;
  align-items: center;
  padding: 0 20rpx;
  padding-bottom: env(safe-area-inset-bottom);
  box-shadow: 0 -2rpx 16rpx rgba(0,0,0,0.06);
  z-index: 100;
}

.bar-left {
  display: flex;
  align-items: center;
  padding: 0 16rpx;
}

.select-text {
  font-size: 26rpx;
  color: #333;
}

.bar-center {
  flex: 1;
  display: flex;
  align-items: baseline;
  justify-content: flex-end;
  margin-right: 20rpx;
}

.total-label {
  font-size: 26rpx;
  color: #666;
  margin-right: 4rpx;
}

.total-price {
  display: flex;
  align-items: baseline;
}

.total-symbol {
  font-size: 24rpx;
  color: #ff6b35;
  font-weight: 600;
}

.total-value {
  font-size: 40rpx;
  color: #ff6b35;
  font-weight: 700;
}

.checkout-btn {
  background: #ff6b35;
  color: #fff;
  padding: 20rpx 40rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 600;
  flex-shrink: 0;
}

.checkout-btn.disabled {
  background: #ccc;
}

.checkout-btn:active {
  opacity: 0.9;
}

.safe-bottom {
  height: 40rpx;
}
</style>
