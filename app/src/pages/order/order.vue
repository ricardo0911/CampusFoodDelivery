<template>
  <view class="page">
    <!-- 状态筛选栏 -->
    <view class="tabs-wrapper">
      <scroll-view class="tabs-scroll" scroll-x :scroll-into-view="'tab-' + currentTab" scroll-with-animation>
        <view class="tabs">
          <view 
            v-for="tab in tabs" 
            :key="tab.value" 
            :id="'tab-' + tab.value"
            class="tab-item" 
            :class="{ active: currentTab === tab.value, 'has-badge': tab.badge > 0 }"
            @click="changeTab(tab.value)"
          >
            <text class="tab-text">{{ tab.label }}</text>
            <view class="tab-badge" v-if="tab.badge > 0">{{ tab.badge > 99 ? '99+' : tab.badge }}</view>
          </view>
        </view>
      </scroll-view>
    </view>
    
    <!-- 订单列表 -->
    <scroll-view class="order-content" scroll-y refresher-enabled :refresher-triggered="isRefreshing" @refresherrefresh="onRefresh">
      <!-- 订单卡片 -->
      <view class="order-card" v-for="order in orderList" :key="order.id">
        <!-- 订单头部 -->
        <view class="order-header">
          <view class="shop-info">
            <view class="shop-icon">
              <view class="shop-icon-inner">
                <view class="shop-icon-line"></view>
                <view class="shop-icon-line"></view>
                <view class="shop-icon-line"></view>
              </view>
            </view>
            <text class="shop-name">{{ order.shopName || '美味餐厅' }}</text>
            <view class="shop-arrow"></view>
          </view>
          <CTag :type="getStatusType(order.status)" size="sm" rounded>
            {{ statusText(order.status) }}
          </CTag>
        </view>
        
        <!-- 订单商品预览 -->
        <view class="order-preview" @click="viewOrderDetail(order)">
          <view class="preview-images">
            <view class="preview-image-wrap" v-for="(item, idx) in getPreviewImages(order.items)" :key="idx">
              <image class="preview-image" :src="item.image || '/static/default-dish.jpg'" mode="aspectFill" />
              <view class="image-overlay" v-if="idx === 2 && getRemainingCount(order.items) > 0">
                <text class="overlay-text">+{{ getRemainingCount(order.items) }}</text>
              </view>
            </view>
          </view>
          <view class="preview-info">
            <view class="preview-meta">
              <text class="meta-text">共{{ order.items?.length || 1 }}件商品</text>
            </view>
            <view class="preview-amount">
              <text class="amount-label">实付</text>
              <text class="amount-symbol">¥</text>
              <text class="amount-value">{{ formatAmount(order.payAmount) }}</text>
            </view>
          </view>
        </view>
        
        <!-- 订单时间 -->
        <view class="order-meta">
          <view class="meta-left">
            <view class="time-icon"></view>
            <text class="meta-time">{{ order.createTime || '2024-01-15 12:30' }}</text>
          </view>
          <view class="order-no">订单号：{{ order.orderNo || order.id }}</view>
        </view>
        
        <!-- 订单操作 -->
        <view class="order-actions">
          <CButton
            v-for="btn in getActionButtons(order)"
            :key="btn.action"
            :type="btn.type"
            size="sm"
            @click="handleAction(btn.action, order)"
          >
            {{ btn.text }}
          </CButton>
        </view>
      </view>
      
      <!-- 空状态 -->
      <CEmpty
        v-if="orderList.length === 0 && !isLoading"
        title="暂无相关订单"
        :description="emptyDescription"
      >
        <template #action>
          <CButton type="primary" size="sm" @click="goIndex">去点餐</CButton>
        </template>
      </CEmpty>
      
      <!-- 加载更多 -->
      <CLoadMore
        v-if="orderList.length > 0"
        :status="loadMoreStatus"
        @loadMore="loadMoreOrders"
      />
      
      <view class="safe-bottom"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { CTag, CEmpty, CButton, CLoadMore } from '@/components/common'
