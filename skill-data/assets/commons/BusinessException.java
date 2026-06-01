package ${basePackage}.exception;

import ${basePackage}.enums.ErrorCode;
import lombok.Getter;

/**
 * 业务异常类
 *
 * @author Code Generator
 * @since ${generateDate}
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
