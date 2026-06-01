package ${basePackage}.wrapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页响应结果包装器
 *
 * @author Code Generator
 * @since ${generateDate}
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value = "分页响应结果", description = "分页数据的统一返回格式")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "当前页")
    private Long current;

    @ApiModelProperty(value = "每页大小")
    private Long size;

    @ApiModelProperty(value = "总记录数")
    private Long total;

    @ApiModelProperty(value = "总页数")
    private Long pages;

    @ApiModelProperty(value = "数据列表")
    private java.util.List<T> records;

    public PageResult() {
    }

    public PageResult(Long current, Long size, Long total, java.util.List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.pages = (total + size - 1) / size;
        this.records = records;
    }

    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> of(Long current, Long size, Long total, java.util.List<T> records) {
        return new PageResult<>(current, size, total, records);
    }
}
