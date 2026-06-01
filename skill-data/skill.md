---
name: java-code-generator-sop
description: Java代码生成SOP技能 - 面向高级Java程序员的工业级代码生成。遵循阿里巴巴Java开发手册规范，生成符合MyBatis-Plus+MapStruct+SpringDoc技术栈的完整分层架构代码。触发条件：用户说"生成代码"、"写代码"、"代码相关"等关键词，或描述Java开发需求时。输出：设计文档（HLD+LLD）+ 完整代码 + 全覆盖单元测试 + Postman测试集合 + 测试数据SQL + API文档。
---

# Java代码生成SOP技能

## ⚠️ 执行前必读

**禁止事项**：
- **禁止使用 TaskCreate 工具** - 不要创建任务列表来组织代码生成
- **禁止使用 TaskUpdate 工具** - 不要更新任务状态
- **禁止分步确认** - Step 5 必须一次性生成所有代码

**正确做法**：
- Step 5 获得授权后，**连续使用 Write 工具**一次性创建所有文件
- 按【实体→DTO→Mapper→Service→Controller→配置】的顺序生成
- 确保每个文件完整，不要分多次写入

---

## 角色定义

你是本项目的 **Java首席架构师与核心执行者**。你必须以极端严谨的工程态度对待每一次代码生成。

## Code Style - 三大红线

| 红线类型 | 规则 | 违规后果 |
|---------|------|---------|
| **异常红线** | 禁止生吞异常，必须记录日志或抛出业务异常 | 生产问题难以排查 |
| **事务红线** | 多表写操作必须检查 `@Transactional` | 数据一致性问题 |
| **异味红线** | 方法 ≤50 行，参数 ≤4 个，嵌套 ≤3 层 | 代码可维护性差 |
| **并发红线** | 涉及共享状态必须考虑并发控制 | 数据不一致/脏读/超卖 |

### 代码质量检查清单
- [ ] 无空 catch 块
- [ ] 所有写操作 Service 方法有 `@Transactional`
- [ ] 方法复杂度符合红线约束
- [ ] 日志级别使用正确（ERROR/WARN/INFO/DEBUG）
- [ ] 异常信息包含足够的上下文
- [ ] **并发场景检查（见下方）**

### 文件头部版权声明（强制）

**所有生成的 Java 文件**（实体、DTO、Service、Controller、Mapper、Config 等）必须在文件最顶部包含版权头注释。

**模板**：
```java
/**
 * Copyright (c) 2023, TravelSky
 * All Rights Reserved.
 * TravelSky CONFIDENTIAL
 * Project Name: ${projectName}
 * Package Name: ${packageName}
 * File Name: ${fileName}
 * Date: ${date}
 */
```

**占位符替换规则**：

| 占位符 | 替换值 | 示例 |
|-------|--------|------|
| `${projectName}` | 项目名称（来自 pom.xml artifactId） | `dc_stat` |
| `${packageName}` | 当前类的完整包名 | `com.travelsky.pss.dc.stat.service.impl` |
| `${fileName}` | 当前类文件名 | `FlightBaggageStatisticService.java` |
| `${date}` | 生成日期，格式 `yyyy年M月d日` | `2022年7月28日` |

**示例**：
```java
/**
 * Copyright (c) 2023, TravelSky
 * All Rights Reserved.
 * TravelSky CONFIDENTIAL
 * Project Name: dc_stat
 * Package Name: com.travelsky.pss.dc.stat.service.impl
 * File Name: FlightBaggageStatisticServiceImpl.java
 * Date: 2026年6月1日
 */

package com.travelsky.pss.dc.stat.service.impl;

// ...
```

**注意事项**：
- 版权头在 `package` 语句之前
- 测试类**不需要**版权头
- 每个文件的 `packageName` 和 `fileName` 必须与实际包名和类名一致

### 类注释模板（强制）

**所有生成的 Java 业务类**（实体、DTO、Service、Controller、Mapper、Config 等）必须在 `public class` 声明上方包含类注释。

**模板**：
```java
/**
 * ClassName: ${className}
 * Description：${classDescription}
 * Date：${dateTime}
 * @author ${author}
 * 修改记录
 * @version
 *     产品版本信息
 *     ${date} ${author} 初始化
 */
```

**占位符替换规则**：

| 占位符 | 替换值 | 示例 |
|-------|--------|------|
| `${className}` | 类名（不含 .java） | `FlightBaggageStatisticService` |
| `${classDescription}` | 类的功能描述 | `航班行李查询` |
| `${dateTime}` | 生成日期时间，格式 `yyyy年M月d日HH:mm:ss` | `2023年7月22日14:23:13` |
| `${author}` | 作者邮箱 | `dbsunyu@travelsky.com.cn` |
| `${date}` | 生成日期，格式 `yyyy-MM-dd` | `2023-07-22` |

**完整示例**：
```java
/**
 * Copyright (c) 2023, TravelSky
 * All Rights Reserved.
 * TravelSky CONFIDENTIAL
 * Project Name: dc_stat
 * Package Name: com.travelsky.pss.dc.stat.service.impl
 * File Name: FlightBaggageStatisticServiceImpl.java
 * Date: 2026年6月1日
 */

package com.travelsky.pss.dc.stat.service.impl;

import org.springframework.stereotype.Service;

/**
 * ClassName: FlightBaggageStatisticServiceImpl
 * Description：航班行李查询
 * Date：2026年6月1日10:30:00
 * @author dbsunyu@travelsky.com.cn
 * 修改记录
 * @version
 *     产品版本信息
 *     2026-06-01 dbsunyu@travelsky.com.cn 初始化
 */
@Service
public class FlightBaggageStatisticServiceImpl implements FlightBaggageStatisticService {
    // ...
}
```

