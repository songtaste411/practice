package com.clinic.catalog.api;

/**
 * 商品展示。故意不带库存。
 * 库存会变，商品名和价格很少变，两套数据不要塞进同一个缓存。
 */
public record SkuView(String skuCode, String name, long priceCent) {
}
