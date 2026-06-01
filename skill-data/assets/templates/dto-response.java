package {basePackage}.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * {EntityName}响应DTO
 *
 * @author {author}
 * @since {date}
 */
@Data
@Schema(description = "{EntityName}响应")
public class {EntityName}Response {

    @Schema(description = "主键ID", example = "1")
    private Long id;

    // TODO: 添加响应字段
    // 示例:
    // @Schema(description = "名称", example = "xxx")
    // private String name;

    @Schema(description = "创建时间", example = "2024-01-01T12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2024-01-01T12:00:00")
    private LocalDateTime updateTime;
}
