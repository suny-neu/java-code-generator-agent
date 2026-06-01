# Java 代码生成 Agent - 使用说明

## 简介

Java 代码生成 Agent 是一个基于 AI 的工业级代码生成工具。你只需要用自然语言描述需求，Agent 会按照严格的 6 步 SOP 自动生成完整的 Java 分层架构代码，打包成 ZIP 下载到本地。

**你不需要写代码，不需要装环境，不需要花 token。**

## 访问地址

```
https://你的railway地址.up.railway.app
```

用浏览器打开即可使用。

## 使用流程

### 1. 描述需求

在输入框里用自然语言描述你要生成什么，例如：

> 帮我生成一个用户管理模块，包含用户注册、登录、查询个人信息、修改密码功能。用户表有用户名、手机号、邮箱、密码、状态字段。

### 2. 与 Agent 对话确认

Agent 会按 6 个步骤跟你确认：

| 步骤 | Agent 做什么 | 你需要做什么 |
|------|-------------|-------------|
| **Step 1** 环境校验 | 自动检测项目配置 | 确认或补充缺失配置 |
| **Step 2** 需求解构 | 复述需求、识别边界条件 | 确认理解是否准确 |
| **Step 3** 表结构设计 | 设计数据库表和索引 | 确认表结构 |
| **Step 3.5** 设计文档 | 生成概要设计和详细设计 | 确认设计 |
| **Step 4** 依赖审查 | 检查需要新增的 Maven 依赖 | 确认是否引入 |
| **Step 5** 代码生成 | 生成全部代码文件 | 输入项目名，点击"生成代码" |

每一步 Agent 都会停下来等你确认，你说"没问题"或"确认"就进入下一步。

### 3. 生成代码

前 4 步确认完毕后，点击页面上的 **"生成代码"** 按钮，输入项目名称，Agent 会一次性生成全部文件。

### 4. 下载 ZIP

生成完成后，点击 **"下载 ZIP"** 按钮，浏览器会下载一个 ZIP 文件。

## 下载内容说明

ZIP 文件解压后是一个完整的 Java 项目结构：

```
你的项目名/
├── pom.xml                              # Maven 依赖配置
├── src/main/java/com/xxx/
│   ├── controller/                      # Controller 层
│   │   └── XxxController.java
│   ├── service/                         # Service 层
│   │   ├── XxxService.java              # 接口
│   │   └── impl/XxxServiceImpl.java     # 实现
│   ├── repository/                      # Mapper 层
│   │   └── XxxMapper.java
│   ├── entity/                          # 数据库实体
│   │   └── XxxDO.java
│   ├── dto/                             # 数据传输对象
│   │   ├── request/XxxCreateRequest.java
│   │   ├── request/XxxUpdateRequest.java
│   │   └── response/XxxResponse.java
│   ├── converter/                       # MapStruct 转换器
│   │   └── XxxConverter.java
│   ├── enums/                           # 枚举类
│   ├── exception/                       # 异常定义
│   ├── wrapper/                         # 统一响应包装
│   └── config/                          # 配置类
├── src/main/resources/
│   ├── application.yml                  # 应用配置
│   └── mapper/                          # MyBatis XML（如有自定义 SQL）
├── src/test/java/com/xxx/               # 单元测试
│   ├── service/XxxServiceTest.java
│   ├── controller/XxxControllerTest.java
│   ├── repository/XxxMapperTest.java
│   └── dto/                             # DTO 测试
└── docs/                                # 文档
    ├── design-hld.md                    # 概要设计文档
    ├── design-lld.md                    # 详细设计文档
    ├── postman-collection.json          # Postman 测试集合
    └── test-data.sql                    # 测试数据 SQL
```

## 完整交付物清单

Agent 一次生成会输出以下**全部文件**，不是只有代码：

### 一、业务代码

| 文件 | 说明 |
|------|------|
| `XxxController.java` | REST 接口层，含 Swagger 注解 |
| `XxxService.java` | Service 接口 |
| `XxxServiceImpl.java` | Service 实现类，含并发控制和事务管理 |
| `XxxMapper.java` | MyBatis-Plus Mapper 接口 |
| `XxxDO.java` | 数据库实体（含乐观锁 version、自动填充时间） |
| `XxxCreateRequest.java` | 创建请求 DTO（含 @Valid 参数校验注解） |
| `XxxUpdateRequest.java` | 更新请求 DTO |
| `XxxResponse.java` | 响应 DTO |
| `XxxConverter.java` | MapStruct 对象转换器 |

### 二、设计文档

| 文件 | 说明 |
|------|------|
| `docs/design-hld.md` | **概要设计文档（HLD）** — 系统架构图、模块划分、核心接口列表、ER 图、技术选型 |
| `docs/design-lld.md` | **详细设计文档（LLD）** — 类图、时序图、核心算法、异常处理策略、并发/事务设计 |

### 三、单元测试（全覆盖）

| 文件 | 覆盖场景 |
|------|---------|
| `XxxServiceTest.java` | 成功、失败、异常、空数据、边界条件 |
| `XxxControllerTest.java` | 成功、参数校验、异常、返回值验证（@InjectMocks 直接调用，不用 MockMvc） |
| `XxxMapperTest.java` | CRUD 全操作、分页、批量操作、乐观锁 |
| `XxxCreateRequestTest.java` | @Valid 参数校验、Getter/Setter、toString |
| `XxxResponseTest.java` | Getter/Setter、equals、hashCode、序列化 |
| `XxxEnumTest.java`（如有枚举） | 枚举值、code 转换、valueOf、@JsonValue |