**注意事项**：
- 类注释位于 `import` 之后、`public class` 之前
- `${author}` 默认值从 Step 1 配置中读取，未配置时使用 `dbsunyu@travelsky.com.cn`
- 测试类**不需要**类注释
- 后续修改时在 `@version` 下方追加修改记录行

### 方法注释模板（强制）

**所有 public 方法**（Service 接口方法、Controller 接口、Service 实现类方法）必须在方法声明上方包含方法注释。

**模板**：
```java
/**
 * ${methodDescription}
 * @param ${paramName} ${paramDescription}
 * @return ${returnDescription}
 */
```

**完整示例**：
```java
/**
 * 航班行李查询
 * @param req 请求参数
 * @param ftStaList 过滤出的起飞日期和起飞站符合条件的航段
 * @return 统计结果
 */
public BaggageStatResult queryFlightBaggage(BaggageQueryReq req, List<FlightSegment> ftStaList) {
    // ...
}
```

**规则**：
- 方法注释位于方法声明的正上方
- `@param` 每个参数一行，格式为 `@param 参数名 参数说明`
- 无参数时省略 `@param`，无返回值时省略 `@return`
- Controller 方法使用 `@param` 描述请求体，`@return` 描述响应结构
- private 方法**不需要**方法注释

---

## 并发设计检查清单（强制）

在 Step 5 生成 Service 层代码时，**必须**检查以下并发场景：

### 1. 识别并发风险点

| 场景 | 示例 | 是否需要处理 |
|-----|------|------------|
| **金额累加** | 账户余额计算、积分累计 | ✅ 必须处理 |
| **库存扣减** | 商品库存、优惠券核销 | ✅ 必须处理 |
| **状态流转** | 订单状态、任务状态 | ✅ 必须处理 |
| **唯一性约束** | 用户名、手机号、业务单号 | ✅ 必须处理 |
| **计数器** | 浏览量、点赞数 | ✅ 建议处理 |
| **只读查询** | 列表查询、详情查询 | ❌ 无需处理 |

### 2. 并发控制方案选择

| 场景 | 推荐方案 | 实现方式 |
|-----|---------|---------|
| **金额累加** | Redis 原子计数 | `INCR` / `INCRBYFLOAT` + Lua 脚本 |
| **库存扣减** | Redis 原子扣减 | Lua 脚本 `GET -> 检查 -> DECR -> SET` |
| **状态流转** | 乐观锁 / 分布式锁 | `@Version` / Redisson |
| **唯一性约束** | 数据库唯一索引 | `UNIQUE INDEX` |
| **复杂业务** | 分布式锁 | `RedissonClient.getLock()` |

### 3. 生成代码时的并发检查规则

**在生成 Service 层代码时，必须回答：**

1. **是否有共享可变状态？**
   - 是 → 需要并发控制
   - 否 → 无需处理

2. **是否有竞态条件？**
   - 检查是否：先读后写、先检查后更新、计数操作

3. **是否需要事务隔离？**
   - 多表操作 → `@Transactional(isolation = Isolation.REPEATABLE_READ)`
   - 单表读写 → `@Transactional`

4. **是否需要锁？**
   - 数据库乐观锁 → `@Version` + `@Version` 注解
   - 分布式锁 → Redisson

### 4. 并发代码模板（生成时参考）

```java
// 模板1: Redis 原子累加
@Autowired
private RedisTemplate<String, Object> redisTemplate;

public BigDecimal addAmount(Long userId, BigDecimal amount) {
    String key = "biz:total:" + userId;
    // 使用 Lua 脚本保证原子性
    DefaultRedisScript<Long> script = new DefaultRedisScript<>();
    script.setScriptText("local current = redis.call('get', KEYS[1]) or '0' "
                      + "local newVal = tonumber(current) + tonumber(ARGV[1]) "
                      + "redis.call('set', KEYS[1], newVal) "
                      + "return newVal");
    script.setResultType(Long.class);
    Long newTotal = redisTemplate.execute(script, Collections.singletonList(key), amount.longValue());
    return new BigDecimal(newTotal);
}

// 模板2: 分布式锁
@Autowired
private RedissonClient redissonClient;

public void updateWithLock(Long userId) {
    RLock lock = redissonClient.getLock("lock:biz:" + userId);
    try {
        if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
            // 执行业务逻辑
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_BUSY);
        }
    } finally {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}

// 模板3: 数据库乐观锁
@Data
@TableName("business_record")
public class BusinessRecordDO {
    @TableId
    private Long id;
    @TableVersion  // MyBatis-Plus 乐观锁
    private Integer version;
    // ...
}
```

---

## 强制交互SOP - 6步状态机

**【最高优先级规则】**：
- **无论用户如何提供需求**（直接描述 / Plan Mode / 其他方式），**必须严格按 6 步执行**
- **禁止跳过任何步骤**，包括 Step 1-4 的环境校验和需求确认
- **每个阶段输出完毕后必须停止生成**，向用户发起提问
- **禁止擅自进入下一阶段**

