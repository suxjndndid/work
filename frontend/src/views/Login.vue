<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const form = reactive({ username: '', password: '' })

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push('/')
  } catch { /* handled by interceptor */ }
  finally { loading.value = false }
}
</script>

<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-circle c1"></div>
      <div class="bg-circle c2"></div>
      <div class="bg-circle c3"></div>
    </div>
    <div class="login-card">
      <div class="login-header">
        <el-icon :size="40" color="var(--clay-primary)"><MagicStick /></el-icon>
        <h1>AI 备课助手</h1>
        <p>智能教案生成与学情分析系统</p>
      </div>
      <el-form @submit.prevent="handleLogin" class="login-form">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" placeholder="密码" type="password" show-password size="large" :prefix-icon="Lock" @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="handleLogin" style="width:100%;height:48px;font-size:16px;border-radius:var(--clay-radius);margin-top:8px;">
          登 录
        </el-button>
      </el-form>
      <div class="login-footer">
        <span>默认账号: teacher1 / 123456</span>
      </div>
    </div>
  </div>
</template>

<script>
import { User, Lock } from '@element-plus/icons-vue'
export default { components: { User, Lock } }
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--clay-primary-light) 0%, var(--clay-bg) 50%, var(--clay-secondary-light) 100%);
  position: relative;
  overflow: hidden;
}
.login-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.3;
}
.c1 { width: 400px; height: 400px; background: var(--clay-primary); top: -100px; left: -100px; }
.c2 { width: 300px; height: 300px; background: var(--clay-secondary); bottom: -80px; right: -60px; }
.c3 { width: 200px; height: 200px; background: var(--clay-tertiary); top: 50%; right: 20%; opacity: 0.15; }

.login-card {
  width: 420px;
  background: var(--clay-card);
  border-radius: var(--clay-radius-xl);
  box-shadow: var(--clay-shadow), 0 20px 60px rgba(101,123,127,0.12);
  padding: 48px 40px;
  position: relative;
  z-index: 1;
}
.login-header {
  text-align: center;
  margin-bottom: 36px;
}
.login-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: var(--clay-tertiary-dark);
  margin: 12px 0 8px;
}
.login-header p {
  font-size: 14px;
  color: var(--clay-text-light);
}
.login-form { margin-top: 8px; }
.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 12px;
  color: var(--clay-text-light);
}
</style>
