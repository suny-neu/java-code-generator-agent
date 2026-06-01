# Swagger/OpenAPI注解使用指南

## 基础配置

### 1. Maven依赖

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### 2. 配置类

```java
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("用户管理系统API")
                .description("用户管理系统接口文档")
                .version("1.0.0")
                .contact(new Contact()
                    .name("开发团队")
                    .email("dev@example.com")))
            .externalDocs(new ExternalDocumentation()
                .description("项目文档")
                .url("https://docs.example.com"));
    }
}
```

### 3. application.yml配置

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
  packages-to-scan: com.example.user.controller
  default-consumes-media-type: application/json
  default-produces-media-type: application/json
```

---

## Controller注解

### 1. 类级别注解

```java
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户管理", description = "用户相关接口")
@RequiredArgsConstructor
public class UserController {
    // ...
}
```

### 2. 方法级别注解

```java
@Operation(summary = "创建用户", description = "创建新用户，邮箱必须唯一")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "创建成功",
        content = @Content(schema = @Schema(implementation = UserResponse.class))),
    @ApiResponse(responseCode = "400", description = "参数错误",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "409", description = "邮箱已存在",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
@PostMapping
public Result<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
    // ...
}

@Operation(summary = "查询用户", description = "根据ID查询用户信息")
@GetMapping("/{id}")
public Result<UserResponse> getUser(
    @Parameter(description = "用户ID", required = true, example = "1")
    @PathVariable Long id) {
    // ...
}

@Operation(summary = "查询用户列表", description = "分页查询用户列表")
@GetMapping
public Result<PageResult<UserResponse>> listUsers(
    @Parameter(description = "页码", example = "1")
    @RequestParam(defaultValue = "1") Integer current,
    @Parameter(description = "每页条数", example = "10")
    @RequestParam(defaultValue = "10") Integer size) {
    // ...
}
```

---

## Request DTO注解

### 1. 类级别注解

```java
@Schema(description = "用户创建请求")
public class UserCreateRequest {
    // ...
}
```

### 2. 字段级别注解

```java
@Schema(description = "用户创建请求")
public class UserCreateRequest {

    @Schema(description = "用户名", example = "zhangsan", required = true)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度2-20字符")
    private String username;

    @Schema(description = "邮箱", example = "zhangsan@example.com", required = true)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "密码", example = "Password123", required = true)
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度8-20字符")
    private String password;
}
```

### 3. 嵌套对象

```java
@Schema(description = "订单创建请求")
public class OrderCreateRequest {

    @Schema(description = "用户ID", example = "1", required = true)
    private Long userId;

    @Schema(description = "收货地址", required = true)
    private AddressRequest address;

    @Schema(description = "订单明细", required = true)
    @NotEmpty(message = "订单明细不能为空")
    private List<OrderItemRequest> items;
}

@Schema(description = "收货地址")
public class AddressRequest {
    @Schema(description = "收货人", example = "张三", required = true)
    private String receiver;

    @Schema(description = "手机号", example = "13800138000", required = true)
    private String phone;

    @Schema(description = "详细地址", example = "北京市朝阳区xxx", required = true)
    private String detail;
}
```

---

## Response DTO注解

### 1. 类级别注解

```java
@Schema(description = "用户响应")
@Data
public class UserResponse {
    // ...
}
```

### 2. 字段级别注解

```java
@Schema(description = "用户响应")
public class UserResponse {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "用户状态", example = "1", allowableValues = {"0", "1", "2"})
    private Integer status;

    @Schema(description = "创建时间", example = "2024-01-01T12:00:00")
    private LocalDateTime createTime;
}
```

---

## 统一响应注解

### 1. Result类

```java
@Schema(description = "统一响应结果")
@Getter
@AllArgsConstructor
public class Result<T> {

    @Schema(description = "响应码", example = "200")
    private int code;

    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
```

### 2. PageResult类

```java
@Schema(description = "分页响应结果")
@Data
public class PageResult<T> {

    @Schema(description = "当前页", example = "1")
    private Long current;

    @Schema(description = "每页条数", example = "10")
    private Long size;

    @Schema(description = "总记录数", example = "100")
    private Long total;

    @Schema(description = "数据列表")
    private List<T> records;
}
```

### 3. ErrorResponse类

```java
@Schema(description = "错误响应")
@Data
public class ErrorResponse {

