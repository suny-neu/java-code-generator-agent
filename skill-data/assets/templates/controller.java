package {basePackage}.controller;

import {basePackage}.dto.request.{EntityName}CreateRequest;
import {basePackage}.dto.request.{EntityName}UpdateRequest;
import {basePackage}.dto.response.{EntityName}Response;
import {basePackage}.service.{EntityName}Service;
import {basePackage}.wrapper.PageResult;
import {basePackage}.wrapper.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * {EntityName} Controller
 *
 * @author {author}
 * @since {date}
 */
@RestController
@RequestMapping("/api/v1/{moduleName}")
@Tag(name = "{EntityName}管理", description = "{EntityName}相关接口")
@RequiredArgsConstructor
public class {EntityName}Controller {

    private final {EntityName}Service {entityNameCamel}Service;

    @PostMapping
    @Operation(summary = "创建{EntityName}", description = "创建新的{EntityName}")
    public Result<{EntityName}Response> create{EntityName}(@Valid @RequestBody {EntityName}CreateRequest request) {
        Long id = {entityNameCamel}Service.create{EntityName}(request);
        return Result.success({entityNameCamel}Service.get{EntityName}ById(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询{EntityName}", description = "根据ID查询{EntityName}详情")
    public Result<{EntityName}Response> get{EntityName}ById(
            @Parameter(description = "{EntityName}ID", required = true)
            @PathVariable Long id) {
        return Result.success({entityNameCamel}Service.get{EntityName}ById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新{EntityName}", description = "更新{EntityName}信息")
    public Result<{EntityName}Response> update{EntityName}(
            @Parameter(description = "{EntityName}ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody {EntityName}UpdateRequest request) {
        {entityNameCamel}Service.update{EntityName}(id, request);
        return Result.success({entityNameCamel}Service.get{EntityName}ById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除{EntityName}", description = "删除{EntityName}")
    public Result<Void> delete{EntityName}(
            @Parameter(description = "{EntityName}ID", required = true)
            @PathVariable Long id) {
        {entityNameCamel}Service.delete{EntityName}(id);
        return Result.success();
    }

    @GetMapping
    @Operation(summary = "查询{EntityName}列表", description = "分页查询{EntityName}列表")
    public Result<PageResult<{EntityName}Response>> list{EntityName}s(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success({entityNameCamel}Service.list{EntityName}s(current, size));
    }
}
