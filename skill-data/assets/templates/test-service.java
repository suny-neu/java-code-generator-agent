package {basePackage}.service;

import {basePackage}.converter.{EntityName}Converter;
import {basePackage}.dto.request.{EntityName}CreateRequest;
import {basePackage}.dto.request.{EntityName}UpdateRequest;
import {basePackage}.dto.response.{EntityName}Response;
import {basePackage}.entity.{EntityName};
import {basePackage}.exception.BusinessException;
import {basePackage}.enums.ErrorCode;
import {basePackage}.repository.{EntityName}Mapper;
import {basePackage}.service.impl.{EntityName}ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {EntityName}Service单元测试
 * 测试方式：直接实例化Service + Mock依赖（不启动Spring容器）
 *
 * @author {author}
 * @since {date}
 */
@RunWith(MockitoJUnitRunner.class)
public class {EntityName}ServiceTest {

    @Mock
    private {EntityName}Mapper {entityNameCamel}Mapper;

    @Mock
    private {EntityName}Converter {entityNameCamel}Converter;

    @InjectMocks
    private {EntityName}ServiceImpl {entityNameCamel}Service;

    private {EntityName} mock{EntityName};
    private {EntityName}CreateRequest createRequest;
    private {EntityName}UpdateRequest updateRequest;
    private {EntityName}Response mockResponse;

    @Before
    public void setUp() {
        // 初始化测试数据
        mock{EntityName} = new {EntityName}();
        mock{EntityName}.setId(1L);
        // TODO: 设置更多测试数据

        createRequest = new {EntityName}CreateRequest();
        // TODO: 设置创建请求数据

        updateRequest = new {EntityName}UpdateRequest();
        updateRequest.setId(1L);
        // TODO: 设置更新请求数据

        mockResponse = new {EntityName}Response();
        mockResponse.setId(1L);
        // TODO: 设置响应数据
    }

    // ==================== 创建测试 ====================

    @Test
    public void testCreate{EntityName}_Success() {
        // Given
        when({entityNameCamel}Converter.toEntity(any({EntityName}CreateRequest.class)))
            .thenReturn(mock{EntityName});
        when({entityNameCamel}Mapper.insert(any({EntityName}.class)))
            .thenReturn(1);

        // When
        Long id = {entityNameCamel}Service.create{EntityName}(createRequest);

        // Then
        assertNotNull(id);
        assertEquals(Long.valueOf(1L), id);
        verify({entityNameCamel}Converter, times(1)).toEntity(any({EntityName}CreateRequest.class));
        verify({entityNameCamel}Mapper, times(1)).insert(any({EntityName}.class));
    }

    @Test
    public void testCreate{EntityName}_InsertFailed() {
        // Given
        when({entityNameCamel}Converter.toEntity(any({EntityName}CreateRequest.class)))
            .thenReturn(mock{EntityName});
        when({entityNameCamel}Mapper.insert(any({EntityName}.class)))
            .thenReturn(0);

        // When
        Long id = {entityNameCamel}Service.create{EntityName}(createRequest);
        // 根据实际业务逻辑，可能返回null或抛出异常
        verify({entityNameCamel}Mapper, times(1)).insert(any({EntityName}.class));
    }

    @Test(expected = RuntimeException.class)
    public void testCreate{EntityName}_DatabaseException() {
        // Given
        when({entityNameCamel}Converter.toEntity(any({EntityName}CreateRequest.class)))
            .thenReturn(mock{EntityName});
        when({entityNameCamel}Mapper.insert(any({EntityName}.class)))
            .thenThrow(new RuntimeException("数据库连接失败"));

        // When
        {entityNameCamel}Service.create{EntityName}(createRequest);
    }

    // ==================== 查询测试 ====================

