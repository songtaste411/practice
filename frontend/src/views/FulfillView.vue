<template>
  <div>
    <h1 class="page-title">下单</h1>
    <p class="page-desc">这里就是收银台。先按下面 4 步点一遍，你会看见库存怎么变。</p>

    <section class="guide">
      <strong>第一次就按这个顺序</strong>
      <ol>
        <li>左边商品点「加入」。</li>
        <li>右边点「提交订单」。「还能卖」少 1，「先占着」多 1。先占着的意思是：单下了，钱还没付，货先留着。</li>
        <li>勾上「连点测试」，再提交一次。数量不变，说明连点两次也只会扣一次。</li>
        <li>下面订单点「假装付款」。「先占着」回到 0，订单变成已付款。</li>
      </ol>
    </section>

    <div class="grid-2">
      <section class="card">
        <h3>商品</h3>
        <div v-for="item in shelf" :key="item.skuCode" class="sku">
          <div>
            <strong>{{ item.skuName }}</strong>
            <div :class="item.availableQty > 0 ? 'stock-ok' : 'stock-low'">还能卖 {{ item.availableQty }} · 先占着 {{ item.reservedQty }}</div>
          </div>
          <div style="text-align:right">
            <div class="price">¥ {{ yuan(item.priceCent) }}</div>
            <el-button size="small" :disabled="item.availableQty <= 0" @click="add(item)">加入</el-button>
          </div>
        </div>
      </section>

      <section class="card">
        <h3>这一单</h3>
        <el-form label-position="top">
          <el-form-item label="买家名字">
            <el-input v-model="buyerName" maxlength="64" />
          </el-form-item>
        </el-form>
        <div v-if="cart.length === 0" class="page-desc">还没选商品。先到左边点「加入」。</div>
        <div v-for="line in cart" :key="line.skuCode" class="sku">
          <div>{{ line.skuName }}</div>
          <el-input-number v-model="line.quantity" :min="1" :max="999" size="small" />
        </div>
        <div style="margin:12px 0">一共 ¥ {{ yuan(totalCent) }}</div>
        <el-checkbox v-model="reuseKey">连点测试：再提交刚才那一单，库存不应该再少</el-checkbox>
        <div style="margin-top:12px;display:flex;gap:8px">
          <el-button type="primary" :loading="placing" :disabled="cart.length === 0" @click="place">提交订单</el-button>
          <el-button @click="cart = []">清空</el-button>
        </div>
        <p v-if="lastKey" class="quiet">已经提交过。勾上连点测试再点一次，看库存会不会多扣。</p>
      </section>
    </div>

    <section class="card" style="margin-top:16px">
      <h3>已经下的单</h3>
      <el-table :data="orders" size="small">
        <el-table-column prop="orderNo" label="单号" min-width="180" />
        <el-table-column prop="buyerName" label="买家" width="120" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">{{ statusText(row.status) }}</template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row }">¥ {{ yuan(row.totalCent) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button v-if="row.status === 'RESERVED'" size="small" type="primary" @click="pay(row.orderNo)">假装付款</el-button>
            <span v-else class="trace">付过了</span>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api/http'

interface Stock {
  skuCode: string
  skuName: string
  availableQty: number
  reservedQty: number
  priceCent: number
}
interface OrderLine { skuCode: string; skuName: string; quantity: number; priceCent: number }
interface Order {
  orderNo: string
  status: string
  buyerName: string
  totalCent: number
  replayed: boolean
  lines: OrderLine[]
}
interface CartLine { skuCode: string; skuName: string; quantity: number; priceCent: number }

const shelf = ref<Stock[]>([])
const orders = ref<Order[]>([])
const cart = ref<CartLine[]>([])
const buyerName = ref('小明')
const placing = ref(false)
const reuseKey = ref(false)
const lastKey = ref('')

const totalCent = computed(() => cart.value.reduce((sum, line) => sum + line.priceCent * line.quantity, 0))

function yuan(cent: number) {
  return (cent / 100).toFixed(2)
}

function statusText(status: string) {
  if (status === 'RESERVED') return '待付款'
  if (status === 'PAID') return '已付款'
  if (status === 'CANCELLED') return '已取消'
  return status
}

function add(item: Stock) {
  const found = cart.value.find((line) => line.skuCode === item.skuCode)
  if (found) {
    found.quantity += 1
    return
  }
  cart.value.push({ skuCode: item.skuCode, skuName: item.skuName, quantity: 1, priceCent: item.priceCent })
}

async function load() {
  const [stocks, orderList] = await Promise.all([
    api<Stock[]>('get', '/api/inventory'),
    api<Order[]>('get', '/api/orders')
  ])
  shelf.value = stocks
  orders.value = orderList
}

async function place() {
  placing.value = true
  try {
    const key = reuseKey.value && lastKey.value ? lastKey.value : crypto.randomUUID()
    lastKey.value = key
    const order = await api<Order>('post', '/api/orders', {
      buyerName: buyerName.value,
      lines: cart.value.map((line) => ({ skuCode: line.skuCode, quantity: line.quantity }))
    }, { 'Idempotency-Key': key })
    ElMessage.success(order.replayed ? '没有新下单，还是刚才那一单，库存没再少' : '下单成功。货先占着，等付款')
    if (!order.replayed) {
      cart.value = []
    }
    await load()
  } finally {
    placing.value = false
  }
}

async function pay(orderNo: string) {
  const result = await api<{ duplicate: boolean; orderStatus: string }>('post', '/api/payments/mock-callback', {
    orderNo,
    payNo: `PAY-${orderNo}`
  })
  ElMessage.success(result.duplicate ? '这次付款是重复的，已经忽略' : '付款成功。占着的货变成卖掉了')
  await load()
}

onMounted(load)
</script>