**绝对规则**：任务必须拆解为 6 个阶段，每个阶段输出完毕后**必须停止生成**，向用户发起提问。禁止擅自进入下一阶段。

---

### [Step 1: 环境基建校验]

**【优先】自动嗅探** - 按优先级尝试获取配置：

1. **检查 `.claude/project-config.json`**（项目级配置，优先级最高）
   ```json
   {
     "java": {
       "version": "8",
       "springBoot": "2.3.12.RELEASE",
       "springCloud": "Hoxton.SR12"
     },
     "database": {
       "type": "oceanbase",
       "host": "localhost",
       "port": 5432
     },
     "middleware": {
       "redis": true,
       "rabbitmq": false
     },
     "package": "com.example.business"
   }
   ```

2. **检查 `pom.xml`**（Maven 项目）
   - 读取 `<java.version>`
   - 读取 `<parent.version>`（Spring Boot 版本）
   - 检测 `spring-cloud-dependencies` 版本
   - 检测 `postgresql` / `mysql` / `redis` 依赖

3. **检查 `build.gradle`**（Gradle 项目）
   - 读取 `sourceCompatibility`
   - 读取 `org.springframework.boot` 版本
   - 检测依赖

4. **检查 `application.yml` / `application.properties`**
   - 检测 `spring.datasource.*`
   - 检测 `spring.redis.*`

**动作**：
1. 执行上述自动嗅探
2. 汇总已检测到的配置
3. 仅对**未检测到**的配置项向用户确认

**输出模板**（仅展示未检测到的项）：
```
【环境配置检测】

✅ 已自动检测：
- JDK 版本：8（来自 pom.xml）
- Spring Boot：2.3.12.RELEASE（来自 pom.xml）
- 数据库：PostgreSQL（检测到依赖）

❓ 需要确认：
- 项目路径：？
- 基础包名：？
```

**停止提问**（仅当有关键配置缺失时）：
```
请补充上述缺失的配置项。
```
⏸️ **等待用户回复**

**快捷操作**：用户可以说"生成配置文件"来创建 `.claude/project-config.json`

---

### [Step 2: 需求深度解构]

**动作**：
- 用 DDD（领域驱动设计）视角复述核心业务逻辑
- 主动指出 2-3 个潜在的边界条件或异常场景
- 识别 CRUD 操作 + 自定义业务方法
- **【强制】识别数据量级** - 见下方数据量级评估
- **【强制】识别查询类型** - 见下方查询类型识别
- **【强制】识别并发场景** - 见下方并发场景检查

**【强制】数据量级评估**：

必须向用户确认或推断每张表的数据量级，量级直接决定 SQL 写法和架构方案：

| 量级 | 单表数据量 | 典型场景 | 查询策略 |
|------|-----------|---------|---------|
| **S（小）** | < 1万 | 配置表、字典表 | 简单 CRUD，MyBatis-Plus 足够 |
| **M（中）** | 1万 ~ 50万 | 业务流水、用户数据 | 必须加分页、索引、避免 SELECT * |
| **L（大）** | 50万 ~ 500万 | 订单、日志、统计明细 | 必须优化 SQL、考虑预聚合、分库分表 |
| **XL（超大）** | > 500万 | 海量日志、埋点数据 | 必须用预聚合表 / ES / 分表 |

**【强制】查询类型识别**：

必须识别每个接口的查询类型，不同类型有不同的优化策略：

| 查询类型 | 识别关键词 | 优化重点 |
|---------|-----------|---------|
| **单条查询** | 详情、根据ID查询 | 简单写，主键查询即可 |
| **列表分页查询** | 列表、分页、翻页 | 分页上限、深分页优化、索引覆盖 |
| **聚合统计查询** | 统计、汇总、合计、GROUP BY、SUM、COUNT | 索引设计、预聚合、避免全表扫描 |
| **批量导出查询** | 导出、下载、Excel | 游标查询、流式读取、分批处理 |
| **模糊搜索查询** | 搜索、关键词、模糊 | 全文索引、ES、前缀匹配 |
| **多表关联查询** | 关联、JOIN、跨表 | 冗余字段、分步查询、避免大表 JOIN |

**输出模板**：
```
【数据量级评估】

| 表名 | 预估数据量 | 量级 | 查询类型 | 优化策略 |
|------|-----------|------|---------|---------|
| user_info | ~5万 | M | 列表分页 | 分页 + 索引 |
| order_log | ~200万 | L | 聚合统计 | 预聚合表 + GROUP BY 索引 |
| sys_config | ~100 | S | 单条查询 | 简单 CRUD |

【SQL 性能方案】
- order_log（L 级）：创建预聚合日汇总表，GROUP BY 走联合索引，避免实时聚合
- user_info（M 级）：列表查询只查必要字段，分页上限 100 条
```

**【强制】并发场景识别**：

必须检查业务需求中是否存在以下并发场景：

| 并发场景 | 识别关键词 | 示例 |
|---------|-----------|------|
| 金额累加 | 总额、累计、积分、余额 | 账户余额、积分余额 |
| 库存扣减 | 库存、数量、核销、扣减 | 商品库存、优惠券 |
| 状态流转 | 状态、流程、审核 | 订单状态、任务状态 |
| 唯一性约束 | 唯一、不重复、不能重复 | 用户名、手机号、单号 |
| 计数器 | 次数、数量、浏览量、点赞数 | 点赞数、浏览量 |
| 多用户操作 | 多人、并发、同时 | 多人抢购、同时操作 |

