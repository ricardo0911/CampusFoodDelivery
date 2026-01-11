<template>
  <view class="page">
    <!-- 头部 -->
    <view class="header">
      <text class="header-title">购物车</text>
      <text class="header-action" v-if="cartList.length > 0" @click="clearCart">清空</text>
    </view>
    
    <!-- 购物车列表 -->
    <scroll-view class="cart-content" scroll-y v-if="cartList.length > 0">
      <!-- 店铺分组 -->
      <view class="shop-group">
        <view class="shop-header">
          <view class="shop-check" :class="{ checked: allSelected }" @click="toggleAll">
            <text v-if="allSelected">✓</text>
          </view>
          <text class="shop-name">🏪 当前店铺</text>
        </view>
        
        <!-- 商品卡片 -->
        <view class="cart-card" v-for="item in cartList" :key="item.id">
          <view class="item-check" :class="{ checked: item.selected }" @click="toggleSelect(item)">
            <text v-if="item.selected">✓</text>
          </view>
          
          <image class="item-image" :src="item.dishImage || '/static/default-dish.jpg'" mode="aspectFill" />
          
          <view class="item-content">
            <text class="item-name">{{ item.dishName }}</text>
            <text class="item-spec" v-if="item.spec">{{ item.spec }}</text>
            
            <view class="item-footer">
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
      
      <!-- 优惠信息 -->
      <view class="promo-section">
        <view class="promo-item">
          <text class="promo-label">🎁 优惠券</text>
          <text class="promo-value">暂无可用 ></text>
        </view>
        <view class="promo-item">
          <text class="promo-label">💳 支付方式</text>
          <text class="promo-value">在线支付 ></text>
        </view>
      </view>
      
      <!-- 费用明细 -->
      <view class="price-detail">
        <view class="price-row">
          <text class="price-label">商品金额</text>
          <text class="price-amount">¥{{ totalAmount }}</text>
        </view>
        <view class="price-row">
          <text class="price-label">配送费</text>
          <text class="price-amount">¥3.00</text>
        </view>
        <view class="price-row" v-if="discount > 0">
          <text class="price-label">优惠</text>
          <text class="price-amount discount">-¥{{ discount.toFixed(2) }}</text>
        </view>
      </view>
      
      <view class="safe-bottom"></view>
    </scroll-view>
    
    <!-- 空购物车 -->
    <view class="empty-state" v-else>
      <view class="empty-icon">🛒</view>
      <text class="empty-title">购物车空空如也</text>
      <text class="empty-desc">快去挑选美食吧</text>
      <view class="empty-btn" @click="goIndex">去逛逛</view>
    </view>
    
    <!-- 底部结算栏 -->
    <view class="checkout-bar" v-if="cartList.length > 0">
      <view class="bar-left">
        <view class="select-all" @click="toggleAll">
          <view class="check-box" :class="{ checked: allSelected }">
            <text v-if="allSelected">✓</text>
          </view>
          <text class="select-text">全选</text>
        </view>
      </view>
      
      <view class="bar-right">
        <view class="total-area">
          <text class="total-label">合计：</text>
          <text class="total-symbol">¥</text>
          <text class="total-value">{{ finalAmount }}</text>
        </view>
        <view class="checkout-btn" @click="checkout">
          <text>去结算({{ selectedCount }})</text>
        </view>
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
    cartList.value = (res.data || []).map(item => ({ ...item, selected: true }))
  } catch (e) {
    console.error(e)
    // 模拟数据
    cartList.value = [
      { id: 1, dishName: '招牌黄焖鸡', dishImage: '/static/shop1.jpg', unitPrice: 28, quantity: 2, selected: true, spec: '微辣' },
      { id: 2, dishName: '扬州炒饭', dishImage: '/static/shop2.jpg', unitPrice: 15, quantity: 1, selected: true },
      { id: 3, dishName: '冰镇可乐', dishImage: '/static/shop3.jpg', unitPrice: 5, quantity: 2, selected: true },
    ]
  }
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
   现代购物车页面样式 - 美团风格
   ================================ */

/* 页面容器 */
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fff8f5 0%, #f5f6fa 100%);
  padding-bottom: 140rpx;
}

