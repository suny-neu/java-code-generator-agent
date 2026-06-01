# SQL 性能规范

## 1. 按数据量级选择查询策略

### S 级（< 1万）— 简单写法

直接使用 MyBatis-Plus 的 `LambdaQueryWrapper`，无需特殊优化。

```java
// S级：简单 CRUD 足够
@Override
public PageResult<UserResponse> listUsers(Integer current, Integer size) {
    Page<User> page = new Page<>(current, size);
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(User::getStatus, 1);
    Page<User> result = userMapper.selectPage(page, wrapper);
    return PageResult.of(result, userConverter.toResponseList(result.getRecords()));
}
```

### M 级（1万 ~ 50万）— 索引 + 分页 + 避免 SELECT *

```java
// M级：必须加索引、分页、只查必要字段
// 1. Entity 中排除大字段
@TableField(select = false)
private String content;  // TEXT/BLOB 列表不查

// 2. 分页查询只查必要字段
@Override
public PageResult<UserResponse> listUsers(Integer current, Integer size) {
    Page<User> page = new Page<>(current, Math.min(size, MAX_PAGE_SIZE));
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    wrapper.select(User::getId, User::getName, User::getStatus)  // 只查3个字段
           .eq(User::getStatus, 1)
           .orderByDesc(User::getCreateTime);
    Page<User> result = userMapper.selectPage(page, wrapper);
    return PageResult.of(result, userConverter.toResponseList(result.getRecords()));
}
```

### L 级（50万 ~ 500万）— 优化 SQL + 预聚合

```java
// L级：手写 SQL，避免深分页，使用游标或 WHERE id > lastId

// Mapper XML 中的优化查询
// <select id="selectPageOptimized" resultType="com.xxx.entity.OrderDO">
//     SELECT id, order_no, amount, status, create_time
//     FROM t_order
//     WHERE id > #{lastId}
//       AND create_time >= #{startTime}
//       AND create_time < #{endTime}
//     ORDER BY id ASC
//     LIMIT #{pageSize}
// </select>

// Service 中使用优化分页
@Override
public List<OrderResponse> listOrdersOptimized(Long lastId, Integer pageSize) {
    List<Order> orders = orderMapper.selectPageOptimized(
        lastId, startTime, endTime, Math.min(pageSize, 500)
    );
    return orderConverter.toResponseList(orders);
}
```

### XL 级（> 500万）— 预聚合表 / ES / 分表

```java
// XL级：不实时查原始表，查预聚合结果

// 1. 预聚合表设计
// CREATE TABLE t_order_daily_stat (
//     stat_date DATE NOT NULL,
//     status TINYINT NOT NULL,
//     total_count BIGINT DEFAULT 0,
//     total_amount DECIMAL(15,2) DEFAULT 0,
//     PRIMARY KEY (stat_date, status)
// );

// 2. 定时任务每日汇总
@Scheduled(cron = "0 5 0 * * ?")
public void dailyAggregation() {
    orderStatMapper.aggregateYesterday();
}

// 3. 查询直接读汇总表
@Override
public OrderStatResponse getDailyStat(LocalDate date) {
    return orderStatMapper.selectByDate(date);
}
```

---

## 2. GROUP BY 聚合查询规范

### 2.1 索引设计（强制）

GROUP BY 查询**必须**有对应的联合索引，索引顺序 = GROUP BY 字段 + WHERE 过滤字段：

```sql
-- 错误：没有索引，全表扫描
SELECT status, COUNT(*) FROM t_order GROUP BY status;

-- 正确：创建索引
CREATE INDEX idx_status_create_time ON t_order(status, create_time);

-- 正确：WHERE 先过滤再 GROUP BY
SELECT status, COUNT(*) AS cnt, SUM(amount) AS total
FROM t_order
WHERE create_time >= '2024-01-01'
  AND create_time < '2024-02-01'
GROUP BY status;
```

### 2.2 不同量级的 GROUP BY 写法

**S/M 级（< 50万）— 直接 GROUP BY**

```java
// Mapper 接口
List<OrderStatDTO> selectGroupByStatus(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);
```

```xml
<!-- Mapper XML -->
<select id="selectGroupByStatus" resultType="com.xxx.dto.OrderStatDTO">
    SELECT status, COUNT(*) AS cnt, SUM(amount) AS total_amount
    FROM t_order
    WHERE create_time >= #{startTime}
      AND create_time < #{endTime}
    GROUP BY status
</select>
```

