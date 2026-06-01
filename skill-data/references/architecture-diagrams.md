# 结构图绘制指南

## Mermaid图表语法

### 1. 类图(Class Diagram)

#### 基本语法

```mermaid
classDiagram
    class User {
        -Long id
        -String name
        -String email
        +getName() String
        +setName(name) void
    }
```

#### 关系语法

| 关系 | 语法 | 说明 |
|-----|------|-----|
| 继承 | `Child <|-- Parent` | 泛化关系 |
| 实现 | `Class ..|> Interface` | 实现接口 |
| 组合 | `Container *-- Component` | 强拥有 |
| 聚合 | `Aggregate o-- Component` | 弱拥有 |
| 关联 | `Class1 --> Class2` | 关联关系 |
| 依赖 | `Class1 ..> Class2` | 依赖关系 |

#### 完整示例

```mermaid
classDiagram
    class UserController {
        +createUser(Request) Result
        +getUserById(Long) Result
    }

    class UserService {
        <<interface>>
        +createUser(Request) Long
        +getUserById(Long) User
    }

    class UserServiceImpl {
        -userMapper: UserMapper
        +createUser(Request) Long
        +getUserById(Long) User
    }

    class UserMapper {
        <<interface>>
        +selectById(Long) User
        +insert(User) int
    }

    class User {
        -Long id
        -String name
        -String email
        +getId() Long
    }

    UserController --> UserService : uses
    UserService ..|> UserServiceImpl : implements
    UserServiceImpl --> UserMapper : uses
    UserMapper ..|> BaseMapper : extends
    UserServiceImpl --> User : creates
```

---

### 2. 时序图(Sequence Diagram)

#### 基本语法

```mermaid
sequenceDiagram
    actor User
    participant Controller
    participant Service
    participant Database

    User->>Controller: request
    Controller->>Service: process()
    Service->>Database: query()
    Database-->>Service: result
    Service-->>Controller: response
    Controller-->>User: response
```

#### 完整示例

```mermaid
sequenceDiagram
    actor Client
    participant Controller as UserController
    participant Service as UserService
    participant Mapper as UserMapper
    participant DB as Database

    Client->>Controller: POST /api/v1/users
    Note over Controller: @Valid 校验参数
    Controller->>Service: createUser(request)
    Service->>Service: checkEmailUnique()
    Service->>Mapper: selectByEmail(email)
    Mapper->>DB: SELECT * FROM t_user WHERE email=?
    DB-->>Mapper: null
    Mapper-->>Service: null
    Note over Service: 邮箱可用，继续创建
    Service->>Service: encryptPassword()
    Service->>Mapper: insert(user)
    Mapper->>DB: INSERT INTO t_user
    DB-->>Mapper: 1 row affected
    Mapper-->>Service: userId = 1
    Service-->>Controller: userId
    Controller->>Service: getUserById(userId)
    Service-->>Controller: userResponse
    Controller-->>Client: 200 OK, {"code":200,"data":{...}}
```

#### 异常流程

```mermaid
sequenceDiagram
    actor Client
    participant Controller
    participant Service
    participant Mapper

    Client->>Controller: POST /api/v1/users
    Controller->>Service: createUser(request)
    Service->>Mapper: selectByEmail(email)
    Mapper-->>Service: existingUser
    Note over Service: 邮箱已存在
    Service-->>Controller: BusinessException(409)
    Controller-->>Client: 409 Conflict
```

---

### 3. ER图(ER Diagram)

#### 基本语法

```mermaid
erDiagram
    USER {
        bigint id PK
        varchar username
        varchar email UK
    }
    ORDER {
        bigint id PK
        bigint user_id FK
    }
    USER ||--o{ ORDER : places
```

#### 关系类型

| 关系 | 语法 | 说明 |
|-----|------|-----|
| 一对一 | `A \|\| --o\|\| B` | 1:1 |
| 一对多 | `A \|\| --o\{ B` | 1:N |
| 多对多 | `A \} --o\{ B` | M:N |

#### 完整示例

```mermaid
erDiagram
    USER ||--o{ USER_ROLE : has
    USER ||--o{ ORDER : places
    ROLE ||--o{ USER_ROLE : belongs
    ORDER ||--|{ ORDER_ITEM : contains
    PRODUCT ||--o{ ORDER_ITEM : ordered

    USER {
        bigint id PK
        varchar username
        varchar email UK
        varchar phone UK
        varchar password
        tinyint status
        datetime create_time
        datetime update_time
    }

    ROLE {
        bigint id PK
        varchar name UK
        varchar description
        datetime create_time
    }

    USER_ROLE {
        bigint user_id PK,FK
        bigint role_id PK,FK
        datetime create_time
    }

    ORDER {
        bigint id PK
        bigint user_id FK
        varchar order_no UK
        decimal total_amount
        tinyint status
        datetime create_time
        datetime update_time
    }

    ORDER_ITEM {
        bigint id PK
        bigint order_id FK
        bigint product_id FK
        int quantity
        decimal price
        decimal sub_total
    }

    PRODUCT {
        bigint id PK
        varchar name
        varchar description
        decimal price
        int stock
        datetime create_time
    }
```

