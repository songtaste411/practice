package com.clinic.ordering.domain;

import com.clinic.shared.kernel.BizException;
import com.clinic.shared.kernel.Clocks;
import com.clinic.shared.kernel.ErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "customer_order")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String orderNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    @Column(nullable = false)
    private long totalCent;

    @Column(nullable = false, length = 64)
    private String buyerName;

    @Column(length = 80)
    private String idempotencyKey;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderLine> lines = new ArrayList<>();

    public static CustomerOrder open(String orderNo, String buyerName, String idempotencyKey) {
        var order = new CustomerOrder();
        order.orderNo = orderNo;
        order.buyerName = buyerName;
        order.idempotencyKey = idempotencyKey;
        order.status = OrderStatus.RESERVED;
        order.totalCent = 0;
        order.createdAt = Clocks.now();
        order.updatedAt = order.createdAt;
        return order;
    }

    public void addLine(OrderLine line) {
        line.bind(this);
        lines.add(line);
        totalCent += line.getPriceCent() * line.getQuantity();
    }

    public void markPaid() {
        if (status != OrderStatus.RESERVED) {
            throw new BizException(ErrorCode.ORDER_STATE, "订单现在是 " + status + "，不能支付");
        }
        status = OrderStatus.PAID;
        updatedAt = Clocks.now();
    }
}
