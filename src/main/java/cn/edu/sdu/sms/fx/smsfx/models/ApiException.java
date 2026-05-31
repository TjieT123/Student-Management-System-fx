package cn.edu.sdu.sms.fx.smsfx.models;

/**
 * API 异常类
 */
public class ApiException extends RuntimeException {
    private final int code;

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
