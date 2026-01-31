<template>
  <view class="page">
    <!-- 购物车列表 -->
    <scroll-view class="cart-scroll" scroll-y v-if="cartList.length > 0">
      <!-- 店铺分组 -->
      <view class="shop-group" v-for="(group, shopId) in groupedCart" :key="shopId">
        <!-- 店铺头部 -->
        <view class="shop-header">
          <view 
            class="shop-check" 
            :class="{ checked: isShopAllSelected(shopId) }" 
            @click="toggleShopSelect(shopId)"
          >
            <view class="check-icon" v-if="isShopAllSelected(shopId)">
              <view class="check-mark"></view>
            </view>
          </view>
          <view class="shop-tag">外卖</view>
          <text class="shop-name">{{ group.shopName || '美味餐厅' }}</text>
          <view class="shop-arrow">
            <view class="arrow-right"></view>
          </view>
          <view class="delivery-info">
            <view class="lightning-icon">
              <view class="bolt"></view>
            </view>
            <text class="delivery-text">{{ group.deliveryTime || 30 }}分钟达</text>
          </view>
        </view>
        
        <!-- 商品列表 -->
        <view class="cart-item" v-for="item in group.items" :key="item.id">
          <view 
            class="item-check" 
            :class="{ checked: item.selected }" 
            @click="toggleSelect(item)"
          >
            <view class="check-icon" v-if="item.selected">
              <view class="check-mark"></view>
            </view>
          </view>
          
          <view class="item-image-wrap">
            <image 
              class="item-image" 
              :src="item.dishImage || '/static/default-dish.jpg'" 
              mode="aspectFill" 
            />
            <view class="swipe-hint" v-if="item.id === group.items[0].id">
              <view class="hint-line"></view>
              <text class="hint-text">左滑删除</text>
            </view>
          </view>
          
          <view class="item-info">
            <text class="item-name">{{ item.dishName }}</text>
            <text class="item-spec" v-if="item.spec">{{ item.spec }}</text>
            
            <view class="item-bottom">
              <view class="price-row">
                <text class="price-symbol">¥</text>
                <text class="price-value">{{ item.unitPrice }}</text>
              </view>
              
              <view class="quantity-control">
                <view class="qty-btn minus" @click="updateQuantity(item, -1)">
                  <view class="minus-icon"></view>
                </view>
                <text class="qty-num">{{ item.quantity }}</text>
                <view class="qty-btn plus" @click="updateQuantity(item, 1)">
                  <view class="plus-icon">
                    <view class="plus-h"></view>
                    <view class="plus-v"></view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 优惠券提示 -->
      <view class="coupon-hint" v-if="cartList.length > 0">
        <view class="coupon-icon">
          <view class="coupon-shape">
            <view class="coupon-notch"></view>
            <view class="coupon-notch right"></view>
          </view>
        </view>
        <text class="coupon-text">已优惠 ¥0.00，再买 ¥10 可减 ¥5</text>
        <view class="coupon-arrow">
          <view class="arrow-small"></view>
        </view>
      </view>
      
      <view class="safe-bottom"></view>
    </scroll-view>
    
    <!-- 空购物车 -->
    <CEmpty
      v-else
      title="购物车是空的"
      description="去挑选心仪的美食吧"
    >
      <template #action>
        <CButton type="primary" size="sm" @click="goIndex">去首页看看</CButton>
      </template>
    </CEmpty>
    
    <!-- 底部结算栏 -->
    <CCartBar
      v-if="cartList.length > 0"
      :total-price="parseFloat(totalAmount)"
      :selected-count="selectedCount"
      :all-selected="allSelected"
      @toggleSelectAll="toggleAll"
      @checkout="checkout"
    />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { CCartBar, CEmpty, CButton } from '@/components/common'
import { get } from '@/utils/request'

const cartList = ref([])