/* 头部 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  padding-top: calc(var(--status-bar-height) + 30rpx);
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f5e 100%);
}

.header-title {
  font-size: 38rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: 2rpx;
}

.header-action {
  font-size: 28rpx;
  color: rgba(255,255,255,0.9);
  padding: 10rpx 24rpx;
  background: rgba(255,255,255,0.2);
  border-radius: 30rpx;
}

/* 购物车内容 */
.cart-content {
  padding: 24rpx;
}

/* 店铺分组 */
.shop-group {
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.04);
}

.shop-header {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
  background: #fafafa;
}

/* 复选框样式 */
.shop-check, .item-check, .check-box {
  width: 44rpx;
  height: 44rpx;
  border: 2rpx solid #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  font-size: 24rpx;
  color: #fff;
  transition: all 0.2s ease;
  flex-shrink: 0;
  background: #fff;
}

.shop-check.checked, .item-check.checked, .check-box.checked {
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f5e 100%);
  border-color: transparent;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.35);
}

.shop-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

/* 商品卡片 */
.cart-card {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #f8f8f8;
}

.cart-card:last-child {
  border-bottom: none;
}

.item-image {
  width: 140rpx;
  height: 140rpx;
  border-radius: 16rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
  background: #f5f5f5;
}

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 140rpx;
  justify-content: space-between;
}

.item-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #222;
  margin-bottom: 6rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-spec {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 12rpx;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 价格区域 */
.price-area {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 24rpx;
  color: #ff6b35;
  font-weight: 600;
}

.price-value {
  font-size: 36rpx;
  color: #ff6b35;
  font-weight: 700;
}

/* 数量控制器 */
.quantity-control {
  display: flex;
  align-items: center;
  background: #f8f8f8;
  border-radius: 30rpx;
  padding: 4rpx;
}

.qty-btn {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  transition: all 0.2s ease;
}

.qty-btn.minus {
  background: #fff;
  color: #999;
  border: 1rpx solid #eee;
}

.qty-btn.plus {
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f5e 100%);
  color: #fff;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);
}

.qty-btn:active {
  transform: scale(0.92);
}

.qty-num {
  min-width: 64rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 700;
  color: #333;
}

/* 优惠信息区 */
.promo-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 8rpx 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.04);
}

.promo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f8f8f8;
}

.promo-item:last-child {
  border-bottom: none;
}

.promo-label {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.promo-value {
  font-size: 26rpx;
  color: #999;
}

/* 费用明细 */
.price-detail {
  background: #fff;
  border-radius: 20rpx;
  padding: 8rpx 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.04);
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f8f8f8;
}

.price-row:last-child {
  border-bottom: none;
}

.price-label {
  font-size: 28rpx;
  color: #666;
}

.price-amount {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.price-amount.discount {
  color: #ff6b35;
}

/* 空购物车 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 32rpx;
  opacity: 0.6;
}

.empty-title {
  font-size: 32rpx;
  color: #333;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #999;
  margin-bottom: 48rpx;
}

.empty-btn {
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f5e 100%);
  color: #fff;
  padding: 24rpx 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 600;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.3);
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
  justify-content: space-between;
  padding: 0 24rpx;
  padding-bottom: env(safe-area-inset-bottom);
  box-shadow: 0 -4rpx 24rpx rgba(0,0,0,0.08);
  z-index: 100;
}

.bar-left {
  display: flex;
  align-items: center;
}

.select-all {
  display: flex;
  align-items: center;
}

.select-text {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.bar-right {
  display: flex;
  align-items: center;
}

.total-area {
  display: flex;
  align-items: baseline;
  margin-right: 24rpx;
}

.total-label {
  font-size: 26rpx;
  color: #333;
}

.total-symbol {
  font-size: 26rpx;
  color: #ff6b35;
  font-weight: 600;
}

.total-value {
  font-size: 42rpx;
  color: #ff6b35;
  font-weight: 700;
}

.checkout-btn {
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f5e 100%);
  color: #fff;
  padding: 20rpx 48rpx;
  border-radius: 40rpx;
  font-size: 30rpx;
  font-weight: 600;
  box-shadow: 0 6rpx 20rpx rgba(255, 107, 53, 0.35);
}

.checkout-btn:active {
  transform: scale(0.98);
  opacity: 0.9;
}

.safe-bottom {
  height: 40rpx;
}
</style>
