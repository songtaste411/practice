package com.clinic.inventory.domain;

/**
 * 库存策略。健康实现叫 none。
 * 明天的病例另写一个实现，switchName 返回 day01 这种名字。
 * 不要去改健康实现来“制造 bug”，把病关在新类里，开关一拨就能对比。
 */
public interface ReserveStrategy {

    /** 和页面上的开关、application.yml 的 clinic.cases.active 对得上。 */
    String switchName();

    /** 给人看的一句话。 */
    String title();

    void reserve(Long skuId, int quantity);

    void confirmPaid(Long skuId, int quantity);

    void release(Long skuId, int quantity);
}
