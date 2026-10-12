# 病例菜单

这些是以后可以一天做一个的题。架子上的健康实现已经把「正确样子」留好了。做题时用新策略把某一个环节写坏，再自己修好。

| 编号 | 你可以制造的现象 | 挂钩点 |
| --- | --- | --- |
| DAY-01 | 乱开线程比线程池慢（上下文切换） | `incident/lab/ContextSwitchLab` |
| DAY-01b | 连点两下，库存少了两次（以后再做） | `ReserveStrategy.reserve`、幂等键 |
| DAY-02 | 支付回调来两次，预占被扣穿 | `PaymentTx` |
| DAY-03 | 取消了订单，可售数量没有回来 | `ReserveStrategy.release`，订单状态 |
| DAY-04 | 商品改价后，已经下的单金额也变了 | 下单时要把价格抄到订单行 |
| DAY-05 | 页面上的库存和数据库对不上 | `SkuCache`，有人把库存放进了缓存 |
| DAY-06 | 订单回滚了，消息已经发出去 | `OutboxPublisher`，有人在事务里直接发 |
| DAY-07 | 订单一多，列表接口越来越慢 | 循环里查库。健康查询在 `InventoryQueryService` |
| DAY-08 | 对账总是差一分 | 金额用了小数。健康实现用「分」 |
| DAY-09 | 同一个幂等键，内容换了也下成功 | `idempotency_record.request_hash` |
| DAY-10 | 乐观锁冲突后重试永远失败 | 重试包进了同一个事务。健康重试在 `PlaceOrderAppService` |

做完一个，把根因写进病例类的 `veteranPoint`，并记到 `case-log.md`。
