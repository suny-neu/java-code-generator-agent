# 分层架构最佳实践

## 标准分层架构

```
┌─────────────────────────────────────┐
│         Controller Layer            │  ← REST接口层
├─────────────────────────────────────┤
│         Service Layer               │  ← 业务逻辑层
├─────────────────────────────────────┤
│         Repository Layer            │  ← 数据访问层
├─────────────────────────────────────┤
│         Database                    │  ← 数据存储
└─────────────────────────────────────┘
```

---

## 各层职责

### 1. Controller Layer（控制器层）

**职责**：
- 接收HTTP请求
- 参数校验（@Valid）
- 调用Service层
- 返回响应结果
- **不包含业务逻辑**

**代码示例**：
```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public Result<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        Long userId = userService.createUser(request);
        UserResponse response = userService.getUserById(userId);
        return Result.success(response);
    }

    @GetMapping("/{id}")
    public Result<UserResponse> getUser(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return Result.success(response);
    }
}
```

**规则**：
- ✅ 使用 `@RestController` 注解
- ✅ 使用 `@RequestMapping` 定义统一前缀
- ✅ 使用 `@Valid` 进行参数校验
- ✅ 返回统一响应格式 `Result<T>`
- ❌ 不包含业务逻辑
- ❌ 不直接访问数据库
- ❌ 不使用事务

---

### 2. Service Layer（服务层）

**职责**：
- 实现业务逻辑
- 事务管理（@Transactional）
- 调用Repository层
- 数据转换（Entity ↔ DTO）
- 异常处理

**代码示例**：
```java
public interface UserService {
    Long createUser(UserCreateRequest request);
    UserResponse getUserById(Long id);
    void updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
}
```

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserConverter userConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateRequest request) {
        // 1. 业务校验
        checkEmailUnique(request.getEmail());

        // 2. 构建实体
        User user = userConverter.toEntity(request);
        user.setPassword(PasswordUtil.encrypt(request.getPassword()));

        // 3. 持久化
        userMapper.insert(user);

        // 4. 返回结果
        log.info("用户创建成功, userId={}", user.getId());
        return user.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }
        return userConverter.toResponse(user);
    }

    private void checkEmailUnique(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, "邮箱已存在");
        }
    }
}
```

**规则**：
- ✅ 使用 `@Service` 注解
- ✅ 使用接口+实现类分离
- ✅ 写操作使用 `@Transactional(rollbackFor = Exception.class)`
- ✅ 读操作使用 `@Transactional(readOnly = true)`
- ✅ 使用构造器注入（`@RequiredArgsConstructor` + `final`）
- ❌ 不包含HTTP相关代码
- ❌ 不直接返回Entity给Controller

---

### 3. Repository Layer（数据访问层）

**职责**：
- 数据库CRUD操作
- 复杂查询
- **不包含业务逻辑**

**代码示例**：
```java
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据邮箱查询用户
     */
    default User selectByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return selectOne(wrapper);
    }

    /**
     * 查询活跃用户列表
     */
    default List<User> selectActiveUsers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, UserStatus.ACTIVE);
        return selectList(wrapper);
    }
}
```

**规则**：
- ✅ 继承 `BaseMapper<T>` 获得基础CRUD
- ✅ 使用 `LambdaQueryWrapper` 避免硬编码字段名
- ✅ 复杂查询使用自定义方法
- ❌ 不包含业务逻辑
- ❌ 不抛出业务异常（交给Service层）

---

## 数据流转

### 1. 创建操作流程

```
Request → Controller → Service → Repository → Database
   ↓          ↓          ↓           ↓           ↓
  JSON      DTO       Entity      Entity      Table
```

**代码示例**：
```java
// Controller: 接收JSON请求
@PostMapping
public Result<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
    Long userId = userService.createUser(request);
    return Result.success(userService.getUserById(userId));
}

// Service: DTO → Entity → Repository
public Long createUser(UserCreateRequest request) {
    User user = userConverter.toEntity(request);  // DTO转Entity
    userMapper.insert(user);                       // 持久化
    return user.getId();
}

// Repository: Entity → Database
userMapper.insert(user);  // MyBatis-Plus自动处理
```

### 2. 查询操作流程

```
Request → Controller → Service → Repository → Database
   ↓          ↓          ↓           ↓           ↓
  JSON      DTO       DTO        Entity      Table
```

**代码示例**：
```java
// Controller: 返回JSON响应
@GetMapping("/{id}")
public Result<UserResponse> getUser(@PathVariable Long id) {
    return Result.success(userService.getUserById(id));
}

// Service: Entity → DTO
public UserResponse getUserById(Long id) {
    User user = userMapper.selectById(id);         // 查询Entity
    return userConverter.toResponse(user);          // 转换为DTO
}
```

---

## 各层依赖关系

```
Controller → Service → Repository → Database
    ↓          ↓           ↓
   DTO       DTO        Entity
```

**规则**：
- Controller 依赖 Service
- Service 依赖 Repository
- **下层不知道上层的存在**
- **Entity不传递到Controller**

---

## 常见错误规避

### 错误1: Controller包含业务逻辑
```java
// ❌ 错误：Controller包含业务逻辑
@GetMapping("/users/{id}")
public Result<User> getUser(@PathVariable Long id) {
    User user = userMapper.selectById(id);
    if (user == null) {
        throw new NotFoundException("用户不存在");  // 业务逻辑应该在Service
    }
    return Result.success(user);  // 直接返回Entity
}

// ✅ 正确
@GetMapping("/users/{id}")
public Result<UserResponse> getUser(@PathVariable Long id) {
    return Result.success(userService.getUserById(id));
}
```

### 错误2: Service返回Entity
```java
// ❌ 错误：Service返回Entity给Controller
public User getUserById(Long id) {
    return userMapper.selectById(id);
}

// ✅ 正确：Service返回DTO
public UserResponse getUserById(Long id) {
    User user = userMapper.selectById(id);
    return userConverter.toResponse(user);
}
```

### 错误3: Repository包含业务逻辑
```java
// ❌ 错误：Repository包含业务逻辑
@Mapper
public interface UserMapper extends BaseMapper<User> {
    default User selectByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        User user = selectOne(wrapper);
        if (user == null) {
            throw new NotFoundException("用户不存在");  // 业务异常不应该在Repository
        }
        return user;
    }
}

// ✅ 正确：只负责查询
@Mapper
public interface UserMapper extends BaseMapper<User> {
    default User selectByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return selectOne(wrapper);
    }
}
```

---

## 包结构规范

### 按功能分包（推荐）

```
com.example.user/
├── controller/
│   └── UserController.java
├── service/
│   ├── UserService.java
│   └── impl/
│       └── UserServiceImpl.java
├── repository/
│   └── UserMapper.java
├── entity/
│   └── User.java
├── dto/
│   ├── request/
│   │   ├── UserCreateRequest.java
│   │   └── UserUpdateRequest.java
│   └── response/
│       └── UserResponse.java
├── converter/
│   └── UserConverter.java
├── enums/
│   └── UserStatus.java
└── exception/
    └── UserNotFoundException.java
```

### 按技术分包（不推荐）

```
com.example/
├── controller/
│   ├── UserController.java
│   └── OrderController.java
├── service/
│   ├── UserService.java
│   └── OrderService.java
└── repository/
    ├── UserMapper.java
    └── OrderMapper.java
```

**推荐按功能分包**的原因：
- 高内聚：相关功能代码在一起
- 低耦合：不同模块之间互不干扰
- 易于维护：修改某个功能只需要关注对应的包
