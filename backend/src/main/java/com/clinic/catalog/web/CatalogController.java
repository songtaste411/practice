package com.clinic.catalog.web;

import com.clinic.catalog.api.SkuView;
import com.clinic.catalog.app.CatalogQueryService;
import com.clinic.shared.web.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogQueryService catalog;

    public CatalogController(CatalogQueryService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/skus")
    public ApiResponse<List<SkuView>> skus() {
        return ApiResponse.ok(catalog.list());
    }
}