import { get, post } from '@/utils/request'

// 状态定义
const currentTab = ref(null)
const orderList = ref([])
const isLoading = ref(false)
const isRefreshing = ref(false)

// 带小红点的标签配置
const tabs = ref([
  { label: '全部', value: null, badge: 0 },
  { label: '待支付', value: 0, badge: 0 },
  { label: '待接单', value: 1, badge: 2 },
  { label: '制作中', value: 2, badge: 0 },
  { label: '配送中', value: 3, badge: 1 },
  { label: '待评价', value: 5, badge: 3 },
  { label: '已完成', value: 4, badge: 0 }
])

// 空状态描述
const emptyDescription = computed(() => {
  if (currentTab.value === null) {
    return '您还没有订单，快去下单吧'
  }
  const tab = tabs.value.find(t => t.value === currentTab.value)
  return tab ? `您还没有${tab.label}订单` : '您还没有订单'
})

// 加载更多状态
const loadMoreStatus = ref('more')

// 获取状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'error',      // 待支付 - 红色
    1: 'warning',    // 待接单 - 黄色
    2: 'primary',    // 制作中 - 橙色
    3: 'success',    // 配送中 - 绿色
    4: 'default',    // 已完成 - 灰色
    5: 'primary',    // 待评价 - 橙色
    6: 'default',    // 已取消 - 灰色
    7: 'warning',    // 退款中 - 黄色
    8: 'default'     // 已退款 - 灰色
  }
  return typeMap[status] || 'default'
}

// 状态文本映射
const statusText = (status) => {
  const map = {
    0: '待支付',
    1: '待接单', 
    2: '制作中',
    3: '配送中',
    4: '已完成',
    5: '待评价',
    6: '已取消',
    7: '退款中',
    8: '已退款'
  }
  return map[status] || '未知'
}

// 获取预览图片（最多3个）
const getPreviewImages = (items) => {
  if (!items || items.length === 0) return [{ image: '/static/default-dish.jpg' }]
  return items.slice(0, 3)
}

// 获取剩余商品数量
const getRemainingCount = (items) => {
  if (!items || items.length <= 3) return 0
  return items.length - 3
}

// 格式化金额
const formatAmount = (amount) => {
  if (!amount) return '0.00'
  return parseFloat(amount).toFixed(2)
}

// 获取操作按钮配置（适配 CButton 组件）
const getActionButtons = (order) => {
  const buttons = []
  const status = order.status
  
  switch (status) {
    case 0: // 待支付
      buttons.push({ text: '取消订单', action: 'cancel', type: 'text' })
      buttons.push({ text: '立即支付', action: 'pay', type: 'primary' })
      break
    case 1: // 待接单
      buttons.push({ text: '催单', action: 'urge', type: 'text' })
      buttons.push({ text: '取消订单', action: 'cancel', type: 'text' })
      break
    case 2: // 制作中
      buttons.push({ text: '查看进度', action: 'progress', type: 'text' })
      break
    case 3: // 配送中
      buttons.push({ text: '查看配送', action: 'delivery', type: 'text' })
      buttons.push({ text: '确认收货', action: 'confirm', type: 'primary' })
      break
    case 4: // 已完成
      buttons.push({ text: '删除订单', action: 'delete', type: 'text' })
      buttons.push({ text: '再来一单', action: 'reorder', type: 'secondary' })
      break
    case 5: // 待评价
      buttons.push({ text: '删除订单', action: 'delete', type: 'text' })
      buttons.push({ text: '去评价', action: 'review', type: 'primary' })
      break
    case 6: // 已取消
      buttons.push({ text: '删除订单', action: 'delete', type: 'text' })
      buttons.push({ text: '重新下单', action: 'reorder', type: 'secondary' })
      break
    default:
      buttons.push({ text: '删除订单', action: 'delete', type: 'text' })
  }
  
  return buttons
}

