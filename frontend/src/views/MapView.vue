<template>
  <div>
    <h1 class="page-title">程序怎么分</h1>
    <p class="page-desc">不用背。把它想成一家店，每个人只做自己的事。</p>
    <p class="quiet">{{ healthText }}</p>

    <section class="guide">
      <strong>五个柜台</strong>
      <ol>
        <li>登录：只认人，不管卖什么。</li>
        <li>商品：只记名字和价钱，不管还剩几件。</li>
        <li>库存：只记还能卖几件、有几件被下单占着。</li>
        <li>订单：记谁买了什么。</li>
        <li>付款：把「先占着」变成「卖掉了」。</li>
      </ol>
      <p class="quiet">价钱和库存分开写，是为了以后改库存时，不会把价钱改坏。</p>
    </section>

    <details class="card">
      <summary>代码放在哪（第一次不用打开）</summary>
      <div class="grid-2" style="margin-top:12px">
        <section>
          <h3>各管一块</h3>
          <div v-for="item in contexts" :key="item.packageName" class="sku">
            <div>
              <strong>{{ item.name }}</strong>
              <div>{{ item.duty }}</div>
            </div>
            <code>{{ item.packageName }}</code>
          </div>
        </section>
        <section>
          <h3>以后故障会改这些地方</h3>
          <div v-for="item in hooks" :key="item.name" class="sku">
            <div>
              <strong>{{ item.name }}</strong>
              <div>{{ item.why }}</div>
              <div class="trace">{{ item.where }}</div>
            </div>
          </div>
        </section>
      </div>
    </details>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { api } from '../api/http'
import http from '../api/http'

interface ContextCard { name: string; duty: string; packageName: string }
interface HookCard { name: string; where: string; why: string }

const contexts = ref<ContextCard[]>([])
const hooks = ref<HookCard[]>([])
const health = ref('检查中')
const healthText = computed(() => {
  if (health.value === '检查中') return '正在看程序开没开'
  if (health.value === 'UP') return '程序正在运行'
  return `程序状态：${health.value}`
})

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
