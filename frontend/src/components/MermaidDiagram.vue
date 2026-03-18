<script setup>
import { ref, watch, onMounted, nextTick } from 'vue'
import mermaid from 'mermaid'

const props = defineProps({
  code: { type: String, default: '' },
  title: { type: String, default: '' },
  description: { type: String, default: '' },
})

const container = ref(null)
const error = ref('')
let renderCount = 0

mermaid.initialize({
  startOnLoad: false,
  theme: 'base',
  themeVariables: {
    primaryColor: '#8AB6BD',
    primaryTextColor: '#657B7F',
    primaryBorderColor: '#657B7F',
    lineColor: '#657B7F',
    secondaryColor: '#F9D8C5',
    tertiaryColor: '#E8F4F6',
    fontFamily: 'inherit',
  },
  flowchart: { useMaxWidth: true, htmlLabels: true, curve: 'basis' },
  securityLevel: 'loose',
})

async function renderDiagram() {
  if (!props.code || !container.value) return
  error.value = ''
  try {
    const id = `mermaid-${++renderCount}-${Date.now()}`
    const { svg } = await mermaid.render(id, props.code)
    container.value.innerHTML = svg
  } catch (e) {
    error.value = e.message || '图表渲染失败'
    container.value.innerHTML = ''
  }
}

watch(() => props.code, () => nextTick(renderDiagram))
onMounted(() => nextTick(renderDiagram))
</script>

<template>
  <div class="mermaid-diagram">
    <div v-if="title" class="mermaid-title">{{ title }}</div>
    <div ref="container" class="mermaid-container"></div>
    <div v-if="error" class="mermaid-error">
      <p style="font-weight:600;margin-bottom:8px;">图表渲染失败</p>
      <pre style="font-size:12px;white-space:pre-wrap;">{{ error }}</pre>
      <details style="margin-top:8px;">
        <summary style="cursor:pointer;font-size:12px;color:var(--clay-text-light);">查看Mermaid源码</summary>
        <pre style="margin-top:8px;font-size:12px;white-space:pre-wrap;background:var(--clay-bg);padding:12px;border-radius:8px;">{{ code }}</pre>
      </details>
    </div>
    <div v-if="description" class="mermaid-desc">{{ description }}</div>
  </div>
</template>

<style scoped>
.mermaid-diagram {
  text-align: center;
}
.mermaid-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--clay-tertiary-dark);
  margin-bottom: 12px;
}
.mermaid-container {
  overflow-x: auto;
  padding: 8px;
}
.mermaid-container :deep(svg) {
  max-width: 100%;
  height: auto;
}
.mermaid-error {
  background: rgba(219, 130, 130, 0.1);
  border: 1px solid rgba(219, 130, 130, 0.3);
  border-radius: 8px;
  padding: 12px 16px;
  color: #B54747;
  text-align: left;
}
.mermaid-desc {
  font-size: 13px;
  color: var(--clay-text-light);
  margin-top: 12px;
  font-style: italic;
}
</style>
