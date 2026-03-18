<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getLessonPlan, updateLessonPlan, regenerateLessonPlan, getLessonPlanVersions, extractKeywords, generateImage } from '../../api'
import MdRender from '../../components/MdRender.vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const id = route.params.id
const loading = ref(false)
const plan = ref({})
const versions = ref([])
const editing = ref(false)
const editContent = ref('')
const regenerating = ref(false)
const keywords = ref([])
const extractingKw = ref(false)
const generatingImg = ref(false)
const generatedImageUrl = ref('')

async function loadPlan() {
  loading.value = true
  try {
    const res = await getLessonPlan(id)
    plan.value = res.data || {}
    editContent.value = plan.value.content || ''
  } catch {}
  loading.value = false
}

async function loadVersions() {
  try {
    const res = await getLessonPlanVersions(id)
    versions.value = res.data || []
  } catch {}
}

async function handleSave() {
  try {
    await updateLessonPlan(id, { ...plan.value, content: editContent.value })
    ElMessage.success('保存成功')
    editing.value = false
    loadPlan()
  } catch {}
}

async function handleRegenerate() {
  regenerating.value = true
  try {
    await regenerateLessonPlan(id)
    ElMessage.success('重新生成成功')
    loadPlan()
  } catch {}
  regenerating.value = false
}

async function handleExtractKeywords() {
  extractingKw.value = true
  try {
    const res = await extractKeywords(id)
    keywords.value = res.data || []
    ElMessage.success('关键词提取成功')
  } catch {}
  extractingKw.value = false
}

async function handleGenerateImage(kw) {
  generatingImg.value = true
  try {
    const res = await generateImage(id, kw)
    generatedImageUrl.value = res.data?.url || res.data || ''
    ElMessage.success('图片生成成功')
  } catch {}
  generatingImg.value = false
}

onMounted(() => { loadPlan(); loadVersions() })
</script>

<template>
  <div v-loading="loading">
    <div class="page-header">
      <div style="display:flex;align-items:center;gap:12px;">
        <el-button @click="router.back()" circle><el-icon><ArrowLeft /></el-icon></el-button>
        <h2>{{ plan.topic || '教案详情' }}</h2>
        <el-tag v-if="plan.subject" size="small">{{ plan.subject }}</el-tag>
        <el-tag v-if="plan.grade" size="small" type="warning">{{ plan.grade }}</el-tag>
      </div>
      <div style="display:flex;gap:12px;">
        <el-button v-if="!editing" @click="editing = true"><el-icon><Edit /></el-icon>编辑</el-button>
        <el-button v-if="editing" type="primary" @click="handleSave"><el-icon><Check /></el-icon>保存</el-button>
        <el-button v-if="editing" @click="editing = false; editContent = plan.content">取消</el-button>
        <el-button type="warning" :loading="regenerating" @click="handleRegenerate"><el-icon><Refresh /></el-icon>重新生成</el-button>
      </div>
    </div>

    <!-- Info cards -->
    <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;margin-bottom:24px;">
      <div class="clay-stat-card">
        <div style="font-size:13px;opacity:0.8;">教学目标</div>
        <div style="font-size:15px;font-weight:600;margin-top:4px;">{{ plan.objectives || '-' }}</div>
      </div>
      <div class="clay-stat-card">
        <div style="font-size:13px;opacity:0.8;">课时</div>
        <div style="font-size:15px;font-weight:600;margin-top:4px;">{{ plan.duration || 45 }} 分钟</div>
      </div>
      <div class="clay-stat-card">
        <div style="font-size:13px;opacity:0.8;">版本数</div>
        <div style="font-size:15px;font-weight:600;margin-top:4px;">{{ versions.length }}</div>
      </div>
    </div>

    <!-- Content -->
    <el-card style="margin-bottom:24px;">
      <template #header>
        <span style="font-weight:600;">教案内容</span>
      </template>
      <el-input v-if="editing" v-model="editContent" type="textarea" :rows="20" />
      <MdRender v-else :content="plan.content || '暂无内容，请点击重新生成或手动编辑'" style="min-height:200px;" />
    </el-card>

    <!-- AI Tools row -->
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px;">
      <!-- Keywords -->
      <el-card>
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center;">
            <span style="font-weight:600;">AI 图片关键词</span>
            <el-button type="primary" size="small" :loading="extractingKw" @click="handleExtractKeywords">
              <el-icon><MagicStick /></el-icon>&nbsp;提取关键词
            </el-button>
          </div>
        </template>
        <div v-if="keywords.length" style="display:flex;flex-wrap:wrap;gap:8px;">
          <el-tag v-for="kw in keywords" :key="kw" size="large" style="cursor:pointer;" @click="handleGenerateImage(kw)">
            {{ kw }}<el-icon style="margin-left:4px;"><Picture /></el-icon>
          </el-tag>
        </div>
        <el-empty v-else description="点击提取关键词后，可点击关键词生成图片" :image-size="60" />
      </el-card>

      <!-- Generated image -->
      <el-card>
        <template #header>
          <span style="font-weight:600;">AI 生成图片</span>
        </template>
        <div v-if="generatingImg" style="text-align:center;padding:40px;">
          <el-icon class="is-loading" :size="32" color="var(--clay-primary)"><Loading /></el-icon>
          <p style="margin-top:12px;color:var(--clay-text-light);">图片生成中...</p>
        </div>
        <el-image v-else-if="generatedImageUrl" :src="generatedImageUrl" fit="contain" style="width:100%;max-height:300px;border-radius:var(--clay-radius-sm);" />
        <el-empty v-else description="选择关键词生成教学图片" :image-size="60" />
      </el-card>
    </div>

    <!-- Version history -->
    <el-card v-if="versions.length" style="margin-top:24px;">
      <template #header><span style="font-weight:600;">版本历史</span></template>
      <el-timeline>
        <el-timeline-item v-for="v in versions" :key="v.id" :timestamp="v.createdAt || v.createTime" placement="top">
          <p>版本 {{ v.version || v.id }}</p>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>
