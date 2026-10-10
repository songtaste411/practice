package com.clinic.ordering.web;

import com.clinic.ordering.api.OrderView;
import com.clinic.ordering.api.PlaceOrderRequest;
import com.clinic.ordering.app.PlaceOrderAppService;
import com.clinic.shared.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final PlaceOrderAppService orders;

    public OrderController(PlaceOrderAppService orders) {
        this.orders = orders;
    }

    @PostMapping
    public ApiResponse<OrderView> place(
            @RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
            @Valid @RequestBody PlaceOrderRequest request) {
        return ApiResponse.ok(orders.place(request, idemKey));
    }

    @GetMapping
    public ApiResponse<List<OrderView>> list() {
        return ApiResponse.ok(orders.list());
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<OrderView> get(@PathVariable String orderNo) {
        return ApiResponse.ok(orders.get(orderNo));
    }
}
