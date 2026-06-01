package {basePackage}.wrapper;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应结果
 *
 * @author {author}
 * @since {date}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页响应结果")
public class PageResult<T> {

    @Schema(description = "当前页", example = "1")
    private Long current;

    @Schema(description = "每页条数", example = "10")
    private Long size;

    @Schema(description = "总记录数", example = "100")
    private Long total;

    @Schema(description = "数据列表")
    private List<T> records;

    @Schema(description = "总页数", example = "10")
    public Long getPages() {
        if (size == null || size == 0) {
            return 0L;
        }
        return (total + size - 1) / size;
    }
}
