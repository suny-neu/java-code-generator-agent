package {basePackage}.exception;

/**
 * 错误码枚举
 *
 * @author {author}
 * @since {date}
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 成功
    SUCCESS(0, "操作成功"),

    // 客户端错误 4xxx
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    VALIDATION_ERROR(4001, "参数校验失败"),
    INVALID_STATUS(4002, "状态不正确"),

    // 服务端错误 5xxx
    INTERNAL_ERROR(500, "系统内部错误"),
    DATABASE_ERROR(5001, "数据库错误"),
    EXTERNAL_API_ERROR(5002, "外部接口错误");

    private final int code;
    private final String message;
}
