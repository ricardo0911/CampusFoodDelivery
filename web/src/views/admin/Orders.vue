<template>
  <el-card>
    <template #header><span>订单管理</span></template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="shopName" label="店铺" />
      <el-table-column prop="contactName" label="联系人" />
      <el-table-column prop="payAmount" label="金额">
        <template #default="{ row }">¥{{ row.payAmount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="下单时间" width="180" />
    </el-table>
    <el-pagination style="margin-top: 20px" background layout="prev, pager, next" :total="total" :page-size="10" v-model:current-page="page" @current-change="loadData" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'

const list = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)

const statusType = (s) => ['warning','info','primary','','success','danger','warning','success'][s]
const statusText = (s) => ['待支付','待接单','制作中','配送中','已完成','已取消','退款中','已退款'][s]

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/order/list', { params: { page: page.value, size: 10 } })
    list.value = res.data.records
    total.value = res.data.total
  } catch (e) { console.error(e) }
  loading.value = false
}

onMounted(loadData)
</script>