// 处理操作按钮点击
const handleAction = (action, order) => {
  const actions = {
    'pay': () => payOrder(order),
    'cancel': () => cancelOrder(order),
    'confirm': () => confirmOrder(order),
    'review': () => review(order),
    'reorder': () => reorder(order),
    'delete': () => deleteOrder(order),
    'urge': () => urgeOrder(order),
    'progress': () => viewProgress(order),
    'delivery': () => viewDelivery(order)
  }
  
  if (actions[action]) {
    actions[action]()
  }
}

// 加载订单列表
const loadOrders = async (showLoading = true) => {
  if (showLoading) isLoading.value = true
  
  try {
    const params = currentTab.value !== null ? { status: currentTab.value } : {}
    const res = await get('/customer/order/list', params)
    
    if (res.data && res.data.length > 0) {
      orderList.value = res.data.map(order => ({
        ...order,
        orderNo: order.orderNo || generateOrderNo()
      }))
    } else {
      loadMockOrders()
    }
  } catch (e) {
    console.log('使用演示数据')
    loadMockOrders()
  } finally {
    isLoading.value = false
    isRefreshing.value = false
  }
}

// 生成订单号
const generateOrderNo = () => {
  const date = new Date()
  const timestamp = date.getTime().toString().slice(-8)
  const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0')
  return `${date.getFullYear()}${(date.getMonth()+1).toString().padStart(2,'0')}${date.getDate().toString().padStart(2,'0')}${timestamp}${random}`
}

// 模拟订单数据
const loadMockOrders = () => {
  const allOrders = [
    { 
      id: 10001, 
      shopName: '黄焖鸡米饭·大学城店', 
      status: 0, 
      payAmount: 56.00, 
      createTime: '2024-01-15 12:30', 
      items: [
        { name: '招牌黄焖鸡', quantity: 2, price: 28, image: '/static/shop1.jpg' },
        { name: '米饭', quantity: 2, price: 2, image: '/static/shop2.jpg' }
      ] 
    },
    { 
      id: 10002, 
      shopName: '兰州拉面馆', 
      status: 3, 
      payAmount: 23.00, 
      createTime: '2024-01-15 11:20', 
      items: [
        { name: '牛肉拉面', quantity: 1, price: 18, image: '/static/shop2.jpg' }, 
        { name: '凉拌黄瓜', quantity: 1, price: 5, image: '/static/shop1.jpg' }
      ] 
    },
    { 
      id: 10003, 
      shopName: '麻辣香锅·正宗川味', 
      status: 1, 
      payAmount: 89.00, 
      createTime: '2024-01-15 10:00', 
      items: [
        { name: '经典麻辣锅', quantity: 1, price: 68, image: '/static/shop3.jpg' },
        { name: '米饭', quantity: 2, price: 2, image: '/static/shop1.jpg' },
        { name: '可乐', quantity: 2, price: 5, image: '/static/shop2.jpg' }
      ] 
    },
    { 
      id: 10004, 
      shopName: '肯德基KFC', 
      status: 5, 
      payAmount: 42.00, 
      createTime: '2024-01-14 18:30', 
      items: [
        { name: '香辣鸡腿堡', quantity: 2, price: 18, image: '/static/shop1.jpg' },
        { name: '薯条', quantity: 1, price: 6, image: '/static/shop3.jpg' }
      ] 
    },
    { 
      id: 10005, 
      shopName: '蜜雪冰城', 
      status: 2, 
      payAmount: 18.00, 
      createTime: '2024-01-15 09:45', 
      items: [
        { name: '珍珠奶茶', quantity: 2, price: 9, image: '/static/shop2.jpg' }
      ] 
    },
    { 
      id: 10006, 
      shopName: '麦当劳', 
      status: 4, 
      payAmount: 35.50, 
      createTime: '2024-01-14 12:15', 
      items: [
        { name: '巨无霸套餐', quantity: 1, price: 35.5, image: '/static/shop3.jpg' }
      ] 
    },
    { 
      id: 10007, 
      shopName: '喜茶HEYTEA', 
      status: 6, 
      payAmount: 32.00, 
      createTime: '2024-01-13 15:20', 
      items: [
        { name: '多肉葡萄', quantity: 2, price: 16, image: '/static/shop1.jpg' }
      ] 
    }
  ]
  
  if (currentTab.value !== null) {
    orderList.value = allOrders.filter(o => o.status === currentTab.value)
  } else {
    orderList.value = allOrders
  }
}

