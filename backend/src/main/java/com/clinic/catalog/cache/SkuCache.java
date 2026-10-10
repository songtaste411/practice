package com.clinic.catalog.cache;

import com.clinic.catalog.api.SkuView;

import java.util.List;
import java.util.Optional;

/**
 * 只缓存商品名和价格。谁把库存放进这里，超卖病就会很难查。
 */
public interface SkuCache {

    Optional<List<SkuView>> get();

    void put(List<SkuView> skus);

    void evict();
}
