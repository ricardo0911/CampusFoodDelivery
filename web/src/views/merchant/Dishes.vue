<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>菜品管理</span>
        <el-button type="primary" @click="showDialog()">添加菜品</el-button>
      </div>
    </template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="name" label="菜品名称" />
      <el-table-column prop="price" label="价格">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" />
      <el-table-column prop="sales" label="销量" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="showDialog(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button size="small" type="danger" @click="deleteDish(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑菜品' : '添加菜品'" width="500px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
      <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
      <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="saveDish">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({ id: null, name: '', price: 0, stock: 100, description: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/merchant/dish/list')
    list.value = res.data
  } catch (e) { console.error(e) }
  loading.value = false
}

const showDialog = (row) => {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, row)
  } else {
    form.id = null; form.name = ''; form.price = 0; form.stock = 100; form.description = ''
  }
  dialogVisible.value = true
}

const saveDish = async () => {
  try {
    if (isEdit.value) {
      await request.put(`/merchant/dish/update/${form.id}`, form)
    } else {
      await request.post('/merchant/dish/add', form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (e) { console.error(e) }
}

const toggleStatus = async (row) => {
  try {
    await request.post(`/merchant/dish/toggle-status/${row.id}`)
    loadData()
  } catch (e) { console.error(e) }
}

const deleteDish = async (row) => {
  await ElMessageBox.confirm('确定删除此菜品?', '提示')
  try {
    await request.delete(`/merchant/dish/delete/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { console.error(e) }
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
