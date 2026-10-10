package com.clinic.shared.kernel;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 统一用 UTC 记时间。
 * 不要一会儿用本地时区一会儿用数据库时区，对账时会对不齐。
 */
public final class Clocks {

    private Clocks() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