    @Test
    public void testGet{EntityName}ById_Success() {
        // Given
        when({entityNameCamel}Mapper.selectById(eq(1L)))
            .thenReturn(mock{EntityName});
        when({entityNameCamel}Converter.toResponse(any({EntityName}.class)))
            .thenReturn(mockResponse);

        // When
        {EntityName}Response response = {entityNameCamel}Service.get{EntityName}ById(1L);

        // Then
        assertNotNull(response);
        assertEquals(Long.valueOf(1L), response.getId());
        verify({entityNameCamel}Mapper, times(1)).selectById(eq(1L));
        verify({entityNameCamel}Converter, times(1)).toResponse(any({EntityName}.class));
    }

    @Test
    public void testGet{EntityName}ById_NotFound() {
        // Given
        when({entityNameCamel}Mapper.selectById(eq(999L)))
            .thenReturn(null);

        // When & Then
        try {
            {entityNameCamel}Service.get{EntityName}ById(999L);
            fail("应该抛出BusinessException");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.NOT_FOUND.getCode(), e.getCode());
        }

        verify({entityNameCamel}Mapper, times(1)).selectById(eq(999L));
        verify({entityNameCamel}Converter, never()).toResponse(any());
    }

    @Test(expected = BusinessException.class)
    public void testGet{EntityName}ById_NullResponse() {
        // Given
        when({entityNameCamel}Mapper.selectById(eq(1L)))
            .thenReturn(null);

        // When
        {entityNameCamel}Service.get{EntityName}ById(1L);
    }

    @Test
    public void testGet{EntityName}Page_Success() {
        // Given
        IPage<{EntityName}> mockPage = mock(IPage.class);
        when(mockPage.getRecords()).thenReturn(Arrays.asList(mock{EntityName}));
        when({entityNameCamel}Mapper.selectPage(any(), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);
        when({entityNameCamel}Converter.toResponseList(anyList()))
            .thenReturn(Arrays.asList(mockResponse));

        // When
        IPage<{EntityName}Response> result = {entityNameCamel}Service.get{EntityName}Page(1L, 10L);

        // Then
        assertNotNull(result);
        verify({entityNameCamel}Mapper, times(1)).selectPage(any(), any(LambdaQueryWrapper.class));
        verify({entityNameCamel}Converter, times(1)).toResponseList(anyList());
    }

    @Test
    public void testGet{EntityName}Page_EmptyData() {
        // Given
        IPage<{EntityName}> mockPage = mock(IPage.class);
        when(mockPage.getRecords()).thenReturn(Collections.emptyList());
        when({entityNameCamel}Mapper.selectPage(any(), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);
        when({entityNameCamel}Converter.toResponseList(anyList()))
            .thenReturn(Collections.emptyList());

        // When
        IPage<{EntityName}Response> result = {entityNameCamel}Service.get{EntityName}Page(1L, 10L);

        // Then
        assertNotNull(result);
        verify({entityNameCamel}Mapper, times(1)).selectPage(any(), any(LambdaQueryWrapper.class));
    }

    @Test
    public void testGetAll{EntityName}s_Success() {
        // Given
        List<{EntityName}> mockList = Arrays.asList(mock{EntityName});
        when({entityNameCamel}Mapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(mockList);
        when({entityNameCamel}Converter.toResponseList(anyList()))
            .thenReturn(Arrays.asList(mockResponse));

        // When
        List<{EntityName}Response> result = {entityNameCamel}Service.getAll{EntityName}s();

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify({entityNameCamel}Mapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    public void testGetAll{EntityName}s_EmptyList() {
        // Given
        when({entityNameCamel}Mapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList());
        when({entityNameCamel}Converter.toResponseList(anyList()))
            .thenReturn(Collections.emptyList());

        // When
        List<{EntityName}Response> result = {entityNameCamel}Service.getAll{EntityName}s();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== 更新测试 ====================

    @Test
    public void testUpdate{EntityName}_Success() {
        // Given
        when({entityNameCamel}Mapper.selectById(eq(1L)))
            .thenReturn(mock{EntityName});
        when({entityNameCamel}Converter.toEntity(any({EntityName}UpdateRequest.class)))
            .thenReturn(mock{EntityName});
        when({entityNameCamel}Mapper.updateById(any({EntityName}.class)))
            .thenReturn(1);

        // When
        {entityNameCamel}Service.update{EntityName}(1L, updateRequest);

        // Then
        verify({entityNameCamel}Mapper, times(1)).selectById(eq(1L));
        verify({entityNameCamel}Converter, times(1)).toEntity(any({EntityName}UpdateRequest.class));
        verify({entityNameCamel}Mapper, times(1)).updateById(any({EntityName}.class));
    }

    @Test
    public void testUpdate{EntityName}_NotFound() {
        // Given
        when({entityNameCamel}Mapper.selectById(eq(999L)))
            .thenReturn(null);

        // When & Then
        try {
            {entityNameCamel}Service.update{EntityName}(999L, updateRequest);
            fail("应该抛出BusinessException");
        } catch (BusinessException e) {
            // 预期异常
        }

        verify({entityNameCamel}Mapper, times(1)).selectById(eq(999L));
        verify({entityNameCamel}Converter, never()).toEntity(any());
        verify({entityNameCamel}Mapper, never()).updateById(any());
    }

    @Test
    public void testUpdate{EntityName}_UpdateFailed() {
        // Given
        when({entityNameCamel}Mapper.selectById(eq(1L)))
            .thenReturn(mock{EntityName});
        when({entityNameCamel}Converter.toEntity(any({EntityName}UpdateRequest.class)))
            .thenReturn(mock{EntityName});
        when({entityNameCamel}Mapper.updateById(any({EntityName}.class)))
            .thenReturn(0);

        // When
        {entityNameCamel}Service.update{EntityName}(1L, updateRequest);

        // Then
        verify({entityNameCamel}Mapper, times(1)).updateById(any({EntityName}.class));
    }

    // ==================== 删除测试 ====================

    @Test
    public void testDelete{EntityName}_Success() {
        // Given
        when({entityNameCamel}Mapper.selectById(eq(1L)))
            .thenReturn(mock{EntityName});
        when({entityNameCamel}Mapper.deleteById(eq(1L)))
            .thenReturn(1);

        // When
        {entityNameCamel}Service.delete{EntityName}(1L);

        // Then
        verify({entityNameCamel}Mapper, times(1)).selectById(eq(1L));
        verify({entityNameCamel}Mapper, times(1)).deleteById(eq(1L));
    }

    @Test
    public void testDelete{EntityName}_NotFound() {
        // Given
        when({entityNameCamel}Mapper.selectById(eq(999L)))
            .thenReturn(null);

        // When & Then
        try {
            {entityNameCamel}Service.delete{EntityName}(999L);
            fail("应该抛出BusinessException");
        } catch (BusinessException e) {
            // 预期异常
        }

        verify({entityNameCamel}Mapper, times(1)).selectById(eq(999L));
        verify({entityNameCamel}Mapper, never()).deleteById(any());
    }

    @Test
    public void testBatchDelete{EntityName}_Success() {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        when({entityNameCamel}Mapper.selectBatchIds(anyList()))
            .thenReturn(Arrays.asList(mock{EntityName}, mock{EntityName}, mock{EntityName}));
        when({entityNameCamel}Mapper.deleteBatchIds(anyList()))
            .thenReturn(3);

        // When
        {entityNameCamel}Service.batchDelete{EntityName}(ids);

        // Then
        verify({entityNameCamel}Mapper, times(1)).selectBatchIds(anyList());
        verify({entityNameCamel}Mapper, times(1)).deleteBatchIds(anyList());
    }

    @Test
    public void testBatchDelete{EntityName}_EmptyList() {
        // Given
        when({entityNameCamel}Mapper.selectBatchIds(anyList()))
            .thenReturn(Collections.emptyList());

        // When
        {entityNameCamel}Service.batchDelete{EntityName}(Collections.emptyList());

        // Then
        verify({entityNameCamel}Mapper, times(1)).selectBatchIds(anyList());
        verify({entityNameCamel}Mapper, never()).deleteBatchIds(anyList());
    }
}
