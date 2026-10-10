# 明天加一个病例

房子不用动。一个病 = 一张病例卡 + 可选的一套库存策略。

## 用脚本生成空壳

在项目根目录：

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\new-case.ps1 -Day 01 -Name Oversell -Title "连点超卖"
```

会多出两个文件：

- `backend/src/main/java/com/clinic/incident/cases/Day01OversellCase.java`  
  给人看的：现象、考察点、复现说明。
- `backend/src/main/java/com/clinic/inventory/domain/Day01OversellStrategy.java`  
  真正改变扣库存行为的地方。开关名是 `day01`。

脚本不会覆盖已有文件。

## 你要写的东西

1. 在策略类里写下「有病」的扣库存方式。三个方法都要实现：`reserve`、`confirmPaid`、`release`。用不到的那个，可以转调健康逻辑，也可以先抛清晰的错。
2. 在病例类里把现象写清楚。
3. 重启后端。新类会被自动扫到，不用登记。
4. 打开病例板，把开关拨到 `day01`。下一笔订单立刻走这套策略。
5. 到履约台按现象操作。
6. 修好之后把开关拨回 `none`，确认健康路径还在。
7. 打开 `docs/case-log.md`，记一笔。

## 规则

- 不要改 `HealthyReserveStrategy`。健康实现是对照物。
- 同一时间只拨一个开关。两个病一起开，你分不清是谁。
- 开关在内存里。重启后回到 `application.yml` 的 `clinic.cases.active`。
- 支付、缓存以后也可以照 `ReserveStrategyRouter` 再做一个路由器。现在先把库存这个挂钩用熟。

## 手写也可以

复制 `Day99TemplateCase`，改编号和文案。  
如果这个病要改行为，再写一个 `ReserveStrategy`，`switchName()` 返回和页面开关一样的字符串。