### 四、测试资源

| 文件 | 说明 |
|------|------|
| `docs/postman-collection.json` | **Postman 测试集合** — 可直接导入 Postman 运行，包含所有接口的请求示例 |
| `docs/test-data.sql` | **测试数据 SQL** — 初始化测试数据的 INSERT 语句 |

### 五、项目配置

| 文件 | 说明 |
|------|------|
| `pom.xml` | Maven 依赖配置（增量模式，不覆盖已有依赖） |
| `application.yml` | 数据源、Redis、MyBatis-Plus、Swagger 配置 |
| `schema.sql` | 建表 DDL（含并发控制索引、唯一索引） |

### 六、公共组件（首次生成）

| 文件 | 说明 |
|------|------|
| `Result.java` | 统一响应包装 |
| `PageResult.java` | 分页响应包装 |
| `BusinessException.java` | 业务异常类 |
| `ErrorCode.java` | 错误码枚举 |
| `GlobalExceptionHandler.java` | 全局异常处理器 |
| `MyBatisPlusConfig.java` | 分页插件配置 |
| `MyMetaObjectHandler.java` | 自动填充 createTime/updateTime |
| `SwaggerConfig.java` | API 文档配置 |
| `RedisConfig.java` | Redis 配置 |

---

## 代码规范

| 规范 | 说明 |
|------|------|
| 技术栈 | Spring Boot + MyBatis-Plus + MapStruct + SpringDoc |
| 分层架构 | Controller → Service → Mapper，职责清晰 |
| 统一响应 | `Result<T>` 包装，统一 code/message/data 格式 |
| 统一异常 | `BusinessException` + `GlobalExceptionHandler` |
| 代码红线 | 方法 ≤50 行、参数 ≤4 个、禁止空 catch |
| 版权头 | 每个文件头部自动加 TravelSky 版权声明 |
| 类注释 | ClassName / Description / @author / @version |
| 方法注释 | 每个公开方法有 @param / @return 注释 |
| 并发控制 | 金额累加用 Redis 原子操作、状态流转用乐观锁 |
| SQL 性能 | 根据数据量级（S/M/L/XL）自动选择查询策略、索引设计 |
| 单元测试 | JUnit 4 + Mockito，全覆盖 Service/Controller/Mapper/DTO/Enum |
| API 文档 | Swagger/OpenAPI 注解，启动后访问 `/swagger-ui.html` |

## 使用示例

### 示例 1：用户管理

**你的输入**：
> 帮我生成一个用户管理模块。用户可以注册（用户名+手机号+密码）、登录（手机号+密码）、查看个人信息、修改密码。用户表要记录注册时间和最后登录时间。

**Agent 会生成**：
- UserController（注册、登录、查询、修改密码 4 个接口）
- UserService + UserServiceImpl（业务逻辑 + 参数校验）
- UserMapper（数据库操作）
- UserDO / UserCreateRequest / UserLoginRequest / UserResponse
- 完整单元测试 + Postman 测试集合

### 示例 2：订单管理

**你的输入**：
> 生成一个订单管理模块。支持创建订单、查询订单列表（分页）、取消订单。订单有状态：待支付、已支付、已取消。订单表大概 50 万数据量。订单金额用 BigDecimal。

**Agent 会额外处理**：
- 数据量级评估为 M 级，自动加分页上限（MAX_PAGE_SIZE=100）
- 订单状态流转加并发控制（乐观锁 @Version）
- 金额字段用 BigDecimal，查询只取必要字段避免 SELECT *
- GROUP BY 查询加联合索引设计

### 示例 3：已有项目追加模块

**你的输入**：
> 我已有一个 Spring Boot 项目，包名是 com.travelsky.pss.dc.stat，现在要加一个航班行李统计模块。统计每天的行李数量和重量，按航站分组。数据量大概每天 10 万条，要查最近一年的数据。

**Agent 会**：
- 识别为 L 级数据量，设计预聚合日汇总表
- GROUP BY 加联合索引，避免全表聚合
- 自动检测已有项目的包名和依赖，只生成增量代码
- 不覆盖已有的公共组件（Result、异常类等）

## 注意事项

1. **需求描述越详细越好** — 包含字段名、业务规则、数据量级，Agent 生成的代码质量越高
2. **每一步都要确认** — 不要跳步，发现 Agent 理解错了及时纠正
3. **检查生成的代码** — AI 生成的内容需要你人工审查后再使用
4. **测试数据需要调整** — 生成的 SQL 测试数据是示例，需要替换成真实数据
5. **数据库连接需要配置** — 下载后需要修改 `application.yml` 里的数据库连接信息

## 常见问题

**Q：生成的代码能直接运行吗？**
A：解压到项目目录后，配置好数据库连接（改 `application.yml`），创建好数据库表（执行 `schema.sql`），就可以启动。

**Q：支持哪些数据库？**
A：支持 OceanBase（MySQL 模式）、MySQL、PostgreSQL。

**Q：可以追加到已有项目吗？**
A：可以。在 Step 1 告诉 Agent 你的项目包名和现有依赖，它会只生成增量代码，不覆盖已有文件。

**Q：一次能生成多个模块吗？**
A：建议一次生成一个模块。如果多个模块有关联，可以在需求里一起描述。

**Q：生成不满意怎么办？**
A：在对话中直接告诉 Agent 哪里需要修改，它会重新生成。你也可以只要求修改某个文件，不用全部重生成。
