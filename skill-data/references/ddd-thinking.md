# DDD领域驱动设计思维

## 核心概念

### 1. 领域(Domain)
- 问题空间
- 业务范围
- 业务规则

### 2. 领域模型(Domain Model)
- 对业务的抽象
- 包含业务规则和行为
- 技术无关

### 3. 界限上下文(Bounded Context)
- 系统的边界
- 特定领域的模型
- 通用语言(Ubiquitous Language)

---

## 战略设计

### 1. 领域划分

```
用户域(User Domain)
├── 用户注册(User Registration)
├── 用户认证(User Authentication)
└── 用户管理(User Management)

订单域(Order Domain)
├── 下单(Order Creation)
├── 支付(Payment)
└── 发货(Delivery)

商品域(Product Domain)
├── 商品管理(Product Management)
├── 库存管理(Inventory)
└── 分类管理(Category)
```

### 2. 上下文映射

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│  User       │────────>│  Order      │────────>│  Payment    │
│  Context    │         │  Context    │         │  Context    │
└─────────────┘         └─────────────┘         └─────────────┘
        ↑                        ↑
        │                        │
        └────────────────────────┘
                    │
            ┌─────────────┐
            │  Product    │
            │  Context    │
            └─────────────┘
```

---

## 战术设计

### 1. 分层架构

```
┌─────────────────────────────────────┐
│   User Interface Layer (用户界面层) │
│   - Controller                     │
│   - DTO                            │
├─────────────────────────────────────┤
│   Application Layer (应用层)        │
│   - ApplicationService             │
│   - Command/Query                  │
├─────────────────────────────────────┤
│   Domain Layer (领域层)             │
│   - Aggregate (聚合根)              │
│   - Entity (实体)                  │
│   - Value Object (值对象)           │
│   - Domain Service (领域服务)       │
│   - Repository Interface           │
├─────────────────────────────────────┤
│   Infrastructure Layer (基础设施层) │
│   - Repository Implementation      │
│   - External Service               │
└─────────────────────────────────────┘
```

### 2. 核心元素

#### 聚合根(Aggregate Root)

```java
@Entity
@Getter
@Setter
public class Order {
    private Long id;
    private String orderNo;
    private List<OrderItem> items;
    private OrderStatus status;
    private BigDecimal totalAmount;

    // 业务行为
    public void addItem(Product product, int quantity) {
        OrderItem item = new OrderItem(product, quantity);
        this.items.add(item);
        this.calculateTotal();
    }

    public void pay(BigDecimal amount) {
        if (this.status != OrderStatus.PENDING) {
            throw new BusinessException("订单状态不允许支付");
        }
        this.status = OrderStatus.PAID;
    }

