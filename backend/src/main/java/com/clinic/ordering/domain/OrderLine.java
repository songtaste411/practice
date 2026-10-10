package com.clinic.ordering.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "order_line")
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long skuId;

    @Column(nullable = false, length = 64)
    private String skuCode;

    @Column(nullable = false, length = 128)
    private String skuName;

    @Column(nullable = false)
    private int quantity;

    /** 下单当时的单价（分）。之后商品改价，不能回头改这行。 */
    @Column(nullable = false)
    private long priceCent;

    /**
     * 用对方的主键，而不是中间表。
     * 单向 OneToMany 再加非空外键，Hibernate 可能先插入空的 order_id，数据库直接拒绝。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    public static OrderLine of(Long skuId, String skuCode, String skuName, int quantity, long priceCent) {
        var line = new OrderLine();
        line.skuId = skuId;
        line.skuCode = skuCode;
        line.skuName = skuName;
        line.quantity = quantity;
        line.priceCent = priceCent;
        return line;
    }

    void bind(CustomerOrder owner) {
        this.order = owner;
    }
}
