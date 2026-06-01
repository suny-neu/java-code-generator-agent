package {basePackage}.service.impl;

import {basePackage}.converter.{EntityName}Converter;
import {basePackage}.dto.request.{EntityName}CreateRequest;
import {basePackage}.dto.request.{EntityName}UpdateRequest;
import {basePackage}.dto.response.{EntityName}Response;
import {basePackage}.entity.{EntityName};
import {basePackage}.exception.BusinessException;
import {basePackage}.exception.NotFoundException;
import {basePackage}.repository.{EntityName}Mapper;
import {basePackage}.service.{EntityName}Service;
import {basePackage}.wrapper.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {EntityName} Service实现类
 *
 * @author {author}
 * @since {date}
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class {EntityName}ServiceImpl implements {EntityName}Service {

    private final {EntityName}Mapper {entityNameCamel}Mapper;
    private final {EntityName}Converter {entityNameCamel}Converter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create{EntityName}({EntityName}CreateRequest request) {
        log.info("创建{EntityName}, request={}", request);

        // 1. 业务校验
        // TODO: 添加业务校验逻辑

        // 2. 构建实体
        {EntityName} {entityNameCamel} = {entityNameCamel}Converter.toEntity(request);

        // 3. 持久化
        {entityNameCamel}Mapper.insert({entityNameCamel});

        log.info("{EntityName}创建成功, id={}", {entityNameCamel}.getId());
        return {entityNameCamel}.getId();
    }

    @Override
    public {EntityName}Response get{EntityName}ById(Long id) {
        {EntityName} {entityNameCamel} = {entityNameCamel}Mapper.selectById(id);
        if ({entityNameCamel} == null) {
            throw new NotFoundException("{EntityName}", id);
        }
        return {entityNameCamel}Converter.toResponse({entityNameCamel});
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update{EntityName}(Long id, {EntityName}UpdateRequest request) {
        log.info("更新{EntityName}, id={}, request={}", id, request);

        // 1. 查询实体
        {EntityName} existing{EntityName} = {entityNameCamel}Mapper.selectById(id);
        if (existing{EntityName} == null) {
            throw new NotFoundException("{EntityName}", id);
        }

        // 2. 业务校验
        // TODO: 添加业务校验逻辑

        // 3. 更新实体
        {EntityName} {entityNameCamel} = {entityNameCamel}Converter.toEntity(request);
        {entityNameCamel}.setId(id);
        {entityNameCamel}Mapper.updateById({entityNameCamel});

        log.info("{EntityName}更新成功, id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete{EntityName}(Long id) {
        log.info("删除{EntityName}, id={}", id);

        // 1. 查询实体
        {EntityName} {entityNameCamel} = {entityNameCamel}Mapper.selectById(id);
        if ({entityNameCamel} == null) {
            throw new NotFoundException("{EntityName}", id);
        }

        // 2. 业务校验
        // TODO: 添加删除前置校验

        // 3. 删除实体
        {entityNameCamel}Mapper.deleteById(id);

        log.info("{EntityName}删除成功, id={}", id);
    }

    @Override
    public PageResult<{EntityName}Response> list{EntityName}s(Integer current, Integer size) {
        Page<{EntityName}> page = new Page<>(current, size);
        LambdaQueryWrapper<{EntityName}> wrapper = new LambdaQueryWrapper<>();
        // TODO: 添加查询条件

        Page<{EntityName}> result = {entityNameCamel}Mapper.selectPage(page, wrapper);

        PageResult<{EntityName}Response> pageResult = new PageResult<>();
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setTotal(result.getTotal());
        pageResult.setRecords({entityNameCamel}Converter.toResponseList(result.getRecords()));

        return pageResult;
    }
}
