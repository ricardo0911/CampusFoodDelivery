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
.page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 140rpx;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  padding-top: calc(var(--status-bar-height) + 30rpx);
  background: #fff;
}

.header-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #1a1a2e;
}

.header-action {
  font-size: 28rpx;
  color: #999;
}

.cart-content {
  padding: 20rpx;
}

.shop-group {
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
}

/* 购物车头部 */
.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 30rpx;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.02);
}

.header-left {
  font-size: 34rpx;
  font-weight: bold;
  color: #1a1a2e;
}

.edit-btn {
  font-size: 28rpx;
  color: #666;
  padding: 8rpx 20rpx;
}

/* 购物车列表 */
.cart-list {
  padding: 24rpx;
  padding-bottom: 120rpx;
}

.shop-card {
  background: #fff;
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.05);
}

.shop-header {
  display: flex;
  align-items: center;
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f9f9f9;
}

.shop-check, .item-check, .check-box {
  width: 42rpx;
  height: 42rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  font-size: 24rpx;
  color: #fff;
  transition: all 0.2s;
  flex-shrink: 0;
}

.shop-check.checked, .item-check.checked, .check-box.checked {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  border-color: transparent;
  box-shadow: 0 4rpx 10rpx rgba(255, 107, 53, 0.3);
}

.shop-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #1a1a2e;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  background: #fff;
}

.item-image {
  width: 140rpx;
  height: 140rpx;
  border-radius: 16rpx;
  margin-right: 24rpx;
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.06);
}

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 140rpx;
}

.item-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #1a1a2e;
  margin-bottom: 8rpx;
}

.item-spec {
  font-size: 24rpx;
  color: #999;
  align-self: flex-start;
  background: #f5f5f5;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: 16rpx;
}

.price-area {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 24rpx;
  color: #ff4444;
  font-weight: bold;
}

.price-value {
  font-size: 34rpx;
  color: #ff4444;
  font-weight: 800;
}

.quantity-control {
  display: flex;
  align-items: center;
  background: #f7f7f7;
  border-radius: 36rpx;
  padding: 6rpx;
}

.qty-btn {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  transition: all 0.2s;
}

.qty-btn.minus {
  background: #fff;
  color: #666;
  box-shadow: 0 2rpx 6rpx rgba(0,0,0,0.05);
}

.qty-btn.plus {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: #fff;
  box-shadow: 0 2rpx 8rpx rgba(255, 107, 53, 0.3);
}

.qty-btn:active {
  transform: scale(0.9);
}

.qty-num {
  min-width: 60rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

/* 底部结算栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05);
  z-index: 100;
}


.price-amount.discount {
  color: #ff4444;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
}

.empty-title {
  font-size: 32rpx;
  color: #333;
  margin-bottom: 16rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #999;
  margin-bottom: 40rpx;
}

.empty-btn {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: #fff;
  padding: 24rpx 80rpx;
  border-radius: 50rpx;
  font-size: 30rpx;
}

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
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05);
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
  color: #ff4444;
}

.total-value {
  font-size: 40rpx;
  color: #ff4444;
  font-weight: bold;
}

.checkout-btn {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: #fff;
  padding: 20rpx 50rpx;
  border-radius: 50rpx;
  font-size: 30rpx;
  font-weight: bold;
}

.safe-bottom {
  height: 40rpx;
}
</style>
