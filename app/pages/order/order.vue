<template>
  <view class="container">
    <!-- 订单筛选 -->
    <view class="tabs">
      <view v-for="tab in tabs" :key="tab.value" 
            class="tab-item" :class="{ active: currentTab === tab.value }"
            @click="changeTab(tab.value)">
        {{ tab.label }}
      </view>
    </view>
    
    <!-- 订单列表 -->
    <view class="order-list">
      <view class="order-item" v-for="order in orderList" :key="order.id">
        <view class="order-header">
          <text class="shop-name">{{ order.shopName }}</text>
          <text class="status" :class="'status-' + order.status">{{ statusText(order.status) }}</text>
        </view>
        <view class="order-info">
          <text class="order-no">订单号: {{ order.orderNo }}</text>
          <text class="order-amount">¥{{ order.payAmount }}</text>
        </view>
        <view class="order-actions">
          <view class="btn" v-if="order.status === 0" @click="payOrder(order)">去支付</view>
          <view class="btn" v-if="order.status === 3" @click="confirmOrder(order)">确认收货</view>
          <view class="btn outline" v-if="order.status <= 1" @click="cancelOrder(order)">取消</view>
        </view>
      </view>
    </view>
    
    <view class="empty" v-if="orderList.length === 0">
      <text>暂无订单</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get, post } from '@/utils/request'

const tabs = [
  { label: '全部', value: null },
  { label: '待支付', value: 0 },
  { label: '待接单', value: 1 },
  { label: '制作中', value: 2 },
  { label: '配送中', value: 3 },
  { label: '已完成', value: 4 }
]
const currentTab = ref(null)
const orderList = ref([])

const statusText = (s) => ['待支付','待接单','制作中','配送中','已完成','已取消','退款中','已退款'][s]

const loadOrders = async () => {
  try {
    const params = currentTab.value !== null ? { status: currentTab.value } : {}
    const res = await get('/customer/order/list', params)
    orderList.value = res.data || []
  } catch (e) { console.error(e) }
}

const changeTab = (val) => {
  currentTab.value = val
  loadOrders()
}

const payOrder = async (order) => {
  try {
    await post(`/customer/order/${order.id}/pay`)
    uni.showToast({ title: '支付成功', icon: 'success' })
    loadOrders()
  } catch (e) { console.error(e) }
}

const confirmOrder = async (order) => {
  try {
    await post(`/customer/order/${order.id}/confirm`)
    uni.showToast({ title: '已确认收货', icon: 'success' })
    loadOrders()
  } catch (e) { console.error(e) }
}

const cancelOrder = async (order) => {
  try {
    await post(`/customer/order/${order.id}/cancel`, { reason: '用户取消' })
    uni.showToast({ title: '已取消', icon: 'success' })
    loadOrders()
  } catch (e) { console.error(e) }
}

onMounted(loadOrders)
</script>

<style scoped>
.container { padding: 20rpx; }
.tabs { display: flex; background: #fff; border-radius: 16rpx; margin-bottom: 20rpx; overflow-x: auto; }
.tab-item { padding: 20rpx 24rpx; font-size: 26rpx; white-space: nowrap; }
.tab-item.active { color: #667eea; font-weight: bold; border-bottom: 4rpx solid #667eea; }
.order-item { background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 20rpx; }
.order-header { display: flex; justify-content: space-between; margin-bottom: 16rpx; }
.shop-name { font-size: 30rpx; font-weight: bold; }
.status { font-size: 26rpx; }
.status-0 { color: #ff9500; }
.status-4 { color: #52c41a; }
.status-5 { color: #999; }
.order-info { display: flex; justify-content: space-between; font-size: 26rpx; color: #666; }
.order-amount { font-size: 32rpx; color: #ff4444; font-weight: bold; }
.order-actions { display: flex; justify-content: flex-end; gap: 16rpx; margin-top: 20rpx; }
.btn {
  padding: 12rpx 32rpx; border-radius: 30rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; font-size: 26rpx;
}
.btn.outline { background: #fff; border: 1rpx solid #999; color: #666; }
.empty { text-align: center; padding: 100rpx; color: #999; }
</style>
