import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo } from '../api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  async function login(form) {
    const res = await loginApi(form)
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    userInfo.value = res.data
    return res
  }

  async function fetchInfo() {
    const res = await getUserInfo()
    userInfo.value = res.data
    return res.data
  }

  async function logout() {
    try { await logoutApi() } catch {}
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return { token, userInfo, login, fetchInfo, logout }
})
