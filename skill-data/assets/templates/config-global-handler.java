package {basePackage}.config;

import {basePackage}.exception.BusinessException;
import {basePackage}.exception.NotFoundException;
import {basePackage}.wrapper.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author {author}
 * @since {date}
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HandlerMethod method) {
        log.warn("业务异常: method={}, code={}, message={}",
            method.getMethod().getName(), e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 资源不存在异常处理
     */
    @ExceptionHandler(NotFoundException.class)
    public Result<Void> handleNotFoundException(NotFoundException e) {
        log.warn("资源不存在: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String errors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", errors);
        return Result.error(ErrorCode.VALIDATION_ERROR.getCode(), errors);
    }

    /**
     * 其他未捕获异常处理
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HandlerMethod method) {
        log.error("系统异常: method={}", method.getMethod().getName(), e);
        return Result.error(ErrorCode.INTERNAL_ERROR.getCode(), "系统异常，请稍后重试");
    }
}
