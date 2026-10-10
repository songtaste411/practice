package com.clinic.catalog.cache;

import com.clinic.catalog.api.SkuView;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** 本机默认实现：不连 Redis，每次读数据库。 */
@Component
@ConditionalOnProperty(name = "clinic.redis.enabled", havingValue = "false", matchIfMissing = true)
public class NoopSkuCache implements SkuCache {

    @Override
    public Optional<List<SkuView>> get() {
        return Optional.empty();
    }

    @Override
    public void put(List<SkuView> skus) {
        // 内存模式没有缓存可写。
    }

    @Override
    public void evict() {
        // 同上。
    }
}
