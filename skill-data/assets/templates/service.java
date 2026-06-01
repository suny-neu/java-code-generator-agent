package {basePackage}.service;

import {basePackage}.dto.request.{EntityName}CreateRequest;
import {basePackage}.dto.request.{EntityName}UpdateRequest;
import {basePackage}.dto.response.{EntityName}Response;
import {basePackage}.wrapper.PageResult;

/**
 * {EntityName} Service接口
 *
 * @author {author}
 * @since {date}
 */
public interface {EntityName}Service {

    /**
     * 创建{EntityName}
     *
     * @param request 创建请求
     * @return {EntityName}ID
     */
    Long create{EntityName}({EntityName}CreateRequest request);

    /**
     * 根据ID查询{EntityName}
     *
     * @param id {EntityName}ID
     * @return {EntityName}响应
     */
    {EntityName}Response get{EntityName}ById(Long id);

    /**
     * 更新{EntityName}
     *
     * @param id {EntityName}ID
     * @param request 更新请求
     */
    void update{EntityName}(Long id, {EntityName}UpdateRequest request);

    /**
     * 删除{EntityName}
     *
     * @param id {EntityName}ID
     */
    void delete{EntityName}(Long id);

    /**
     * 分页查询{EntityName}列表
     *
     * @param current 页码
     * @param size 每页条数
     * @return 分页结果
     */
    PageResult<{EntityName}Response> list{EntityName}s(Integer current, Integer size);
}
