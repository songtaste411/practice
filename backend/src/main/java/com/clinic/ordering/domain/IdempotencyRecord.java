package com.clinic.ordering.domain;

import com.clinic.shared.kernel.Clocks;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 幂等记录。和订单在同一个事务里插入。
 * 相同的键 + 相同的请求，第二次直接把第一次的结果还回去。
 * 相同的键 + 不同的请求，拒绝。这能抓住「前端把键复用错了」。
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "idempotency_record")
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String idemKey;

    @Column(nullable = false, length = 64)
    private String requestHash;

    @Column(nullable = false, length = 4000)
    private String responseBody;

    @Column(nullable = false)
    private java.time.LocalDateTime createdAt;

    public static IdempotencyRecord create(String idemKey, String requestHash, String responseBody) {
        var record = new IdempotencyRecord();
        record.idemKey = idemKey;
        record.requestHash = requestHash;
        record.responseBody = responseBody;
        record.createdAt = Clocks.now();
        return record;
    }
}