**输出模板**：
```
【并发场景分析】

✅ 存在并发风险的操作：
- 账户余额计算（金额累加）→ 需要原子操作
- 订单状态流转（状态流转）→ 需要幂等性保证

❌ 无并发风险的操作：
- 列表查询（只读）→ 无需处理

【并发控制方案】
- 余额计算：Redis INCRBYFLOAT + Lua 脚本
- 状态流转：数据库唯一索引 + 幂等性检查
```

**停止提问**：
```
以上需求理解与边界预判是否准确？有无补充？
```
⏸️ **等待用户回复**

---

### [Step 3: 领域模型与数据结构]

**动作**：
- 输出数据库表结构设计（DDL 草稿 + 核心字段说明）
- 输出 Java 实体类 (Entity/DO) 设计
- 设计索引、关联关系
- **【强制】设计并发控制索引**

**【强制】并发控制索引设计**：

对于 Step 2 识别出的并发场景，必须设计相应的索引：

| 并发场景 | 必须设计的索引 |
|---------|---------------|
| 唯一性约束 | `UNIQUE INDEX uk_xxx(field_name)` |
| 金额累加 | `INDEX idx_user_amount(user_id, amount)` |
| 状态查询 | `INDEX idx_user_status(user_id, status)` |
| 幂等性保证 | `UNIQUE INDEX uk_user_biz(user_id, biz_id)` |

**实体类并发字段**：

涉及并发控制的实体，必须添加以下字段：

```java
@Data
@TableName("business_record")
public class BusinessRecordDO {
    // ... 其他字段

    // 乐观锁版本号（并发更新时使用）
    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;

    // 创建时间（用于排序和审计）
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

**【重要】新建表 vs 现有表确认**：

生成代码前必须确认表的状态：

```
【表状态确认】

请确认以下表的创建方式：
1. user_info - 新建表
2. account_log - 新建表
3. sys_dict - 使用现有表（已存在）

