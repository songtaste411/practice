package com.clinic.payment.web;

import com.clinic.payment.api.PayRequest;
import com.clinic.payment.api.PayResult;
import com.clinic.payment.app.PaymentAppService;
import com.clinic.shared.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentAppService payments;

    public PaymentController(PaymentAppService payments) {
        this.payments = payments;
    }

    @PostMapping("/mock-callback")
    public ApiResponse<PayResult> callback(@Valid @RequestBody PayRequest request) {
        return ApiResponse.ok(payments.pay(request.orderNo(), request.payNo()));
    }
}