**L 级（50万 ~ 500万）— WHERE 严格过滤 + 覆盖索引**

```xml
<!-- 必须加时间范围过滤，避免全表聚合 -->
<select id="selectGroupByStatusOptimized" resultType="com.xxx.dto.OrderStatDTO">
    SELECT status, COUNT(*) AS cnt, SUM(amount) AS total_amount
    FROM t_order
    WHERE create_time >= #{startTime}
      AND create_time < #{endTime}
      AND status IN (1, 2, 3)
    GROUP BY status
    ORDER BY NULL
</select>
```

```sql
-- 覆盖索引：查询字段都在索引中，不需要回表
CREATE INDEX idx_status_time_amount ON t_order(status, create_time, amount);
```

**XL 级（> 500万）— 预聚合表**

```sql
-- 预聚合表
CREATE TABLE t_order_daily_stat (
    stat_date DATE NOT NULL COMMENT '统计日期',
    status TINYINT NOT NULL COMMENT '订单状态',
    total_count BIGINT DEFAULT 0 COMMENT '数量',
    total_amount DECIMAL(15,2) DEFAULT 0 COMMENT '金额',
    PRIMARY KEY (stat_date, status),
    INDEX idx_stat_date (stat_date)
) COMMENT '订单日统计汇总表';

-- 定时聚合 SQL（每天凌晨执行）
INSERT INTO t_order_daily_stat (stat_date, status, total_count, total_amount)
SELECT DATE(create_time), status, COUNT(*), SUM(amount)
FROM t_order
WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 1 DAY)
  AND create_time < CURDATE()
GROUP BY DATE(create_time), status
ON DUPLICATE KEY UPDATE
    total_count = VALUES(total_count),
    total_amount = VALUES(total_amount);

-- 查询直接读汇总表（毫秒级）
SELECT stat_date, status, total_count, total_amount
FROM t_order_daily_stat
WHERE stat_date BETWEEN #{startDate} AND #{endDate}
ORDER BY stat_date;
```

### 2.3 GROUP BY 常见性能陷阱

| 陷阱 | 后果 | 解决方案 |
|------|------|---------|
| GROUP BY 字段无索引 | 全表扫描 + 临时表 + filesort | 创建联合索引 |
| 先 GROUP BY 再 WHERE | 聚合全表数据 | WHERE 必须在 GROUP BY 之前 |
| `SELECT *` + GROUP BY | 回表查大量无用字段 | 只 SELECT 聚合字段和 GROUP BY 字段 |
| GROUP BY + ORDER BY 无索引 | 额外排序开销 | 不需要排序时加 `ORDER BY NULL`（MySQL） |
| 大表实时 GROUP BY | 查询超时 | 用预聚合表代替实时聚合 |
| HAVING 过滤聚合结果 | 全量聚合后才过滤 | 尽量用 WHERE 在聚合前过滤 |

---

## 3. 分页查询规范

### 3.1 分页安全上限（强制）

所有分页接口**必须**限制每页最大条数：

```java
// Controller 中强制限制
private static final int MAX_PAGE_SIZE = 100;

@GetMapping
public Result<PageResult<XxxResponse>> list(
        @RequestParam(defaultValue = "1") Integer current,
        @RequestParam(defaultValue = "10") Integer size) {
    size = Math.min(size, MAX_PAGE_SIZE);
    return Result.success(xxxService.listPage(current, size));
}
```

### 3.2 深分页优化

传统 `LIMIT offset, size` 在 offset 很大时性能极差：

```sql
-- ❌ 深分页（offset=100万）：扫描 100万+10 条，丢弃前 100万条
SELECT * FROM t_order ORDER BY id LIMIT 1000000, 10;

-- ✅ 游标分页（WHERE id > lastId）：只扫描 10 条
SELECT * FROM t_order WHERE id > #{lastId} ORDER BY id LIMIT 10;

-- ✅ 延迟关联（子查询只查主键，再回表）
SELECT o.* FROM t_order o
INNER JOIN (
    SELECT id FROM t_order ORDER BY id LIMIT 1000000, 10
) t ON o.id = t.id;
```

**M 级以上**数据量必须提供游标分页接口：

