package com.clinic.payment.app;

import com.clinic.payment.api.PayResult;
import com.clinic.shared.kernel.BizException;
import com.clinic.shared.kernel.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

/**
 * 支付入口。唯一约束冲突说明有人抢先提交了同一笔回调，把赢家的结果读出来即可。
 */
@Slf4j
@Service
public class PaymentAppService {

    private final PaymentTx tx;

    public PaymentAppService(PaymentTx tx) {
        this.tx = tx;
    }

    public PayResult pay(String orderNo, String payNo) {
        try {
            return tx.doPay(orderNo.trim(), payNo.trim());
        } catch (DataIntegrityViolationException ex) {
            log.warn("[payment] 支付单号唯一约束冲突，改为读取已有记录 payNo={}", payNo);
            return tx.reload(payNo.trim());
        } catch (OptimisticLockingFailureException ex) {
            log.warn("[payment] 确认库存时乐观锁冲突 payNo={}", payNo);
            throw new BizException(ErrorCode.CONFLICT, "支付确认时库存版本冲突，请重试");
        }
    }
}