// 按店铺分组
const groupedCart = computed(() => {
  const groups = {}
  cartList.value.forEach(item => {
    const shopId = item.shopId || 'default'
    if (!groups[shopId]) {
      groups[shopId] = {
        shopName: item.shopName || '美味餐厅',
        deliveryTime: 30,
        items: []
      }
    }
    groups[shopId].items.push(item)
  })
  return groups
})

const totalAmount = computed(() => {
  return cartList.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + item.unitPrice * item.quantity, 0)
    .toFixed(2)
})

const selectedCount = computed(() => {
  return cartList.value.filter(item => item.selected).reduce((sum, item) => sum + item.quantity, 0)
})

const allSelected = computed(() => {
  return cartList.value.length > 0 && cartList.value.every(item => item.selected)
})

const isShopAllSelected = (shopId) => {
  const group = groupedCart.value[shopId]
  return group && group.items.every(item => item.selected)
}

const toggleShopSelect = (shopId) => {
  const group = groupedCart.value[shopId]
  const newValue = !isShopAllSelected(shopId)
  group.items.forEach(item => item.selected = newValue)
}

const loadCart = () => {
  // 从本地存储读取
  const localCart = uni.getStorageSync('cartList')
  if (localCart && localCart.length > 0) {
    cartList.value = localCart.map(item => ({ ...item, selected: true }))
    return
  }
  
  // 使用演示数据
  cartList.value = [
    { id: 1, shopId: 1, shopName: '黄焖鸡米饭', dishName: '招牌黄焖鸡', dishImage: '/static/shop1.jpg', unitPrice: 28, quantity: 2, selected: true, spec: '微辣' },
    { id: 2, shopId: 1, shopName: '黄焖鸡米饭', dishName: '扬州炒饭', dishImage: '/static/shop2.jpg', unitPrice: 15, quantity: 1, selected: true },
    { id: 3, shopId: 2, shopName: '蜜雪冰城', dishName: '冰镇可乐', dishImage: '/static/shop3.jpg', unitPrice: 5, quantity: 2, selected: true },
    { id: 4, shopId: 2, shopName: '蜜雪冰城', dishName: '珍珠奶茶', dishImage: '/static/shop2.jpg', unitPrice: 12, quantity: 1, selected: true },
  ]
}

const toggleSelect = (item) => {
  item.selected = !item.selected
}

const toggleAll = () => {
  const newVal = !allSelected.value
  cartList.value.forEach(item => item.selected = newVal)
}

const updateQuantity = (item, delta) => {
  const newQty = item.quantity + delta
  if (newQty <= 0) {
    cartList.value = cartList.value.filter(i => i.id !== item.id)
  } else {
    item.quantity = newQty
  }
  // 同步到本地存储
  uni.setStorageSync('cartList', cartList.value)
}

const goIndex = () => { uni.switchTab({ url: '/pages/index/index' }) }

const checkout = () => {
  if (selectedCount.value === 0) {
    uni.showToast({ title: '请选择商品', icon: 'none' })
    return
  }
  // 保存选中的商品到本地存储
  uni.setStorageSync('cartList', cartList.value.filter(item => item.selected))
  // 跳转到订单确认页面
  uni.navigateTo({ url: '/pages/checkout/checkout' })
}

onMounted(loadCart)
</script>

<style scoped>
/* 页面容器 */
.page {
  min-height: 100vh;
  background: #f8f8f8;
  padding-bottom: 140rpx;
}

/* 购物车滚动区域 */
.cart-scroll {
  height: calc(100vh - 140rpx);
}

