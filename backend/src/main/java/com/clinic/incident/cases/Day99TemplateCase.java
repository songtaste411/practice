package com.clinic.incident.cases;

import com.clinic.incident.IncidentCase;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 复制这个类，改成 Day01XxxCase。
 * 如果这个病会改变扣库存的方式，再写一个 ReserveStrategy，switchName 返回 day01。
 * 不要改 HealthyReserveStrategy。健康实现留着，用来和病对比。
 */
@Component
public class Day99TemplateCase implements IncidentCase {

    @Override
    public String code() {
        return "DAY-99";
    }

    @Override
    public String title() {
        return "以后加新题的空位";
    }

    @Override
    public String symptom() {
        return "这不是故障，是以后加新练习题的空位。";
    }

    @Override
    public String veteranPoint() {
        return "今天跳过。新题要能单独打开，也能单独关掉。";
    }

    @Override
    public String howToEnable() {
        return "先不用管。以后要加题时，再看说明书里的「以后再看」。";
    }

    @Override
    public boolean mutatesBehavior() {
        return false;
    }

    @Override
    public ReproductionResult reproduce() {
        return new ReproductionResult(code(), "模板不会改数据。", List.of(
                "运行 tools/new-case.ps1，生成 Day01 病例和空策略",
                "在新策略里写下有病的扣库存方式",
                "打开病例板，把开关拨到 day01",
                "回到履约台连点下单，看库存和订单是否对得上",
                "修好之后把开关拨回 none，确认健康路径还在",
                "在 docs/case-log.md 记一笔：现象、根因、怎么修的"
        ), null);
    }
}
