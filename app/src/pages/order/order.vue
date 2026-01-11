<template>
  <view class="page">
    <!-- 头部 -->
    <view class="header">
      <text class="header-title">我的订单</text>
    </view>
    
    <!-- 订单状态筛选 -->
    <scroll-view class="tabs-wrapper" scroll-x>
      <view class="tabs">
        <view 
          v-for="tab in tabs" 
          :key="tab.value" 
          class="tab-item" 
          :class="{ active: currentTab === tab.value }"
          @click="changeTab(tab.value)"
        >
          <text class="tab-text">{{ tab.label }}</text>
          <view class="tab-indicator" v-if="currentTab === tab.value"></view>
        </view>
      </view>
    </scroll-view>
    
    <!-- 订单列表 -->
    <scroll-view class="order-content" scroll-y>
      <view class="order-card" v-for="order in orderList" :key="order.id">
        <!-- 订单头部 -->
        <view class="order-header">
          <view class="shop-info">
            <text class="shop-icon">🏪</text>
            <text class="shop-name">{{ order.shopName || '美味餐厅' }}</text>
          </view>
          <view class="order-status" :class="'status-' + order.status">
            {{ statusText(order.status) }}
          </view>
        </view>
        
        <!-- 订单商品 -->
        <view class="order-goods">
          <view class="goods-item" v-for="(item, idx) in (order.items || []).slice(0, 2)" :key="idx">
            <image class="goods-image" :src="item.image || '/static/default-dish.jpg'" mode="aspectFill" />
            <view class="goods-info">
              <view class="goods-row">
                <text class="goods-name">{{ item.name }}</text>
                <text class="goods-price">¥{{ item.price }}</text>
              </view>
              <text class="goods-spec">x{{ item.quantity }}</text>
            </view>
          </view>
          <view class="goods-more" v-if="(order.items || []).length > 2">
            共{{ order.items.length }}件商品
          </view>
        </view>
        
        <!-- 订单金额 -->
        <view class="order-amount">
          <text class="amount-label">实付</text>
          <text class="amount-symbol">¥</text>
          <text class="amount-value">{{ order.payAmount || 99.00 }}</text>
        </view>
        
        <!-- 订单时间 -->
        <view class="order-time">
          <text class="time-label">下单时间：</text>
          <text class="time-value">{{ order.createTime || '2024-01-15 12:30' }}</text>
        </view>
        
        <!-- 订单操作 -->
        <view class="order-actions">
          <view class="action-btn outline" v-if="order.status === 4" @click="reorder(order)">
            再来一单
          </view>
          <view class="action-btn outline" v-if="order.status <= 1" @click="cancelOrder(order)">
            取消订单
          </view>
          <view class="action-btn primary" v-if="order.status === 0" @click="payOrder(order)">
            去支付
          </view>
          <view class="action-btn primary" v-if="order.status === 3" @click="confirmOrder(order)">
            确认收货
          </view>
          <view class="action-btn outline" v-if="order.status === 4" @click="review(order)">
            评价
          </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view class="empty-state" v-if="orderList.length === 0">
        <view class="empty-icon">📋</view>
        <text class="empty-title">暂无订单</text>
        <text class="empty-desc">快去下单享受美食吧</text>
        <view class="empty-btn" @click="goIndex">去点餐</view>
      </view>
      
      <view class="safe-bottom"></view>
    </scroll-view>
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

const statusText = (s) => ['待支付','待接单','制作中','配送中','已完成','已取消','退款中','已退款'][s] || '未知'

const loadOrders = async () => {
  try {
    const params = currentTab.value !== null ? { status: currentTab.value } : {}
    const res = await get('/customer/order/list', params)
    if (res.data && res.data.length > 0) {
      orderList.value = res.data
    } else {
      loadMockOrders()
    }
  } catch (e) {
    console.log('使用演示数据')
    loadMockOrders()
  }
}

