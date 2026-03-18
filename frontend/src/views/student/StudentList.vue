<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getStudentList, createStudent, updateStudent, deleteStudent } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, keyword: '' })
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({ id: null, name: '', studentNo: '', gender: null, className: '' })

async function loadList() {
  loading.value = true
  try {
    const res = await getStudentList(query)
    list.value = res.data?.records || res.data || []
    total.value = res.data?.total || list.value.length
  } catch {}
  loading.value = false
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', studentNo: '', gender: null, className: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, { id: row.id, name: row.name, studentNo: row.studentNo, gender: row.gender, className: row.className })
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.name) { ElMessage.warning('请输入学生姓名'); return }
  try {
    if (isEdit.value) await updateStudent(form.id, form)
    else await createStudent(form)
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadList()
  } catch {}
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除学生「${row.name}」？`, '提示', { type: 'warning' })
  await deleteStudent(row.id)
  ElMessage.success('删除成功')
  loadList()
}

onMounted(loadList)
</script>

<template>
  <div>
    <div class="page-header">
      <h2>学生管理</h2>
      <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>添加学生</el-button>
    </div>

    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索学生姓名/学号" clearable style="width:240px" @clear="loadList" @keyup.enter="loadList" />
      <el-button type="primary" @click="loadList"><el-icon><Search /></el-icon>搜索</el-button>
    </div>

    <el-card>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">{{ row.gender === 1 || row.gender === '男' ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="className" label="班级" width="120">
          <template #default="{ row }">{{ row.className || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display:flex;justify-content:flex-end;margin-top:16px;">
        <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="loadList" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑学生' : '添加学生'" width="460px" destroy-on-close>
      <el-form label-width="70px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="学号"><el-input v-model="form.studentNo" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="班级"><el-input v-model="form.className" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
