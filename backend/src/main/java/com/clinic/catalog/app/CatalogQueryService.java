package com.clinic.catalog.app;

import com.clinic.catalog.api.SkuView;
import com.clinic.catalog.cache.SkuCache;
import com.clinic.catalog.repo.SkuRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogQueryService {

    private final SkuRepository skus;
    private final SkuCache cache;

    public CatalogQueryService(SkuRepository skus, SkuCache cache) {
        this.skus = skus;
        this.cache = cache;
    }

    public List<SkuView> list() {
        var cached = cache.get();
        if (cached.isPresent()) {
            return cached.get();
        }
        var views = skus.findAll().stream()
                .map(sku -> new SkuView(sku.getSkuCode(), sku.getName(), sku.getPriceCent()))
                .toList();
        cache.put(views);
        return views;
    }
}
