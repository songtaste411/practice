<template>
  <div>
    <h1 class="page-title">故障开关</h1>
    <p class="page-desc">今天商店是好的，不用改下面的开关。这个页面是以后用的：打开一个故障，再回到「下单」看它坏成什么样。</p>

    <section class="guide">
      <strong>你现在只要知道两件事</strong>
      <ol>
        <li>开关停在「正常扣库存」。别改它。</li>
        <li>下面点「试一次」，会真的买一杯龙井。然后回到「下单」页，能看到这笔单。</li>
      </ol>
    </section>

    <section class="card" style="margin-bottom:16px">
      <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap">
        <span>库存怎么扣</span>
        <el-select v-model="selected" style="width:280px">
          <el-option v-for="item in strategies" :key="item.switchName" :label="strategyLabel(item)" :value="item.switchName" />
        </el-select>
        <el-button type="primary" @click="activate">就用这个</el-button>
        <el-tag v-if="active" type="warning">正在用：{{ activeLabel(active) }}</el-tag>
      </div>
      <p class="quiet">重启程序后会回到「正常扣库存」。</p>
    </section>

    <section v-for="item in todayCases" :key="item.code" class="card" style="margin-bottom:12px">
      <div style="display:flex;justify-content:space-between;gap:12px;align-items:center">
        <h3 style="margin:0">{{ cardTitle(item) }}</h3>
        <el-tag :type="item.mutatesBehavior ? 'danger' : 'info'">{{ item.mutatesBehavior ? '打开后下单会变' : '不会改下单' }}</el-tag>
      </div>
      <p><strong>点下去会怎样：</strong>{{ explain(item).symptom }}</p>
      <p><strong>要记住：</strong>{{ explain(item).point }}</p>
      <p><strong>要不要先开开关：</strong>{{ explain(item).how }}</p>
      <el-button size="small" @click="reproduce(item.code)">试一次</el-button>
      <div v-if="results[item.code]" class="hint" style="margin-top:12px">
        <div>{{ results[item.code].summary }}</div>
        <ol>
          <li v-for="step in results[item.code].steps" :key="step">{{ step }}</li>
        </ol>
      </div>
    </section>

    <details v-if="laterCases.length" class="card">
      <summary>以后再看：怎么加一道新题</summary>
      <section v-for="item in laterCases" :key="item.code" style="margin-top:12px">
        <p><strong>{{ cardTitle(item) }}</strong></p>
        <p>{{ explain(item).symptom }}</p>
        <p>{{ explain(item).how }}</p>
      </section>
    </details>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
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
const results = ref<Record<string, { summary: string; steps: string[] }>>({})

const todayCases = computed(() => cases.value.filter((item) => item.code !== 'DAY-99'))
const laterCases = computed(() => cases.value.filter((item) => item.code === 'DAY-99'))

function strategyLabel(item: StrategyCard) {
  if (item.switchName === 'none') return '正常扣库存'
  return `${item.title}（${item.switchName}）`
}

function activeLabel(name: string) {
  if (name === 'none') return '正常扣库存'
  return name
}

function cardTitle(item: IncidentCard) {
  if (item.code === 'DAY-00') return '试一次正常下单'
  if (item.code === 'DAY-99') return '以后加新题的空位'
  return `${item.code} ${item.title}`
}

function explain(item: IncidentCard) {
  if (item.code === 'DAY-00') {
    return {
      symptom: '点「试一次」会真的买一杯龙井。回到「下单」页，还能卖的数量少 1，多出一笔待付款。',
      point: '先记住正常情况：下单一杯只少一件，付款也不会再少一件。',
      how: '不用改开关，直接点。'
    }
  }
  if (item.code === 'DAY-99') {
    return {
      symptom: '这不是故障，是以后加新练习题的空位。',
      point: '今天跳过。',
      how: '先不用管。'
    }
  }
  return { symptom: item.symptom, point: item.veteranPoint, how: item.howToEnable }
}

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
  ElMessage.success(`现在用：${activeLabel(board.activeSwitch)}`)
}

async function reproduce(code: string) {
  const result = await api<{ summary: string; steps: string[] }>('post', `/api/incidents/${code}/reproduce`)
  const failed = result.summary?.includes('没买成') || result.summary?.includes('没有下成')
  const jargon = (result.steps || []).some((step) => step.includes('幂等') || step.includes('Outbox'))
  results.value = {
    ...results.value,
    [code]: {
      summary: failed
        ? result.summary
        : (result.summary?.startsWith('买好了') ? result.summary : '买好了。回到「下单」页，能看到一杯待付款的龙井，库存少了 1。'),
      steps: jargon
        ? ['这是真的下一单，不是假数据。', '再到「下单」页点「假装付款」，占着的货才算卖掉。']
        : (result.steps || [])
    }
  }
}

onMounted(load)
</script>
