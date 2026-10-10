package com.clinic.inventory.domain;

import com.clinic.inventory.repo.InventoryStockRepository;
import com.clinic.shared.kernel.BizException;
import com.clinic.shared.kernel.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * 健康策略：读出行，改数量，立刻 flush。
 * flush 会把乐观锁冲突当场抛出来，外层才能重试。
 * 如果拖到方法结束才写库，重试逻辑会和别的异常缠在一起。
 */
@Component
public class HealthyReserveStrategy implements ReserveStrategy {

    private final InventoryStockRepository stocks;

    public HealthyReserveStrategy(InventoryStockRepository stocks) {
        this.stocks = stocks;
    }

    @Override
    public String switchName() {
        return "none";
    }

    @Override
    public String title() {
        return "健康实现：乐观锁，扣库存和写订单在同一个事务";
    }

    @Override
    public void reserve(Long skuId, int quantity) {
        var stock = load(skuId);
        stock.reserve(quantity);
        stocks.saveAndFlush(stock);
    }

    @Override
    public void confirmPaid(Long skuId, int quantity) {
        var stock = load(skuId);
        stock.confirmPaid(quantity);
        stocks.saveAndFlush(stock);
    }

    @Override
    public void release(Long skuId, int quantity) {
        var stock = load(skuId);
        stock.release(quantity);
        stocks.saveAndFlush(stock);
    }

    private com.clinic.inventory.domain.InventoryStock load(Long skuId) {
        return stocks.findBySkuId(skuId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "没有这个商品的库存"));
    }
}
