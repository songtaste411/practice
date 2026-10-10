package com.clinic.ordering.app;

import com.clinic.ordering.api.OrderView;
import com.clinic.ordering.api.PlaceOrderRequest;
import com.clinic.ordering.repo.CustomerOrderRepository;
import com.clinic.shared.kernel.BizException;
import com.clinic.shared.kernel.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 下单入口。事务在 PlaceOrderTx 里面。
 * 这里只负责：并发冲突时重试，幂等键撞车时把赢家的结果还回去。
 */
@Slf4j
@Service
public class PlaceOrderAppService {

    private static final int MAX_ATTEMPTS = 3;

    private final PlaceOrderTx tx;
    private final CustomerOrderRepository orders;

    public PlaceOrderAppService(PlaceOrderTx tx, CustomerOrderRepository orders) {
        this.tx = tx;
        this.orders = orders;
    }

    public OrderView place(PlaceOrderRequest request, String idemKey) {
        var key = normalizeKey(idemKey);
        for (var attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return tx.execute(request, key);
            } catch (OptimisticLockingFailureException ex) {
                log.warn("[order] 乐观锁冲突，第 {} 次 key={}", attempt, key);
                if (attempt == MAX_ATTEMPTS) {
                    throw new BizException(ErrorCode.CONFLICT, "库存正在被别人抢，请再试一次");
                }
            } catch (DataIntegrityViolationException ex) {
                if (key != null) {
                    var replay = tx.replayIfAny(key, request);
                    if (replay != null) {
                        log.info("[order] 并发下单被唯一约束拦住，返回第一次的结果 key={}", key);
                        return replay;
                    }
                }
                log.error("[order] 数据约束冲突", ex);
                throw new BizException(ErrorCode.CONFLICT, "请求和已有数据冲突");
            }
        }
        throw new BizException(ErrorCode.CONFLICT, "下单失败");
    }

    @Transactional(readOnly = true)
    public List<OrderView> list() {
        return orders.findAllByOrderByIdDesc().stream().map(OrderView::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderView get(String orderNo) {
        return orders.findByOrderNo(orderNo)
                .map(OrderView::from)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "订单不存在"));
    }

    private String normalizeKey(String idemKey) {
        if (idemKey == null || idemKey.isBlank()) {
            return null;
        }
        var trimmed = idemKey.trim();
        if (trimmed.length() > 80) {
            throw new BizException(ErrorCode.BAD_REQUEST, "幂等键最长 80 个字符");
        }
        return trimmed;
    }
}
