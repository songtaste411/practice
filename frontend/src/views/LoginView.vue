<template>
  <div class="login-wrap">
    <div class="login-card">
      <h1 class="page-title">老兵诊所</h1>
      <p class="page-desc">这是一个小商店。你先把「选商品、下单、付款」走一遍。商店现在是好的，以后才会加故障。</p>
      <div class="hint">账号和密码已经填好了：demo / demo123。点下面的按钮就能进去。</div>
      <el-form @submit.prevent="submit">
        <el-form-item label="账号">
          <el-input v-model="username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" show-password autocomplete="current-password" @keyup.enter="submit" />
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width:100%" @click="submit">进去下单</el-button>
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
