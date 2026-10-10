package com.clinic.incident.api;

import java.util.List;

public record SystemMap(List<ContextCard> contexts, List<HookCard> hooks) {

    public record ContextCard(String name, String duty, String packageName) {
    }

    public record HookCard(String name, String where, String why) {
    }
}
