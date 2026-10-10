package com.clinic.inventory.api;

public record StockView(
        String skuCode,
        String skuName,
        int availableQty,
        int reservedQty,
        long priceCent
) {
}
