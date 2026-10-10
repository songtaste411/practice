<template>
  <div class="login-wrap">
    <div class="login-card">
      <h1 class="page-title">老兵诊所</h1>
      <p class="page-desc">先把房子盖好。明天开始，一天送一个线上故障进来，你来修。</p>
      <div class="hint">演示账号 demo，密码 demo123。金额在系统里按「分」计算，页面上显示成元。</div>
      <el-form @submit.prevent="submit">
        <el-form-item label="账号">
          <el-input v-model="username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" show-password autocomplete="current-password" @keyup.enter="submit" />
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width:100%" @click="submit">进入诊所</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/http'
import { useSession } from '../stores/session'

const username = ref('demo')
const password = ref('demo123')
const loading = ref(false)
const router = useRouter()
const session = useSession()

async function submit() {
  loading.value = true
  try {
    const data = await api<{ token: string; displayName: string }>('post', '/api/auth/login', {
      username: username.value,
      password: password.value
    })
    session.save(data.token, data.displayName)
    router.push('/fulfill')
  } finally {
    loading.value = false
  }
}
</script>
