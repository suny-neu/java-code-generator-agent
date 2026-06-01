package {basePackage}.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * {EntityName}创建请求DTO
 *
 * @author {author}
 * @since {date}
 */
@Data
@Schema(description = "{EntityName}创建请求")
public class {EntityName}CreateRequest {

    // TODO: 添加请求字段
    // 示例:
    // @Schema(description = "名称", example = "xxx", required = true)
    // @NotBlank(message = "名称不能为空")
    // @Size(max = 50, message = "名称长度不能超过50")
    // private String name;
}
