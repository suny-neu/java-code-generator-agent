package ${basePackage}.enums;

import lombok.Getter;

/**
 * 错误码枚举
 *
 * @author Code Generator
 * @since ${generateDate}
 */
@Getter
public enum ErrorCode {

    // 通用错误码 1xxx
    SUCCESS(200, "操作成功"),
    SYSTEM_ERROR(1000, "系统异常"),
    INVALID_PARAM(1001, "参数校验失败"),
    NO_PERMISSION(1002, "无权限操作"),
    DATA_NOT_FOUND(1003, "数据不存在"),
    DATA_ALREADY_EXISTS(1004, "数据已存在"),

    // 用户相关错误码 2xxx
    USER_NOT_FOUND(2001, "用户不存在"),
    USERNAME_ALREADY_EXISTS(2002, "用户名已存在"),
    USER_DISABLED(2003, "用户已被禁用"),

    // 业务模块错误码 3xxx-9xxx（根据实际业务扩展）
    // TODO: 添加业务相关错误码

    ;

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
