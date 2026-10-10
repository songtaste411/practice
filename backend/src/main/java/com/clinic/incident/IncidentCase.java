package com.clinic.incident;

import java.util.List;

/**
 * 一个病例。讲解类病例只负责把现象说清楚。
 * 会改行为的病，另外实现 ReserveStrategy，用同一个开关名挂上去。
 */
public interface IncidentCase {

    String code();

    String title();

    /** 线上会看到什么。 */
    String symptom();

    /** 这个病在考哪块老兵功夫。先写考察点，别把答案写死。 */
    String veteranPoint();

    /** 明天的人怎样把这个病接进架子。 */
    String howToEnable();

    /** true 表示它会换掉健康策略。讲解类病例是 false。 */
    boolean mutatesBehavior();

    default ReproductionResult reproduce() {
        return new ReproductionResult(code(), "这个病例还没有复现脚本", List.of("打开 docs/how-to-add-a-case.md"), null);
    }

    record ReproductionResult(String code, String summary, List<String> steps, Object payload) {
    }
}
