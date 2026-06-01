package {basePackage}.converter;

import {basePackage}.dto.request.{EntityName}CreateRequest;
import {basePackage}.dto.request.{EntityName}UpdateRequest;
import {basePackage}.dto.response.{EntityName}Response;
import {basePackage}.entity.{EntityName};
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * {EntityName}对象转换器
 *
 * @author {author}
 * @since {date}
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface {EntityName}Converter {

    /**
     * 创建请求DTO转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    {EntityName} toEntity({EntityName}CreateRequest request);

    /**
     * 更新请求DTO转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    {EntityName} toEntity({EntityName}UpdateRequest request);

    /**
     * 实体转响应DTO
     */
    @Mapping(target = "deleted", ignore = true)
    {EntityName}Response toResponse({EntityName} entity);

    /**
     * 实体列表转响应DTO列表
     */
    List<{EntityName}Response> toResponseList(List<{EntityName}> entities);

    /**
     * 更新实体（忽略null值）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity({EntityName}UpdateRequest request, @MappingTarget {EntityName} entity);
}
