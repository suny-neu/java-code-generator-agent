# 阿里巴巴Java开发手册核心规范摘要

## 命名规范

### 1. 类命名
- **驼峰式**，首字母大写：`UserService`, `OrderController`
- **DTO类**: 以DTO结尾：`UserDTO`, `OrderResponseDTO`
- **VO类**: 以VO结尾：`UserVO`
- **Entity类**: 实体名称：`User`, `Order`
- **工具类**: 以Util结尾：`DateUtil`, `StringUtil`
- **异常类**: 以Exception结尾：`BusinessException`, `NotFoundException`

### 2. 方法命名
- **获取单个对象**: `get + 名词`：`getUserById`, `getOrder`
- **获取列表**: `list + 名词`：`listUsers`, `listOrdersByStatus`
- **保存/新增**: `save + 名词`：`saveUser`, `saveOrder`
- **删除**: `remove + 名词`：`removeUser`, `deleteOrder`
- **更新**: `update + 名词`：`updateUser`, `updateOrder`
- **判断**: `is/has/can + 形容词`：`isValid`, `hasPermission`, `canAccess`

### 3. 变量命名
- **驼峰式**，首字母小写：`userName`, `orderStatus`
- **常量**: 全大写，下划线分隔：`MAX_COUNT`, `DEFAULT_STATUS`
- **布尔变量**: 以is开头：`isActive`, `hasPermission`

### 4. 包命名
- **全小写**，点分隔：`com.example.user.service`
- **按功能分层**：
```
com.example.{module}/
├── controller/
├── service/
│   └── impl/
├── repository/
├── entity/
├── dto/
├── vo/
├── converter/
├── enums/
├── exception/
└── config/
```

---

## 代码格式

### 1. 缩进
- **4个空格**，禁止使用Tab

### 2. 行宽
- **单行不超过120个字符**

### 3. 大括号
- **左大括号不换行**
```java
// ✅ 正确
public void method() {
    // code
}

// ❌ 错误
public void method()
{
    // code
}
```

### 4. 空格
- **运算符两侧加空格**：`a + b`, `count > 0`
- **逗号后面加空格**：`method(a, b, c)`
- **关键字后加空格**：`if (condition)`, `for (int i = 0;)`

---

## OOP规范

### 1. 单一职责
- 一个类只负责一项职责
- 方法行数不超过50行
- 参数不超过4个

### 2. 开闭原则
- 对扩展开放，对修改关闭
- 使用接口和抽象类

### 3. 里氏替换
- 子类可以替换父类
- 不要重写父类的已实现方法

### 4. 接口隔离
- 接口应该小而专
- 不要让类实现不需要的方法

### 5. 依赖倒置
- 依赖抽象而非具体实现
- 使用依赖注入

---

## 集合处理

### 1. 判断空
```java
// ✅ 正确：使用工具类
if (CollectionUtils.isEmpty(list)) {
    // 处理空集合
}

// ✅ 正确：使用Java 8+
if (list == null || list.isEmpty()) {
    // 处理空集合
}
```

### 2. 遍历
```java
// ✅ 推荐：使用增强for循环
for (User user : userList) {
    // 处理用户
}

// ✅ 推荐：使用Stream API (Java 8+)
userList.stream()
    .filter(User::isActive)
    .forEach(user -> // 处理);

// ❌ 避免：使用索引遍历（除非需要索引）
for (int i = 0; i < userList.size(); i++) {
    User user = userList.get(i);
}
```

### 3. 转换数组
```java
// ✅ 正确
List<String> list = new ArrayList<>();
String[] array = list.toArray(new String[0]);

// ❌ 错误：不要使用无参toArray
String[] array = list.toArray();
```

---

## 异常处理

### 1. 捕获异常
```java
// ✅ 正确：捕获具体异常
try {
    // code
} catch (SQLException e) {
    log.error("数据库异常", e);
    throw new BusinessException("系统异常");
}

// ❌ 错误：捕获太泛的异常
try {
    // code
} catch (Exception e) {
    // ignore
}
```

### 2. 不要生吞异常
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

### 3. 使用finally
```java
// ✅ 正确：使用try-with-resources (Java 7+)
try (InputStream is = new FileInputStream(file)) {
    // 使用流
} catch (IOException e) {
    log.error("读取文件失败", e);
}

// ✅ 正确：手动关闭资源
InputStream is = null;
try {
    is = new FileInputStream(file);
    // 使用流
} catch (IOException e) {
    log.error("读取文件失败", e);
} finally {
    if (is != null) {
        try {
            is.close();
        } catch (IOException e) {
            log.error("关闭流失败", e);
        }
    }
}
```

---

## 日志规范

### 1. 日志级别
| 级别 | 使用场景 |
|-----|---------|
| ERROR | 系统错误、需要立即处理的异常 |
| WARN | 潜在问题、可恢复的异常 |
| INFO | 关键业务流程、重要状态变更 |
| DEBUG | 调试信息、详细的执行流程 |

### 2. 日志格式
```java
// ✅ 正确：使用占位符
log.info("用户创建成功, userId={}, username={}", user.getId(), user.getUsername());

// ❌ 错误：字符串拼接
log.info("用户创建成功, userId=" + user.getId() + ", username=" + user.getUsername());
```

### 3. 异常日志
```java
// ✅ 正确：包含异常堆栈
try {
    // code
} catch (Exception e) {
    log.error("处理失败, userId={}", userId, e);
}

// ❌ 错误：不包含异常堆栈
try {
    // code
} catch (Exception e) {
    log.error("处理失败, userId={}, error={}", userId, e.getMessage());
}
```

---

## 其他规范

### 1. 避免魔法值
```java
// ❌ 错误
if (user.getStatus() == 1) {
}

// ✅ 正确
public interface UserStatus {
    int ACTIVE = 1;
    int INACTIVE = 0;
}

if (user.getStatus() == UserStatus.ACTIVE) {
}
```

### 2. 使用常量
```java
// ✅ 正确
public interface Constants {
    String DEFAULT_AVATAR = "https://example.com/default.png";
    int MAX_LOGIN_RETRY = 5;
}
```

### 3. equals判断
```java
// ❌ 错误：可能NPE
if (user.getName().equals("admin")) {
}

// ✅ 正确：使用常量调用equals
if ("admin".equals(user.getName())) {
}

// ✅ 正确：使用Objects.equals (Java 7+)
if (Objects.equals(user.getName(), "admin")) {
}
```

### 4. 字符串比较
```java
// ❌ 错误：使用==比较字符串
if (str1 == str2) {
}

// ✅ 正确：使用equals
if (str1.equals(str2)) {
}
```

### 5. NPE预防
```java
// ✅ 使用Optional (Java 8+)
public String getUsername(User user) {
    return Optional.ofNullable(user)
        .map(User::getName)
        .orElse("匿名");
}

// ✅ 使用StringUtils
if (StringUtils.isEmpty(str)) {
}
```
