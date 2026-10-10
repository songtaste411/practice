package com.clinic.shared.kernel;

/**
 * 对外错误码。HTTP 状态给网关看，code 给前端和病例日志看。
 * 不要只回一个 500，否则第二天你分不清是库存不够还是程序崩了。
 */
public enum ErrorCode {
    OK(0, 200),
    BAD_REQUEST(400, 400),
    UNAUTHORIZED(401, 401),
    NOT_FOUND(404, 404),
    CONFLICT(40900, 409),
    STOCK_NOT_ENOUGH(40901, 409),
    IDEMPOTENCY_CONFLICT(40902, 409),
    ORDER_STATE(40903, 409),
    INTERNAL(500, 500);

    private final int code;
    private final int http;

    ErrorCode(int code, int http) {
        this.code = code;
        this.http = http;
    }

    public int code() {
        return code;
    }

    public int http() {
        return http;
    }
}
