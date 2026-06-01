# Code Style - 三大红线规范

## 概述

本规范定义Java代码生成的三大红线，违反任何一条都会导致严重的生产问题。代码生成时必须严格遵守。

---

## 红线一：异常红线

### 规则
**禁止生吞异常，必须记录日志或抛出业务异常。**

### 违规后果
生产问题难以排查，无法定位错误源头。

### 错误示例
```java
// ❌ 错误：生吞异常
public void createUser(UserRequest request) {
    try {
        // 业务逻辑
    } catch (Exception e) {
        // 什么都不做，异常被生吞
    }
}

// ❌ 错误：只打印堆栈但不处理
public void createUser(UserRequest request) {
    try {
        // 业务逻辑
    } catch (Exception e) {
        e.printStackTrace(); // 生产环境不应该使用printStackTrace
    }
}
```

### 正确示例
```java
// ✅ 正确：记录日志并抛出业务异常
public Long createUser(UserRequest request) {
    try {
        // 业务逻辑
        return userId;
    } catch (DuplicateKeyException e) {
        log.error("用户创建失败，邮箱已存在: {}", request.getEmail(), e);
        throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, "邮箱已存在");
    } catch (Exception e) {
        log.error("用户创建失败，未知错误: {}", request, e);
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统异常，请稍后重试");
    }
}

// ✅ 正确：使用 @Slf4j 注解
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    public Long createUser(UserRequest request) {
        // 业务逻辑
    }
}
```

### 日志级别规范
| 级别 | 使用场景 | 示例 |
|-----|---------|------|
| ERROR | 系统错误、需要立即处理的异常 | 数据库连接失败、关键业务异常 |
| WARN | 潜在问题、可恢复的异常 | 重试操作、降级处理 |
| INFO | 关键业务流程、重要状态变更 | 用户创建成功、订单状态变更 |
| DEBUG | 调试信息、详细的执行流程 | 方法入参、中间变量值 |

---

## 红线二：事务红线

### 规则
**多表写操作必须检查 `@Transactional`。**

### 违规后果
数据一致性问题，部分更新成功部分失败。

### 错误示例
```java
// ❌ 错误：多表写操作没有事务注解
public void createOrderWithItems(OrderRequest request) {
    // 写入订单表
    orderMapper.insert(order);
    // 写入订单项表 - 如果这里失败，订单表已经插入，数据不一致
    orderItemMapper.insertBatch(orderItems);
}
```

### 正确示例
```java
// ✅ 正确：添加 @Transactional 注解
@Transactional(rollbackFor = Exception.class)
public void createOrderWithItems(OrderRequest request) {
    // 写入订单表
    orderMapper.insert(order);
    // 写入订单项表
    orderItemMapper.insertBatch(orderItems);
}
```

### 事务注解规范
```java
// 标准写法：明确指定回滚异常类型
@Transactional(rollbackFor = Exception.class)

// 只读事务：查询方法使用
@Transactional(readOnly = true)

// 指定事务传播行为
@Transactional(propagation = Propagation.REQUIRES_NEW)

// 指定事务隔离级别
@Transactional(isolation = Isolation.READ_COMMITTED)
```

### 需要添加事务的场景
- 多表写操作（INSERT/UPDATE/DELETE）
- 调用多个外部API需要保证一致性
- 涉及分布式锁的操作

### 不需要事务的场景
- 单表单条写操作
- 只读查询（建议使用 `@Transactional(readOnly = true)` 提升性能）

---

## 红线三：异味红线

### 规则
- 方法不超过 **50 行**
- 参数不超过 **4 个**
- 嵌套不超过 **3 层**

### 违规后果
代码可维护性差，难以理解和测试。

### 错误示例

#### 方法过长
```java
// ❌ 错误：方法超过50行
public void processOrder(OrderRequest request) {
    // 100+ 行代码...
}
```

#### 参数过多
```java
// ❌ 错误：参数超过4个
public User createUser(String name, String email, String phone,
                       String address, String city, String country) {
    // ...
}
```

#### 嵌套过深
```java
// ❌ 错误：嵌套超过3层
public void processOrder(Order order) {
    if (order != null) {
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    if (item.getProduct().getStock() > 0) { // 4层嵌套
                        // ...
                    }
                }
            }
        }
    }
}
```

### 正确示例

#### 方法拆分
```java
// ✅ 正确：拆分为多个小方法
public void processOrder(OrderRequest request) {
    validateOrder(request);
    Order order = buildOrder(request);
    saveOrder(order);
    processPayment(order);
    updateInventory(order);
}

private void validateOrder(OrderRequest request) {
    // 验证逻辑
}

private Order buildOrder(OrderRequest request) {
    // 构建订单
}
```

#### 参数对象封装
```java
// ✅ 正确：使用DTO封装参数
public User createUser(CreateUserRequest request) {
    // request 包含所有需要的字段
}

@Data
public class CreateUserRequest {
    private String name;
    private String email;
    private String phone;
    private Address address;
}
```

#### 提前返回
```java
// ✅ 正确：使用提前返回减少嵌套
public void processOrder(Order order) {
    if (order == null) {
        return;
    }
    if (order.getItems() == null || order.getItems().isEmpty()) {
        return;
    }
    for (OrderItem item : order.getItems()) {
        processOrderItem(item);
    }
}
```

---

## 代码质量检查清单

在生成代码时，确保以下检查项全部通过：

### 异常处理
- [ ] 无空 catch 块
- [ ] 异常信息包含足够的上下文
- [ ] 日志级别使用正确（ERROR/WARN/INFO/DEBUG）
- [ ] 业务异常使用自定义异常类

### 事务管理
- [ ] 多表写操作有 `@Transactional`
- [ ] 事务注解指定 `rollbackFor = Exception.class`
- [ ] 只读查询使用 `readOnly = true`

### 代码结构
- [ ] 方法行数 ≤ 50
- [ ] 方法参数 ≤ 4
- [ ] 嵌套层级 ≤ 3
- [ ] 单一职责原则
- [ ] 代码重复率低

---

## 常见反模式识别

### 1. 神教数字
```java
// ❌ 错误
if (user.getStatus() == 1) { }

// ✅ 正确
if (user.getStatus() == UserStatus.ACTIVE.getCode()) { }
```

### 2. 过度使用null
```java
// ❌ 错误
public User getUser(String id) {
    return userMapper.selectById(id); // 可能返回null
}

// ✅ 正确
public User getUser(String id) {
    User user = userMapper.selectById(id);
    if (user == null) {
        throw new NotFoundException("用户不存在");
    }
    return user;
}
```

### 3. 字符串比较
```java
// ❌ 错误
if (status.equals("ACTIVE")) { }

// ✅ 正确
if (UserStatus.ACTIVE.name().equals(status)) { }
```
