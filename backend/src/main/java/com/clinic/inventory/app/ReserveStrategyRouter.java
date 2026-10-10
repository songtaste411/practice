package com.clinic.inventory.app;

import com.clinic.inventory.domain.ReserveStrategy;
import com.clinic.shared.kernel.BizException;
import com.clinic.shared.kernel.ErrorCode;
import com.clinic.shared.switcher.CaseSwitch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 每次扣库存前看一眼当前开关，挑对应的策略。
 * 策略都常驻在内存里，拨开关不用重启。
 * 同一时间只生效一个，避免两个病一起发作你分不清。
 */
@Slf4j
@Component
public class ReserveStrategyRouter {

    private final CaseSwitch caseSwitch;
    private final List<ReserveStrategy> strategies;

    public ReserveStrategyRouter(CaseSwitch caseSwitch, List<ReserveStrategy> strategies) {
        this.caseSwitch = caseSwitch;
        this.strategies = List.copyOf(strategies);
        var names = names();
        var duplicated = names.stream().filter(name -> Collections.frequency(names, name) > 1).distinct().toList();
        if (!duplicated.isEmpty()) {
            throw new IllegalStateException("库存策略开关重名: " + duplicated);
        }
        log.info("已注册库存策略: {}", names);
    }

    public ReserveStrategy current() {
        var name = caseSwitch.get();
        return strategies.stream()
                .filter(strategy -> strategy.switchName().equals(name))
                .findFirst()
                .orElseThrow(() -> new BizException(ErrorCode.BAD_REQUEST,
                        "开关「" + name + "」没有库存策略。现有：" + names()));
    }

    public void activate(String name) {
        var target = name == null ? "" : name.trim();
        if (strategies.stream().noneMatch(strategy -> strategy.switchName().equals(target))) {
            throw new BizException(ErrorCode.BAD_REQUEST, "没有这个开关。现有：" + names());
        }
        caseSwitch.set(target);
        log.warn("训练场库存策略已切换为 {}。重启后回到 application.yml", target);
    }

    public List<ReserveStrategy> all() {
        return strategies;
    }

    public String activeName() {
        return caseSwitch.get();
    }

    public List<String> names() {
        return strategies.stream().map(ReserveStrategy::switchName).sorted().toList();
    }
}
