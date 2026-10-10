package com.clinic.shared.web;

import com.clinic.shared.kernel.BizException;
import com.clinic.shared.kernel.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 错误出口只有这一个。别在每个接口里 try/catch 然后各写各的 JSON。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Void>> onBiz(BizException ex) {
        log.warn("业务拒绝 code={} msg={}", ex.errorCode().code(), ex.getMessage());
        return ResponseEntity.status(ex.errorCode().http())
                .body(ApiResponse.fail(ex.errorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> onValid(MethodArgumentNotValidException ex) {
        var message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getDefaultMessage() == null ? "参数不对" : err.getDefaultMessage())
                .collect(Collectors.joining("；"));
        return ResponseEntity.badRequest().body(ApiResponse.fail(ErrorCode.BAD_REQUEST, message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> onBody(HttpMessageNotReadableException ex) {
        log.warn("请求体无法解析: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.fail(ErrorCode.BAD_REQUEST, "请求体不是合法 JSON"));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> onLock(OptimisticLockingFailureException ex) {
        log.warn("乐观锁冲突没有被重试吃掉", ex);
        return ResponseEntity.status(ErrorCode.CONFLICT.http())
                .body(ApiResponse.fail(ErrorCode.CONFLICT, "数据刚被别人改过，请重试"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> onOther(Exception ex) {
        log.error("未处理异常", ex);
        return ResponseEntity.status(ErrorCode.INTERNAL.http())
                .body(ApiResponse.fail(ErrorCode.INTERNAL, "系统出错了。把追踪号交给排查的人"));
    }
}
