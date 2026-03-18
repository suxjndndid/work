<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getKnowledgePointList, createKnowledgePoint, updateKnowledgePoint, deleteKnowledgePoint, getCourseList } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const courses = ref([])
const selectedCourseId = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({ id: null, name: '', courseId: '', parentId: null, description: '' })

async function loadList() {
  if (!selectedCourseId.value) { list.value = []; return }
  loading.value = true
  try {
    const res = await getKnowledgePointList(selectedCourseId.value)
    list.value = res.data || []
  } catch {}
  loading.value = false
}

async function loadCourses() {
  try {
    const res = await getCourseList({ current: 1, size: 100 })
    courses.value = res.data?.records || res.data || []
  } catch {}
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', courseId: selectedCourseId.value, parentId: null, description: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, { id: row.id, name: row.name, courseId: row.courseId, parentId: row.parentId, description: row.description })
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.name) { ElMessage.warning('请输入知识点名称'); return }
  try {
    if (isEdit.value) await updateKnowledgePoint(form.id, form)
    else await createKnowledgePoint(form)
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadList()
  } catch {}
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除知识点「${row.name}」？`, '提示', { type: 'warning' })
  await deleteKnowledgePoint(row.id)
  ElMessage.success('删除成功')
  loadList()
}

onMounted(loadCourses)
</script>

<template>
  <div>
    <div class="page-header">
      <h2>知识点管理</h2>
      <el-button type="primary" @click="selectedCourseId ? openCreate() : ElMessage.warning('请先在左侧选择一个课程')"><el-icon><Plus /></el-icon>添加知识点</el-button>
    </div>

    <div class="filter-bar">
      <el-select v-model="selectedCourseId" placeholder="请先选择课程" style="width:240px" @change="loadList">
        <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
    </div>

    <el-card v-if="selectedCourseId">
      <el-table :data="list" v-loading="loading" row-key="id" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" stripe>
        <el-table-column prop="name" label="知识点名称" min-width="200" />
        <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-card v-else>
      <el-empty description="请先选择课程查看知识点" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑知识点' : '添加知识点'" width="460px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="父知识点">
          <el-select v-model="form.parentId" placeholder="无（顶级）" clearable style="width:100%">
            <el-option v-for="kp in list" :key="kp.id" :label="kp.name" :value="kp.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
