package {basePackage}.controller;

import {basePackage}.dto.request.{EntityName}CreateRequest;
import {basePackage}.dto.request.{EntityName}UpdateRequest;
import {basePackage}.dto.response.{EntityName}Response;
import {basePackage}.service.{EntityName}Service;
import {basePackage}.wrapper.Result;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {EntityName}Controller单元测试
 * 测试方式：直接实例化Controller + Mock依赖（不使用MockMvc，不启动Spring容器）
 *
 * @author {author}
 * @since {date}
 */
@RunWith(MockitoJUnitRunner.class)
public class {EntityName}ControllerTest {

    @Mock
    private {EntityName}Service {entityNameCamel}Service;

    @InjectMocks
    private {EntityName}Controller {entityNameCamel}Controller;

    private {EntityName}Response mockResponse;
    private {EntityName}CreateRequest createRequest;
    private {EntityName}UpdateRequest updateRequest;

    @Before
    public void setUp() {
        // 初始化测试数据
        mockResponse = new {EntityName}Response();
        mockResponse.setId(1L);
        // TODO: 设置更多测试数据

        createRequest = new {EntityName}CreateRequest();
        // TODO: 设置创建请求数据

        updateRequest = new {EntityName}UpdateRequest();
        updateRequest.setId(1L);
        // TODO: 设置更新请求数据
    }

    // ==================== 创建测试 ====================

    @Test
    public void testCreate{EntityName}_Success() {
        // Given
        when({entityNameCamel}Service.create{EntityName}(any({EntityName}CreateRequest.class)))
            .thenReturn(1L);

        // When
        Result<Long> result = {entityNameCamel}Controller.create{EntityName}(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(Long.valueOf(1L), result.getData());
        verify({entityNameCamel}Service, times(1)).create{EntityName}(any({EntityName}CreateRequest.class));
    }

    @Test(expected = RuntimeException.class)
    public void testCreate{EntityName}_ServiceException() {
        // Given
        when({entityNameCamel}Service.create{EntityName}(any({EntityName}CreateRequest.class)))
            .thenThrow(new RuntimeException("数据库异常"));

        // When
        {entityNameCamel}Controller.create{EntityName}(createRequest);
    }

    // ==================== 查询测试 ====================

    @Test
    public void testGet{EntityName}ById_Success() {
        // Given
        when({entityNameCamel}Service.get{EntityName}ById(eq(1L))).thenReturn(mockResponse);

        // When
        Result<{EntityName}Response> result = {entityNameCamel}Controller.get{EntityName}ById(1L);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(Long.valueOf(1L), result.getData().getId());
        verify({entityNameCamel}Service, times(1)).get{EntityName}ById(eq(1L));
    }

    @Test
    public void testGet{EntityName}ById_NotFound() {
        // Given
        when({entityNameCamel}Service.get{EntityName}ById(eq(999L))).thenReturn(null);

        // When
        Result<{EntityName}Response> result = {entityNameCamel}Controller.get{EntityName}ById(999L);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify({entityNameCamel}Service, times(1)).get{EntityName}ById(eq(999L));
    }

    @Test
    public void testGet{EntityName}Page_Success() {
        // Given
        when({entityNameCamel}Service.get{EntityName}Page(eq(1L), eq(10L)))
            .thenReturn(null); // 返回IPage对象

        // When
        Result<?> result = {entityNameCamel}Controller.get{EntityName}Page(1L, 10L);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify({entityNameCamel}Service, times(1)).get{EntityName}Page(eq(1L), eq(10L));
    }

    @Test
    public void testGet{EntityName}Page_EmptyData() {
        // Given
        when({entityNameCamel}Service.get{EntityName}Page(eq(1L), eq(10L)))
            .thenReturn(null);

        // When
        Result<?> result = {entityNameCamel}Controller.get{EntityName}Page(1L, 10L);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify({entityNameCamel}Service, times(1)).get{EntityName}Page(eq(1L), eq(10L));
    }

    // ==================== 更新测试 ====================

    @Test
    public void testUpdate{EntityName}_Success() {
        // Given
        doNothing().when({entityNameCamel}Service).update{EntityName}(any({EntityName}UpdateRequest.class));

        // When
        Result<Void> result = {entityNameCamel}Controller.update{EntityName}(1L, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify({entityNameCamel}Service, times(1)).update{EntityName}(any({EntityName}UpdateRequest.class));
    }

    @Test(expected = RuntimeException.class)
    public void testUpdate{EntityName}_ServiceException() {
        // Given
        doThrow(new RuntimeException("更新失败"))
            .when({entityNameCamel}Service).update{EntityName}(any({EntityName}UpdateRequest.class));

        // When
        {entityNameCamel}Controller.update{EntityName}(1L, updateRequest);
    }

    // ==================== 删除测试 ====================

    @Test
    public void testDelete{EntityName}_Success() {
        // Given
        doNothing().when({entityNameCamel}Service).delete{EntityName}(eq(1L));

        // When
        Result<Void> result = {entityNameCamel}Controller.delete{EntityName}(1L);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify({entityNameCamel}Service, times(1)).delete{EntityName}(eq(1L));
    }

    @Test
    public void testDelete{EntityName}_NotFound() {
        // Given
        doNothing().when({entityNameCamel}Service).delete{EntityName}(eq(999L));

        // When
        Result<Void> result = {entityNameCamel}Controller.delete{EntityName}(999L);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify({entityNameCamel}Service, times(1)).delete{EntityName}(eq(999L));
    }

    // ==================== 批量操作测试 ====================

    @Test
    public void testBatchDelete{EntityName}_Success() {
        // Given
        doNothing().when({entityNameCamel}Service).batchDelete{EntityName}(anyList());

        // When
        Result<Void> result = {entityNameCamel}Controller.batchDelete{EntityName}(Arrays.asList(1L, 2L, 3L));

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify({entityNameCamel}Service, times(1)).batchDelete{EntityName}(anyList());
    }

    @Test
    public void testBatchDelete{EntityName}_EmptyList() {
        // Given
        doNothing().when({entityNameCamel}Service).batchDelete{EntityName}(anyList());

        // When
        Result<Void> result = {entityNameCamel}Controller.batchDelete{EntityName}(Collections.emptyList());

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify({entityNameCamel}Service, times(1)).batchDelete{EntityName}(anyList());
    }
}
