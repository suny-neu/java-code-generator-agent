# 单元测试编写规范

## 测试框架

- **JUnit 4**
- **Mockito** (Mock框架)
- **Spring Boot Test** (集成测试)

---

## 测试命名规范

### 1. 测试类命名

```
{被测类名}Test

示例:
- UserServiceTest
- UserControllerTest
- OrderServiceTest
```

### 2. 测试方法命名

```
test{方法名}_{场景}

示例:
- testCreateUser_Success
- testGetUserById_NotFound
- testCreateUser_DatabaseException
```

---

## 测试结构

### 1. AAA模式（Arrange-Act-Assert）

```java
@Test
public void testGetUserById_Success() {
    // Arrange（准备）
    Long userId = 1L;
    User user = new User();
    user.setId(userId);
    user.setName("张三");
    when(userMapper.selectById(userId)).thenReturn(user);

    // Act（执行）
    UserResponse response = userService.getUserById(userId);

    // Assert（断言）
    assertNotNull(response);
    assertEquals(userId, response.getId());
    assertEquals("张三", response.getName());
}
```

### 2. Given-When-Then模式

```java
@Test
public void testGetUserById_Success() {
    // Given
    Long userId = 1L;
    User user = new User(userId, "张三");
    given(userMapper.selectById(userId)).willReturn(user);

    // When
    UserResponse response = userService.getUserById(userId);

    // Then
    assertNotNull(response);
    assertEquals(userId, response.getId());
}
```

---

## Service层测试

### 1. 基本结构

```java
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserConverter userConverter;

    @InjectMocks
    private UserServiceImpl userService;

    @Before
    public void setUp() {
        // 初始化测试数据
    }

    @Test
    public void testCreateUser_Success() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setName("张三");
        request.setEmail("zhangsan@example.com");

        User user = new User();
        user.setId(1L);
        user.setName("张三");

        when(userConverter.toEntity(request)).thenReturn(user);
        when(userMapper.insert(any(User.class))).thenReturn(1);
        when(userMapper.selectByEmail(anyString())).thenReturn(null);

        // When
        Long userId = userService.createUser(request);

        // Then
        assertEquals(Long.valueOf(1L), userId);
        verify(userMapper).insert(any(User.class));
    }

    @Test
    public void testCreateUser_EmailAlreadyExists() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("zhangsan@example.com");

        User existingUser = new User();
        existingUser.setId(1L);

        when(userMapper.selectByEmail("zhangsan@example.com")).thenReturn(existingUser);

        // When & Then
        try {
            userService.createUser(request);
            fail("应该抛出BusinessException");
        } catch (BusinessException e) {
            assertTrue(e.getMessage().contains("邮箱已存在"));
        }
    }
}
```

### 2. Mock使用规范

```java
// ✅ 正确：使用any()匹配任意参数
when(userMapper.selectById(any(Long.class))).thenReturn(user);
when(userMapper.insert(any(User.class))).thenReturn(1);

// ✅ 正确：使用eq()匹配特定参数
when(userMapper.selectByEmail(eq("test@example.com"))).thenReturn(user);

// ❌ 错误：混用any()和具体值
when(userMapper.selectById(any(Long.class))).thenReturn(user);
when(userMapper.selectByEmail("test@example.com")).thenReturn(user);  // 不一致
```

### 3. 验证Mock调用

```java
// 验证方法被调用
verify(userMapper).insert(any(User.class));

// 验证方法从未被调用
verify(userMapper, never()).deleteById(any(Long.class));

// 验证方法被调用指定次数
verify(userMapper, times(1)).insert(any(User.class));
verify(userMapper, atLeastOnce()).selectById(any(Long.class));
```

---

## Controller层测试

### 1. 不使用MockMvc（规范要求）

```java
@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Before
    public void setUp() {
        // 初始化测试数据
    }

    @Test
    public void testGetUserById_Success() {
        // Given
        Long userId = 1L;
        UserResponse response = new UserResponse();
        response.setId(userId);
        response.setName("张三");

        when(userService.getUserById(userId)).thenReturn(response);

        // When
        Result<UserResponse> result = userController.getUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(userId, result.getData().getId());
    }

    @Test
    public void testCreateUser_Success() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setName("张三");
        request.setEmail("zhangsan@example.com");

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(1L);

        // When
        Result<Long> result = userController.createUser(request);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(Long.valueOf(1L), result.getData());
    }

    @Test(expected = RuntimeException.class)
    public void testCreateUser_ServiceException() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        when(userService.createUser(any(UserCreateRequest.class)))
            .thenThrow(new RuntimeException("数据库异常"));

        // When
        userController.createUser(request);
    }
}
```

---

## Repository层测试

### 1. 使用SpringRunner + SpringBootTest

