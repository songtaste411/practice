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
        return "模板：复制我，变成明天的病例";
    }

    @Override
    public String symptom() {
        return "我本身没有病。我只告诉你新病例该放哪。";
    }

    @Override
    public String veteranPoint() {
        return "病要可开关、可复现、可单独关掉。修完把根因写回这个病例的 veteranPoint，别只留在聊天记录里。";
    }

    @Override
    public String howToEnable() {
        return "在项目根目录运行：powershell -ExecutionPolicy Bypass -File .\\tools\\new-case.ps1 -Day 01 -Name Oversell -Title \"连点超卖\"";
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