/* 店铺分组 */
.shop-group {
  background: #fff;
  margin: 20rpx;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

/* 店铺头部 */
.shop-header {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

/* 选择框样式 */
.shop-check, .item-check, .check-all {
  width: 44rpx;
  height: 44rpx;
  border: 2rpx solid #dcdcdc;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  background: #fff;
  flex-shrink: 0;
  transition: all 0.2s ease;
}

.shop-check.checked, .item-check.checked, .check-all.checked {
  background: #ff4d4f;
  border-color: #ff4d4f;
}

.check-icon {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.check-mark {
  width: 10rpx;
  height: 18rpx;
  border-right: 3rpx solid #fff;
  border-bottom: 3rpx solid #fff;
  transform: rotate(45deg);
  margin-bottom: 4rpx;
}

/* 店铺标签 */
.shop-tag {
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
  color: #fff;
  font-size: 20rpx;
  padding: 6rpx 14rpx;
  border-radius: 6rpx;
  margin-right: 16rpx;
  font-weight: 500;
}

.shop-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  flex: 1;
}

/* CSS箭头 */
.shop-arrow {
  width: 20rpx;
  height: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.arrow-right {
  width: 8rpx;
  height: 8rpx;
  border-top: 3rpx solid #999;
  border-right: 3rpx solid #999;
  transform: rotate(45deg);
}

/* 配送时间 */
.delivery-info {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.lightning-icon {
  width: 24rpx;
  height: 24rpx;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bolt {
  width: 12rpx;
  height: 18rpx;
  position: relative;
}

.bolt::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 6rpx solid transparent;
  border-right: 2rpx solid transparent;
  border-top: 8rpx solid #ff4d4f;
}

.bolt::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 2rpx solid transparent;
  border-right: 6rpx solid transparent;
  border-bottom: 8rpx solid #ff4d4f;
}

.delivery-text {
  font-size: 24rpx;
  color: #ff4d4f;
  font-weight: 500;
}

/* 商品项 */
.cart-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #f8f8f8;
  position: relative;
}

.cart-item:last-child {
  border-bottom: none;
}

/* 图片容器 */
.item-image-wrap {
  position: relative;
  flex-shrink: 0;
}

.item-image {
  width: 180rpx;
  height: 180rpx;
  border-radius: 16rpx;
  background: #f8f8f8;
  object-fit: cover;
}

/* 左滑删除提示 */
.swipe-hint {
  position: absolute;
  bottom: 8rpx;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 6rpx;
  background: rgba(0, 0, 0, 0.5);
  padding: 4rpx 10rpx;
  border-radius: 20rpx;
}

.hint-line {
  width: 16rpx;
  height: 3rpx;
  background: #fff;
  border-radius: 2rpx;
}

.hint-text {
  font-size: 18rpx;
  color: #fff;
}

/* 商品信息 */
.item-info {
  flex: 1;
  margin-left: 24rpx;
  min-height: 180rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.item-name {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-spec {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}

.item-bottom {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: auto;
  padding-top: 16rpx;
}

/* 价格 */
.price-row {
  display: flex;
  align-items: baseline;
  gap: 2rpx;
}

.price-symbol {
  font-size: 24rpx;
  color: #ff4d4f;
  font-weight: 600;
}

.price-value {
  font-size: 40rpx;
  color: #ff4d4f;
  font-weight: 700;
}

/* 数量控制 */
.quantity-control {
  display: flex;
  align-items: center;
  gap: 0;
  background: #f5f5f5;
  border-radius: 32rpx;
  padding: 4rpx;
}

.qty-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.qty-btn.minus {
  background: #fff;
}

.qty-btn.minus:active {
  background: #e8e8e8;
}

.minus-icon {
  width: 20rpx;
  height: 3rpx;
  background: #666;
  border-radius: 2rpx;
}

.qty-btn.plus {
  background: #ff4d4f;
}

.qty-btn.plus:active {
  background: #e04446;
}

.plus-icon {
  position: relative;
  width: 20rpx;
  height: 20rpx;
}

.plus-h {
  position: absolute;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  width: 20rpx;
  height: 3rpx;
  background: #fff;
  border-radius: 2rpx;
}

.plus-v {
  position: absolute;
  left: 50%;
  top: 0;
  transform: translateX(-50%);
  width: 3rpx;
  height: 20rpx;
  background: #fff;
  border-radius: 2rpx;
}

.qty-num {
  min-width: 56rpx;
  text-align: center;
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

/* 优惠券提示 */
.coupon-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx 32rpx;
  margin: 0 20rpx 20rpx;
  background: #fff;
  border-radius: 16rpx;
  gap: 12rpx;
}

.coupon-icon {
  width: 32rpx;
  height: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.coupon-shape {
  width: 32rpx;
  height: 22rpx;
  background: #ff4d4f;
  border-radius: 4rpx;
  position: relative;
}

.coupon-notch {
  position: absolute;
  left: -4rpx;
  top: 50%;
  transform: translateY(-50%);
  width: 8rpx;
  height: 8rpx;
  background: #fff;
  border-radius: 50%;
}

.coupon-notch.right {
  left: auto;
  right: -4rpx;
}

.coupon-text {
  font-size: 24rpx;
  color: #666;
  flex: 1;
  text-align: center;
}

.coupon-arrow {
  width: 16rpx;
  height: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.arrow-small {
  width: 6rpx;
  height: 6rpx;
  border-top: 2rpx solid #999;
  border-right: 2rpx solid #999;
  transform: rotate(45deg);
}

/* 空购物车 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
}

.empty-cart-icon {
  width: 200rpx;
  height: 200rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40rpx;
}

.cart-body {
  position: relative;
  width: 120rpx;
  height: 100rpx;
}

.cart-handle {
  position: absolute;
  top: -20rpx;
  left: 20rpx;
  width: 50rpx;
  height: 30rpx;
  border: 6rpx solid #ddd;
  border-bottom: none;
  border-radius: 50rpx 50rpx 0 0;
}

.cart-basket {
  position: absolute;
  bottom: 20rpx;
  left: 0;
  width: 100%;
  height: 60rpx;
  background: #f0f0f0;
  border-radius: 0 0 20rpx 20rpx;
  overflow: hidden;
}

.basket-line {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 2rpx;
  height: 100%;
  background: #e0e0e0;
}

.basket-line::before,
.basket-line::after {
  content: '';
  position: absolute;
  top: 0;
  width: 2rpx;
  height: 100%;
  background: #e0e0e0;
}

.basket-line::before {
  left: -30rpx;
}

.basket-line::after {
  right: -30rpx;
}

.cart-wheel {
  position: absolute;
  bottom: 0;
  width: 24rpx;
  height: 24rpx;
  background: #ddd;
  border-radius: 50%;
}

.cart-wheel.left {
  left: 20rpx;
}

.cart-wheel.right {
  right: 20rpx;
}

.empty-title {
  font-size: 36rpx;
  color: #333;
  font-weight: 600;
  margin-bottom: 16rpx;
}

.empty-desc {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 48rpx;
}

.empty-btn {
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
  color: #fff;
  padding: 28rpx 72rpx;
  border-radius: 44rpx;
  font-size: 30rpx;
  font-weight: 600;
  box-shadow: 0 8rpx 24rpx rgba(255, 77, 79, 0.3);
}

/* 底部结算栏 */
.checkout-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background: #fff;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  padding-bottom: env(safe-area-inset-bottom);
  box-shadow: 0 -2rpx 20rpx rgba(0, 0, 0, 0.08);
  z-index: 100;
}

.bar-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.select-text {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.bar-center {
  flex: 1;
  display: flex;
  align-items: baseline;
  justify-content: flex-end;
  margin-right: 24rpx;
  gap: 4rpx;
}

.total-label {
  font-size: 28rpx;
  color: #333;
  margin-right: 8rpx;
}

.total-symbol {
  font-size: 24rpx;
  color: #ff4d4f;
  font-weight: 600;
}

.total-value {
  font-size: 48rpx;
  color: #ff4d4f;
  font-weight: 700;
}

.checkout-btn {
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
  color: #fff;
  height: 80rpx;
  padding: 0 48rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 77, 79, 0.3);
  transition: all 0.2s ease;
}

.checkout-btn.disabled {
  background: #e0e0e0;
  box-shadow: none;
}

.checkout-text {
  font-size: 32rpx;
  font-weight: 600;
}

.checkout-count {
  font-size: 26rpx;
  font-weight: 500;
}

.safe-bottom {
  height: 60rpx;
}
</style>
