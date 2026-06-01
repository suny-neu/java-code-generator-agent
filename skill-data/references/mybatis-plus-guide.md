# MyBatis-Plus最佳实践

## 基础配置

### 1. Maven依赖

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.5</version>
</dependency>
```

### 2. application.yml配置

```yaml
mybatis-plus:
  # 配置文件位置
  mapper-locations: classpath*:/mapper/**/*.xml

  configuration:
    # 驼峰命名转换
    map-underscore-to-camel-case: true
    # 日志输出
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    # 开启二级缓存
    cache-enabled: true

  global-config:
    db-config:
      # 主键类型（AUTO自增）
      id-type: auto
      # 逻辑删除字段
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      # 自动填充
      insert-strategy: not_null
      update-strategy: not_null
```

---

## 实体类注解

### 1. 基本注解

```java
@Data
@TableName("t_user")  // 指定表名
public class User {

    @TableId(type = IdType.AUTO)  // 主键自增
    private Long id;

    @TableField("username")  // 指定字段名
    private String username;

    @TableField("email")
    private String email;

    @TableField(exist = false)  // 不在数据库中的字段
    private String tempField;

    @TableField(fill = FieldFill.INSERT)  // 插入时自动填充
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)  // 插入和更新时自动填充
    private LocalDateTime updateTime;

    @TableLogic  // 逻辑删除字段
    private Integer deleted;
}
```

### 2. 枚举字段处理

```java
@Getter
@AllArgsConstructor
public enum UserStatus {
    ACTIVE(1, "正常"),
    INACTIVE(0, "禁用");

    private final int code;
    private final String desc;
}

// 实体类中使用
@TableField(value = "status", typeHandler = MybatisPlusEnumTypeHandler.class)
private UserStatus status;
```

---

## Mapper接口

### 1. 继承BaseMapper

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 自动获得基础CRUD方法

    // 自定义方法
    User selectByEmail(String email);

    List<User> selectByStatus(Integer status);
}
```

### 2. BaseMapper提供的方法

```java
// 插入
int insert(T entity);

// 删除
int deleteById(Serializable id);
int deleteByMap(Map<String, Object> columnMap);
int delete(Wrapper<T> wrapper);
int deleteBatchIds(Collection<? extends Serializable> idList);

// 更新
int updateById(T entity);
int update(T entity, Wrapper<T> wrapper);

// 查询
T selectById(Serializable id);
List<T> selectBatchIds(Collection<? extends Serializable> idList);
List<T> selectByMap(Map<String, Object> columnMap);
T selectOne(Wrapper<T> wrapper);
Integer selectCount(Wrapper<T> wrapper);
List<T> selectList(Wrapper<T> wrapper>);
List<Map<String, Object>> selectMaps(Wrapper<T> wrapper);
Page<T> selectPage(Page<T> page, Wrapper<T> wrapper);
```

---

## 条件构造器

### 1. LambdaQueryWrapper（推荐）

```java
// 查询邮箱为test@example.com的用户
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getEmail, "test@example.com");
User user = userMapper.selectOne(wrapper);

// 复杂条件查询
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getStatus, UserStatus.ACTIVE)
       .like(User::getUsername, "张")
       .ge(User::getCreateTime, LocalDateTime.now().minusDays(7))
       .orderByDesc(User::getCreateTime);
List<User> users = userMapper.selectList(wrapper);
```

### 2. 条件方法

| 方法 | 说明 | 示例 |
|-----|------|-----|
| eq | 等于 | `wrapper.eq(User::getId, 1L)` |
| ne | 不等于 | `wrapper.ne(User::getStatus, 0)` |
| gt | 大于 | `wrapper.gt(User::getAge, 18)` |
| ge | 大于等于 | `wrapper.ge(User::getAge, 18)` |
| lt | 小于 | `wrapper.lt(User::getAge, 60)` |
| le | 小于等于 | `wrapper.le(User::getAge, 60)` |
| like | 模糊查询 | `wrapper.like(User::getName, "张")` |
| notLike | 不包含 | `wrapper.notLike(User::getName, "李")` |
| in | IN查询 | `wrapper.in(User::getId, Arrays.asList(1L, 2L))` |
| notIn | NOT IN | `wrapper.notIn(User::getId, Arrays.asList(1L))` |
| between | BETWEEN | `wrapper.between(User::getAge, 18, 60)` |
| isNull | IS NULL | `wrapper.isNull(User::getPhone)` |
| isNotNull | IS NOT NULL | `wrapper.isNotNull(User::getPhone)` |
| orderByAsc | 升序 | `wrapper.orderByAsc(User::getCreateTime)` |
| orderByDesc | 降序 | `wrapper.orderByDesc(User::getCreateTime)` |

