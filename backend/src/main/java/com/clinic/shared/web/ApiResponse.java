package com.clinic.shared.web;

import com.clinic.shared.kernel.ErrorCode;
import org.slf4j.MDC;

/**
 * 所有接口都包这一层。前端只认 code / message / traceId / data。
 * traceId 用来把一次点击和后台日志对上。
 */
public record ApiResponse<T>(int code, String message, String traceId, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "ok", currentTrace(), data);
    }

    public static ApiResponse<Void> fail(ErrorCode error, String message) {
        return new ApiResponse<>(error.code(), message, currentTrace(), null);
    }

    private static String currentTrace() {
        var id = MDC.get("traceId");
        return id == null ? "" : id;
    }
}
