package com.clinic.incident.cases;

import com.clinic.incident.IncidentCase;
import com.clinic.incident.lab.ContextSwitchLab;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 第一天：感受「线程开太多」带来的慢。
 * 不改下单逻辑。点「试一次」会先后用乱开线程和线程池干同一批活，把两边耗时摆出来。
 */
@Slf4j
@Component
public class Day01ContextSwitchCase implements IncidentCase {

    private final ContextSwitchLab lab;

    public Day01ContextSwitchCase(ContextSwitchLab lab) {
        this.lab = lab;
    }

    @Override
    public String code() {
        return "DAY-01";
    }

    @Override
    public String title() {
        return "线程开太多，反而更慢";
    }

    @Override
    public String symptom() {
        return "点「试一次」后，同一批 2000 件小活会跑两遍：先每个任务乱开一个线程，再用固定人数的线程池。乱开那一边通常更慢，峰值线程数也会高很多。";
    }

    @Override
    public String veteranPoint() {
        return "频繁换线程会浪费 CPU。线程数不是越多越好；用合理大小的线程池，少创建、少销毁、少切换。";
    }

    @Override
    public String howToEnable() {
        return "不用改开关。直接点「试一次」。这一题不改下单，只比两种干法谁更快。";
    }

    @Override
    public boolean mutatesBehavior() {
        return false;
    }

    @Override
    public ReproductionResult reproduce() {
        var steps = List.of(
                "先用「每个任务 new 一个线程」干完 2000 件小活，记下耗时和峰值线程数",
                "再用固定人数的线程池干同样的活",
                "对比两边：乱开通常更慢，因为 CPU 忙着换人"
        );
        ContextSwitchLab.Comparison comparison = lab.run();
        log.info("[day01] {}", comparison.summary());
        return new ReproductionResult(
                code(),
                comparison.summary(),
                steps,
                Map.of(
                        "taskCount", comparison.taskCount(),
                        "poolSize", comparison.poolSize(),
                        "wasteful", comparison.wasteful(),
                        "pooled", comparison.pooled()
                )
        );
    }
}
