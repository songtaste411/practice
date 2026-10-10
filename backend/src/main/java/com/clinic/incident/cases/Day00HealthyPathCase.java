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
        return "试一次正常下单";
    }

    @Override
    public String symptom() {
        return "点「试一次」会真的买一杯龙井。回到「下单」页，还能卖的数量少 1，多出一笔待付款。";
    }

    @Override
    public String veteranPoint() {
        return "先记住正常情况：下单一杯只少一件，付款也不会再少一件。";
    }

    @Override
    public String howToEnable() {
        return "不用改开关，直接点。库存不够时，结果里会告诉你还剩多少。";
    }

    @Override
    public boolean mutatesBehavior() {
        return false;
    }

    @Override
    public ReproductionResult reproduce() {
        var steps = List.of(
                "真的买一杯龙井",
                "还能卖的少 1，先占着的多 1",
                "回到「下单」页能看到这笔待付款",
                "再点假装付款，占着的货才算卖掉"
        );
        try {
            var view = orders.place(
                    new PlaceOrderRequest("病例复现", List.of(new PlaceOrderRequest.Line("TEA", 1))),
                    "day00-" + UUID.randomUUID());
            log.info("[day00] 体检订单 {}", view.orderNo());
            return new ReproductionResult(code(), "买好了，订单号 " + view.orderNo(), steps, view);
        } catch (BizException ex) {
            return new ReproductionResult(code(), "没买成：" + ex.getMessage(), steps,
                    Map.of("code", ex.errorCode().code()));
        }
    }
}
