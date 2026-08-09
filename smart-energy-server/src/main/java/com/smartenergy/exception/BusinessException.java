package com.smartenergy.exception;

/**
 * 业务异常
 * <p>
 * 用于标识可预期的业务错误（如资源不存在、参数不合法等），
 * 由 GlobalExceptionHandler 统一处理并返回合适的 HTTP 状态码和 Result。
 *
 * @author smart-energy
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(400, message);
    }

    public int getCode() {
        return code;
    }

    /**
     * 资源不存在（404）
     */
    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }

    /**
     * 参数校验失败（400）
     */
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }
}
