package {basePackage}.repository;

import {basePackage}.entity.{EntityName};
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * {EntityName}Mapper单元测试
 * 测试方式：执行真实数据库操作验证
 *
 * @author {author}
 * @since {date}
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class {EntityName}MapperTest {

    @Autowired
    private {EntityName}Mapper {entityNameCamel}Mapper;

    private {EntityName} testEntity;

    @Before
    public void setUp() {
        // 准备测试数据
        testEntity = new {EntityName}();
        // TODO: 设置必填字段
        // testEntity.setField1("test_value");
    }

    @Test
    @Transactional
    public void testInsert() {
        // When
        int result = {entityNameCamel}Mapper.insert(testEntity);

        // Then
        assertTrue(result > 0);
        assertNotNull(testEntity.getId());
    }

    @Test
    @Transactional
    public void testInsertWithAutoFill() {
        // When
        {entityNameCamel}Mapper.insert(testEntity);

        // Then
        assertNotNull(testEntity.getCreateTime());
    }

    @Test
    @Transactional
    public void testSelectById() {
        // Given
        {entityNameCamel}Mapper.insert(testEntity);
        Long id = testEntity.getId();

        // When
        {EntityName} result = {entityNameCamel}Mapper.selectById(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    public void testSelectByIdNotFound() {
        // When
        {EntityName} result = {entityNameCamel}Mapper.selectById(999999L);

        // Then
        assertNull(result);
    }

    @Test
    @Transactional
    public void testSelectList() {
        // Given
        {entityNameCamel}Mapper.insert(testEntity);

        // When - 使用LambdaQueryWrapper构建查询条件
        LambdaQueryWrapper<{EntityName}> wrapper = new LambdaQueryWrapper<>();
        // TODO: 添加查询条件
        // wrapper.eq({EntityName}::getField, "value");

        List<{EntityName}> result = {entityNameCamel}Mapper.selectList(wrapper);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Transactional
    public void testSelectListAll() {
        // Given
        {entityNameCamel}Mapper.insert(testEntity);

        // When
        List<{EntityName}> result = {entityNameCamel}Mapper.selectList(null);

        // Then
        assertNotNull(result);
        assertTrue(result.size() >= 1);
    }

    @Test
    @Transactional
    public void testSelectOne() {
        // Given
        {entityNameCamel}Mapper.insert(testEntity);

        LambdaQueryWrapper<{EntityName}> wrapper = new LambdaQueryWrapper<>();
        // TODO: 添加唯一查询条件
        // wrapper.eq({EntityName}::getUniqueField, "unique_value");

        // When
        {EntityName} result = {entityNameCamel}Mapper.selectOne(wrapper);

        // Then
        assertNotNull(result);
    }

    @Test
    @Transactional
    public void testSelectCount() {
        // Given
        {entityNameCamel}Mapper.insert(testEntity);

        LambdaQueryWrapper<{EntityName}> wrapper = new LambdaQueryWrapper<>();
        // TODO: 添加统计条件

        // When
        Long count = {entityNameCamel}Mapper.selectCount(wrapper);

        // Then
        assertNotNull(count);
        assertTrue(count >= 1);
    }

    @Test
    @Transactional
    public void testUpdateById() {
        // Given
        {entityNameCamel}Mapper.insert(testEntity);
        Long id = testEntity.getId();

        // When - 修改字段
        // testEntity.setField1("updated_value");
        int result = {entityNameCamel}Mapper.updateById(testEntity);

        // Then
        assertTrue(result > 0);

        // 验证更新后的值
        {EntityName} updated = {entityNameCamel}Mapper.selectById(id);
        // assertEquals("updated_value", updated.getField1());
    }

    @Test
    @Transactional
    public void testUpdateWithAutoFill() {
        // Given
        {entityNameCamel}Mapper.insert(testEntity);
        LocalDateTime oldUpdateTime = testEntity.getUpdateTime();

        // When
        testEntity.setId(testEntity.getId());
        {entityNameCamel}Mapper.updateById(testEntity);

        // Then
        {EntityName} updated = {entityNameCamel}Mapper.selectById(testEntity.getId());
        // assertTrue(updated.getUpdateTime().isAfter(oldUpdateTime));
    }

    @Test
    @Transactional
    public void testUpdate() {
        // Given
        {entityNameCamel}Mapper.insert(testEntity);

        {EntityName} updateEntity = new {EntityName}();
        // updateEntity.setField1("batch_update_value");

        LambdaQueryWrapper<{EntityName}> wrapper = new LambdaQueryWrapper<>();
        // TODO: 添加更新条件
        // wrapper.eq({EntityName}::getField, "old_value");

        // When
        int result = {entityNameCamel}Mapper.update(updateEntity, wrapper);

        // Then
        assertTrue(result >= 0);
    }

    @Test
    @Transactional
    public void testDeleteById() {
        // Given
        {entityNameCamel}Mapper.insert(testEntity);
        Long id = testEntity.getId();

        // When
        int result = {entityNameCamel}Mapper.deleteById(id);

        // Then
        assertTrue(result > 0);

        // 验证记录已被删除
        {EntityName} deleted = {entityNameCamel}Mapper.selectById(id);
        assertNull(deleted);
    }

    @Test
    @Transactional
    public void testDelete() {
        // Given
        {entityNameCamel}Mapper.insert(testEntity);

        LambdaQueryWrapper<{EntityName}> wrapper = new LambdaQueryWrapper<>();
        // TODO: 添加删除条件
        // wrapper.eq({EntityName}::getField, "value");

        // When
        int result = {entityNameCamel}Mapper.delete(wrapper);

        // Then
        assertTrue(result >= 0);
    }

    @Test
    @Transactional
    public void testSelectPage() {
        // Given - 插入多条数据用于分页
        for (int i = 0; i < 5; i++) {
            {EntityName} entity = new {EntityName}();
            // entity.setField1("test_" + i);
            {entityNameCamel}Mapper.insert(entity);
        }

        // When
        Page<{EntityName}> page = new Page<>(1, 2);
        LambdaQueryWrapper<{EntityName}> wrapper = new LambdaQueryWrapper<>();
        IPage<{EntityName}> result = {entityNameCamel}Mapper.selectPage(page, wrapper);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getSize());
        assertTrue(result.getTotal() >= 5);
        assertTrue(result.getRecords().size() <= 2);
    }

    @Test
    @Transactional
    public void testSelectBatchIds() {
        // Given
        {entityNameCamel}Mapper.insert(testEntity);
        Long id1 = testEntity.getId();

        {EntityName} entity2 = new {EntityName}();
        {entityNameCamel}Mapper.insert(entity2);
        Long id2 = entity2.getId();

        // When
        List<{EntityName}> result = {entityNameCamel}Mapper.selectBatchIds(Arrays.asList(id1, id2));

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @Transactional
    public void testInsertBatch() {
        // Given - 准备批量数据
        List<{EntityName}> entities = Arrays.asList(
            new {EntityName}(),
            new {EntityName}(),
            new {EntityName}()
        );
        // TODO: 设置每个实体的字段

        // When - 如果Mapper自定义了insertBatch方法
        // int result = {entityNameCamel}Mapper.insertBatch(entities);

        // Then
        // assertTrue(result > 0);
    }

    @Test
    @Transactional
    public void testSelectMaps() {
        // Given
        {entityNameCamel}Mapper.insert(testEntity);

        LambdaQueryWrapper<{EntityName}> wrapper = new LambdaQueryWrapper<>();
        // TODO: 添加查询条件

        // When
        List<java.util.Map<String, Object>> result = {entityNameCamel}Mapper.selectMaps(wrapper);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Transactional
    public void testSelectObjs() {
        // Given
        {entityNameCamel}Mapper.insert(testEntity);

        LambdaQueryWrapper<{EntityName}> wrapper = new LambdaQueryWrapper<>();
        // TODO: 添加查询条件，选择特定列

        // When
        List<Object> result = {entityNameCamel}Mapper.selectObjs(wrapper);

        // Then
        assertNotNull(result);
    }

    @Test
    @Transactional
    public void testExists() {
        // Given
        {entityNameCamel}Mapper.insert(testEntity);

        LambdaQueryWrapper<{EntityName}> wrapper = new LambdaQueryWrapper<>();
        // TODO: 添加查询条件
        // wrapper.eq({EntityName}::getField, "value");

        // When
        boolean exists = {entityNameCamel}Mapper.selectCount(wrapper) > 0;

        // Then
        assertTrue(exists);
    }

    @Test
    @Transactional
    public void testOptimisticLock() {
        // Given - 插入记录
        {entityNameCamel}Mapper.insert(testEntity);
        Integer oldVersion = testEntity.getVersion();

        // When - 更新记录
        testEntity.setId(testEntity.getId());
        {entityNameCamel}Mapper.updateById(testEntity);

        // Then
        {EntityName} updated = {entityNameCamel}Mapper.selectById(testEntity.getId());
        // assertEquals(oldVersion + 1, updated.getVersion());
    }
}
