package com.clinic.inventory.domain;

import com.clinic.shared.kernel.BizException;
import com.clinic.shared.kernel.Clocks;
import com.clinic.shared.kernel.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 一行库存。version 是乐观锁。
 * 两个人同时读到剩余 1 件时，后写入的那个会因为 version 变了而失败，
 * 而不是两个人都把库存扣成 0 以下。
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "inventory_stock")
public class InventoryStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long skuId;

    @Column(nullable = false)
    private int availableQty;

    @Column(nullable = false)
    private int reservedQty;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static InventoryStock init(Long skuId, int availableQty) {
        var stock = new InventoryStock();
        stock.skuId = skuId;
        stock.availableQty = availableQty;
        stock.reservedQty = 0;
        stock.updatedAt = Clocks.now();
        return stock;
    }

    /** 下单预占：可售减少，预占增加。钱还没付。 */
    public void reserve(int quantity) {
        ensurePositive(quantity);
        if (availableQty < quantity) {
            throw new BizException(ErrorCode.STOCK_NOT_ENOUGH, "库存不够，还剩 " + availableQty + " 件");
        }
        availableQty -= quantity;
        reservedQty += quantity;
        updatedAt = Clocks.now();
    }

    /** 支付成功：预占消耗掉。可售数在预占时已经减过，这里不能再减一次。 */
    public void confirmPaid(int quantity) {
        ensurePositive(quantity);
        if (reservedQty < quantity) {
            throw new BizException(ErrorCode.CONFLICT, "预占数量不够，支付确认失败");
        }
        reservedQty -= quantity;
        updatedAt = Clocks.now();
    }

    /** 取消订单时把预占退回可售。明天做取消病例会用到。 */
    public void release(int quantity) {
        ensurePositive(quantity);
        if (reservedQty < quantity) {
            throw new BizException(ErrorCode.CONFLICT, "预占数量不够，无法退回");
        }
        reservedQty -= quantity;
        availableQty += quantity;
        updatedAt = Clocks.now();
    }

    private void ensurePositive(int quantity) {
        if (quantity <= 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "数量必须大于 0");
        }
    }
}
