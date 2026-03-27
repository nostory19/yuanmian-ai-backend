<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { health } from '@/api/healthController.ts'
import {useLoginUserStore} from '@/stores/loginUser.ts'
// 测试后端接口
health().then((res) => {
  console.log(res)
})

// const loginUserStore = useLoginUserStore()
// loginUserStore.fetchLoginUser()



</script>

<template>
  <BasicLayout />
</template>

<style scoped>
/* App.vue 样式已迁移到 BasicLayout.vue */
</style>
