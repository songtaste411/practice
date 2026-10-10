package com.clinic.shared.switcher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 训练场开关。决定这一刻用哪套库存策略。
 * 启动值来自 application.yml 的 clinic.cases.active。
 * 页面上可以临时改，改的是内存，重启后回到配置文件。
 * 这是演练开关，不要原样搬到生产当永久配置中心。
 */
@Component
public class CaseSwitch {

    private final AtomicReference<String> active = new AtomicReference<>("none");

    public CaseSwitch(@Value("${clinic.cases.active:none}") String fromConfig) {
        if (fromConfig == null || fromConfig.isBlank()) {
            active.set("none");
        } else {
            active.set(fromConfig.trim());
        }
    }

    public String get() {
        return active.get();
    }

    public void set(String name) {
        active.set(name);
    }
}
