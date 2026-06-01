package {basePackage}.exception;

/**
 * 资源不存在异常
 *
 * @author {author}
 * @since {date}
 */
public class NotFoundException extends BusinessException {

    public NotFoundException(String resource) {
        super(ErrorCode.NOT_FOUND, resource + "不存在");
    }

    public NotFoundException(String resource, Long id) {
        super(ErrorCode.NOT_FOUND, resource + "[id=" + id + "]不存在");
    }
}
