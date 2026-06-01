# {EntityName}测试数据

## 测试数据说明
本文档包含{EntityName}模块的测试数据SQL脚本。

## 表结构

```sql
-- {EntityName}表
CREATE TABLE IF NOT EXISTS `{tableName}` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  -- TODO: 添加字段定义
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='{EntityName}表';
```

## 测试数据

```sql
-- 清空测试数据
DELETE FROM `{tableName}`;

-- 插入测试数据
INSERT INTO `{tableName}` (`id`, {fields}, `create_time`, `update_time`) VALUES
(1, {values1}, '2024-01-01 12:00:00', '2024-01-01 12:00:00'),
(2, {values2}, '2024-01-02 12:00:00', '2024-01-02 12:00:00'),
(3, {values3}, '2024-01-03 12:00:00', '2024-01-03 12:00:00');
```

## 测试场景

### 正常场景
- 场景1: 创建{EntityName}
- 场景2: 查询{EntityName}
- 场景3: 更新{EntityName}
- 场景4: 删除{EntityName}
- 场景5: 分页查询

### 边界场景
- 场景1: 查询不存在的{EntityName}
- 场景2: 创建重复{EntityName}
- 场景3: 更新不存在的{EntityName}
- 场景4: 删除不存在的{EntityName}
