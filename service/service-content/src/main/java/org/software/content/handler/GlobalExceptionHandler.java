package org.software.content.handler;

import lombok.extern.slf4j.Slf4j;
import org.software.model.Response;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.exception.SystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j

// TODO: 移动到网关
public class GlobalExceptionHandler {

    @ExceptionHandler(SystemException.class)
    public Response systemExceptionHandler(SystemException e){
        //打印异常信息
        log.error(e.getMessage());
        //从异常对象中获取提示信息封装返回
        return Response.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Response exceptionHandler(Exception e){
        e.printStackTrace();
        //打印异常信息
        log.error("服务器异常");
        //从异常对象中获取提示信息封装返回
        return Response.error(HttpCodeEnum.SYSTEM_ERROR.getCode(), HttpCodeEnum.SYSTEM_ERROR.getMsg());
    }
}
