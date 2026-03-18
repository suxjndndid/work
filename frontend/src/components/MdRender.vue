<script setup>
import { computed } from 'vue'

const props = defineProps({
  content: { type: String, default: '' },
})

// Simple markdown renderer - handles common patterns
const rendered = computed(() => {
  if (!props.content) return ''
  let html = props.content
    // Escape HTML
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    // Headers
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    // Bold & Italic
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    // Unordered lists
    .replace(/^[-*] (.+)$/gm, '<li>$1</li>')
    // Ordered lists
    .replace(/^\d+\.\s(.+)$/gm, '<li>$1</li>')
    // Code blocks
    .replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
    // Inline code
    .replace(/`(.+?)`/g, '<code>$1</code>')
    // Horizontal rule
    .replace(/^---$/gm, '<hr/>')
    // Line breaks (preserve blank lines as paragraphs)
    .replace(/\n\n/g, '</p><p>')
    .replace(/\n/g, '<br/>')

  // Wrap consecutive <li> in <ul>
  html = html.replace(/((?:<li>.*?<\/li><br\/>?)+)/g, '<ul>$1</ul>')
  html = html.replace(/<ul>([\s\S]*?)<\/ul>/g, (m, inner) => '<ul>' + inner.replace(/<br\/>/g, '') + '</ul>')

  return '<p>' + html + '</p>'
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
.md-render :deep(ul) { padding-left: 20px; margin: 8px 0; }
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
</style>