    private void calculateTotal() {
        this.totalAmount = items.stream()
            .map(OrderItem::getSubTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

#### 实体(Entity)

```java
@Entity
@Getter
public class User {
    private Long id;
    private String username;
    private String email;
    private UserStatus status;

    // 标识相等
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

#### 值对象(Value Object)

```java
@Getter
@AllArgsConstructor
public class Email {
    private final String value;

    public Email(String value) {
        if (!isValid(value)) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        this.value = value;
    }

    private boolean isValid(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    // 值对象相等
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Email email = (Email) obj;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
```

#### 领域服务(Domain Service)

```java
@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long register(UserRegistrationCommand command) {
        // 1. 检查邮箱唯一性
        if (userRepository.existsByEmail(command.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        // 2. 创建用户
        User user = new User();
        user.setUsername(command.getUsername());
        user.setEmail(command.getEmail());
        user.setPassword(passwordEncoder.encode(command.getPassword()));

        // 3. 保存用户
        userRepository.save(user);

        return user.getId();
    }
}
```

---

## 仓储模式(Repository Pattern)

### 1. 仓储接口(领域层)

```java
public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    void delete(User user);
}
```

### 2. 仓储实现(基础设施层)

```java
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;
    private final UserConverter userConverter;

    @Override
    public User save(User user) {
        userMapper.insert(user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        UserPO po = userMapper.selectById(id);
        return Optional.ofNullable(userConverter.toDomain(po));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getEmail, email);
        UserPO po = userMapper.selectOne(wrapper);
        return Optional.ofNullable(userConverter.toDomain(po));
    }
}
```

---

## 应用服务(Application Service)

### 1. 命令(Command)

```java
@Data
public class CreateUserCommand {
    private String username;
    private String email;
    private String password;
}
```

### 2. 查询(Query)

```java
@Data
public class UserQuery {
    private String keyword;
    private UserStatus status;
    private Integer page;
    private Integer size;
}
```

### 3. 应用服务

```java
@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final UserRepository userRepository;
    private final UserRegistrationService registrationService;

    // 命令处理
    public Long handle(CreateUserCommand command) {
        return registrationService.register(command);
    }

    // 查询处理
    public Page<UserDTO> handle(UserQuery query) {
        Page<User> users = userRepository.findByCondition(query);
        return users.map(this::toDTO);
    }

    private UserDTO toDTO(User user) {
        // 转换逻辑
    }
}
```

---

## 领域事件(Domain Event)

### 1. 事件定义

```java
@Getter
@AllArgsConstructor
public class UserCreatedEvent {
    private final Long userId;
    private final String username;
    private final String email;
    private final LocalDateTime occurredOn;
}
```

### 2. 事件发布

```java
@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final ApplicationEventPublisher eventPublisher;

    public Long register(UserRegistrationCommand command) {
        User user = new User(command);
        userRepository.save(user);

        // 发布领域事件
        UserCreatedEvent event = new UserCreatedEvent(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            LocalDateTime.now()
        );
        eventPublisher.publishEvent(event);

        return user.getId();
    }
}
```

### 3. 事件处理

```java
@Component
@Slf4j
public class UserEventListener {

    @EventListener
    public void handleUserCreated(UserCreatedEvent event) {
        log.info("用户创建: userId={}, username={}", event.getUserId(), event.getUsername());
        // 发送欢迎邮件
        // 发送短信通知
        // 同步到其他系统
    }
}
```

---

## DDD代码组织

### 按层次分包

```
com.example.user/
├── application/          # 应用层
│   ├── service/         # 应用服务
│   ├── command/         # 命令
│   └── query/           # 查询
├── domain/              # 领域层
│   ├── model/           # 领域模型
│   │   ├── aggregate/   # 聚合根
│   │   ├── entity/      # 实体
│   │   └── valueobject/ # 值对象
│   ├── service/         # 领域服务
│   ├── repository/      # 仓储接口
│   └── event/           # 领域事件
├── infrastructure/      # 基础设施层
│   ├── persistence/     # 持久化
│   │   ├── mapper/      # MyBatis Mapper
│   │   ├── po/          # 持久化对象
│   │   └── repository/  # 仓储实现
│   └── external/        # 外部服务
└── interfaces/          # 接口层
    ├── controller/      # 控制器
    └── dto/             # 数据传输对象
```

### 按功能分包（推荐）

```
com.example.user/
├── controller/
│   └── UserController.java
├── application/
│   ├── UserApplicationService.java
│   ├── CreateUserCommand.java
│   └── UserQuery.java
├── domain/
│   ├── User.java (聚合根)
│   ├── Email.java (值对象)
│   ├── UserRepository.java (仓储接口)
│   ├── UserRegistrationService.java (领域服务)
│   └── UserCreatedEvent.java (领域事件)
└── infrastructure/
    ├── UserMapper.java
    ├── UserRepositoryImpl.java
    └── UserPO.java
```

---

## DDD实践检查清单

- [ ] 领域模型是否包含业务规则
- [ ] 聚合根是否保护内部不变性
- [ ] 值对象是否不可变
- [ ] 仓储接口是否在领域层
- [ ] 领域服务是否处理无状态业务逻辑
- [ ] 应用服务是否协调领域对象
- [ ] 领域事件是否解耦业务流程
- [ ] 通用语言是否在代码中体现
