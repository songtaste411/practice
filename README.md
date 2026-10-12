# 老兵诊所

这是一个能下单的小商店。商店主链路是好的。你先把正常买卖走一遍，再去做第一天的对比题。

账号 `demo`，密码 `demo123`。页面上已经填好。

三件商品：龙井试饮、夜班马克杯、并发这件小事。

## 打开网页之后

浏览器打开 http://localhost:5173 ，点「进去下单」。左边有三个页面：

| 页面 | 现在要不要看 |
| --- | --- |
| 下单 | 要。按页面上的 4 步点一遍 |
| 故障开关 | 今天要。打开 DAY-01，点「试一次」 |
| 程序怎么分 | 第一次不用看 |

## 第一天：线程开太多，反而更慢

对应课本里那句：频繁的上下文切换会浪费 CPU；线程数不是越多越好，要用合理的线程池。

1. 打开「故障开关」。
2. 找到 **DAY-01 线程开太多，反而更慢**。
3. 点「试一次」（大约几秒，按钮会转圈）。
4. 看下面两块：
   - 左边「乱开线程」：每个小任务都 `new` 一个线程
   - 右边「线程池」：固定几个工人反复接活
5. 比耗时和峰值线程数。乱开那一边通常更慢——CPU 忙着换人，真正干活的时间反而少。

这一题**不会改下单**，不用动「库存怎么扣」开关。

## 在「下单」页你会看到

1. 点「加入」，再点「提交订单」。「还能卖」少 1，「先占着」多 1。
2. 勾上「连点测试」，再提交一次。数量不变。
3. 点「假装付款」。「先占着」回到 0，订单变成已付款。

## 这几个词

| 页面上的词 | 意思 |
| --- | --- |
| 还能卖 | 现在可以买的数量 |
| 先占着 | 有人下单了还没付款，这几件先留着 |
| 待付款 | 单下了，钱没付 |
| 已付款 | 付过了，货算卖掉 |
| 乱开线程 | 每个任务单独开一个线程，又多又短命 |
| 线程池 | 固定几个工人反复干活，少创建、少切换 |
| 峰值线程 | 跑的时候，进程里线程数摸到的最高值 |

价钱在程序内部用「分」记，页面上已经换成元。28.00 元在程序里是 2800。

## 先跑起来（本机，不用 Docker）

需要 JDK 21、Maven 3.9 以上、Node.js 20 以上。这台电脑的默认 `java` 可能是 8，用下面的脚本会自动切到 JDK 21。

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\run-api.ps1
```

另开一个窗口：

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\run-web.ps1
```

浏览器打开 http://localhost:5173 。

用 IDEA 直接运行 `ClinicApplication` 也可以。如果报「找不到 getSkuCode」：右侧 Maven 面板点刷新，再选 Build → Rebuild Project。

更接近生产的启动方式、镜像和 K8s，看：

- [docs/01-local.md](docs/01-local.md)
- [docs/02-image.md](docs/02-image.md)
- [docs/03-compose.md](docs/03-compose.md)
- [docs/04-k8s.md](docs/04-k8s.md)

Compose 起来之后，网页在 http://localhost:18080 。

## 以后再看：怎么加一个故障

第一次不用看这里。

看 [docs/how-to-add-a-case.md](docs/how-to-add-a-case.md)。  
题目菜单在 [docs/case-roadmap.md](docs/case-roadmap.md)。  
做完记到 [docs/case-log.md](docs/case-log.md)。

最短命令：

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\new-case.ps1 -Day 02 -Name PayDouble -Title "支付回调两次"
```

## 以后再看：程序怎么分

一个后端进程，里面按业务划开包。这叫模块化单体。  
边界先划清，以后某块真的要拆出去，是从包拆，而不是第一天就拆成很多服务。

```
浏览器 (Vue3 + Vite + TypeScript + Pinia + Element Plus)
        |
        |  /api
        v
