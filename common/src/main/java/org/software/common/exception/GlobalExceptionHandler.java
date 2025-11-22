package org.software.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.software.model.Response;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.exception.BusinessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Response businessExceptionHandler(BusinessException e){
        log.error("业务异常：{}", e.getMessage());
        return Response.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Response exceptionHandler(Exception e, HttpServletRequest request){
        log.error("系统异常 | 请求URI={}", request.getRequestURI(), e);
        return Response.error(HttpCodeEnum.SYSTEM_ERROR.getCode(), HttpCodeEnum.SYSTEM_ERROR.getMsg());
    }
}