    @Schema(description = "错误码", example = "400")
    private int code;

    @Schema(description = "错误消息", example = "参数校验失败")
    private String message;

    @Schema(description = "错误详情")
    private List<FieldError> errors;

    @Data
    @AllArgsConstructor
    public static class FieldError {
        @Schema(description = "字段名", example = "email")
        private String field;

        @Schema(description = "错误消息", example = "邮箱格式不正确")
        private String message;
    }
}
```

---

## 枚举注解

```java
@Getter
@AllArgsConstructor
@Schema(description = "用户状态")
public enum UserStatus {

    @Schema(description = "正常")
    ACTIVE(1, "正常"),

    @Schema(description = "禁用")
    INACTIVE(0, "禁用"),

    @Schema(description = "锁定")
    LOCKED(2, "锁定");

    @Schema(description = "状态码")
    private final int code;

    @Schema(description = "状态描述")
    private final String desc;
}
```

---

## 完整示例

### Controller

```java
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户管理", description = "用户相关接口")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户，邮箱必须唯一")
    @ApiResponse(responseCode = "200", description = "创建成功")
    @ApiResponse(responseCode = "400", description = "参数错误")
    @ApiResponse(responseCode = "409", description = "邮箱已存在")
    public Result<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        Long userId = userService.createUser(request);
        return Result.success(userService.getUserById(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询用户", description = "根据ID查询用户信息")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @ApiResponse(responseCode = "404", description = "用户不存在")
    public Result<UserResponse> getUser(
        @Parameter(description = "用户ID", required = true, example = "1")
        @PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @GetMapping
    @Operation(summary = "查询用户列表", description = "分页查询用户列表")
    public Result<PageResult<UserResponse>> listUsers(
        @Parameter(description = "页码", example = "1")
        @RequestParam(defaultValue = "1") Integer current,
        @Parameter(description = "每页条数", example = "10")
        @RequestParam(defaultValue = "10") Integer size,
        @Parameter(description = "关键词（用户名/邮箱）")
        @RequestParam(required = false) String keyword) {
        return Result.success(userService.listUsers(current, size, keyword));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "更新用户信息")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @ApiResponse(responseCode = "404", description = "用户不存在")
    public Result<UserResponse> updateUser(
        @Parameter(description = "用户ID", required = true)
        @PathVariable Long id,
        @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, request);
        return Result.success(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "删除用户")
    @ApiResponse(responseCode = "200", description = "删除成功")
    @ApiResponse(responseCode = "404", description = "用户不存在")
    public Result<Void> deleteUser(
        @Parameter(description = "用户ID", required = true)
        @PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
}
```

### Request DTO

```java
@Schema(description = "用户创建请求")
@Data
public class UserCreateRequest {

    @Schema(description = "用户名", example = "zhangsan", required = true, minLength = 2, maxLength = 20)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度2-20字符")
    private String username;

    @Schema(description = "邮箱", example = "zhangsan@example.com", required = true)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "密码", example = "Password123", required = true, minLength = 8, maxLength = 20)
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度8-20字符")
    private String password;
}
```

### Response DTO

```java
@Schema(description = "用户响应")
@Data
public class UserResponse {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "用户状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间", example = "2024-01-01T12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2024-01-01T12:00:00")
    private LocalDateTime updateTime;
}
```

---

## Swagger UI访问

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/v3/api-docs`
- Swagger UI Route: `http://localhost:8080/swagger-ui/index.html`

---

## 注解总结

| 注解 | 位置 | 描述 |
|-----|------|-----|
| @Tag | Controller类 | 接口分组 |
| @Operation | 方法 | 接口描述 |
| @ApiResponse | 方法 | 响应描述 |
| @Parameter | 参数 | 参数描述 |
| @Schema | 类/字段 | 模型描述 |
| @ArraySchema | 集合字段 | 数组描述 |

---

## 最佳实践

1. **所有Controller添加@Tag注解**
2. **所有方法添加@Operation注解**
3. **所有DTO添加@Schema注解**
4. **所有必填字段标记required=true**
5. **提供example示例值**
6. **枚举类型添加allowableValues**
7. **复杂对象使用@Schema作为内部类**
