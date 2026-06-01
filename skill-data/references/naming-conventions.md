# 命名规范

## 1. 类命名

### 1.1 基本规则
- **驼峰式**，首字母大写
- **名词**，不是动词
- **见名知意**

### 1.2 各层类命名规范

| 层级 | 后缀 | 示例 | 说明 |
|-----|------|-----|------|
| Controller | 无 / Controller | `UserController` | REST接口控制器 |
| Service | 无 / Service | `UserService` | 业务逻辑接口 |
| Service Impl | Impl | `UserServiceImpl` | 业务逻辑实现类 |
| Repository | Mapper / Repository | `UserMapper` | 数据访问接口 |
| Entity | 无 | `User` | 数据库实体 |
| DTO (Request) | Request / Cmd | `UserCreateRequest` | 请求DTO |
| DTO (Response) | Response / DTO | `UserResponse` | 响应DTO |
| VO | VO | `UserVO` | 视图对象 |
| Converter | Converter | `UserConverter` | 对象转换器 |
| Exception | Exception | `UserNotFoundException` | 异常类 |
| Enum | 无 | `UserStatus` | 枚举类 |
| Config | Config / Configuration | `SwaggerConfig` | 配置类 |
| Util | Util | `DateUtil` | 工具类 |

### 1.3 常用类名前缀/后缀

| 前缀/后缀 | 含义 | 示例 |
|---------|------|------|
| I... | 接口 | `IUserService` |
| Base... | 基类 | `BaseController` |
| Abstract... | 抽象类 | `AbstractService` |
| Default... | 默认实现 | `DefaultUserService` |
| Simple... | 简单实现 | `SimpleUserService` |

---

## 2. 方法命名

### 2.1 CRUD操作

| 操作 | 前缀 | 示例 | 返回类型 |
|-----|------|-----|---------|
| 新增 | save / insert / add / create | `saveUser`, `createUser` | Long / void |
| 删除 | remove / delete | `removeUser`, `deleteUser` | void / boolean |
| 修改 | update / modify | `updateUser`, `modifyUser` | void / boolean |
| 查询单个 | get / find / load | `getUserById`, `findUser` | T / Optional<T> |
| 查询列表 | list / query | `listUsers`, `queryUsers` | List<T> |
| 查询分页 | page / paginate | `pageUsers` | Page<T> |
| 查询数量 | count | `countUsers` | int / long |

### 2.2 布尔判断方法

| 前缀 | 含义 | 示例 | 返回类型 |
|-----|------|-----|---------|
| is | 是... | `isValid`, `isActive` | boolean |
| has | 有... | `hasPermission`, `hasRole` | boolean |
| can | 能... | `canAccess`, `canDelete` | boolean |
| should | 应该... | `shouldUpdate` | boolean |
| contains | 包含... | `containsUser` | boolean |

### 2.3 数据转换方法

| 前缀 | 含义 | 示例 |
|-----|------|-----|
| to | 转换为... | `toEntity`, `toDTO`, `toString` |
| from | 从...转换 | `fromEntity`, `fromDTO` |
| convert | 转换 | `convertToDTO`, `convertToEntity` |
| as | 作为... | `asList`, `asMap` |

### 2.4 数据处理方法

| 前缀 | 含义 | 示例 |
|-----|------|-----|
| calculate | 计算 | `calculatePrice`, `calculateTotal` |
| compute | 计算 | `computeScore` |
| process | 处理 | `processOrder`, `processPayment` |
| handle | 处理 | `handleError`, `handleEvent` |
| execute | 执行 | `executeQuery`, `executeCommand` |
| perform | 执行 | `performAction` |
| validate | 校验 | `validateEmail`, `validatePassword` |
| check | 检查 | `checkPermission`, `checkDuplicate` |

### 2.5 方法命名示例

```java
// ✅ 正确的方法命名
public interface UserService {
    // CRUD
    Long createUser(UserCreateRequest request);
    UserResponse getUserById(Long id);
    void updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
    List<UserResponse> listUsers();
    Page<UserResponse> pageUsers(PageRequest pageRequest);
    long countUsers();

    // 布尔判断
    boolean isEmailUnique(String email);
    boolean hasPermission(Long userId, String resource);
    boolean canDelete(Long userId);

    // 数据转换
    User toEntity(UserCreateRequest request);
    UserResponse toResponse(User user);

    // 业务处理
    void processUserRegistration(UserCreateRequest request);
    void handleUserLogin(LoginRequest request);
    boolean validatePassword(String rawPassword, String encodedPassword);
}
```

---

## 3. 变量命名

### 3.1 基本规则
- **驼峰式**，首字母小写
- **见名知意**
- **避免单字符**（循环变量除外）

### 3.2 常用变量命名

| 类型 | 命名 | 示例 |
|-----|------|-----|
| String | 描述性名词 | `username`, `email`, `password` |
| int/long | 描述性名词 | `userId`, `count`, `totalAmount` |
| boolean | is/has/can开头 | `isActive`, `hasPermission` |
| List | 复数形式 | `users`, `orders`, `items` |
| Set | 复数形式 | `permissions`, `roles` |
| Map | keyToValue映射 | `idToUser`, `nameToCount` |
| Date/LocalDateTime | 描述性名词 + Time/Date | `createTime`, `updateTime`, `loginTime` |