### 3. 条件拼接

```java
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getStatus, UserStatus.ACTIVE)
       .and(w -> w.eq(User::getEmail, "test@example.com")
                  .or()
                  .eq(User::getPhone, "13800138000"))
       .or(w -> w.eq(User::getStatus, UserStatus.LOCKED)
                  .ge(User::getCreateTime, LocalDateTime.now().minusDays(30)));
```

---

## 分页查询

### 1. 配置分页插件

```java
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

### 2. 使用分页

```java
// 创建分页对象
Page<User> page = new Page<>(current, size);

// 创建条件构造器
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getStatus, UserStatus.ACTIVE)
       .orderByDesc(User::getCreateTime);

// 执行分页查询
Page<User> result = userMapper.selectPage(page, wrapper);

// 获取结果
List<User> records = result.getRecords();    // 数据列表
long total = result.getTotal();               // 总记录数
long pages = result.getPages();               // 总页数
```

### 3. 分页DTO

```java
@Data
public class PageRequest {
    @Min(value = 1, message = "页码最小为1")
    private Integer current = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private Integer size = 10;
}
```

---

## Service层封装

### 1. 继承IService

```java
public interface UserService extends IService<User> {
    // 自定义方法
    User getByEmail(String email);
}

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User getByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return getOne(wrapper);
    }
}
```

### 2. IService提供的方法

```java
// 插入
boolean save(T entity);
boolean saveBatch(Collection<T> entityList);

// 删除
boolean removeById(Serializable id);
boolean removeByMap(Map<String, Object> columnMap);
boolean remove(Wrapper<T> wrapper);

// 更新
boolean updateById(T entity);
boolean update(T entity, Wrapper<T> wrapper);
boolean updateBatchById(Collection<T> entityList);

// 查询
T getById(Serializable id);
List<T> list();
List<T> list(Wrapper<T> wrapper);
long count();
Page<T> page(Page<T> page);
```

---

## 逻辑删除

### 1. 配置逻辑删除

```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted  # 逻辑删除字段
      logic-delete-value: 1        # 删除后的值
      logic-not-delete-value: 0    # 未删除的值
```

### 2. 实体类配置

```java
@TableLogic
private Integer deleted;
```

### 3. 使用逻辑删除

```java
// 删除操作会自动更新deleted字段
userMapper.deleteById(1L);

// 查询操作会自动过滤deleted=1的记录
userMapper.selectList(null);  // 自动添加 WHERE deleted = 0
```

---

## 自动填充

### 1. 配置MetaObjectHandler

```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

### 2. 实体类配置

```java
@TableField(fill = FieldFill.INSERT)
private LocalDateTime createTime;

@TableField(fill = FieldFill.INSERT_UPDATE)
private LocalDateTime updateTime;
```

---

## 最佳实践

### 1. 避免全表查询

```java
// ❌ 错误：全表查询
List<User> users = userMapper.selectList(null);

// ✅ 正确：带条件查询
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getStatus, UserStatus.ACTIVE);
List<User> users = userMapper.selectList(wrapper);
```

### 2. 批量操作

```java
// ✅ 使用批量插入
List<User> users = Arrays.asList(user1, user2, user3);
userService.saveBatch(users);

// ✅ 使用批量更新
userService.updateBatchById(users);
```

### 3. 事务中使用

```java
@Transactional(rollbackFor = Exception.class)
public void createOrderWithItems(Order order, List<OrderItem> items) {
    // 插入订单
    orderMapper.insert(order);

    // 插入订单明细
    items.forEach(item -> {
        item.setOrderId(order.getId());
        orderItemMapper.insert(item);
    });
}
```

### 4. 性能优化

```java
// ✅ 只查询需要的字段
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.select(User::getId, User::getName, User::getEmail);
wrapper.eq(User::getStatus, UserStatus.ACTIVE);
List<User> users = userMapper.selectList(wrapper);

// ✅ 使用分页避免一次性加载大量数据
Page<User> page = new Page<>(1, 100);
Page<User> result = userMapper.selectPage(page, wrapper);
```