```java
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Before
    public void setUp() {
        // 准备测试数据
    }

    @Test
    @Transactional
    public void testInsert() {
        // Given
        User user = new User();
        user.setName("张三");
        user.setEmail("zhangsan@example.com");

        // When
        int rows = userMapper.insert(user);

        // Then
        assertTrue(rows > 0);
        assertNotNull(user.getId());
    }

    @Test
    @Transactional
    public void testSelectById() {
        // Given
        User user = new User();
        user.setName("张三");
        user.setEmail("zhangsan@example.com");
        userMapper.insert(user);

        // When
        User foundUser = userMapper.selectById(user.getId());

        // Then
        assertNotNull(foundUser);
        assertEquals("张三", foundUser.getName());
    }
}
```

---

## 测试覆盖率

### 1. 覆盖率目标

| 层级 | 覆盖率目标 |
|-----|----------|
| Service | ≥ 80% |
| Controller | ≥ 70% |
| Repository | ≥ 60% |

### 2. 覆盖率命令

```bash
# Maven
mvn clean test jacoco:report

# Gradle
./gradlew test jacocoTestReport
```

---

## 常用断言（JUnit 4）

### 1. 对象断言

```java
// 对象相等
assertEquals(expected, actual);

// 对象不等
assertFalse(expected.equals(actual));

// 对象为null
 assertNull(actual);

// 对象不为null
assertNotNull(actual);

// 对象是某个类的实例
assertTrue(actual instanceof User.class);
```

### 2. 布尔断言

```java
// 为true
assertTrue(condition);

// 为false
assertFalse(condition);
```

### 3. 集合断言

```java
// 集合为空
assertTrue(list.isEmpty());

// 集合不为空
assertFalse(list.isEmpty());

// 集合大小
assertEquals(3, list.size());

// 集合包含元素
assertTrue(list.contains(user));
```

### 4. 数字断言

```java
// 相等
assertEquals(expected, actual);

// 大于
assertTrue(actual > expected);

// 小于
assertTrue(actual < expected);
```

### 5. 字符串断言

```java
// 相等
assertEquals(expected, actual);

// 包含
assertTrue(actual.contains("substring"));

// 为空
assertTrue(actual.isEmpty());
```

---

## 异常测试

### 1. 使用 @Test(expected = ...)

```java
@Test(expected = BusinessException.class)
public void testGetUserById_NotFound() {
    // Given
    Long userId = 999L;
    when(userMapper.selectById(userId)).thenReturn(null);

    // When
    userService.getUserById(userId);
}
```

### 2. 使用 try/catch + fail()

```java
@Test
public void testCreateUser_EmailAlreadyExists() {
    // Given
    UserCreateRequest request = new UserCreateRequest();
    request.setEmail("test@example.com");

    User existingUser = new User();
    when(userMapper.selectByEmail(anyString())).thenReturn(existingUser);

    // When & Then
    try {
        userService.createUser(request);
        fail("应该抛出BusinessException");
    } catch (BusinessException e) {
        assertTrue(e.getMessage().contains("邮箱已存在"));
        assertEquals(ErrorCode.DUPLICATE.getCode(), e.getCode());
    }
}
```

---

## 测试最佳实践

### 1. 单元测试原则

- **独立性**: 每个测试互不影响
- **可重复性**: 多次运行结果一致
- **快速性**: 单元测试应该快速执行
- **可读性**: 测试代码应该清晰易懂

### 2. 测试隔离

```java
// ✅ 正确：每个测试独立
@Before
public void setUp() {
    // 初始化测试数据
}

@After
public void tearDown() {
    // 清理测试数据
}

// ❌ 错误：测试之间有依赖关系
```

### 3. 避免测试私有方法

```java
// ❌ 不要测试私有方法
// ✅ 通过公共方法测试私有方法的行为
```

### 4. 所有测试类和方法必须加 public

```java
// ✅ 正确
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {
    @Test
    public void testCreateUser_Success() {
        // ...
    }
}

// ❌ 错误：缺少 public
class UserServiceTest {
    @Test
    void testCreateUser_Success() {
        // ...
    }
}
```

---

## 测试检查清单

- [ ] 测试类命名正确（{ClassName}Test）
- [ ] 测试类和方法都有 `public` 修饰符
- [ ] 使用 JUnit 4（`@Test` 来自 `org.junit.Test`）
- [ ] 使用 `@RunWith(MockitoJUnitRunner.class)`（Service/Controller）
- [ ] 使用 `@RunWith(SpringRunner.class)`（Mapper）
- [ ] 使用AAA或Given-When-Then结构
- [ ] 测试独立，无副作用
- [ ] 断言清晰且充分
- [ ] 异常测试覆盖异常场景
- [ ] 边界条件测试覆盖
- [ ] Mock使用正确
- [ ] 测试覆盖率达标
