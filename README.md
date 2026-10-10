# 老兵诊所

生产疑难杂症训练场。

病人不是人，是线上那种「点了一次，库存少了两次」的故障。  
这个仓库先把医院盖好：前后端、订单、库存、支付、追踪号、幂等、消息出站。  
从第二天起，一天只加一个病，在现成的挂钩上写，然后自己修好。

参考了 `java-ai-rag-tender-system` 的四层部署写法：本机、镜像、Compose、K8s。业务换成了可插拔的履约训练场。

## 现在这栋房子里有什么

一条能跑通的履约链路：

1. 登录
2. 看商品和库存
3. 下单（预占库存）
4. 用同一个幂等键再提交一次（不会再扣库存）
5. 模拟支付（预占被确认，重复回调不会扣第二次）

三件演示商品：龙井试饮、夜班马克杯、并发这件小事。  
账号 `demo`，密码 `demo123`。

金额在接口里是**分**（整数）。页面上除以 100 显示成元。

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

用 IDEA 直接运行 `ClinicApplication` 也可以。Lombok 插件只负责编辑器认识 `@Getter`，真正编译还要找到 Lombok 的 jar。`pom.xml` 里已经写了版本。如果还报「找不到 getSkuCode」：右侧 Maven 面板点刷新，再选菜单 Build → Rebuild Project。

更接近生产的启动方式、镜像和 K8s，看：

- [docs/01-local.md](docs/01-local.md)
- [docs/02-image.md](docs/02-image.md)
- [docs/03-compose.md](docs/03-compose.md)
- [docs/04-k8s.md](docs/04-k8s.md)

Compose 起来之后，网页在 http://localhost:18080 。

## 每天怎么加一个病

看 [docs/how-to-add-a-case.md](docs/how-to-add-a-case.md)。  
题目菜单在 [docs/case-roadmap.md](docs/case-roadmap.md)。  
做完记到 [docs/case-log.md](docs/case-log.md)。

最短命令：

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\new-case.ps1 -Day 01 -Name Oversell -Title "连点超卖"
```

重启后端，打开「病例板」，把开关拨到 `day01`。

## 架构（人话）

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
  inventory  库存策略（明天的病主要挂这里）
  ordering   下单、幂等、状态
  payment    支付回调
  incident   病例板和开关
  shared     追踪号、错误码、Outbox
        |
        +-- 本机：内存数据库 H2
        +-- Compose / K8s：Postgres + Redis
```

已经埋好的老兵挂钩：

| 挂钩 | 健康时长什么样 |
| --- | --- |
| 乐观锁 | 库存行有 `version`。两个人同时扣，后写的那个会失败并重试 |
| 短事务 | 扣库存、写订单、写幂等、写 Outbox 在同一个事务里。重试在事务外面 |
| 幂等 | 请求头 `Idempotency-Key`。相同键 + 相同内容，返回第一次的订单 |
| 金额 | 用分，整数。不用小数 |
| 支付 | 支付单号唯一，订单只能从「已预占」变成「已支付」 |
| Outbox | 消息和订单一起提交。现在投递只打日志，位置留好了 |
| 缓存 | 只缓存商品名和价格 30 秒。库存不进缓存 |
| 追踪号 | 每个响应有 `traceId`，日志里同一串字符 |
| 虚拟线程 | Java 21 已打开 |

页面上的「架构地图」和后端 `ArchitectureCatalog` 是同一份说明。

## 目录

```
backend/     Java API
frontend/    网页
docs/        四层部署、加病例、日志
deploy/k8s/  K8s 清单
tools/       生成新病例的脚本
docker-compose.yml
```

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

### 商品

`GET /api/catalog/skus`

返回数组。每项：`skuCode`、`name`、`priceCent`（分）。没有库存。

### 库存

`GET /api/inventory`

返回数组。每项：`skuCode`、`skuName`、`availableQty` 可售、`reservedQty` 预占、`priceCent`。

下单成功：可售减少，预占增加。  
支付成功：预占减少，可售不变（下单时已经减过）。

### 下单

`POST /api/orders`

请求头可选：`Idempotency-Key`，最长 80 个字符。

| 字段 | 说明 |
| --- | --- |
| buyerName | 买家，必填，最长 64 |
| lines | 至少 1 行，最多 20 行 |
| lines[].skuCode | 商品编码，例如 `TEA` |
| lines[].quantity | 1 到 999。同一商品不要拆成两行 |

返回 `data`：

| 字段 | 说明 |
| --- | --- |
| orderNo | 订单号 |
| status | `RESERVED` 已预占，`PAID` 已支付 |
| buyerName | 买家 |
| totalCent | 总金额，分 |
| createdAt | UTC 时间 |
| replayed | true 表示这次没有新下单，而是把第一次的结果还回来了 |
| lines | 商品、数量、下单时的单价（分） |

`GET /api/orders` 返回订单数组，新的在前。  
`GET /api/orders/{orderNo}` 返回一笔。没有则 404。

### 模拟支付

`POST /api/payments/mock-callback`

| 字段 | 说明 |
| --- | --- |
| orderNo | 订单号 |
| payNo | 支付单号，最长 64。页面用 `PAY-订单号`，所以连点两次是同一笔 |

返回 `data`：`payNo`、`orderNo`、`orderStatus`、`amountCent`、`duplicate`。  
`duplicate` 为 true 表示这次回调被忽略了。

### 病例

`GET /api/incidents`

返回：

| 字段 | 说明 |
| --- | --- |
| activeSwitch | 当前开关，健康实现是 `none` |
| strategies | 已注册的库存策略。`switchName`、`title`、`active` |
| cases | 病例卡。`code`、`title`、`symptom`、`veteranPoint`、`howToEnable`、`mutatesBehavior` |

`PUT /api/incidents/active`

请求体：`{ "active": "none" }`。名字必须是已经注册的策略。返回拨动之后的病例板。

`POST /api/incidents/{code}/reproduce`

按病例自己的脚本跑一次。`DAY-00` 会真实下一笔龙井。  
返回：`code`、`summary`、`steps`、`payload`。

### 架构地图

`GET /api/meta/map`

返回 `contexts`（边界）和 `hooks`（挂钩点）。

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
Compose 的 Postgres 会留着，只要库里已经有用户，就不会重复种。

## 配置

`backend/src/main/resources/application.yml`

| 配置 | 作用 |
| --- | --- |
| `clinic.cases.active` | 启动时的库存策略。默认 `none` |
| `clinic.redis.enabled` | 本机 false，Docker 里 true |
| `clinic.jwt.secret` | 登录令牌密钥，至少 32 个字符。示例值只能本机用 |
| `server.port` | 默认 8080 |

## 已知限制，以及下一步怎么长

- Outbox 目前只把消息打进日志。接 Kafka 时改 `OutboxPublisher`，失败不要标记已发送。
- 支付和缓存还没有单独的「病例路由器」。库存这套就是模板，支付的病可以照着抄一个。
- 取消订单的状态留了 `CANCELLED`，退回库存的方法留了 `release`，还没有接口。这是 DAY-03 的位置。
- K8s 的数据库是空目录，Pod 删除后数据消失。
- 训练开关在内存里，多开几个后端实例时各自记各自的。本机和一个容器够用。
- 默认 JWT 密钥是示例。这个项目是训练场，不要带着示例密钥对公网暴露。
