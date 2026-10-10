package com.clinic.ordering.api;

import com.clinic.ordering.domain.CustomerOrder;

import java.util.List;

/**
 * 下单接口的对外形状。改字段名等于改契约，旧的幂等回放会对不上。
 */
public record OrderView(
        String orderNo,
        String status,
        String buyerName,
        long totalCent,
        String createdAt,
        boolean replayed,
        List<Line> lines
) {
    public record Line(String skuCode, String skuName, int quantity, long priceCent) {
    }

    public OrderView withReplayed(boolean value) {
        return new OrderView(orderNo, status, buyerName, totalCent, createdAt, value, lines);
    }

    public static OrderView from(CustomerOrder order) {
        var lines = order.getLines().stream()
                .map(line -> new Line(line.getSkuCode(), line.getSkuName(), line.getQuantity(), line.getPriceCent()))
                .toList();
        return new OrderView(
                order.getOrderNo(),
                order.getStatus().name(),
                order.getBuyerName(),
                order.getTotalCent(),
                order.getCreatedAt().toString(),
                false,
                lines
        );
    }
}
