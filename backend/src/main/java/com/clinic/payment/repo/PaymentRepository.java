package com.clinic.payment.repo;

import com.clinic.payment.domain.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentRecord, Long> {

    Optional<PaymentRecord> findByPayNo(String payNo);
}
