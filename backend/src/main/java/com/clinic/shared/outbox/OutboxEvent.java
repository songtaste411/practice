package com.clinic.shared.outbox;

import com.clinic.shared.kernel.Clocks;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 待发送消息。和订单写在同一个事务里。
 * 事务提交成功，这行一定在。事务回滚，这行也没有。
 * 不要在事务里直接发消息：事务回滚了，消息已经飞出去，对方会收到一笔不存在的订单。
 *
 * payload 用普通字符串，不用 @Lob。
 * PostgreSQL 里对 String 用 @Lob 会变成 oid 大对象，是老坑。
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "outbox_event")
public class OutboxEvent {

    public static final String PENDING = "PENDING";
    public static final String PUBLISHED = "PUBLISHED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String eventType;

    @Column(nullable = false, length = 64)
    private String aggregateId;

    @Column(nullable = false, length = 2000)
    private String payload;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    public static OutboxEvent pending(String eventType, String aggregateId, String payload) {
        var event = new OutboxEvent();
        event.eventType = eventType;
        event.aggregateId = aggregateId;
        event.payload = payload;
        event.status = PENDING;
        event.createdAt = Clocks.now();
        return event;
    }

    public void markPublished(LocalDateTime when) {
        this.status = PUBLISHED;
        this.publishedAt = when;
    }
}
