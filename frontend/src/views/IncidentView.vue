<template>
  <div>
    <h1 class="page-title">故障开关</h1>
    <p class="page-desc">今天先做第一题：线程开太多会不会更慢。不用改下面的开关，直接点 DAY-01 的「试一次」。</p>

    <section class="guide">
      <strong>第一天怎么点</strong>
      <ol>
        <li>找到「DAY-01 线程开太多，反而更慢」。</li>
        <li>点「试一次」。程序会先后用两种方式干同一批活：乱开线程，再用线程池。</li>
        <li>看下面两块时间：哪边更慢、峰值线程谁更多。慢的那边，CPU 很多时间花在换人上。</li>
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
      <p class="quiet">这一题不用改开关。重启程序后开关会回到「正常扣库存」。</p>
    </section>

    <section v-for="item in todayCases" :key="item.code" class="card" style="margin-bottom:12px">
      <div style="display:flex;justify-content:space-between;gap:12px;align-items:center">
        <h3 style="margin:0">{{ cardTitle(item) }}</h3>
        <el-tag :type="item.mutatesBehavior ? 'danger' : 'info'">{{ item.mutatesBehavior ? '打开后下单会变' : '不会改下单' }}</el-tag>
      </div>
      <p><strong>点下去会怎样：</strong>{{ explain(item).symptom }}</p>
      <p><strong>要记住：</strong>{{ explain(item).point }}</p>
      <p><strong>要不要先开开关：</strong>{{ explain(item).how }}</p>
      <el-button size="small" :loading="busy === item.code" @click="reproduce(item.code)">试一次</el-button>

      <div v-if="results[item.code]" class="hint" style="margin-top:12px">
        <div>{{ results[item.code].summary }}</div>
        <ol>
          <li v-for="step in results[item.code].steps" :key="step">{{ step }}</li>
        </ol>
      </div>

      <div v-if="item.code === 'DAY-01' && race" class="race">
        <div class="race-card bad">
          <div class="race-title">{{ race.wasteful.mode }}</div>
          <div class="race-ms">{{ race.wasteful.elapsedMs }} <small>毫秒</small></div>
          <div class="race-meta">工人约 {{ race.wasteful.workerThreads }} 个 · 峰值约 {{ race.wasteful.peakThreads }} 个线程</div>
          <p>{{ race.wasteful.note }}</p>
        </div>
        <div class="race-card good">
          <div class="race-title">{{ race.pooled.mode }}</div>
          <div class="race-ms">{{ race.pooled.elapsedMs }} <small>毫秒</small></div>
          <div class="race-meta">工人 {{ race.pooled.workerThreads }} 个 · 峰值约 {{ race.pooled.peakThreads }} 个线程</div>
          <p>{{ race.pooled.note }}</p>
        </div>
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
interface RunSide {
  mode: string
  taskCount: number
  workerThreads: number
  elapsedMs: number
  peakThreads: number
  note: string
}
interface RacePayload {
  taskCount: number
  poolSize: number
  wasteful: RunSide
  pooled: RunSide
}

const strategies = ref<StrategyCard[]>([])
const cases = ref<IncidentCard[]>([])
const active = ref('none')
const selected = ref('none')
const results = ref<Record<string, { summary: string; steps: string[] }>>({})
const race = ref<RacePayload | null>(null)
const busy = ref('')

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
  if (item.code === 'DAY-01') {
    return {
      symptom: item.symptom,
      point: item.veteranPoint,
      how: item.howToEnable
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
  busy.value = code
  try {
    const result = await api<{ summary: string; steps: string[]; payload?: RacePayload | null }>(
      'post',
      `/api/incidents/${code}/reproduce`,
      undefined,
      undefined,
      code === 'DAY-01' ? 90000 : undefined,
    )
    if (code === 'DAY-01') {
      results.value = {
        ...results.value,
        [code]: {
          summary: result.summary,
          steps: result.steps || []
        }
      }
      race.value = result.payload ?? null
      ElMessage.success('两边都跑完了，看下面两块时间')
      return
    }

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
  } finally {
    busy.value = ''
  }
}

onMounted(load)
</script>

<style scoped>
.race {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 16px;
}
.race-card {
  border-radius: 12px;
  padding: 14px 16px;
  min-width: 0;
}
.race-card.bad {
  background: #fff7ed;
  border: 1px solid #fdba74;
}
.race-card.good {
  background: #f0fdf4;
  border: 1px solid #86efac;
}
.race-title {
  font-weight: 650;
  margin-bottom: 6px;
}
.race-ms {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.1;
}
.race-ms small {
  font-size: 14px;
  font-weight: 500;
}
.race-meta {
  color: #57534e;
  font-size: 13px;
  margin: 8px 0;
}
.race-card p {
  margin: 0;
  color: #44403c;
  font-size: 14px;
}
@media (max-width: 700px) {
  .race { grid-template-columns: 1fr; }
}
</style>