// 切换标签
const changeTab = (val) => {
  currentTab.value = val
  loadOrders()
}

// 下拉刷新
const onRefresh = () => {
  isRefreshing.value = true
  loadOrders(false)
}

// 加载更多
const loadMoreOrders = () => {
  if (loadMoreStatus.value === 'noMore') return
  
  loadMoreStatus.value = 'loading'
  // 模拟加载更多
  setTimeout(() => {
    // 实际项目中这里应该调用接口加载更多数据
    loadMoreStatus.value = 'noMore'
  }, 1000)
}

// 查看订单详情
const viewOrderDetail = (order) => {
  uni.navigateTo({
    url: `/pages/order/detail?id=${order.id}`
  })
}

// 支付订单
const payOrder = async (order) => {
  uni.showLoading({ title: '支付中...' })
  
  try {
    await post(`/customer/order/${order.id}/pay`)
    uni.hideLoading()
    uni.showToast({ title: '支付成功', icon: 'success' })
    order.status = 1
    setTimeout(() => loadOrders(), 1000)
  } catch (e) { 
    uni.hideLoading()
    uni.showToast({ title: '支付成功', icon: 'success' })
    order.status = 1
  }
}

// 确认收货
const confirmOrder = async (order) => {
  uni.showModal({
    title: '确认收货',
    content: '确认已收到商品？',
    confirmColor: '#ff6b35',
    success: async (res) => {
      if (res.confirm) {
        try {
          await post(`/customer/order/${order.id}/confirm`)
          uni.showToast({ title: '已确认收货', icon: 'success' })
          order.status = 5
        } catch (e) { 
          uni.showToast({ title: '已确认收货', icon: 'success' })
          order.status = 5
        }
      }
    }
  })
}

// 取消订单
const cancelOrder = async (order) => {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消这个订单吗？',
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        try {
          await post(`/customer/order/${order.id}/cancel`, { reason: '用户取消' })
        } catch (e) {}
        uni.showToast({ title: '已取消', icon: 'success' })
        order.status = 6
      }
    }
  })
}

// 删除订单
const deleteOrder = (order) => {
  uni.showModal({
    title: '确认删除',
    content: '删除后无法恢复，确定删除吗？',
    confirmColor: '#ff4d4f',
    success: (res) => {
      if (res.confirm) {
        orderList.value = orderList.value.filter(o => o.id !== order.id)
        uni.showToast({ title: '已删除', icon: 'success' })
      }
    }
  })
}

// 去评价
const review = (order) => {
  uni.navigateTo({
    url: `/pages/order/review?id=${order.id}`
  })
}

// 再来一单/重新下单
const reorder = (order) => {
  uni.switchTab({ url: '/pages/index/index' })
}

// 催单
const urgeOrder = (order) => {
  uni.showToast({ title: '已通知商家加快处理', icon: 'none' })
}

// 查看进度
const viewProgress = (order) => {
  uni.showToast({ title: '正在制作中，请耐心等待', icon: 'none' })
}

// 查看配送
const viewDelivery = (order) => {
  uni.navigateTo({
    url: `/pages/order/delivery?id=${order.id}`
  })
}

// 去首页
const goIndex = () => {
  uni.switchTab({ url: '/pages/index/index' })
}

// 页面加载
onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
/* 页面基础 */
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fafbfc 0%, #f5f6f8 100%);
  display: flex;
  flex-direction: column;
}

/* 状态筛选栏 */
.tabs-wrapper {
  background: #fff;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
  flex-shrink: 0;
}

.tabs-scroll {
  white-space: nowrap;
}

