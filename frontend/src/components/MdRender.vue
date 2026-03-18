<script setup>
import { computed } from 'vue'
import { marked } from 'marked'

const props = defineProps({
  content: { type: String, default: '' },
})

marked.setOptions({
  breaks: true,
  gfm: true,
})

const rendered = computed(() => {
  if (!props.content) return ''
  return marked.parse(props.content)
})
</script>

<template>
  <div class="md-render" v-html="rendered"></div>
</template>

<style scoped>
.md-render {
  line-height: 1.8;
  color: var(--clay-text);
}
.md-render :deep(h1) { font-size: 22px; font-weight: 700; color: var(--clay-tertiary-dark); margin: 20px 0 12px; }
.md-render :deep(h2) { font-size: 18px; font-weight: 600; color: var(--clay-tertiary-dark); margin: 16px 0 10px; }
.md-render :deep(h3) { font-size: 15px; font-weight: 600; color: var(--clay-tertiary); margin: 12px 0 8px; }
.md-render :deep(strong) { color: var(--clay-tertiary-dark); }
.md-render :deep(ul), .md-render :deep(ol) { padding-left: 20px; margin: 8px 0; }
.md-render :deep(li) { margin: 4px 0; }
.md-render :deep(pre) {
  background: var(--clay-bg);
  border-radius: var(--clay-radius-sm);
  padding: 12px 16px;
  overflow-x: auto;
  margin: 8px 0;
}
.md-render :deep(code) {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
}
.md-render :deep(p code) {
  background: rgba(138,182,189,0.15);
  padding: 2px 6px;
  border-radius: 4px;
  color: var(--clay-primary-dark);
}
.md-render :deep(hr) {
  border: none;
  border-top: 1px solid var(--clay-border);
  margin: 16px 0;
}
.md-render :deep(p) { margin: 6px 0; }
.md-render :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0;
  font-size: 14px;
}
.md-render :deep(th) {
  background: rgba(138,182,189,0.2);
  color: var(--clay-tertiary-dark);
  font-weight: 600;
  padding: 10px 12px;
  border: 1px solid var(--clay-border);
  text-align: left;
}
.md-render :deep(td) {
  padding: 8px 12px;
  border: 1px solid var(--clay-border);
}
.md-render :deep(tr:nth-child(even)) {
  background: rgba(138,182,189,0.05);
}
.md-render :deep(blockquote) {
  border-left: 4px solid var(--clay-primary);
  padding: 8px 16px;
  margin: 12px 0;
  background: rgba(138,182,189,0.08);
  border-radius: 0 var(--clay-radius-sm) var(--clay-radius-sm) 0;
}
</style>
