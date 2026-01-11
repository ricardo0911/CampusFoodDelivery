<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>订单管理</span>
        <el-radio-group v-model="status" @change="loadData">
          <el-radio-button :label="null">全部</el-radio-button>
          <el-radio-button :label="1">待接单</el-radio-button>
          <el-radio-button :label="2">制作中</el-radio-button>
          <el-radio-button :label="3">配送中</el-radio-button>
          <el-radio-button :label="4">已完成</el-radio-button>
        </el-radio-group>
      </div>
    </template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="contactName" label="联系人" />
      <el-table-column prop="contactPhone" label="电话" />
      <el-table-column prop="payAmount" label="金额">
        <template #default="{ row }">¥{{ row.payAmount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" type="primary" size="small" @click="accept(row)">接单</el-button>
          <el-button v-if="row.status === 2" type="success" size="small" @click="deliver(row)">配送</el-button>
          <el-button v-if="row.status === 3" type="success" size="small" @click="complete(row)">完成</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const list = ref([])
const loading = ref(false)
const status = ref(null)

const statusType = (s) => ['warning','info','primary','','success','danger'][s]
const statusText = (s) => ['待支付','待接单','制作中','配送中','已完成','已取消'][s]

const loadData = async () => {
  loading.value = true
  try {
    const params = status.value !== null ? { status: status.value } : {}
    const res = await request.get('/merchant/order/list', { params })
    list.value = res.data
  } catch (e) { console.error(e) }
  loading.value = false
}

const accept = async (row) => {
  await request.post(`/merchant/order/${row.id}/accept`)
  ElMessage.success('接单成功')
  loadData()
}

const deliver = async (row) => {
  await request.post(`/merchant/order/${row.id}/deliver`)
  ElMessage.success('开始配送')
  loadData()
}

const complete = async (row) => {
  await request.post(`/merchant/order/${row.id}/complete`)
  ElMessage.success('订单完成')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; }
</style>
