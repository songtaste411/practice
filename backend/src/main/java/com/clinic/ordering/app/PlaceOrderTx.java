package com.clinic.ordering.app;

import com.clinic.catalog.repo.SkuRepository;
import com.clinic.inventory.app.ReserveStrategyRouter;
import com.clinic.ordering.api.OrderView;
import com.clinic.ordering.api.PlaceOrderRequest;
import com.clinic.ordering.domain.CustomerOrder;
import com.clinic.ordering.domain.IdempotencyRecord;
import com.clinic.ordering.domain.OrderLine;
import com.clinic.ordering.repo.CustomerOrderRepository;
import com.clinic.ordering.repo.IdempotencyRepository;
import com.clinic.shared.kernel.BizException;
import com.clinic.shared.kernel.ErrorCode;
import com.clinic.shared.outbox.OutboxEvent;
import com.clinic.shared.outbox.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 真正落库的下单事务。重试不要写在这个类的外面再套一层事务，
 * 否则第一次乐观锁失败会把事务标成只能回滚，重试也没用。
 */
@Slf4j
@Service
public class PlaceOrderTx {

    private static final DateTimeFormatter ORDER_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final SkuRepository skus;
    private final CustomerOrderRepository orders;
    private final IdempotencyRepository idempotency;
    private final OutboxRepository outbox;
    private final ReserveStrategyRouter inventory;
    private final ObjectMapper mapper;

    public PlaceOrderTx(SkuRepository skus, CustomerOrderRepository orders, IdempotencyRepository idempotency,
                        OutboxRepository outbox, ReserveStrategyRouter inventory, ObjectMapper mapper) {
        this.skus = skus;
        this.orders = orders;
        this.idempotency = idempotency;
        this.outbox = outbox;
        this.inventory = inventory;
        this.mapper = mapper;
    }

    @Transactional
    public OrderView execute(PlaceOrderRequest request, String idemKey) {
        var replay = readReplay(idemKey, request);
        if (replay != null) {
            log.info("[order] 幂等命中 key={}", idemKey);
            return replay;
        }
        ensureLines(request);
        var order = CustomerOrder.open(newOrderNo(), request.buyerName().trim(), idemKey);
        for (var line : request.lines()) {
            var sku = skus.findBySkuCode(line.skuCode().trim())
                    .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "没有这个商品：" + line.skuCode()));
            inventory.current().reserve(sku.getId(), line.quantity());
            order.addLine(OrderLine.of(sku.getId(), sku.getSkuCode(), sku.getName(), line.quantity(), sku.getPriceCent()));
        }
        orders.saveAndFlush(order);
        outbox.save(OutboxEvent.pending("OrderReserved", order.getOrderNo(), payload(order.getOrderNo())));
        var view = OrderView.from(order);
        if (idemKey != null) {
            idempotency.save(IdempotencyRecord.create(idemKey, hash(request), write(view)));
        }
        log.info("[order] 已预占 orderNo={} totalCent={} strategy={}",
                order.getOrderNo(), order.getTotalCent(), inventory.activeName());
        return view;
    }

    @Transactional(readOnly = true)
    public OrderView replayIfAny(String idemKey, PlaceOrderRequest request) {
        return readReplay(idemKey, request);
    }

    private OrderView readReplay(String idemKey, PlaceOrderRequest request) {
        if (idemKey == null) {
            return null;
        }
        var existing = idempotency.findByIdemKey(idemKey).orElse(null);
        if (existing == null) {
            return null;
        }
        if (!existing.getRequestHash().equals(hash(request))) {
            throw new BizException(ErrorCode.IDEMPOTENCY_CONFLICT, "同一个幂等键，两次请求内容不一样");
        }
        try {
            return mapper.readValue(existing.getResponseBody(), OrderView.class).withReplayed(true);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("幂等记录损坏 key=" + idemKey, ex);
        }
    }

    private void ensureLines(PlaceOrderRequest request) {
        if (request.lines().size() > 20) {
            throw new BizException(ErrorCode.BAD_REQUEST, "一次最多 20 行");
        }
        var seen = new HashSet<String>();
        for (var line : request.lines()) {
            if (!seen.add(line.skuCode().trim())) {
                throw new BizException(ErrorCode.BAD_REQUEST, "同一商品请合成一行：" + line.skuCode());
            }
        }
    }

    private String payload(String orderNo) {
        return write(java.util.Map.of("orderNo", orderNo));
    }

    private String write(Object value) {
        try {
            var json = mapper.writeValueAsString(value);
            if (json.length() > 4000) {
                throw new BizException(ErrorCode.BAD_REQUEST, "订单内容太大");
            }
            return json;
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("JSON 写入失败", ex);
        }
    }

    static String hash(PlaceOrderRequest request) {
        var body = request.lines().stream()
                .sorted(Comparator.comparing(line -> line.skuCode().trim()))
                .map(line -> line.skuCode().trim() + ":" + line.quantity())
                .reduce((left, right) -> left + "," + right)
                .orElse("");
        var raw = request.buyerName().trim() + "|" + body;
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * 训练场单号。生产里不要只用时间戳，时钟回拨或同一秒打满都会撞车。
     */
    private String newOrderNo() {
        var time = ORDER_NO_TIME.format(LocalDateTime.now());
        var suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "O" + time + suffix;
    }
}
