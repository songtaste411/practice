package com.clinic.inventory.repo;

import com.clinic.inventory.domain.InventoryStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long> {

    Optional<InventoryStock> findBySkuId(Long skuId);
}
