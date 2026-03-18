<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getExerciseList, generateExercise, updateExercise, deleteExercise, getCourseList, getLessonPlanList } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const generating = ref(false)
const list = ref([])
const total = ref(0)
const courses = ref([])
const query = reactive({ current: 1, size: 10, courseId: '', lessonPlanId: '' })
const genDialogVisible = ref(false)
const genForm = reactive({ lessonPlanId: '', courseId: '', topic: '', questionType: '选择题', difficulty: '中等', count: 5 })
const editDialogVisible = ref(false)
const editForm = reactive({ id: null, questionContent: '', answer: '', questionType: '', difficulty: '', options: '' })

const questionTypes = ['选择题', '填空题', '判断题', '简答题', '计算题']
const difficulties = ['简单', '中等', '困难']

async function loadList() {
  loading.value = true
  try {
    const res = await getExerciseList(query)
    list.value = res.data?.records || res.data || []
    total.value = res.data?.total || list.value.length
  } catch {}
  loading.value = false
}

async function loadCourses() {
  try {
    const res = await getCourseList({ current: 1, size: 100 })
    courses.value = res.data?.records || res.data || []
  } catch {}
}

function openGenerate() {
  Object.assign(genForm, { lessonPlanId: '', courseId: '', topic: '', questionType: '选择题', difficulty: '中等', count: 5 })
  genDialogVisible.value = true
}

async function handleGenerate() {
  if (!genForm.topic) { ElMessage.warning('请输入题目主题'); return }
  generating.value = true
  try {
    await generateExercise(genForm)
    ElMessage.success('习题生成成功！')
    genDialogVisible.value = false
    loadList()
  } catch {}
  generating.value = false
}

function openEdit(row) {
  Object.assign(editForm, {
    id: row.id,
    questionContent: row.questionContent,
    answer: row.answer,
    questionType: row.questionType,
    difficulty: row.difficulty,
    options: typeof row.options === 'string' ? row.options : JSON.stringify(row.options || '', null, 2),
  })
  editDialogVisible.value = true
}

async function handleSaveEdit() {
  try {
    await updateExercise(editForm.id, editForm)
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    loadList()
  } catch {}
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除此习题？', '提示', { type: 'warning' })
  await deleteExercise(row.id)
  ElMessage.success('删除成功')
  loadList()
}

function difficultyType(d) {
  return d === '简单' ? 'success' : d === '中等' ? 'warning' : 'danger'
}

onMounted(() => { loadList(); loadCourses() })
</script>

<template>
  <div>
    <div class="page-header">
      <h2>练习题管理</h2>
      <el-button type="primary" @click="openGenerate"><el-icon><MagicStick /></el-icon>AI 生成习题</el-button>
    </div>

    <div class="filter-bar">
      <el-select v-model="query.courseId" placeholder="选择课程" clearable style="width:180px" @change="loadList">
        <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-select v-model="query.questionType" placeholder="题型" clearable style="width:120px" @change="loadList">
        <el-option v-for="t in questionTypes" :key="t" :label="t" :value="t" />
      </el-select>
      <el-button type="primary" @click="loadList"><el-icon><Search /></el-icon>搜索</el-button>
    </div>

    <el-card>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="questionContent" label="题目" min-width="280" show-overflow-tooltip />
        <el-table-column prop="questionType" label="题型" width="100">
          <template #default="{ row }"><el-tag size="small" type="info">{{ row.questionType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="80">
          <template #default="{ row }"><el-tag size="small" :type="difficultyType(row.difficulty)">{{ row.difficulty }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="answer" label="答案" min-width="200" show-overflow-tooltip />
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

    <!-- AI Generate Dialog -->
    <el-dialog v-model="genDialogVisible" title="AI 生成习题" width="520px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="课程">
          <el-select v-model="genForm.courseId" placeholder="选择课程" style="width:100%">
            <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="主题"><el-input v-model="genForm.topic" placeholder="例：集合的概念" /></el-form-item>
        <el-form-item label="题型">
          <el-select v-model="genForm.questionType" style="width:100%">
            <el-option v-for="t in questionTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-radio-group v-model="genForm.difficulty">
            <el-radio v-for="d in difficulties" :key="d" :value="d">{{ d }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="数量"><el-input-number v-model="genForm.count" :min="1" :max="20" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="genDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="generating" @click="handleGenerate"><el-icon><MagicStick /></el-icon>&nbsp;开始生成</el-button>
      </template>
    </el-dialog>

    <!-- Edit Dialog -->
    <el-dialog v-model="editDialogVisible" title="编辑习题" width="560px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="题型">
          <el-select v-model="editForm.questionType" style="width:100%">
            <el-option v-for="t in questionTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-radio-group v-model="editForm.difficulty">
            <el-radio v-for="d in difficulties" :key="d" :value="d">{{ d }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题目"><el-input v-model="editForm.questionContent" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="选项"><el-input v-model="editForm.options" type="textarea" :rows="3" placeholder="JSON格式选项" /></el-form-item>
        <el-form-item label="答案"><el-input v-model="editForm.answer" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
