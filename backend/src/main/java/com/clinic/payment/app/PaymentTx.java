package com.clinic.payment.app;

import com.clinic.inventory.app.ReserveStrategyRouter;
import com.clinic.ordering.domain.OrderStatus;
import com.clinic.ordering.repo.CustomerOrderRepository;
import com.clinic.payment.api.PayResult;
import com.clinic.payment.domain.PaymentRecord;
import com.clinic.payment.repo.PaymentRepository;
import com.clinic.shared.kernel.BizException;
import com.clinic.shared.kernel.ErrorCode;
import com.clinic.shared.outbox.OutboxEvent;
import com.clinic.shared.outbox.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 支付回调事务。
 * 两道锁：支付单号唯一；订单状态只能从 RESERVED 到 PAID。
 * 少任何一道，重复回调都会把预占扣成负数。
 */
@Slf4j
@Service
public class PaymentTx {

    private final CustomerOrderRepository orders;
    private final PaymentRepository payments;
    private final OutboxRepository outbox;
    private final ReserveStrategyRouter inventory;
    private final ObjectMapper mapper;

    public PaymentTx(CustomerOrderRepository orders, PaymentRepository payments, OutboxRepository outbox,
                     ReserveStrategyRouter inventory, ObjectMapper mapper) {
        this.orders = orders;
        this.payments = payments;
        this.outbox = outbox;
        this.inventory = inventory;
        this.mapper = mapper;
    }

    @Transactional
    public PayResult doPay(String orderNo, String payNo) {
        var order = orders.findByOrderNo(orderNo)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "订单不存在"));
        var existed = payments.findByPayNo(payNo).orElse(null);
        if (existed != null) {
            if (!existed.getOrderId().equals(order.getId())) {
                throw new BizException(ErrorCode.CONFLICT, "这个支付单号已经用在别的订单上了");
            }
            log.info("[payment] 重复回调 payNo={} orderNo={}", payNo, orderNo);
            return new PayResult(payNo, order.getOrderNo(), order.getStatus().name(), existed.getAmountCent(), true);
        }
        if (order.getStatus() == OrderStatus.PAID) {
            log.info("[payment] 订单已支付，忽略新的支付单号 orderNo={} payNo={}", orderNo, payNo);
            return new PayResult(payNo, order.getOrderNo(), OrderStatus.PAID.name(), order.getTotalCent(), true);
        }
        if (order.getStatus() != OrderStatus.RESERVED) {
            throw new BizException(ErrorCode.ORDER_STATE, "订单现在是 " + order.getStatus() + "，不能支付");
        }
        for (var line : order.getLines()) {
            inventory.current().confirmPaid(line.getSkuId(), line.getQuantity());
        }
        payments.saveAndFlush(PaymentRecord.paid(order.getId(), payNo, order.getTotalCent()));
        order.markPaid();
        orders.save(order);
        outbox.save(OutboxEvent.pending("OrderPaid", order.getOrderNo(), payload(order.getOrderNo(), payNo)));
        log.info("[payment] 支付完成 orderNo={} payNo={} cent={}", orderNo, payNo, order.getTotalCent());
        return new PayResult(payNo, order.getOrderNo(), OrderStatus.PAID.name(), order.getTotalCent(), false);
    }

    @Transactional(readOnly = true)
    public PayResult reload(String payNo) {
        var payment = payments.findByPayNo(payNo)
                .orElseThrow(() -> new BizException(ErrorCode.CONFLICT, "支付没有写成，请重试"));
        var order = orders.findById(payment.getOrderId())
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "订单不存在"));
        return new PayResult(payNo, order.getOrderNo(), order.getStatus().name(), payment.getAmountCent(), true);
    }

    private String payload(String orderNo, String payNo) {
        try {
            return mapper.writeValueAsString(Map.of("orderNo", orderNo, "payNo", payNo));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
