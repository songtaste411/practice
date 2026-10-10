package com.clinic.incident;

import com.clinic.shared.kernel.BizException;
import com.clinic.shared.kernel.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class IncidentCaseRegistry {

    private final List<IncidentCase> cases;

    public IncidentCaseRegistry(List<IncidentCase> cases) {
        this.cases = cases.stream().sorted(Comparator.comparing(IncidentCase::code)).toList();
    }

    public List<IncidentCase> all() {
        return cases;
    }

    public IncidentCase require(String code) {
        return cases.stream()
                .filter(item -> item.code().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "没有这个病例：" + code));
    }
}
