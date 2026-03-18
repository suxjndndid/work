<script setup>
import { ref, reactive, onMounted } from 'vue'
import { generatePrepPlan, getCourseList, getLessonPlanList } from '../../api'
import MdRender from '../../components/MdRender.vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const generating = ref(false)
const courses = ref([])
const lessonPlans = ref([])
const genForm = reactive({ courseId: '', lessonPlanId: '', requirements: '' })
const prepPlanResult = ref(null)
const history = ref([])

async function loadCourses() {
  try {
    const res = await getCourseList({ current: 1, size: 100 })
    courses.value = res.data?.records || res.data || []
  } catch {}
}

async function loadLessonPlans() {
  if (!genForm.courseId) return
  try {
    const res = await getLessonPlanList({ courseId: genForm.courseId, current: 1, size: 50 })
    lessonPlans.value = res.data?.records || res.data || []
  } catch {}
}

async function handleGenerate() {
  if (!genForm.courseId) { ElMessage.warning('请选择课程'); return }
  generating.value = true
  try {
    const res = await generatePrepPlan(genForm)
    prepPlanResult.value = res.data
    ElMessage.success('备课方案生成成功！')
  } catch {}
  generating.value = false
}

onMounted(loadCourses)
</script>

<template>
  <div>
    <div class="page-header">
      <h2>备课方案</h2>
    </div>

    <div style="display:grid;grid-template-columns:360px 1fr;gap:24px;">
      <!-- Left: form -->
      <div class="clay-card">
        <h4 style="margin-bottom:20px;color:var(--clay-tertiary-dark);">生成备课方案</h4>
        <el-form label-position="top">
          <el-form-item label="课程">
            <el-select v-model="genForm.courseId" placeholder="选择课程" style="width:100%" @change="loadLessonPlans">
              <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="教案（可选）">
            <el-select v-model="genForm.lessonPlanId" placeholder="选择教案" clearable style="width:100%">
              <el-option v-for="lp in lessonPlans" :key="lp.id" :label="lp.topic || lp.title" :value="lp.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="额外要求">
            <el-input v-model="genForm.requirements" type="textarea" :rows="4" placeholder="可选：对备课方案的特殊要求" />
          </el-form-item>
          <el-button type="primary" style="width:100%;" :loading="generating" @click="handleGenerate" size="large">
            <el-icon><MagicStick /></el-icon>&nbsp;生成备课方案
          </el-button>
        </el-form>
        <div style="margin-top:20px;padding-top:16px;border-top:1px solid var(--clay-border);">
          <p style="font-size:12px;color:var(--clay-text-light);line-height:1.6;">
            备课方案将整合教案、资源、习题和学情推荐，生成一份完整的教学准备方案。
          </p>
        </div>
      </div>

      <!-- Right: result -->
      <div>
        <div v-if="generating" class="clay-card" style="text-align:center;padding:60px;">
          <el-icon class="is-loading" :size="48" color="var(--clay-primary)"><Loading /></el-icon>
          <p style="margin-top:16px;color:var(--clay-text-light);font-size:15px;">正在整合生成备课方案，请稍候...</p>
        </div>
        <div v-else-if="prepPlanResult" class="clay-card">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px;">
            <h3 style="color:var(--clay-tertiary-dark);">备课方案</h3>
            <el-tag type="success">已生成</el-tag>
          </div>
          <MdRender :content="typeof prepPlanResult === 'string' ? prepPlanResult : prepPlanResult.content || JSON.stringify(prepPlanResult, null, 2)" />
        </div>
        <div v-else class="clay-card" style="text-align:center;padding:60px;">
          <el-icon :size="48" color="var(--clay-text-light)"><Files /></el-icon>
          <p style="margin-top:16px;color:var(--clay-text-light);font-size:15px;">选择课程和教案，点击生成备课方案</p>
        </div>
      </div>
    </div>
  </div>
</template>
