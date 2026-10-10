package com.clinic.shared.outbox;

import com.clinic.shared.kernel.Clocks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 现在只把消息打到日志里，并标记已发送。
 * 以后换成 Kafka 时：发送成功才 markPublished。
 * 发送失败保持 PENDING，下一轮再发。这是 Outbox 的全部要义。
 */
@Slf4j
@Component
public class OutboxPublisher {

    private final OutboxRepository outbox;

    public OutboxPublisher(OutboxRepository outbox) {
        this.outbox = outbox;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publish() {
        var batch = outbox.findTop20ByStatusOrderByIdAsc(OutboxEvent.PENDING);
        for (var event : batch) {
            log.info("[outbox] 投递 type={} aggregate={} payload={}",
                    event.getEventType(), event.getAggregateId(), event.getPayload());
            event.markPublished(Clocks.now());
        }
    }
}
