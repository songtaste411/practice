package com.clinic.incident.api;

import java.util.List;

public record IncidentBoard(
        String activeSwitch,
        List<StrategyCard> strategies,
        List<IncidentCard> cases
) {
    public record StrategyCard(String switchName, String title, boolean active) {
    }

    public record IncidentCard(
            String code,
            String title,
            String symptom,
            String veteranPoint,
            String howToEnable,
            boolean mutatesBehavior
    ) {
    }
}