老兵诊所 API (Java 21 + Spring Boot 3.4)
  identity   登录
  catalog    商品和价格
  inventory  库存策略
  ordering   下单、幂等、状态
  payment    支付回调
  incident   病例板和开关（DAY-01 的线程对比在这里）
  shared     追踪号、错误码、Outbox
        |
        +-- 本机：内存数据库 H2
        +-- Compose / K8s：Postgres + Redis
```

## 目录

```
backend/     Java API
frontend/    网页
docs/        四层部署、加病例、日志
deploy/k8s/  K8s 清单
tools/       生成新病例的脚本
docker-compose.yml
```

仓库里如果还有 `assistant/`，那是另一条实验线，不是今天要用的商店。

## 接口说明

统一返回：

```json
{ "code": 0, "message": "ok", "traceId": "16位", "data": {} }
```

`code` 为 0 表示成功。失败时 `data` 为 null，HTTP 状态和 `code` 一起看。

| code | 含义 |
| --- | --- |
| 0 | 成功 |
| 400 | 参数不对 |
| 401 | 没登录或账号密码错 |
| 404 | 没有这个订单、商品或病例 |
| 40900 | 并发冲突或数据冲突 |
| 40901 | 库存不够 |
| 40902 | 同一个幂等键，两次内容不一样 |
| 40903 | 订单状态不允许这次操作 |
| 500 | 程序出错。看日志里的追踪号 |

除了登录和健康检查，请求头都要带：

```
Authorization: Bearer <登录返回的 token>
```

### 登录

`POST /api/auth/login`

| 字段 | 说明 |
| --- | --- |
| username | 账号，演示值 `demo` |
| password | 密码，演示值 `demo123` |

返回 `data`：`token`、`username`、`displayName`。令牌 12 小时有效。

### 商品 / 库存 / 下单 / 支付

见页面流程即可。完整字段仍以旧接口为准：

- `GET /api/catalog/skus`
- `GET /api/inventory`
- `POST /api/orders`（可选头 `Idempotency-Key`）
- `GET /api/orders`
- `POST /api/payments/mock-callback`

### 病例

`GET /api/incidents`  
`PUT /api/incidents/active` 请求体 `{ "active": "none" }`  
`POST /api/incidents/{code}/reproduce`

DAY-01 的 `reproduce` 返回 `payload`：

| 字段 | 说明 |
| --- | --- |
| taskCount | 两边各干多少件小活，现在是 2000 |
| poolSize | 线程池工人数，按本机 CPU 核数取，至少 2 |
| wasteful | 乱开线程那一边：`elapsedMs`、`peakThreads`、`workerThreads` |
| pooled | 线程池那一边，字段同上 |

### 架构地图

`GET /api/meta/map`

### 健康检查

`GET /actuator/health`  
不用登录。正常是 `{"status":"UP"}`。

## 演示数据

| 编码 | 名字 | 单价 | 起始可售 |
| --- | --- | --- | --- |
| TEA | 龙井试饮 | 28.00 元 | 20 |
| MUG | 夜班马克杯 | 39.00 元 | 8 |
| BOOK | 并发这件小事 | 68.00 元 | 5 |

本机内存库在进程退出后清空，下次启动会重新种入这些数据。

## 配置

`backend/src/main/resources/application.yml`

| 配置 | 作用 |
| --- | --- |
| `clinic.cases.active` | 启动时的库存策略。默认 `none` |
| `clinic.redis.enabled` | 本机 false，Docker 里 true |
| `clinic.jwt.secret` | 登录令牌密钥，至少 32 个字符。示例值只能本机用 |
| `server.port` | 默认 8080 |

## 已知限制，以及下一步怎么长

- DAY-01 是本机对比实验，用来摸「上下文切换」；它不模拟千万流量，只把「线程开太多更慢」这件事跑给你看。
- Outbox 目前只把消息打进日志。
- 取消订单的状态留了 `CANCELLED`，退回库存的方法留了 `release`，还没有接口。
- 默认 JWT 密钥是示例。不要带着示例密钥对公网暴露。
