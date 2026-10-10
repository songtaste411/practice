package com.clinic.inventory.web;

import com.clinic.inventory.api.StockView;
import com.clinic.inventory.app.InventoryQueryService;
import com.clinic.shared.web.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryQueryService inventory;

    public InventoryController(InventoryQueryService inventory) {
        this.inventory = inventory;
    }

    @GetMapping
    public ApiResponse<List<StockView>> list() {
        return ApiResponse.ok(inventory.list());
    }
}
