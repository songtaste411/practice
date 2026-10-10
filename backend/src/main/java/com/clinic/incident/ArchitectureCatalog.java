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
                new SystemMap.ContextCard("登录", "只管账号和密码。", "identity"),
                new SystemMap.ContextCard("商品", "只管名字和价钱，不管还剩几件。", "catalog"),
                new SystemMap.ContextCard("库存", "只管还能卖几件、有几件被下单占着。", "inventory"),
                new SystemMap.ContextCard("订单", "谁买了什么。价钱以下单那一刻为准。", "ordering"),
                new SystemMap.ContextCard("付款", "收到付款后，把占着的货变成卖掉。", "payment"),
                new SystemMap.ContextCard("故障开关", "以后的练习题从这里打开。", "incident"),
                new SystemMap.ContextCard("共用的小工具", "每条报错一个编号，方便对日志。", "shared")
        );
    }

    private List<SystemMap.HookCard> hooks() {
        return List.of(
                new SystemMap.HookCard("库存怎么扣", "inventory/domain/ReserveStrategy.java",
                        "以后的故障大多改这里，比如连点扣了两次。"),
                new SystemMap.HookCard("开关选哪一种扣法", "inventory/app/ReserveStrategyRouter.java",
                        "同一时间只用一种扣法。"),
                new SystemMap.HookCard("下单时一起保存", "ordering/app/PlaceOrderTx.java",
                        "扣库存、写订单要一起成功。不能扣了库存却没订单。"),
                new SystemMap.HookCard("防连点", "idempotency_record",
                        "同一单再提交一次，不应该再扣库存。"),
                new SystemMap.HookCard("付款", "payment/app/PaymentTx.java",
                        "同一笔付款来两次，只能算一次。"),
                new SystemMap.HookCard("付款后的通知", "shared/outbox/OutboxPublisher.java",
                        "现在只写在日志里。以后再接到消息队列。"),
                new SystemMap.HookCard("商品缓存", "catalog/cache/SkuCache.java",
                        "只记住名字和价钱。库存不要放进缓存。"),
                new SystemMap.HookCard("报错编号", "shared/web/TraceFilter.java",
                        "每次请求有一个编号，报错时能对上日志。")
        );
    }
}