### 3.3 集合变量命名

```java
// ✅ 正确：使用复数
List<User> users = new ArrayList<>();
Set<String> permissions = new HashSet<>();
Map<Long, User> idToUser = new HashMap<>();

// ❌ 错误：使用单数
List<User> userList = new ArrayList<>();  // 不推荐
Set<String> permissionSet = new HashSet(); // 不推荐
```

### 3.4 循环变量命名

```java
// ✅ 推荐：使用有意义的名称
for (User user : users) {
    // 处理用户
}

// ✅ 可接受：简单场景使用单字符
for (int i = 0; i < size; i++) {
    // 索引循环
}

// ✅ 推荐：嵌套循环使用描述性名称
for (Order order : orders) {
    for (OrderItem item : order.getItems()) {
        // 处理订单项
    }
}
```

---

## 4. 常量命名

### 4.1 基本规则
- **全大写**
- **下划线分隔**
- **使用接口或类集中管理**

### 4.2 常量命名示例

```java
public interface UserConstants {
    // 状态常量
    int STATUS_ACTIVE = 1;
    int STATUS_INACTIVE = 0;
    int STATUS_LOCKED = 2;

    // 限制常量
    int MAX_LOGIN_RETRY = 5;
    int PASSWORD_MIN_LENGTH = 8;

    // 默认值
    String DEFAULT_AVATAR = "https://example.com/default.png";
    String DEFAULT_ROLE = "USER";
}

public interface ErrorCode {
    // 成功
    int SUCCESS = 0;

    // 客户端错误 4xxx
    int BAD_REQUEST = 400;
    int UNAUTHORIZED = 401;
    int FORBIDDEN = 403;
    int NOT_FOUND = 404;

    // 服务端错误 5xxx
    int INTERNAL_ERROR = 500;
    int DATABASE_ERROR = 5001;

    // 业务错误 6xxx
    int USER_NOT_FOUND = 6001;
    int EMAIL_ALREADY_EXISTS = 6002;
}
```

### 4.3 枚举常量

```java
@Getter
@AllArgsConstructor
public enum UserStatus {
    ACTIVE(1, "正常"),
    INACTIVE(0, "禁用"),
    LOCKED(2, "锁定");

    private final int code;
    private final String desc;
}
```

---

## 5. 包命名

### 5.1 基本规则
- **全小写**
- **点分隔**
- **倒置域名 + 项目名 + 模块名**

### 5.2 包结构规范

```
com.example.{project}.{module}/
├── controller/        # 控制器层
├── service/           # 服务层
│   └── impl/         # 服务实现
├── repository/        # 数据访问层
├── entity/           # 实体类
├── dto/              # 数据传输对象
│   ├── request/      # 请求DTO
│   └── response/     # 响应DTO
├── vo/               # 视图对象
├── converter/        # 对象转换器
├── enums/            # 枚举类
├── exception/        # 异常类
├── config/           # 配置类
├── constants/        # 常量类
└── util/             # 工具类
```

### 5.3 包命名示例

```
com.example.usermanagement/
├── controller/
│   └── UserController.java
├── service/
│   ├── UserService.java
│   └── impl/
│       └── UserServiceImpl.java
└── repository/
    └── UserMapper.java
```

---

## 6. 数据库命名

### 6.1 表命名
- **小写**
- **下划线分隔**
- **t_前缀**（可选）

| 表名 | 说明 |
|-----|------|
| t_user | 用户表 |
| t_user_role | 用户角色关联表 |
| t_order | 订单表 |
| t_order_item | 订单明细表 |

### 6.2 字段命名
- **小写**
- **下划线分隔**

| 字段名 | 说明 |
|-------|------|
| id | 主键ID |
| user_id | 用户ID |
| user_name | 用户名 |
| create_time | 创建时间 |
| update_time | 更新时间 |

---

## 7. API命名

### 7.1 RESTful API规范

| 操作 | HTTP方法 | URL路径 | 说明 |
|-----|---------|---------|------|
| 创建 | POST | /api/v1/users | 创建用户 |
| 查询单个 | GET | /api/v1/users/{id} | 查询用户 |
| 查询列表 | GET | /api/v1/users | 查询用户列表 |
| 更新 | PUT | /api/v1/users/{id} | 更新用户 |
| 删除 | DELETE | /api/v1/users/{id} | 删除用户 |
| 部分更新 | PATCH | /api/v1/users/{id} | 部分更新用户 |

### 7.2 URL命名规范

```
/api/v{version}/{resource}/{id}/{sub-resource}/{sub-id}

示例:
- /api/v1/users
- /api/v1/users/123
- /api/v1/users/123/orders
- /api/v1/users/123/orders/456
```

---

## 8. 命名检查清单

- [ ] 类名使用驼峰式，首字母大写
- [ ] 方法名使用驼峰式，首字母小写
- [ ] 变量名使用驼峰式，首字母小写
- [ ] 常量全大写，下划线分隔
- [ ] 包名全小写，点分隔
- [ ] 布尔变量使用is/has/can开头
- [ ] 集合变量使用复数形式
- [ ] 见名知意，避免缩写
- [ ] 避免使用拼音
