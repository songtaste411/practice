package com.clinic.incident.cases;

import com.clinic.incident.IncidentCase;
import com.clinic.ordering.api.PlaceOrderRequest;
import com.clinic.ordering.app.PlaceOrderAppService;
import com.clinic.shared.kernel.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 第 0 天：证明主链路是通的。
 * 点一次复现，就真实下一笔龙井。库存会少 1。这不是病，是体检。
 */
@Slf4j
@Component
public class Day00HealthyPathCase implements IncidentCase {

    private final PlaceOrderAppService orders;

    public Day00HealthyPathCase(PlaceOrderAppService orders) {
        this.orders = orders;
    }

    @Override
    public String code() {
        return "DAY-00";
    }

    @Override
    public String title() {
        return "健康路径体检";
    }

    @Override
    public String symptom() {
        return "点复现会真实下一笔「龙井试饮」。库存少 1，订单状态是 RESERVED，日志里有追踪号和 Outbox。";
    }

    @Override
    public String veteranPoint() {
        return "先记住健康时长什么样：短事务、乐观锁、幂等键、金额用分、消息走 Outbox。以后的病都是这条路的某个环节坏了。";
    }

    @Override
    public String howToEnable() {
        return "不用开。它不替换库存策略。库存不够时，复现结果会告诉你还剩多少。";
    }

    @Override
    public boolean mutatesBehavior() {
        return false;
    }

    @Override
    public ReproductionResult reproduce() {
        var steps = List.of(
                "用一个新的幂等键下一笔 TEA x 1",
                "库存可售减 1，预占加 1",
                "同一事务写入订单、幂等记录、Outbox",
                "到履约台可以再点一次支付，预占会被确认掉"
        );
        try {
            var view = orders.place(
                    new PlaceOrderRequest("病例复现", List.of(new PlaceOrderRequest.Line("TEA", 1))),
                    "day00-" + UUID.randomUUID());
            log.info("[day00] 体检订单 {}", view.orderNo());
            return new ReproductionResult(code(), "主链路是通的，订单号 " + view.orderNo(), steps, view);
        } catch (BizException ex) {
            return new ReproductionResult(code(), "体检没有下成：" + ex.getMessage(), steps,
                    Map.of("code", ex.errorCode().code()));
        }
    }
}
