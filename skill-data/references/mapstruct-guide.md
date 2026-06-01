# MapStruct对象转换指南

## 基础配置

### 1. Maven依赖

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>

<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>
```

### 2. Maven编译插件配置

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>17</source>
                <target>17</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>1.5.5.Final</version>
                    </path>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>1.18.30</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## 基本使用

### 1. 定义Converter接口

```java
@Mapper(componentModel = "spring")
public interface UserConverter {

    User toEntity(UserCreateRequest request);

    User toEntity(UserUpdateRequest request);

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}
```

### 2. 使用Converter

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserConverter userConverter;

    @Override
    public Long createUser(UserCreateRequest request) {
        // DTO → Entity
        User user = userConverter.toEntity(request);
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userMapper.selectById(id);
        // Entity → DTO
        return userConverter.toResponse(user);
    }
}
```

---

## 字段映射

### 1. 字段名不同

```java
@Data
public class User {
    private Long id;
    private String userName;
    private String emailAddress;
}

@Data
public class UserResponse {
    private Long id;
    private String username;  // 字段名不同
    private String email;     // 字段名不同
}
```

```java
@Mapper(componentModel = "spring")
public interface UserConverter {

    @Mapping(source = "userName", target = "username")
    @Mapping(source = "emailAddress", target = "email")
    UserResponse toResponse(User user);
}
```

### 2. 忽略字段

```java
@Mapping(target = "password", ignore = true)  // 忽略密码字段
UserResponse toResponse(User user);
```

### 3. 默认值

```java
@Mapping(target = "status", defaultValue = "1")
User toEntity(UserCreateRequest request);
```

### 4. 常量值

```java
@Mapping(target = "userType", constant = "NORMAL")
UserResponse toResponse(User user);
```

---

## 表达式映射

### 1. 使用表达式

```java
@Mapper(componentModel = "spring")
public interface UserConverter {

    @Mapping(target = "fullName", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    UserResponse toResponse(User user);
}
```

### 2. 使用默认表达式

```java
@Mapper(componentModel = "spring")
public interface UserConverter {

    @Mapping(target = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    UserResponse toResponse(User user);
}
```

---

## 自定义方法

### 1. 在Converter接口中定义

```java
@Mapper(componentModel = "spring")
public interface UserConverter {

    UserResponse toResponse(User user);

    default String formatStatus(Integer status) {
        if (status == null) {
            return "未知";
        }
        return status == 1 ? "正常" : "禁用";
    }
}
```

### 2. 使用@Named方法

```java
@Mapper(componentModel = "spring")
public interface UserConverter {

    @Mapping(target = "statusText", source = "status", qualifiedByName = "formatStatus")
    UserResponse toResponse(User user);

    @Named("formatStatus")
    default String formatStatus(Integer status) {
        return status == 1 ? "正常" : "禁用";
    }
}
```

---

## 集合映射

### 1. List映射

```java
List<UserResponse> toResponseList(List<User> users);
```

### 2. Set映射

```java
Set<UserResponse> toResponseSet(Set<User> users);
```

### 3. Map映射

```java
@MapMapping(valueDateFormat = "yyyy-MM-dd HH:mm:ss")
Map<String, UserResponse> toResponseMap(Map<String, User> userMap);
```

---

## 嵌套对象映射

### 1. 对象嵌套

```java
@Data
public class User {
    private Long id;
    private String name;
    private Department department;  // 嵌套对象
}

@Data
public class UserResponse {
    private Long id;
    private String name;
    private DepartmentResponse department;  // 嵌套对象
}

@Data
public class Department {
    private Long id;
    private String name;
}

@Data
public class DepartmentResponse {
    private Long id;
    private String name;
}
```

```java
@Mapper(componentModel = "spring", uses = DepartmentConverter.class)
public interface UserConverter {

    UserResponse toResponse(User user);
}

@Mapper(componentModel = "spring")
public interface DepartmentConverter {

    DepartmentResponse toResponse(Department department);
}
```

---

## 更新现有对象

### 1. @MappingTarget

```java
@Mapper(componentModel = "spring")
public interface UserConverter {

    @Mapping(target = "id", ignore = true)  // 忽略ID字段
    void updateEntity(UserUpdateRequest request, @MappingTarget User user);
}
```

使用示例：
```java
User user = userMapper.selectById(id);
userConverter.updateEntity(request, user);
userMapper.updateById(user);
```

---

## 类型转换

### 1. String ↔ Date

```java
@Mapping(target = "createTime", source = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
UserResponse toResponse(User user);
```

### 2. 数字类型转换

```java
@Mapping(target = "amount", source = "amount", numberFormat = "#.00")
OrderResponse toResponse(Order order);
```

---

## 最佳实践

### 1. Converter命名规范

```
{Entity}Converter

示例:
- UserConverter
- OrderConverter
- ProductConverter
```

### 2. 方法命名规范

```
to{Target}({Source} source)

示例:
- toEntity(UserCreateRequest request)
- toResponse(User user)
- toResponseList(List<User> users)
- updateEntity(UserUpdateRequest request, @MappingTarget User user)
```

### 3. 使用componentModel = "spring"

```java
@Mapper(componentModel = "spring")  // 生成的实现类会添加@Component注解
public interface UserConverter {
    // 可以直接使用@Autowired注入
}
```

### 4. 批量映射

```java
@Mapper(componentModel = "spring")
public interface UserConverter {

    UserResponse toResponse(User user);

    // 自动调用上面的方法处理集合
    List<UserResponse> toResponseList(List<User> users);

    Page<UserResponse> toResponsePage(Page<User> users);
}
```

### 5. 使用Lombok

```java
@Data
@FieldNameConstants  // MapStruct会使用字段常量
public class UserResponse {
    private Long id;
    private String name;
}
```

---

## 常见问题

### 1. 编译时找不到生成的实现类

**原因**：没有正确配置注解处理器

**解决**：检查Maven编译插件配置

### 2. 字段名不同导致映射失败

**原因**：MapStruct默认按字段名匹配

**解决**：使用@Mapping注解指定映射关系

### 3. 嵌套对象映射为null

**原因**：没有配置嵌套对象的Converter

**解决**：使用uses属性指定嵌套对象的Converter

```java
@Mapper(componentModel = "spring", uses = DepartmentConverter.class)
public interface UserConverter {
    UserResponse toResponse(User user);
}
```

---

## Converter示例

```java
@Mapper(componentModel = "spring")
public interface UserConverter {

    // Entity → Response
    @Mapping(target = "statusText", source = "status", qualifiedByName = "formatStatus")
    UserResponse toResponse(User user);

    // Request → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    User toEntity(UserCreateRequest request);

    // UpdateRequest → Entity (更新现有对象)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void updateEntity(UserUpdateRequest request, @MappingTarget User user);

    // List映射
    List<UserResponse> toResponseList(List<User> users);

    // Page映射
    default Page<UserResponse> toResponsePage(Page<User> page) {
        List<UserResponse> responses = toResponseList(page.getRecords());
        return new Page<>(page.getCurrent(), page.getSize(), page.getTotal(), responses);
    }

    // 格式化状态
    @Named("formatStatus")
    default String formatStatus(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 1 -> "正常";
            case 0 -> "禁用";
            case 2 -> "锁定";
            default -> "未知";
        };
    }
}
```
