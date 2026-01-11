<template>
  <el-card>
    <template #header><span>商家管理</span></template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="账号" />
      <el-table-column prop="contactName" label="联系人" />
      <el-table-column prop="phone" label="电话" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="success" size="small" @click="audit(row, true)">通过</el-button>
          <el-button v-if="row.status === 0" type="danger" size="small" @click="audit(row, false)">驳回</el-button>
          <el-button v-if="row.status === 1" type="warning" size="small" @click="audit(row, false)">禁用</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 20px" background layout="prev, pager, next" :total="total" :page-size="10" v-model:current-page="page" @current-change="loadData" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'

const list = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)

const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }[s])
const statusText = (s) => ({ 0: '待审核', 1: '已通过', 2: '已驳回', 3: '已禁用' }[s])

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/merchant/list', { params: { page: page.value, size: 10 } })
    list.value = res.data.records
    total.value = res.data.total
  } catch (e) { console.error(e) }
  loading.value = false
}

const audit = async (row, approved) => {
  let reason = ''
  if (!approved) {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回商家', { confirmButtonText: '确定', cancelButtonText: '取消' })
    reason = value
  }
  try {
    await request.post(`/admin/merchant/${row.id}/audit`, { approved, reason })
    ElMessage.success('操作成功')
    loadData()
  } catch (e) { console.error(e) }
}

onMounted(loadData)
</script>