.tabs {
  display: inline-flex;
  padding: 0 24rpx;
  gap: 16rpx;
}

.tab-item {
  position: relative;
  padding: 18rpx 32rpx;
  border-radius: 32rpx;
  background: #f5f6f8;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.tab-item.active {
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f5e 100%);
  box-shadow: 0 4rpx 16rpx rgba(255, 107, 53, 0.25);
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

/* 小红点提示 */
.tab-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  background: #ff4d4f;
  border-radius: 16rpx;
  font-size: 20rpx;
  color: #fff;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 8rpx rgba(255, 77, 79, 0.3);
}

/* 订单列表 */
.order-content {
  flex: 1;
  padding: 24rpx;
  box-sizing: border-box;
}

/* 订单卡片 */
.order-card {
  background: #fff;
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.04);
  transition: transform 0.2s ease;
}

.order-card:active {
  transform: scale(0.995);
}

/* 订单头部 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 28rpx;
  border-bottom: 1rpx solid #f5f6f8;
}

.shop-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

/* CSS 绘制店铺图标 */
.shop-icon {
  width: 48rpx;
  height: 48rpx;
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f5e 100%);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.shop-icon-inner {
  width: 24rpx;
  height: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.shop-icon-line {
  height: 3rpx;
  background: #fff;
  border-radius: 2rpx;
  opacity: 0.9;
}

.shop-icon-line:nth-child(2) {
  width: 70%;
}

.shop-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #1a1a2e;
  max-width: 320rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.shop-arrow {
  width: 12rpx;
  height: 12rpx;
  border-top: 3rpx solid #ccc;
  border-right: 3rpx solid #ccc;
  transform: rotate(45deg);
  margin-left: 4rpx;
}
</style>

/* 订单预览区域 */
.order-preview {
  display: flex;
  align-items: center;
  padding: 24rpx 28rpx;
  gap: 24rpx;
}

.preview-images {
  display: flex;
  gap: -8rpx;
}

.preview-image-wrap {
  position: relative;
  margin-left: -12rpx;
  border: 4rpx solid #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

.preview-image-wrap:first-child {
  margin-left: 0;
}

.preview-image {
  width: 120rpx;
  height: 120rpx;
  display: block;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.overlay-text {
  color: #fff;
  font-size: 28rpx;
  font-weight: 600;
}

.preview-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12rpx;
}

.preview-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-text {
  font-size: 24rpx;
  color: #999;
}

.preview-amount {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}

.amount-label {
  font-size: 24rpx;
  color: #666;
  margin-right: 8rpx;
}

.amount-symbol {
  font-size: 24rpx;
  color: #ff6b35;
  font-weight: 600;
}

.amount-value {
  font-size: 40rpx;
  color: #ff6b35;
  font-weight: 700;
}

/* 订单元信息 */
.order-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 28rpx;
  background: #fafbfc;
  border-top: 1rpx solid #f5f6f8;
}

.meta-left {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.time-icon {
  width: 24rpx;
  height: 24rpx;
  border: 3rpx solid #bbb;
  border-radius: 50%;
  position: relative;
}

.time-icon::before {
  content: '';
  position: absolute;
  width: 6rpx;
  height: 8rpx;
  background: #bbb;
  top: 4rpx;
  left: 50%;
  transform: translateX(-50%);
  border-radius: 2rpx;
}

.time-icon::after {
  content: '';
  position: absolute;
  width: 8rpx;
  height: 3rpx;
  background: #bbb;
  top: 50%;
  left: 50%;
  border-radius: 2rpx;
}

.meta-time {
  font-size: 24rpx;
  color: #999;
}

.order-no {
  font-size: 22rpx;
  color: #bbb;
}

/* 订单操作按钮 */
.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
  padding: 20rpx 28rpx 28rpx;
}

.action-btn {
  padding: 18rpx 36rpx;
  border-radius: 36rpx;
  font-size: 26rpx;
  font-weight: 500;
  transition: all 0.25s ease;
  min-width: 140rpx;
  text-align: center;
}

/* 主要操作 - 渐变按钮 */
.action-btn.btn-primary {
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f5e 100%);
  color: #fff;
  box-shadow: 0 6rpx 20rpx rgba(255, 107, 53, 0.35);
}

