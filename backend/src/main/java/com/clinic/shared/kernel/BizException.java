package com.clinic.shared.kernel;

/**
 * 业务拒绝。调用方能看懂的失败，不是程序缺陷。
 * 库存不够、状态不对、幂等键冲突，都走这里。
 */
public class BizException extends RuntimeException {

    private final ErrorCode errorCode;

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
