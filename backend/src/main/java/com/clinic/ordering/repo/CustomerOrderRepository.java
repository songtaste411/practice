package com.clinic.ordering.repo;

import com.clinic.ordering.domain.CustomerOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    @EntityGraph(attributePaths = "lines")
    Optional<CustomerOrder> findByOrderNo(String orderNo);

    @EntityGraph(attributePaths = "lines")
    List<CustomerOrder> findAllByOrderByIdDesc();
}
