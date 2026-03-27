// 获取登录用户信息

import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getLoginUser } from '@/api/userController.ts'
import { getAccessToken, clearTokens } from '@/utils/auth'

export const useLoginUserStore = defineStore('loginUser', () => {
  // 默认值
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })

  // 获取用户登录信息
  async function fetchLoginUser() {
    if (!getAccessToken()) {
      loginUser.value = { userName: '未登录' }
      return
    }
    const res = await getLoginUser()
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data
    } else {
      clearTokens()
      loginUser.value = { userName: '未登录' }
    }
  }

  // 跟新登录用户信息
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return {
    loginUser,
    fetchLoginUser,
    setLoginUser,
  }
})
