<template>
  <div>
    <h1 class="page-title">病例板</h1>
    <p class="page-desc">开关决定现在用哪套库存策略。拨过去立刻生效，重启后回到配置文件。同一时间只开一个病。</p>

    <section class="card" style="margin-bottom:16px">
      <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap">
        <span>当前策略</span>
        <el-select v-model="selected" style="width:280px">
          <el-option v-for="item in strategies" :key="item.switchName" :label="`${item.switchName} · ${item.title}`" :value="item.switchName" />
        </el-select>
        <el-button type="primary" @click="activate">拨动开关</el-button>
        <el-tag v-if="active" type="warning">生效中：{{ active }}</el-tag>
      </div>
    </section>

    <section v-for="item in cases" :key="item.code" class="card" style="margin-bottom:12px">
      <div style="display:flex;justify-content:space-between;gap:12px;align-items:center">
        <h3 style="margin:0">{{ item.code }} {{ item.title }}</h3>
        <el-tag :type="item.mutatesBehavior ? 'danger' : 'info'">{{ item.mutatesBehavior ? '会改行为' : '只讲解' }}</el-tag>
      </div>
      <p><strong>现象：</strong>{{ item.symptom }}</p>
      <p><strong>老兵点：</strong>{{ item.veteranPoint }}</p>
      <p><strong>怎么接：</strong>{{ item.howToEnable }}</p>
      <el-button size="small" @click="reproduce(item.code)">复现</el-button>
      <pre v-if="results[item.code]" class="trace" style="white-space:pre-wrap">{{ results[item.code] }}</pre>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api/http'

interface StrategyCard { switchName: string; title: string; active: boolean }
interface IncidentCard {
  code: string
  title: string
  symptom: string
  veteranPoint: string
  howToEnable: string
  mutatesBehavior: boolean
}
interface Board { activeSwitch: string; strategies: StrategyCard[]; cases: IncidentCard[] }

const strategies = ref<StrategyCard[]>([])
const cases = ref<IncidentCard[]>([])
const active = ref('none')
const selected = ref('none')
const results = ref<Record<string, string>>({})

async function load() {
  const board = await api<Board>('get', '/api/incidents')
  strategies.value = board.strategies
  cases.value = board.cases
  active.value = board.activeSwitch
  selected.value = board.activeSwitch
}

async function activate() {
  const board = await api<Board>('put', '/api/incidents/active', { active: selected.value })
  active.value = board.activeSwitch
  strategies.value = board.strategies
  ElMessage.success(`开关已拨到 ${board.activeSwitch}`)
}

async function reproduce(code: string) {
  const result = await api<unknown>('post', `/api/incidents/${code}/reproduce`)
  results.value = { ...results.value, [code]: JSON.stringify(result, null, 2) }
}

onMounted(load)
</script>