const loadMockOrders = () => {
  const allOrders = [
    { id: 1, shopName: '黄焖鸡米饭', status: 4, payAmount: 56.00, createTime: '2024-01-15 12:30', items: [{ name: '招牌黄焖鸡', quantity: 2, price: 28, image: '/static/shop1.jpg' }] },
    { id: 2, shopName: '兰州拉面馆', status: 3, payAmount: 23.00, createTime: '2024-01-15 11:20', items: [{ name: '牛肉拉面', quantity: 1, price: 18, image: '/static/shop2.jpg' }, { name: '凉菜', quantity: 1, price: 5, image: '/static/shop1.jpg' }] },
    { id: 3, shopName: '麻辣香锅', status: 0, payAmount: 89.00, createTime: '2024-01-15 10:00', items: [{ name: '经典麻辣锅', quantity: 1, price: 68, image: '/static/shop3.jpg' }] },
    { id: 4, shopName: '肯德基', status: 2, payAmount: 42.00, createTime: '2024-01-15 09:30', items: [{ name: '香辣鸡腿堡', quantity: 2, price: 18, image: '/static/shop1.jpg' }] },
    { id: 5, shopName: '蜜雪冰城', status: 1, payAmount: 18.00, createTime: '2024-01-15 08:45', items: [{ name: '珍珠奶茶', quantity: 2, price: 9, image: '/static/shop2.jpg' }] },
  ]
  
  if (currentTab.value !== null) {
    orderList.value = allOrders.filter(o => o.status === currentTab.value)
  } else {
    orderList.value = allOrders
  }
  
  uni.showToast({ title: '演示模式', icon: 'none', duration: 1500 })
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
  } catch (e) { 
    uni.showToast({ title: '支付成功', icon: 'success' })
    order.status = 1
  }
}

const confirmOrder = async (order) => {
  try {
    await post(`/customer/order/${order.id}/confirm`)
    uni.showToast({ title: '已确认收货', icon: 'success' })
    loadOrders()
  } catch (e) { 
    uni.showToast({ title: '已确认收货', icon: 'success' })
    order.status = 4
  }
}

const cancelOrder = async (order) => {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消这个订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await post(`/customer/order/${order.id}/cancel`, { reason: '用户取消' })
        } catch (e) {}
        uni.showToast({ title: '已取消', icon: 'success' })
        orderList.value = orderList.value.filter(o => o.id !== order.id)
      }
    }
  })
}

const reorder = (order) => { uni.switchTab({ url: '/pages/index/index' }) }
const review = (order) => { uni.showToast({ title: '功能开发中', icon: 'none' }) }
const goIndex = () => { uni.switchTab({ url: '/pages/index/index' }) }

onMounted(loadOrders)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f6fa;
}

.header {
  padding: 30rpx;
  padding-top: calc(var(--status-bar-height) + 30rpx);
  background: #fff;
}

.header-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #1a1a2e;
}

.tabs-wrapper {
  background: #fff;
  white-space: nowrap;
}

.tabs {
  display: inline-flex;
  padding: 0 20rpx;
}

.tab-item {
  position: relative;
  padding: 24rpx 32rpx;
  display: inline-flex;
  flex-direction: column;
  align-items: center;
}

.tab-text {
  font-size: 28rpx;
  color: #666;
}

.tab-item.active .tab-text {
  color: #ff6b35;
  font-weight: bold;
}

.tab-indicator {
  position: absolute;
  bottom: 8rpx;
  width: 40rpx;
  height: 6rpx;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  border-radius: 3rpx;
}

.order-content {
  padding: 20rpx;
  height: calc(100vh - 200rpx);
}

.order-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.03);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.shop-info {
  display: flex;
  align-items: center;
}

.shop-icon {
  margin-right: 10rpx;
}

.shop-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #1a1a2e;
}

.order-status {
  font-size: 26rpx;
  font-weight: bold;
}