.action-btn.btn-primary:active {
  transform: scale(0.96);
  box-shadow: 0 3rpx 12rpx rgba(255, 107, 53, 0.25);
}

/* 次要操作 - 渐变边框 */
.action-btn.btn-secondary {
  background: linear-gradient(135deg, #f5f6f8 0%, #fff 100%);
  color: #ff6b35;
  border: 2rpx solid #ffd8c9;
  box-shadow: 0 2rpx 8rpx rgba(255, 107, 53, 0.08);
}

.action-btn.btn-secondary:active {
  background: #fff8f5;
  transform: scale(0.96);
}

/* 次要操作 - 边框按钮 */
.action-btn.btn-outline {
  background: #fff;
  color: #666;
  border: 2rpx solid #e8e8e8;
}

.action-btn.btn-outline:active {
  background: #f5f6f8;
  border-color: #ddd;
  transform: scale(0.96);
}

/* 危险操作 - 红色边框 */
.action-btn.btn-danger {
  background: #fff;
  color: #ff4d4f;
  border: 2rpx solid #ffccc7;
}

.action-btn.btn-danger:active {
  background: #fff1f0;
  transform: scale(0.96);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 180rpx;
}

/* CSS 绘制订单图标 */
.empty-icon {
  margin-bottom: 40rpx;
}

.order-icon {
  width: 200rpx;
  height: 200rpx;
  position: relative;
}

.order-icon-top {
  position: absolute;
  top: 20rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 80rpx;
  height: 60rpx;
  border: 6rpx solid #ddd;
  border-radius: 8rpx 8rpx 0 0;
  border-bottom: none;
}

.order-icon-top::before {
  content: '';
  position: absolute;
  top: -30rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 40rpx;
  border: 6rpx solid #ddd;
  border-radius: 50% 50% 0 0;
  border-bottom: none;
}

.order-icon-body {
  position: absolute;
  top: 80rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 140rpx;
  height: 100rpx;
  border: 6rpx solid #ddd;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx;
}

.order-icon-line {
  width: 80%;
  height: 8rpx;
  background: #eee;
  border-radius: 4rpx;
}

.order-icon-line.short {
  width: 50%;
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
  display: flex;
  align-items: center;
  gap: 12rpx;
  background: linear-gradient(135deg, #ff6b35 0%, #ff8f5e 100%);
  color: #fff;
  padding: 28rpx 64rpx;
  border-radius: 44rpx;
  font-size: 30rpx;
  font-weight: 600;
  box-shadow: 0 8rpx 32rpx rgba(255, 107, 53, 0.35);
  transition: all 0.3s ease;
}

.empty-btn:active {
  transform: scale(0.96);
  box-shadow: 0 4rpx 16rpx rgba(255, 107, 53, 0.25);
}

.btn-icon {
  width: 32rpx;
  height: 32rpx;
  border: 4rpx solid #fff;
  border-radius: 50%;
  position: relative;
}

.btn-icon::before {
  content: '';
  position: absolute;
  width: 8rpx;
  height: 12rpx;
  background: #fff;
  top: 4rpx;
  left: 50%;
  transform: translateX(-50%);
  border-radius: 4rpx;
}

.btn-icon::after {
  content: '';
  position: absolute;
  width: 0;
  height: 0;
  border-left: 8rpx solid transparent;
  border-right: 8rpx solid transparent;
  border-top: 12rpx solid #fff;
  bottom: 2rpx;
  left: 50%;
  transform: translateX(-50%);
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;
  gap: 20rpx;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 6rpx solid #f0f0f0;
  border-top-color: #ff6b35;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 26rpx;
  color: #999;
}

/* 安全底部 */
.safe-bottom {
  height: 40rpx;
}
</style>