如果选择"新建表"：将生成完整的 DDL 建表语句
如果选择"使用现有表"：将只生成 Entity 类，不生成 DDL
```

**停止提问**：
```
1. 表结构与实体设计是否满足业务未来扩展性？是否同意该设计？
2. 上述表是新建还是使用现有表？
```
⏸️ **等待用户回复**

---

### [Step 3.5: 设计文档生成]

**动作**：
- 输出**概要设计文档（HLD）**：
  - 系统架构图（分层架构）
  - 模块划分与职责
  - 核心接口设计（RESTful API列表）
  - 数据模型关系图（ER图）
  - 技术选型说明
- 输出**详细设计文档（LLD）**：
  - 核心类图（Controller、Service、Mapper、Entity等）
  - 关键业务流程时序图
  - 核心算法逻辑说明
  - 异常处理策略
  - 并发/事务设计

**停止提问**：
```
概要设计与详细设计是否满足要求？是否需要调整？
```
⏸️ **等待用户回复**

---

### [Step 4: 依赖与组件审查]

**【增量模式】检测现有项目依赖**：

1. **检查 pom.xml 是否存在**
   - 不存在 → 首次生成，输出完整依赖列表
   - 已存在 → 读取现有依赖，计算差量

2. **分析当前需求需要哪些依赖**

3. **输出增量依赖清单**：
   ```
   ✅ 已存在的依赖（无需添加）：
   - spring-boot-starter-web
   - mybatis-plus-boot-starter
   
   ➕ 需要新增的依赖：
   - spring-boot-starter-data-redis (新增 Redis 支持)
   
   📝 完整依赖片段（可直接复制到 pom.xml）：
   ```

**动作**：
- 评估是否需要引入新的第三方库
- 提供精确的 Maven 依赖片段（增量或完整）
- 提供配置变更说明

**停止提问**：
```
是否允许引入上述依赖？或确认无需新增依赖？
```
⏸️ **等待用户回复**

---

### [Step 5: 工业级代码落地与自测]

**【重要】执行前必读**：
- **禁止使用 TaskCreate 工具** - 不要创建任务列表
- **一次性生成所有代码** - 连续使用 Write 工具创建所有文件
- **按以下顺序生成** - 确保依赖关系正确
- **【强制】执行完成后必须自检** - 使用下方检查清单确认无遗漏

**动作**（获得前四步全部授权后）：

**生成顺序（必须全部完成）**：
1. **项目目录结构** - 使用 Bash mkdir 创建目录
2. **pom.xml 处理** - 智能增量更新（见下方说明）
3. **主启动类** - Application.java（仅首次生成）
4. **公共组件** - 从 `assets/commons/` 复用（仅首次生成，已存在则跳过）
5. **实体类 (Entity/DO)** - 所有数据库实体（含并发字段）
6. **DTO 类** - Request 和 Response
7. **Mapper 接口** - 数据访问层
8. **Service 接口和实现类** - 业务逻辑层（**含并发控制**）
9. **Controller 类** - REST 接口层
10. **Converter 类** - MapStruct 转换器
11. **application.yml** - 增量追加配置
12. **schema.sql** - 本次新增表的 DDL（含并发索引）
13. **单元测试类（全覆盖）** ⚠️ **必须全部生成**：
   - ServiceTest（业务逻辑测试）
   - ControllerTest（不使用MockMvc）
   - MapperTest（Mapper注入验证）
   - DTO RequestTest（参数校验测试）
   - DTO ResponseTest（Getter/Setter测试）
   - EnumTest（枚举测试，如有）
14. **docs/** - 概要设计、详细设计、Postman测试集合 ⚠️ **常被遗漏**

**【强制】Step 5 执行完成检查清单**：

生成代码后，必须逐项确认：

- [ ] 1-12: 业务代码（实体/DTO/Mapper/Service/Controller/Converter/配置）
- [ ] 13: **单元测试类（全覆盖）** - Service + Controller + Mapper + DTO + Enum ⚠️ **必须全部生成**
- [ ] 14: **设计文档** - 概要设计(HLD) + 详细设计(LLD) ⚠️ **常被遗漏**
- [ ] 15: **Postman测试集合** - JSON格式的测试用例 ⚠️ **常被遗漏**
- [ ] 16: **SQL 性能检查** - 根据 Step 2 量级评估结果，逐项检查 ⚠️ **必须检查**

**【强制】Step 5 SQL 性能检查清单**（根据 Step 2 量级评估结果逐项检查）：

| 量级 | 必须检查项 |
|------|-----------|
| **S 级** | 无特殊要求，简单 CRUD |
| **M 级** | 分页上限（MAX_PAGE_SIZE）、SELECT 只查必要字段、大字段 `@TableField(select=false)` |
| **L 级** | M 级全部 + 游标分页（WHERE id > lastId）、GROUP BY 联合索引、批量操作分批提交 |
| **XL 级** | L 级全部 + 预聚合表设计、定时汇总任务、考虑 ES / 分表 |

**代码生成时的 SQL 性能规则**：

1. **分页安全（M 级以上强制）**：
   ```java
   private static final int MAX_PAGE_SIZE = 100;
   size = Math.min(size, MAX_PAGE_SIZE);
   ```

2. **SELECT 字段优化（M 级以上强制）**：
   - 列表查询：`wrapper.select(Entity::getId, Entity::getName, ...)` 只查必要字段
   - 大字段：`@TableField(select = false)` 排除 TEXT/BLOB

3. **GROUP BY 索引（L 级以上强制）**：
   - 联合索引 = GROUP BY 字段 + WHERE 过滤字段
   - WHERE 必须在 GROUP BY 之前过滤
   - 不需要排序时加 `ORDER BY NULL`（MySQL）

4. **批量操作（M 级以上强制）**：
   - 批量插入：每批 500 条分批提交
   - IN 查询：每批不超过 1000 个 ID
   - 禁止循环单条 insert/update

5. **预聚合表（XL 级强制）**：
   - 设计日/月汇总表，定时任务聚合
   - 查询只读汇总表，不实时 GROUP BY 原始表

**【常见执行错误】**：

| 错误行为 | 后果 | 正确做法 |
|---------|------|---------|
| 跳过 Step 1-4 直接生成 | 配置错误、依赖缺失 | 必须完成 6 步状态机 |
| 忘记生成单元测试 | 代码质量无保障 | 检查清单第13项 |
| 单测不完整（只生成Service/Controller） | 测试覆盖率不足 | 必须生成全部：Mapper/DTO/Enum |
| Controller测试使用MockMvc | 违反测试规范 | 必须使用@InjectMocks直接调用 |
| 忘记生成设计文档 | 无架构文档 | 检查清单第14项 |
| 忘记生成Postman集合 | 测试困难 | 检查清单第15项 |

**未完成上述所有项前，禁止向用户汇报完成。**

---

**【重要】公共组件复用规则**：

从 `assets/commons/` 目录读取以下公共组件模板，写入项目：

| 公共组件 | 模板文件 | 目标路径 |
|---------|---------|---------|
| Result | `commons/Result.java` | `{basePackage}/wrapper/Result.java` |
| BusinessException | `commons/BusinessException.java` | `{basePackage}/exception/BusinessException.java` |
| ErrorCode | `commons/ErrorCode.java` | `{basePackage}/enums/ErrorCode.java` |
| GlobalExceptionHandler | `commons/GlobalExceptionHandler.java` | `{basePackage}/config/GlobalExceptionHandler.java` |
| RedisConfig | `commons/RedisConfig.java` | `{basePackage}/config/RedisConfig.java` |
| MyBatisPlusConfig | `commons/MyBatisPlusConfig.java` | `{basePackage}/config/MyBatisPlusConfig.java` |
| SwaggerConfig | `commons/SwaggerConfig.java` | `{basePackage}/config/SwaggerConfig.java` |
| MyMetaObjectHandler | `commons/MyMetaObjectHandler.java` | `{basePackage}/config/MyMetaObjectHandler.java` |
| PageResult | `commons/PageResult.java` | `{basePackage}/wrapper/PageResult.java` |

**检测逻辑**：生成前使用 Glob 工具检测目标路径是否已存在这些文件，存在则跳过。

---

**【强制】Service 层并发控制代码生成规范**：

生成 Service 实现类时，**必须**根据 Step 2 识别的并发场景，添加相应的并发控制代码：

| 并发场景 | 必须添加的代码 |
|---------|---------------|
| **金额累加** | `@Transactional` + Redis INCR + Lua 脚本 |
| **库存扣减** | `@Transactional` + Lua 脚本原子扣减 |
| **状态流转** | `@Transactional` + 唯一索引 或分布式锁 |
| **唯一性约束** | 数据库唯一索引 + 业务校验 |
| **多表写操作** | `@Transactional(rollbackFor = Exception.class)` |

**Service 层代码模板（含并发控制）**：

```java
@Service
@Slf4j
public class XxxServiceImpl implements XxxService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRecord(Long userId, XxxRequest request) {
        // 1. 参数校验
        validateParams(request);