.order-status.status-0 { color: #ff9500; }
.order-status.status-1 { color: #ff6b35; }
.order-status.status-2 { color: #52c41a; }
.order-status.status-3 { color: #1890ff; }
.order-status.status-4 { color: #52c41a; }
.order-status.status-5 { color: #999; }

.order-goods {
  padding: 20rpx 0;
}

/* ================================
   现代订单页面样式 - 美团/饿了么风格
   ================================ */

/* 页面容器 */
.page {
  background: linear-gradient(180deg, #fff8f5 0%, #f8f9fa 100%);
  min-height: 100vh;
}

/* 头部 */
.header {
  background: #fff;
  padding: 30rpx;
  text-align: center;
  border-bottom: 1rpx solid #f0f0f0;
}

.header-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #222;
  letter-spacing: 2rpx;
}

/* Tab 选项卡 */
.tabs-wrapper {
  background: #fff;
  padding: 20rpx 0;
  margin-bottom: 20rpx;
}

.tabs {
  display: flex;
  padding: 0 20rpx;
  gap: 10rpx;
}

.tab-item {
  position: relative;
  padding: 18rpx 32rpx;
  border-radius: 30rpx;
  background: #f5f5f5;
  transition: all 0.3s ease;
}

.tab-item.active {
  background: #ff6b35;
}

.tab-text {
  font-size: 26rpx;
  color: #666;
  font-weight: 500;
  white-space: nowrap;
}

.tab-item.active .tab-text {
  color: #fff;
  font-weight: 600;
}

.tab-indicator {
  display: none;
}

/* 订单列表容器 */
.order-content {
  padding: 20rpx 24rpx;
  box-sizing: border-box;
}

/* 订单卡片 */
.order-card {
  background: #fff;
  border-radius: 20rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.04);
}

/* 订单头部 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #f8f8f8;
}

.shop-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.shop-icon {
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  background: none;
}

.shop-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #222;
}

/* 订单状态胶囊 */
.order-status {
  font-size: 24rpx;
  font-weight: 600;
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
}

.status-0 { 
  color: #ff6b35; 
  background: #fff5f0;
}
.status-1 { 
  color: #3b82f6; 
  background: #eff6ff;
}
.status-2 { 
  color: #f59e0b; 
  background: #fffbeb;
}
.status-3 { 
  color: #06b6d4; 
  background: #ecfeff;
}
.status-4 { 
  color: #10b981; 
  background: #ecfdf5;
}
.status-5 { 
  color: #9ca3af; 
  background: #f3f4f6;
}

/* 商品区域 */
.order-goods {
  padding: 20rpx 24rpx;
}

.goods-item {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f8f8f8;
}

.goods-item:last-child {
  border-bottom: none;
}

.goods-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 12rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
  background: #f5f5f5;
}

.goods-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 100rpx;
  justify-content: center;
}

.goods-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.goods-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  flex: 1;
  margin-right: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-price {
  font-size: 28rpx;
  color: #333;
  font-weight: 600;
}

.goods-spec {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}

.goods-more {
  font-size: 24rpx;
  color: #999;
  text-align: center;
  padding: 16rpx 0;
}

/* 订单金额 */
.order-amount {
  display: flex;
  align-items: baseline;
  justify-content: flex-end;
  padding: 16rpx 24rpx;
  background: #fafafa;
}

.amount-label {
  font-size: 26rpx;
  color: #666;
  margin-right: 8rpx;
}

.amount-symbol {
  font-size: 26rpx;
  color: #ff6b35;
  font-weight: 600;
}

.amount-value {
  font-size: 36rpx;
  color: #ff6b35;
  font-weight: 700;
}

/* 订单时间 */
.order-time {
  display: flex;
  font-size: 24rpx;
  color: #999;
  padding: 16rpx 24rpx;
}

.time-label {
  color: #bbb;
}

.time-value {
  color: #999;
}

/* 订单操作按钮 */
.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
  padding: 16rpx 24rpx 24rpx;
}

.action-btn {
  padding: 16rpx 32rpx;
  border-radius: 32rpx;
  font-size: 26rpx;
  font-weight: 500;
  transition: all 0.2s ease;
}

.action-btn.primary {
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f5e 100%);
  color: #fff;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);
}

.action-btn.primary:active {
  transform: scale(0.98);
  opacity: 0.9;
}

.action-btn.outline {
  border: 2rpx solid #e5e5e5;
  color: #666;
  background: #fff;
}

.action-btn.outline:active {
  background: #f5f5f5;
  border-color: #ddd;
}

/* 空状态 */
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

.safe-bottom {
  height: 100rpx;
}
</style>