---

### 4. 状态图(State Diagram)

#### 基本语法

```mermaid
stateDiagram-v2
    [*] --> Pending
    Pending --> Paid
    Paid --> Shipped
    Shipped --> Delivered
    Delivered --> [*]
```

#### 完整示例

```mermaid
stateDiagram-v2
    [*] --> 待支付

    待支付 --> 已取消: 取消订单
    待支付 --> 已支付: 支付成功

    已支付 --> 已发货: 发货
    已支付 --> 已退款: 申请退款

    已发货 --> 已完成: 确认收货
    已发货 --> 退款中: 申请退款

    退款中 --> 已退款: 退款成功
    退款中 --> 已发货: 拒绝退款

    已完成 --> [*]
    已取消 --> [*]
    已退款 --> [*]
```

---

### 5. 部署图(Deployment Diagram)

#### 基本语法

```mermaid
graph TB
    Client[客户端]
    LB[负载均衡]
    App1[应用服务器1]
    App2[应用服务器2]
    DB[(数据库)]

    Client --> LB
    LB --> App1
    LB --> App2
    App1 --> DB
    App2 --> DB
```

---

### 6. 分层架构图

#### 标准三层架构

```mermaid
graph TB
    subgraph "表示层"
        Controller[Controller]
    end

    subgraph "业务层"
        Service[Service]
    end

    subgraph "持久层"
        Repository[Repository]
    end

    subgraph "数据库"
        DB[(MySQL)]
    end

    Controller --> Service
    Service --> Repository
    Repository --> DB
```

#### DDD分层架构

```mermaid
graph TB
    subgraph "用户接口层"
        Controller[Controller]
        DTO[DTO]
    end

    subgraph "应用层"
        ApplicationService[ApplicationService]
        Command[Command]
        Query[Query]
    end

    subgraph "领域层"
        Aggregate[聚合根]
        Entity[实体]
        ValueObject[值对象]
        DomainService[领域服务]
        RepositoryInterface[仓储接口]
    end

    subgraph "基础设施层"
        RepositoryImpl[仓储实现]
        Mapper[Mapper]
        ExternalService[外部服务]
    end

    Controller --> ApplicationService
    ApplicationService --> Aggregate
    Aggregate --> RepositoryInterface
    RepositoryInterface --> RepositoryImpl
    RepositoryImpl --> Mapper
```

---

## 图表使用场景

### 1. 类图使用场景

- **概要设计**: 展示模块间的类关系
- **详细设计**: 展示类的属性和方法
- **代码评审**: 检查类的设计是否合理

### 2. 时序图使用场景

- **接口设计**: 展示接口调用流程
- **异常处理**: 展示异常场景的处理流程
- **事务边界**: 展示事务的开始和结束

### 3. ER图使用场景

- **数据建模**: 展示表结构和关系
- **需求分析**: 展示业务实体关系
- **数据库设计**: 指导DDL生成

### 4. 状态图使用场景

- **状态机设计**: 展示对象状态变化
- **业务流程**: 展示订单状态流转
- **工作流**: 展示审批流程

---

## Mermaid最佳实践

### 1. 语法规范

- **使用中文注释**: `Note over Component: 说明`
- **简洁明了**: 避免过多的元素
- **逻辑清晰**: 从上到下，从左到右
- **颜色区分**: 使用样式区分不同类型的元素

### 2. 样式定制

```mermaid
%%{init: {'theme':'base', 'themeVariables': {
  'primaryColor':'#f3f9ff',
  'primaryTextColor':'#0d47a1',
  'primaryBorderColor':'#2196f3',
  'lineColor':'#42a5f5',
  'secondaryColor':'#f8fff8',
  'tertiaryColor':'#fff0f0'
}}}%%
classDiagram
    class Service {
        +method() void
    }
```

### 3. 常用样式

```mermaid
classDiagram
    class Controller~蓝色组件~ {
        +handleRequest()
    }

    class Service~绿色组件~ {
        +process()
    }

    class Repository~红色组件~ {
        +save()
    }

    Controller --> Service : calls
    Service --> Repository : uses
```

---

## 检查清单

### 类图
- [ ] 类名准确
- [ ] 属性和方法完整
- [ ] 关系类型正确
- [ ] 可见性符号正确

### 时序图
- [ ] 参与者完整
- [ ] 消息顺序正确
- [ ] 同步/异步消息区分
- [ ] 返回值明确

### ER图
- [ ] 表名和字段名正确
- [ ] 主键标识
- [ ] 外键标识
- [ ] 唯一索引标识
- [ ] 关系类型正确
