package com.clinic.inventory.app;

import com.clinic.catalog.repo.SkuRepository;
import com.clinic.inventory.api.StockView;
import com.clinic.inventory.domain.InventoryStock;
import com.clinic.inventory.repo.InventoryStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 一次把商品和库存都取回来，在内存里配对。
 * 不要写成「查一个商品再查一次库存」的循环，那是 N+1，数据一多数据库会先倒。
 */
@Service
public class InventoryQueryService {

    private final SkuRepository skus;
    private final InventoryStockRepository stocks;

    public InventoryQueryService(SkuRepository skus, InventoryStockRepository stocks) {
        this.skus = skus;
        this.stocks = stocks;
    }

    @Transactional(readOnly = true)
    public List<StockView> list() {
        var bySkuId = stocks.findAll().stream()
                .collect(Collectors.toMap(InventoryStock::getSkuId, Function.identity()));
        return skus.findAll().stream().map(sku -> {
            var stock = bySkuId.get(sku.getId());
            var available = stock == null ? 0 : stock.getAvailableQty();
            var reserved = stock == null ? 0 : stock.getReservedQty();
            return new StockView(sku.getSkuCode(), sku.getName(), available, reserved, sku.getPriceCent());
        }).toList();
    }
}
