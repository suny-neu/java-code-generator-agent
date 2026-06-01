# 常用异常定义和使用场景

## 异常体系

```
Throwable
├── Error (系统级错误，不应捕获)
└── Exception
    ├── RuntimeException (非受检异常)
    │   ├── BusinessException (业务异常)
    │   ├── NotFoundException (资源不存在)
    │   ├── ValidationException (校验异常)
    │   └── ...
    └── IOException, SQLException等 (受检异常)
```

---

## 基础异常类

### 1. 业务异常基类

```java
@Getter
@AllArgsConstructor
public class BusinessException extends RuntimeException {

    private final int code;
    private final String message;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }

    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode.getCode(), message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
```

### 2. 资源不存在异常

```java
public class NotFoundException extends BusinessException {

    public NotFoundException(String resource) {
        super(ErrorCode.NOT_FOUND, resource + "不存在");
    }

    public NotFoundException(String resource, Long id) {
        super(ErrorCode.NOT_FOUND, resource + "[id=" + id + "]不存在");
    }
}
```

### 3. 权限异常

```java
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }
}
```

### 4. 校验异常

```java
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }

    public ValidationException(String field, String message) {
        super(ErrorCode.VALIDATION_ERROR, field + ": " + message);
    }
}
```

### 5. 状态异常

```java
public class InvalidStatusException extends BusinessException {

    public InvalidStatusException(String currentStatus, String expectedStatus) {
        super(ErrorCode.INVALID_STATUS,
              "当前状态[" + currentStatus + "]不允许操作，期望状态[" + expectedStatus + "]");
    }
}
```

---

## 错误码定义

### 1. 错误码接口

```java
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

    // 用户相关错误 6xxx
    USER_NOT_FOUND(6001, "用户不存在"),
    EMAIL_ALREADY_EXISTS(6002, "邮箱已存在"),
    PHONE_ALREADY_EXISTS(6003, "手机号已存在"),
    USERNAME_ALREADY_EXISTS(6004, "用户名已存在"),
    PASSWORD_ERROR(6005, "密码错误"),
    USER_DISABLED(6006, "用户已禁用"),
    USER_LOCKED(6007, "用户已锁定"),

    // 订单相关错误 7xxx
    ORDER_NOT_FOUND(7001, "订单不存在"),
    ORDER_STATUS_ERROR(7002, "订单状态错误"),
    ORDER_PAID(7003, "订单已支付"),
    ORDER_CANCELLED(7004, "订单已取消"),
    INSUFFICIENT_STOCK(7005, "库存不足"),

    // 支付相关错误 8xxx
    PAYMENT_FAILED(8001, "支付失败"),
    PAYMENT_TIMEOUT(8002, "支付超时"),
    PAYMENT_AMOUNT_ERROR(8003, "支付金额错误"),

    // 服务端错误 5xxx
    INTERNAL_ERROR(500, "系统内部错误"),
    DATABASE_ERROR(5001, "数据库错误"),
    EXTERNAL_API_ERROR(5002, "外部接口错误"),
    FILE_UPLOAD_ERROR(5003, "文件上传错误");

    private final int code;
    private final String message;
}
```

---

## 使用场景

### 1. Service层抛出异常

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public UserResponse getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new NotFoundException("用户", id);
        }
        return userConverter.toResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateRequest request) {
        // 检查邮箱唯一性
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, request.getEmail());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 创建用户
        User user = userConverter.toEntity(request);
        userMapper.insert(user);

        return user.getId();
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new NotFoundException("用户", id);
        }

        // 状态校验
        if (user.getStatus().equals(status)) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "用户状态未改变");
        }

        // 更新状态
        user.setStatus(status);
        userMapper.updateById(user);
    }
}
```

### 2. Controller层处理异常

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public Result<UserResponse> getUser(@PathVariable Long id) {
        // ServiceException会被全局异常处理器捕获
        return Result.success(userService.getUserById(id));
    }
}
```

---

## 全局异常处理

### 1. 全局异常处理器

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    // 资源不存在异常
    @ExceptionHandler(NotFoundException.class)
    public Result<Void> handleNotFoundException(NotFoundException e) {
        log.warn("资源不存在: {}", e.getMessage());
        return Result.error(ErrorCode.NOT_FOUND.getCode(), e.getMessage());
    }

    // 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());
        log.warn("参数校验失败: {}", errors);
        return Result.error(ErrorCode.VALIDATION_ERROR.getCode(), String.join("; ", errors));
    }

    // HTTP请求方法不支持
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMethod());
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), "请求方法不支持");
    }

    // 其他未捕获异常
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(ErrorCode.INTERNAL_ERROR.getCode(), "系统异常，请稍后重试");
    }
}
```

### 2. 异常处理顺序

```java
// 异常处理器按顺序匹配，更具体的异常放在前面

@ExceptionHandler(NotFoundException.class)          // 最具体
public Result<Void> handleNotFound(NotFoundException e) { }

@ExceptionHandler(BusinessException.class)         // 较具体
public Result<Void> handleBusiness(BusinessException e) { }

@ExceptionHandler(RuntimeException.class)          // 较泛
public Result<Void> handleRuntime(RuntimeException e) { }

@ExceptionHandler(Exception.class)                // 最泛
public Result<Void> handleAll(Exception e) { }
```

---

## 异常使用规范

### 1. 不要生吞异常

```java
// ❌ 错误：生吞异常
try {
    // code
} catch (Exception e) {
    // 什么都不做
}

// ✅ 正确：记录日志或抛出异常
try {
    // code
} catch (Exception e) {
    log.error("操作失败", e);
    throw new BusinessException("操作失败");
}
```

### 2. 不要捕获太泛的异常

```java
// ❌ 错误：捕获Exception
try {
    // code
} catch (Exception e) {
    // 处理
}

// ✅ 正确：捕获具体异常
try {
    // code
} catch (SQLException e) {
    // 处理数据库异常
} catch (IOException e) {
    // 处理IO异常
}
```

### 3. 使用业务异常

```java
// ❌ 错误：返回null表示错误
public User getUserById(Long id) {
    User user = userMapper.selectById(id);
    return user;  // null表示用户不存在
}

// ✅ 正确：抛出异常
public User getUserById(Long id) {
    User user = userMapper.selectById(id);
    if (user == null) {
        throw new NotFoundException("用户", id);
    }
    return user;
}
```

### 4. 异常链保持

```java
// ✅ 正确：保持异常链
try {
    // code
} catch (SQLException e) {
    log.error("数据库操作失败", e);
    throw new BusinessException(ErrorCode.DATABASE_ERROR, "数据保存失败");
}
```

---

## 异常处理检查清单

- [ ] 业务异常使用BusinessException
- [ ] 资源不存在使用NotFoundException
- [ ] 参数校验失败使用ValidationException
- [ ] 无空catch块
- [ ] 异常包含足够的上下文信息
- [ ] 日志级别正确（ERROR/WARN/INFO）
- [ ] 全局异常处理器正确处理各类异常
- [ ] 不向客户端暴露敏感信息
