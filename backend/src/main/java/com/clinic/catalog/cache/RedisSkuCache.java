package com.clinic.catalog.cache;

import com.clinic.catalog.api.SkuView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Docker / 生产形态才启用。缓存 30 秒，过期自己回源。
 * 读失败就当没命中，不能因为缓存坏了让商品页打不开。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "clinic.redis.enabled", havingValue = "true")
public class RedisSkuCache implements SkuCache {

    static final String KEY = "clinic:sku:list";
    private static final Duration TTL = Duration.ofSeconds(30);

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public RedisSkuCache(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    @Override
    public Optional<List<SkuView>> get() {
        try {
            var raw = redis.opsForValue().get(KEY);
            if (raw == null) {
                log.info("[catalog] 商品缓存未命中");
                return Optional.empty();
            }
            var list = mapper.readValue(raw, new TypeReference<List<SkuView>>() {
            });
            log.info("[catalog] 商品缓存命中 {} 条", list.size());
            return Optional.of(list);
        } catch (Exception ex) {
            log.warn("[catalog] 商品缓存读取失败，改为查数据库", ex);
            return Optional.empty();
        }
    }

    @Override
    public void put(List<SkuView> skus) {
        try {
            redis.opsForValue().set(KEY, mapper.writeValueAsString(skus), TTL);
        } catch (Exception ex) {
            log.warn("[catalog] 商品缓存写入失败", ex);
        }
    }

    @Override
    public void evict() {
        try {
            redis.delete(KEY);
        } catch (Exception ex) {
            log.warn("[catalog] 商品缓存删除失败", ex);
        }
    }
}
