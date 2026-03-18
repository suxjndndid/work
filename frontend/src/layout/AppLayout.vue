<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const isCollapse = ref(false)

const menuItems = [
  { path: '/dashboard', icon: 'HomeFilled', title: '工作台' },
  { path: '/course', icon: 'Notebook', title: '课程管理' },
  { path: '/lesson-plan', icon: 'Document', title: '教案管理' },
  { path: '/exercise', icon: 'EditPen', title: '练习题管理' },
  { path: '/student', icon: 'User', title: '学生管理' },
  { path: '/score', icon: 'TrendCharts', title: '成绩管理' },
  { path: '/knowledge-point', icon: 'Connection', title: '知识点管理' },
  { path: '/analytics', icon: 'DataAnalysis', title: '学情分析' },
  { path: '/prep-plan', icon: 'Files', title: '备课方案' },
]

const activeMenu = computed(() => route.path)
const userName = computed(() => userStore.userInfo?.realName || userStore.userInfo?.username || '用户')

async function handleLogout() {
  await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
  await userStore.logout()
  router.push('/login')
}

// fetch user info on mount
if (userStore.token && !userStore.userInfo) {
  userStore.fetchInfo().catch(() => {})
}
</script>

<template>
  <div class="app-layout">
    <!-- Sidebar -->
    <aside class="sidebar" :class="{ 'is-collapse': isCollapse }">
      <div class="sidebar-logo">
        <el-icon :size="28" color="#F9D8C5"><MagicStick /></el-icon>
        <span v-show="!isCollapse" class="logo-text">AI备课助手</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="transparent"
        text-color="rgba(255,255,255,0.75)"
        active-text-color="#F9D8C5"
        router
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- Main area -->
    <div class="main-area">
      <!-- Header -->
      <header class="app-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse" :size="20">
            <Fold v-if="!isCollapse" /><Expand v-else />
          </el-icon>
          <span class="page-title">{{ route.meta.title }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleLogout">
            <div class="user-info">
              <el-avatar :size="32" style="background: var(--clay-primary);">
                {{ userName.charAt(0) }}
              </el-avatar>
              <span class="user-name">{{ userName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- Content -->
      <main class="app-content">
        <router-view v-slot="{ Component }">
          <transition name="slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script>
import { SwitchButton } from '@element-plus/icons-vue'
export default { components: { SwitchButton } }
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 220px;
  background: linear-gradient(180deg, var(--clay-tertiary-dark) 0%, var(--clay-tertiary) 100%);
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  flex-shrink: 0;
  box-shadow: 4px 0 15px rgba(0,0,0,0.08);
  z-index: 10;
}
.sidebar.is-collapse { width: 64px; }

.sidebar-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  padding: 0 16px;
}
.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  white-space: nowrap;
  letter-spacing: 1px;
}

.sidebar :deep(.el-menu) {
  flex: 1;
  padding: 8px;
}
.sidebar :deep(.el-menu-item) {
  border-radius: var(--clay-radius-sm);
  margin-bottom: 4px;
  height: 44px;
}
.sidebar :deep(.el-menu-item:hover) {
  background: rgba(255,255,255,0.1) !important;
}
.sidebar :deep(.el-menu-item.is-active) {
  background: rgba(249,216,197,0.15) !important;
  font-weight: 600;
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.app-header {
  height: 64px;
  background: var(--clay-card);
  box-shadow: 0 2px 8px rgba(101,123,127,0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.collapse-btn {
  cursor: pointer;
  color: var(--clay-tertiary);
  transition: var(--clay-transition);
}
.collapse-btn:hover { color: var(--clay-primary); }

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--clay-tertiary-dark);
}

.header-right { display: flex; align-items: center; }
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--clay-radius-sm);
  transition: var(--clay-transition);
}
.user-info:hover { background: var(--clay-bg); }
.user-name {
  font-size: 14px;
  color: var(--clay-tertiary);
}

.app-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
</style>
