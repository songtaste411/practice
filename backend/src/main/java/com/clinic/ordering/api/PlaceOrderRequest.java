package com.clinic.ordering.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PlaceOrderRequest(
        @NotBlank(message = "请填写买家") @Size(max = 64, message = "买家名字太长") String buyerName,
        @NotEmpty(message = "请至少选择一件商品") List<@Valid Line> lines
) {
    public record Line(
            @NotBlank(message = "缺少商品编码") String skuCode,
            @Min(value = 1, message = "数量至少为 1") @Max(value = 999, message = "单行数量过大") int quantity
    ) {
    }
}
