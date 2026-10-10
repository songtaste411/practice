<template>
  <div>
    <h1 class="page-title">架构地图</h1>
    <p class="page-desc">一个可以运行的模块化单体。边界用包划开，先不拆成一堆微服务。病从挂钩点长出来。</p>
    <p class="trace">服务状态：{{ health }}</p>

    <div class="grid-2">
      <section class="card">
        <h3>边界</h3>
        <div v-for="item in contexts" :key="item.packageName" class="sku">
          <div>
            <strong>{{ item.name }}</strong>
            <div>{{ item.duty }}</div>
          </div>
          <code>{{ item.packageName }}</code>
        </div>
      </section>
      <section class="card">
        <h3>明天往哪挂钩</h3>
        <div v-for="item in hooks" :key="item.name" class="sku">
          <div>
            <strong>{{ item.name }}</strong>
            <div>{{ item.why }}</div>
            <div class="trace">{{ item.where }}</div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api } from '../api/http'
import http from '../api/http'

interface ContextCard { name: string; duty: string; packageName: string }
interface HookCard { name: string; where: string; why: string }

const contexts = ref<ContextCard[]>([])
const hooks = ref<HookCard[]>([])
const health = ref('检查中')

onMounted(async () => {
  const map = await api<{ contexts: ContextCard[]; hooks: HookCard[] }>('get', '/api/meta/map')
  contexts.value = map.contexts
  hooks.value = map.hooks
  try {
    const res = await http.get('/actuator/health')
    health.value = res.data.status || 'UP'
  } catch {
    health.value = '连不上'
  }
})
</script>
