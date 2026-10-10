package com.clinic.catalog.domain;

import com.clinic.shared.kernel.Clocks;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 商品主数据。这里只有编码、名字、价格。
 * 库存不属于商品。库存放在 inventory，避免一个类什么都管。
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "sku")
public class Sku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String skuCode;

    @Column(nullable = false, length = 128)
    private String name;

    /** 单价，单位是分。 */
    @Column(nullable = false)
    private long priceCent;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static Sku create(String skuCode, String name, long priceCent) {
        var sku = new Sku();
        sku.skuCode = skuCode;
        sku.name = name;
        sku.priceCent = priceCent;
        sku.createdAt = Clocks.now();
        return sku;
    }
}