```java
// Mapper 接口
List<XxxDO> selectByCursor(@Param("lastId") Long lastId,
                            @Param("pageSize") Integer pageSize);

// Mapper XML
// <select id="selectByCursor" resultType="com.xxx.entity.XxxDO">
//     SELECT id, field1, field2
//     FROM t_xxx
//     WHERE id > #{lastId}
//     ORDER BY id ASC
//     LIMIT #{pageSize}
// </select>
```

---

## 4. 批量操作规范

### 4.1 批量插入

```java
// ❌ 循环单条插入（几千条数据要几分钟）
for (XxxCreateRequest req : requestList) {
    XxxDO entity = converter.toEntity(req);
    xxxMapper.insert(entity);
}

// ✅ 分批插入（每批 500 条）
private static final int BATCH_SIZE = 500;

@Override
@Transactional(rollbackFor = Exception.class)
public void batchCreate(List<XxxCreateRequest> requestList) {
    List<XxxDO> entities = requestList.stream()
        .map(converter::toEntity)
        .collect(Collectors.toList());

    // 分批插入
    List<List<XxxDO>> batches = Lists.partition(entities, BATCH_SIZE);
    for (List<XxxDO> batch : batches) {
        xxxMapper.insertBatchSomeColumn(batch);
        // 或使用 IService.saveBatch(batch, BATCH_SIZE)
    }
}
```

### 4.2 批量查询（IN 查询限制）

```java
// ❌ IN 查询无限制（可能传几万个ID）
List<XxxDO> list = xxxMapper.selectBatchIds(idList);

// ✅ IN 查询分批，每批不超过 1000
private static final int IN_QUERY_BATCH_SIZE = 1000;

public List<XxxDO> selectByIds(List<Long> ids) {
    if (ids.isEmpty()) {
        return Collections.emptyList();
    }
    List<List<Long>> batches = Lists.partition(ids, IN_QUERY_BATCH_SIZE);
    return batches.stream()
        .flatMap(batch -> xxxMapper.selectBatchIds(batch).stream())
        .collect(Collectors.toList());
}
```

### 4.3 批量更新

```java
// ❌ 循环单条更新
for (XxxDO entity : entities) {
    xxxMapper.updateById(entity);
}

// ✅ MySQL CASE WHEN 批量更新
// <update id="batchUpdateStatus">
//     UPDATE t_xxx
//     SET status =
//         CASE id
//             <foreach collection="list" item="item">
//                 WHEN #{item.id} THEN #{item.status}
//             </foreach>
//         END
//     WHERE id IN
//     <foreach collection="list" item="item" open="(" separator="," close=")">
//         #{item.id}
//     </foreach>
// </update>
```

---

## 5. SELECT 字段优化

### 5.1 列表查询只查必要字段

```java
// ❌ SELECT * 查所有字段（含 TEXT/BLOB）
Page<XxxDO> result = xxxMapper.selectPage(page, wrapper);

// ✅ 只查列表需要的字段
LambdaQueryWrapper<XxxDO> wrapper = new LambdaQueryWrapper<>();
wrapper.select(XxxDO::getId, XxxDO::getName, XxxDO::getStatus, XxxDO::getCreateTime)
       .orderByDesc(XxxDO::getCreateTime);
```

### 5.2 大字段排除

```java
// Entity 中标记列表不查的大字段
@Data
@TableName("t_article")
public class ArticleDO {
    private Long id;
    private String title;

    @TableField(select = false)  // 列表查询自动排除
    private String content;       // TEXT 类型，列表不需要
}
```

---

## 6. Step 5 代码生成时的 SQL 性能检查清单

在 Step 5 生成代码时，**必须**根据 Step 2 评估的量级执行以下检查：

- [ ] **分页上限**：所有分页接口是否限制 `MAX_PAGE_SIZE`？
- [ ] **SELECT 字段**：列表查询是否只查必要字段？是否排除了大字段？
- [ ] **GROUP BY 索引**：聚合查询字段是否有联合索引？
- [ ] **深分页**：M 级以上是否提供了游标分页？
- [ ] **批量操作**：批量插入是否分批提交（每批 500）？
- [ ] **IN 查询**：是否限制 IN 查询每批不超过 1000？
- [ ] **预聚合表**：L/XL 级的聚合统计是否设计了预聚合表？
- [ ] **WHERE 过滤**：GROUP BY 前是否有 WHERE 时间范围过滤？
