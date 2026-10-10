package com.clinic.incident;

import com.clinic.incident.api.IncidentBoard;
import com.clinic.inventory.app.ReserveStrategyRouter;
import org.springframework.stereotype.Service;

@Service
public class IncidentAppService {

    private final IncidentCaseRegistry registry;
    private final ReserveStrategyRouter router;

    public IncidentAppService(IncidentCaseRegistry registry, ReserveStrategyRouter router) {
        this.registry = registry;
        this.router = router;
    }

    public IncidentBoard board() {
        var active = router.activeName();
        var strategies = router.all().stream()
                .map(strategy -> new IncidentBoard.StrategyCard(
                        strategy.switchName(), strategy.title(), strategy.switchName().equals(active)))
                .sorted(java.util.Comparator.comparing(IncidentBoard.StrategyCard::switchName))
                .toList();
        var cases = registry.all().stream()
                .map(item -> new IncidentBoard.IncidentCard(
                        item.code(), item.title(), item.symptom(), item.veteranPoint(),
                        item.howToEnable(), item.mutatesBehavior()))
                .toList();
        return new IncidentBoard(active, strategies, cases);
    }

    public IncidentBoard activate(String name) {
        router.activate(name);
        return board();
    }

    public IncidentCase.ReproductionResult reproduce(String code) {
        return registry.require(code).reproduce();
    }
}
