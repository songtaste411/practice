package com.clinic.payment.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PayRequest(
        @NotBlank(message = "缺少订单号") String orderNo,
        @NotBlank(message = "缺少支付单号") @Size(max = 64, message = "支付单号太长") String payNo
) {
}