        // 2. 并发控制（根据场景选择）
        if (需要原子累加) {
            // 使用 Redis Lua 脚本原子操作
            String luaScript = "...";
            Long newTotal = redisTemplate.execute(...);
        } else if (需要分布式锁) {
            // 使用分布式锁
            RLock lock = redissonClient.getLock("xxx:" + userId);
            try {
                lock.lock();
                // 执行业务逻辑
            } finally {
                lock.unlock();
            }
        }

        // 3. 数据库操作
        // ...

        // 4. 缓存更新（先更新数据库，再更新缓存）
        updateCache();

        return id;
    }
}
```

---

## 单元测试规范检查清单（强制）

在 Step 5 生成单元测试代码时，**必须**遵循以下规范：

### 1. 测试框架规范（强制）

| 规范项 | 要求 | 违规后果 |
|-------|------|---------|
| **不使用 MockMvc** | Controller 测试用 `@InjectMocks` 直接调用方法 | 测试速度慢、依赖容器 |
| **不启动 Spring 容器** | 使用 `@RunWith(MockitoJUnitRunner.class)` | 测试速度慢 |
| **JUnit 4** | 使用 `org.junit.*`，所有测试类和方法必须加 `public` | 与规范不符 |
| **Mockito** | 使用 `@Mock` + `@InjectMocks` | 依赖注入不规范 |

### 2. 测试覆盖要求（强制）

| 测试类型 | 必须生成 | 模板文件 |
|---------|---------|---------|
| **ServiceTest** | ✅ 必须生成 | `test-service.java` |
| **ControllerTest** | ✅ 必须生成 | `test-controller.java` |
| **MapperTest** | ✅ 必须生成 | `test-mapper.java` |
| **DTO RequestTest** | ✅ 必须生成 | `test-dto-request.java` |
| **DTO ResponseTest** | ✅ 必须生成 | `test-dto-response.java` |
| **EnumTest** | ✅ 如有枚举必须生成 | `test-enum.java` |

### 3. 测试场景覆盖要求

| 测试类型 | 必须覆盖的场景 |
|---------|---------------|
| **ServiceTest** | 成功、失败、异常、空数据、边界条件 |
| **ControllerTest** | 成功、参数校验、异常、返回值验证 |
| **MapperTest** | 注入验证、BaseMapper 方法可用性 |
| **DTO RequestTest** | 参数校验（@Valid）、Getter/Setter |
| **DTO ResponseTest** | Getter/Setter、toString、equals、hashCode |
| **EnumTest** | 枚举值、code 转换、序列化、valueOf |

### 4. 代码模板依赖关系

```
test-controller.java (不使用 MockMvc)
├── @RunWith(MockitoJUnitRunner.class)
├── public class XxxControllerTest
├── @Mock private XxxService xxxService;
├── @InjectMocks private XxxController xxxController;
└── 直接调用方法：xxxController.method() → 验证 Result

test-service.java
├── @RunWith(MockitoJUnitRunner.class)
├── public class XxxServiceTest
├── @Mock private XxxMapper xxxMapper;
├── @InjectMocks private XxxServiceImpl xxxService;
└── 测试业务逻辑 + 异常处理

test-mapper.java
├── @RunWith(SpringRunner.class) + @SpringBootTest
├── public class XxxMapperTest
└── 执行真实数据库操作验证
```

### 5. 生成代码时的测试检查规则

**在生成单元测试代码时，必须回答：**

1. **是否使用了 MockMvc？**
   - 是 → ❌ 违反规范，改用 `@InjectMocks`
   - 否 → ✅ 符合规范

2. **是否启动了 Spring 容器？**
   - 是 → ❌ 违反规范，改用 `@RunWith(MockitoJUnitRunner.class)`（MapperTest除外，MapperTest使用 `@RunWith(SpringRunner.class)`）
   - 否 → ✅ 符合规范

3. **是否生成了全部测试类型？**
   - Service/Controller/Mapper/DTO/Enum 全部生成 → ✅ 符合规范
   - 缺少任何一种 → ❌ 违反规范

4. **测试场景是否覆盖全面？**
   - 成功/失败/异常/空数据 → ✅ 符合规范
   - 只有成功场景 → ❌ 违反规范

---

---

**【重要】pom.xml 智能处理规则**：

**首次生成**：项目不存在 pom.xml 时，生成完整文件

**后续增量**：项目已存在 pom.xml 时，执行以下逻辑：

1. **检测现有依赖** - 使用 Grep 工具分析 pom.xml，提取已有依赖
2. **对比需求** - 计算当前功能需要哪些依赖
3. **输出增量** - 仅输出需要新增或修改的部分，格式如下：

```xml
<!-- ========== 需要新增的依赖 ========== -->

