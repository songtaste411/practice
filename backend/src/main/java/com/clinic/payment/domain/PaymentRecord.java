package com.clinic.payment.domain;

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

@Getter
@NoArgsConstructor
@Entity
@Table(name = "payment_record")
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false, unique = true, length = 64)
    private String payNo;

    @Column(nullable = false)
    private long amountCent;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static PaymentRecord paid(Long orderId, String payNo, long amountCent) {
        var record = new PaymentRecord();
        record.orderId = orderId;
        record.payNo = payNo;
        record.amountCent = amountCent;
        record.status = "PAID";
        record.createdAt = Clocks.now();
        return record;
    }
}
