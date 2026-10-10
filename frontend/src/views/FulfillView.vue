<template>
  <div>
    <h1 class="page-title">履约台</h1>
    <p class="page-desc">下一笔真实订单。连点两次如果库存只少一次，说明幂等是好的。开关如果被拨到某个病例，这里就是发病现场。</p>

    <div class="grid-2">
      <section class="card">
        <h3>货架</h3>
        <div v-for="item in shelf" :key="item.skuCode" class="sku">
          <div>
            <strong>{{ item.skuName }}</strong>
            <div class="trace">{{ item.skuCode }}</div>
            <div :class="item.availableQty > 0 ? 'stock-ok' : 'stock-low'">可售 {{ item.availableQty }} · 预占 {{ item.reservedQty }}</div>
          </div>
          <div style="text-align:right">
            <div class="price">¥ {{ yuan(item.priceCent) }}</div>
            <el-button size="small" :disabled="item.availableQty <= 0" @click="add(item)">加入</el-button>
          </div>
        </div>
      </section>

      <section class="card">
        <h3>开单</h3>
        <el-form label-position="top">
          <el-form-item label="买家">
            <el-input v-model="buyerName" maxlength="64" />
          </el-form-item>
        </el-form>
        <div v-if="cart.length === 0" class="page-desc">还没选商品。</div>
        <div v-for="line in cart" :key="line.skuCode" class="sku">
          <div>{{ line.skuName }}</div>
          <el-input-number v-model="line.quantity" :min="1" :max="999" size="small" />
        </div>
        <div style="margin:12px 0">合计 ¥ {{ yuan(totalCent) }}</div>
        <el-checkbox v-model="reuseKey">用上一次的幂等键再提交（用来验证不会扣两次）</el-checkbox>
        <div style="margin-top:12px;display:flex;gap:8px">
          <el-button type="primary" :loading="placing" :disabled="cart.length === 0" @click="place">提交订单</el-button>
          <el-button @click="cart = []">清空</el-button>
        </div>
        <p v-if="lastKey" class="trace">上次幂等键 {{ lastKey }}</p>
      </section>
    </div>

    <section class="card" style="margin-top:16px">
      <h3>订单</h3>
      <el-table :data="orders" size="small">
        <el-table-column prop="orderNo" label="单号" min-width="180" />
        <el-table-column prop="buyerName" label="买家" width="120" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column label="金额" width="120">
          <template #default="{ row }">¥ {{ yuan(row.totalCent) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button v-if="row.status === 'RESERVED'" size="small" type="primary" @click="pay(row.orderNo)">模拟支付</el-button>
            <span v-else class="trace">已处理</span>
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
const buyerName = ref('演示医生')
const placing = ref(false)
const reuseKey = ref(false)
const lastKey = ref('')

const totalCent = computed(() => cart.value.reduce((sum, line) => sum + line.priceCent * line.quantity, 0))

function yuan(cent: number) {
  return (cent / 100).toFixed(2)
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
    ElMessage.success(order.replayed ? `幂等命中，还是订单 ${order.orderNo}` : `已预占 ${order.orderNo}`)
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
  ElMessage.success(result.duplicate ? '重复回调已被忽略' : '支付完成，预占已确认')
  await load()
}

onMounted(load)
</script>
