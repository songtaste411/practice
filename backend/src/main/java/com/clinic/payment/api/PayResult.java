package com.clinic.payment.api;

/**
 * duplicate=true 表示这次回调被幂等吃掉了，没有再次扣减预占。
 */
public record PayResult(
        String payNo,
        String orderNo,
        String orderStatus,
        long amountCent,
        boolean duplicate
) {
}
