package com.clinic.incident.web;

import com.clinic.incident.ArchitectureCatalog;
import com.clinic.incident.IncidentAppService;
import com.clinic.incident.IncidentCase;
import com.clinic.incident.api.IncidentBoard;
import com.clinic.incident.api.SystemMap;
import com.clinic.shared.web.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class IncidentController {

    private final IncidentAppService incidents;
    private final ArchitectureCatalog catalog;

    public IncidentController(IncidentAppService incidents, ArchitectureCatalog catalog) {
        this.incidents = incidents;
        this.catalog = catalog;
    }

    @GetMapping("/incidents")
    public ApiResponse<IncidentBoard> board() {
        return ApiResponse.ok(incidents.board());
    }

    @PutMapping("/incidents/active")
    public ApiResponse<IncidentBoard> activate(@Valid @RequestBody SwitchBody body) {
        return ApiResponse.ok(incidents.activate(body.active()));
    }

    @PostMapping("/incidents/{code}/reproduce")
    public ApiResponse<IncidentCase.ReproductionResult> reproduce(@PathVariable String code) {
        return ApiResponse.ok(incidents.reproduce(code));
    }

    @GetMapping("/meta/map")
    public ApiResponse<SystemMap> map() {
        return ApiResponse.ok(catalog.map());
    }

    public record SwitchBody(@NotBlank(message = "请选择开关") String active) {
    }
}
