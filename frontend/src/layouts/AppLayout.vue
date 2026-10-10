<template>
  <div class="shell">
    <aside class="side">
      <div class="brand">
        老兵诊所
        <small>一个能下单的小商店</small>
      </div>
      <nav class="nav">
        <router-link to="/fulfill">下单</router-link>
        <router-link to="/incidents">故障开关</router-link>
        <router-link to="/map">程序怎么分</router-link>
      </nav>
      <div class="who">
        <div>{{ session.displayName || '已登录' }}</div>
        <el-button link style="color:#fdba74;padding:8px 0" @click="logout">退出</el-button>
      </div>
    </aside>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useSession } from '../stores/session'

const session = useSession()
const router = useRouter()

function logout() {
  session.clear()
  router.push('/login')
}
</script>
