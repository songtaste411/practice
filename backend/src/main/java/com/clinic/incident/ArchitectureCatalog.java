package com.clinic.incident;

import com.clinic.incident.api.SystemMap;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 架构地图的唯一数据源。页面和说明书都从这里讲，避免两处各写各的。
 */
@Component
public class ArchitectureCatalog {

    public SystemMap map() {
        return new SystemMap(contexts(), hooks());
    }

    private List<SystemMap.ContextCard> contexts() {
        return List.of(
                new SystemMap.ContextCard("身份", "登录、令牌。不管订单。", "identity"),
                new SystemMap.ContextCard("商品", "编码、名字、价格。不管库存。", "catalog"),
                new SystemMap.ContextCard("库存", "可售、预占、乐观锁。不管价格从哪来。", "inventory"),
                new SystemMap.ContextCard("订单", "下单、幂等、状态。价格以下单当时为准。", "ordering"),
                new SystemMap.ContextCard("支付", "回调幂等，把预占确认掉。", "payment"),
                new SystemMap.ContextCard("病例", "开关、现象、复现。病从这里挂进去。", "incident"),
                new SystemMap.ContextCard("共用", "追踪号、错误码、Outbox、时钟。", "shared")
        );
    }

    private List<SystemMap.HookCard> hooks() {
        return List.of(
                new SystemMap.HookCard("库存策略", "inventory/domain/ReserveStrategy.java",
                        "明天的病大多挂在这里：超卖、扣两次、忘记乐观锁。"),
                new SystemMap.HookCard("策略路由", "inventory/app/ReserveStrategyRouter.java",
                        "按开关挑选策略。同一时间只开一个病。"),
                new SystemMap.HookCard("下单事务", "ordering/app/PlaceOrderTx.java",
                        "库存、订单、幂等、Outbox 必须同一个事务。重试在事务外面。"),
                new SystemMap.HookCard("幂等表", "idempotency_record",
                        "防重复点击。键要和请求内容一起存，不能只存键。"),
                new SystemMap.HookCard("支付回调", "payment/app/PaymentTx.java",
                        "支付单号唯一 + 订单状态机。两道都要有。"),
                new SystemMap.HookCard("Outbox", "shared/outbox/OutboxPublisher.java",
                        "现在只打日志。以后从这里换成 Kafka，失败不要标记已发送。"),
                new SystemMap.HookCard("商品缓存", "catalog/cache/SkuCache.java",
                        "只缓存名字和价格。库存禁止进缓存。"),
                new SystemMap.HookCard("追踪号", "shared/web/TraceFilter.java",
                        "报错必须能对上日志。没有追踪号的告警等于没有告警。")
        );
    }
}