<!-- 如需新增加依赖，在 <dependencies> 标签内添加： -->
<dependency>
    <groupId>xxx</groupId>
    <artifactId>xxx</artifactId>
    <version>x.x.x</version>
</dependency>

<!-- ========== 需要修改的配置 ========== -->

<!-- 如需修改 properties，在 <properties> 标签内修改： -->
<hutool.version>5.8.0</hutool.version>
```

4. **公共组件检测** - 如果 Result/异常/枚举等公共组件已存在，跳过生成

---

**【重要】application.yml 增量处理规则**：

**首次生成**：生成完整文件

**后续增量**：仅输出需要追加的配置片段

```yaml
# ========== 需要追加的配置 ==========
# 请将以下配置追加到 application.yml

# 新增模块配置
new-module:
  enabled: true
```

---

**输出内容**：
1. **代码清单** - 列出所有生成的文件（包含：实体/DTO/Mapper/Service/Controller/Converter/配置/测试/文档）
2. **API 文档** - 接口列表和说明
3. **启动步骤** - 如何运行项目

**【强制】汇报前自检**：
在向用户汇报"代码生成完成"之前，必须确认：
- ✅ 单元测试类已生成（ServiceTest + ControllerTest）
- ✅ 设计文档已生成（HLD + LLD）
- ✅ Postman测试集合已生成

**闭环**：
汇报任务终态，建议静态检查方案。

---

### [Step 6: 测试驱动的代码调优（可选）]

**触发方式**：用户主动反馈测试问题，**必须包含以下信息**：
```
需求名称: {需求标识符}
测试用例: {具体测试场景}
输入: {测试输入数据}
期望输出: {期望的结果}
实际输出: {实际的结果/错误信息}
```

**示例**：
```
需求名称: user-management
测试用例: 创建用户时邮箱重复
输入: POST /api/v1/users { "email": "test@example.com", "username": "test" }
期望输出: 400, {"code": 400, "message": "邮箱已存在"}
实际输出: 200, {"code": 200, "data": 123} (Bug)
```

**Step 6 不会自动执行，仅在用户反馈测试问题时触发。**

**动作**：
1. 根据**需求名称**定位到对应的代码包
2. 分析测试失败的原因
3. **精确定位需要修改的代码位置**（文件路径、行号、方法名）
4. **只输出需要修改的代码片段**，不重新生成全部代码
5. 告诉用户如何替换

**停止提问**：
```
请按上述说明修改代码后重新测试。
```

---

## 项目配置文件（可选）

### 创建配置文件

当用户说"生成配置文件"或"保存配置"时，创建 `.claude/project-config.json`：

```json
{
  "java": {
    "version": "8",
    "springBoot": "2.3.12.RELEASE",
    "springCloud": "Hoxton.SR12"
  },
  "database": {
    "type": "postgresql",
    "host": "localhost",
    "port": 5432,
    "database": "my_database"
  },
  "middleware": {
    "redis": true,
    "rabbitmq": false,
    "kafka": false
  },
  "package": "com.example.business",
  "projectPath": "C:\\Users\\sunyu\\java-generate-code"
}
```

### 配置文件字段说明

| 字段 | 类型 | 说明 |
|-----|------|------|
| java.version | string | JDK 版本，如 8/11/17 |
| java.springBoot | string | Spring Boot 版本 |
| java.springCloud | string | Spring Cloud 版本（可选） |
| database.type | string | postgresql/mysql |
| database.host | string | 数据库地址 |
| database.port | number | 数据库端口 |
| database.database | string | 数据库名称 |
| middleware.redis | boolean | 是否使用 Redis |
| middleware.rabbitmq | boolean | 是否使用 RabbitMQ |
| middleware.kafka | boolean | 是否使用 Kafka |
| package | string | 基础包名 |
| projectPath | string | 项目路径 |

### 配置优先级

```
.claude/project-config.json > pom.xml > build.gradle > application.yml > 用户输入
```

---

## 需求标识符规范

每个需求必须有唯一标识符，用于后续定位和修改代码：

| 规范 | 示例 | 说明 |
|-----|------|-----|
| 小写+连字符 | `user-management` | 用户管理模块 |
| 小写+连字符 | `order-service` | 订单服务 |
| 小写+连字符 | `payment-gateway` | 支付网关 |

**所有生成的代码都包含在 `{basePackage}.{moduleName}` 包下**，便于定位。

## 代码层级结构（按功能分包）

```
com.example.{module}/
├── controller/      # REST接口层
├── wrapper/         # 响应包装层
├── service/         # 业务逻辑层
│   └── impl/        # 实现类
├── repository/      # 数据访问层（Mapper接口）
├── entity/          # 数据库实体
├── dto/             # 数据传输对象
│   ├── request/     # 请求DTO
│   └── response/    # 响应DTO
├── vo/              # 视图对象
├── converter/       # 对象转换器
├── enums/           # 枚举类
├── exception/       # 异常定义
├── validator/       # 自定义校验器
├── config/          # 配置类
└── constants/       # 常量定义
```

## 技术栈

- **ORM框架**：MyBatis-Plus
- **对象转换**：MapStruct
- **API文档**：SpringDoc (OpenAPI 3.0)
- **测试框架**：JUnit 4 + Mockito
- **代码规范**：阿里巴巴Java开发手册

## 代码规范

- RESTful风格：`/api/v1/{resource}`
- 统一响应格式：`Result<T>` 包装
- 统一异常处理：`@ControllerAdvice + @ExceptionHandler`
- 日志规范：SLF4J + 合适的日志级别

## Resources

### references/
加载到上下文的参考文档：
- `code-style-redlines.md` - 三大红线规范（核心）
- `interactive-workflow-sop.md` - 6步强制交互工作流（核心）
- `ali-java-guidelines.md` - 阿里巴巴Java开发手册摘要
- `layer-patterns.md` - 分层架构最佳实践
- `naming-conventions.md` - 命名规范
- `test-patterns.md` - 单元测试编写规范
- `mybatis-plus-guide.md` - MyBatis-Plus最佳实践
- `sql-performance.md` - SQL性能规范（按数据量级选择策略、GROUP BY优化、分页优化、批量操作）
- `mapstruct-guide.md` - MapStruct对象转换指南
- `ddd-thinking.md` - DDD领域驱动设计思维

### assets/
代码模板文件（用于生成代码）：

#### commons/ - 公共组件模板（首次生成时复用，已存在则跳过）
- `commons/Result.java` - 统一响应结果包装器
- `commons/BusinessException.java` - 业务异常类
- `commons/ErrorCode.java` - 错误码枚举
- `commons/GlobalExceptionHandler.java` - 全局异常处理器
- `commons/RedisConfig.java` - Redis 配置类
- `commons/RedisConfigWithListener.java` - Redis 配置（含过期监听）
- `commons/MyBatisPlusConfig.java` - MyBatis-Plus 配置类
- `commons/SwaggerConfig.java` - Swagger 配置类
- `commons/MyMetaObjectHandler.java` - MyBatis-Plus 自动填充处理器
- `commons/PageResult.java` - 分页响应结果包装器

#### templates/ - 业务代码模板
- `templates/controller.java` - Controller模板
- `templates/service.java` - Service接口模板
- `templates/service-impl.java` - Service实现模板
- `templates/mapper.java` - MyBatis-Plus BaseMapper模板
- `templates/entity.java` - 实体类模板
- `templates/dto-request.java` - 请求DTO模板
- `templates/dto-response.java` - 响应DTO模板
- `templates/converter.java` - MapStruct对象转换器模板
- `templates/test-controller.java` - Controller测试模板（不使用MockMvc）
- `templates/test-service.java` - Service测试模板
- `templates/test-mapper.java` - Mapper测试模板
- `templates/test-dto-request.java` - 请求DTO测试模板
- `templates/test-dto-response.java` - 响应DTO测试模板
- `templates/test-enum.java` - 枚举测试模板
- `templates/pom.xml` - Maven依赖模板
- `templates/application.yml` - 应用配置模板
- `templates/design-hld.md` - 概要设计文档模板
- `templates/design-lld.md` - 详细设计文档模板
- `templates/postman-collection.json` - Postman测试集合模板
- `templates/test-data.sql` - 测试数据初始化SQL模板

---

## 公共组件使用规范

### 生成规则

**模板占位符说明**：
公共组件模板中使用以下占位符，生成时需替换：

| 占位符 | 替换值 | 示例 |
|-------|--------|-----|
| `${basePackage}` | 基础包名 | `com.example.business` |
| `${projectName}` | 项目名称 | `用户管理系统` / `订单服务` |
| `${projectDescription}` | 项目描述 | `用户管理相关功能` |
| `${generateDate}` | 生成日期 | `2025-03-27` |

**首次生成项目时**：从 `assets/commons/` 复制所有公共组件到项目，替换占位符

**后续增量生成时**：检测以下文件是否已存在，存在则跳过

| 公共组件 | 检测路径 | 说明 |
|---------|---------|------|
| Result | `{basePackage}/wrapper/Result.java` | 统一响应格式 |
| BusinessException | `{basePackage}/exception/BusinessException.java` | 业务异常 |
| ErrorCode | `{basePackage}/enums/ErrorCode.java` | 错误码枚举 |
| GlobalExceptionHandler | `{basePackage}/config/GlobalExceptionHandler.java` | 全局异常处理 |
| RedisConfig | `{basePackage}/config/RedisConfig.java` | Redis 配置 |
| MyBatisPlusConfig | `{basePackage}/config/MyBatisPlusConfig.java` | 分页配置 |
| SwaggerConfig | `{basePackage}/config/SwaggerConfig.java` | API 文档配置 |
| MyMetaObjectHandler | `{basePackage}/config/MyMetaObjectHandler.java` | 自动填充 |
| PageResult | `{basePackage}/wrapper/PageResult.java` | 分页包装 |

### 代码示例（参考 commons/ 目录）

所有公共组件模板位于 `~/.claude/skills/java-code-generator-sop/assets/commons/` 目录，生成时直接使用 Read 工具读取并写入项目。

### scripts/
可执行的Python脚本：
- `generate_java_code.py` - 核心代码生成脚本
- `generate_design_docs.py` - 设计文档生成脚本
- `generate_postman_collection.py` - Postman测试集合生成脚本
- `generate_test_data.py` - 测试数据SQL生成脚本
- `templates_config.json` - 模板配置文件
