<template>
  <view class="container">
    <view class="cart-list" v-if="cartList.length > 0">
      <view class="cart-item" v-for="item in cartList" :key="item.id">
        <image class="item-image" :src="item.dishImage || '/static/default-dish.png'" mode="aspectFill" />
        <view class="item-info">
          <text class="item-name">{{ item.dishName }}</text>
          <text class="item-price">¥{{ item.unitPrice }}</text>
        </view>
        <view class="quantity-ctrl">
          <view class="btn" @click="updateQuantity(item, -1)">-</view>
          <text class="quantity">{{ item.quantity }}</text>
          <view class="btn" @click="updateQuantity(item, 1)">+</view>
        </view>
      </view>
    </view>
    
    <view class="empty" v-else>
      <text>购物车是空的</text>
      <view class="go-shop" @click="goIndex">去逛逛</view>
    </view>
    
    <!-- 结算栏 -->
    <view class="checkout-bar" v-if="cartList.length > 0">
      <view class="total-info">
        <text>合计: </text>
        <text class="total-price">¥{{ totalAmount }}</text>
      </view>
      <view class="checkout-btn" @click="checkout">去结算</view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { get, put, del, post } from '@/utils/request'

const cartList = ref([])

const totalAmount = computed(() => {
  return cartList.value.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0).toFixed(2)
})

const loadCart = async () => {
  try {
    const res = await get('/customer/cart/list')
    cartList.value = res.data || []
  } catch (e) { console.error(e) }
}

const updateQuantity = async (item, delta) => {
  const newQty = item.quantity + delta
  if (newQty <= 0) {
    await del(`/customer/cart/delete/${item.id}`)
  } else {
    await put(`/customer/cart/update/${item.id}`, { quantity: newQty })
  }
  loadCart()
}

const goIndex = () => { uni.switchTab({ url: '/pages/index/index' }) }

const checkout = () => {
  // 简化处理：直接创建订单
  uni.showModal({
    title: '确认下单',
    content: `订单金额: ¥${totalAmount.value}`,
    success: async (res) => {
      if (res.confirm) {
        // 实际应用需要选择地址等
        uni.showToast({ title: '请先完善地址', icon: 'none' })
      }
    }
  })
}

onMounted(loadCart)
</script>

<style scoped>
.container { padding: 20rpx; padding-bottom: 120rpx; }
.cart-item {
  display: flex; align-items: center;
  background: #fff; border-radius: 16rpx;
  padding: 20rpx; margin-bottom: 20rpx;
}
.item-image { width: 120rpx; height: 120rpx; border-radius: 12rpx; margin-right: 20rpx; }
.item-info { flex: 1; }
.item-name { font-size: 28rpx; font-weight: bold; }
.item-price { font-size: 28rpx; color: #ff4444; margin-top: 10rpx; }
.quantity-ctrl { display: flex; align-items: center; }
.btn {
  width: 50rpx; height: 50rpx;
  background: #f0f0f0; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 32rpx;
}
.quantity { width: 60rpx; text-align: center; font-size: 28rpx; }
.empty { text-align: center; padding: 100rpx; color: #999; }
.go-shop {
  margin-top: 30rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; padding: 20rpx 60rpx;
  border-radius: 40rpx; display: inline-block;
}
.checkout-bar {
  position: fixed; bottom: 100rpx; left: 0; right: 0;
  height: 100rpx; background: #fff;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 30rpx; box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.1);
}
.total-price { font-size: 36rpx; color: #ff4444; font-weight: bold; }
.checkout-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; padding: 16rpx 60rpx;
  border-radius: 40rpx;
}
</style>
