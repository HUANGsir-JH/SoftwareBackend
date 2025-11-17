package org.software.model.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.software.model.constants.HttpCodeEnum;

@Getter
@AllArgsConstructor
public class SystemException extends Exception{
    Integer code;
    String message;

    public SystemException(HttpCodeEnum httpCodeEnum) {
        this.code = httpCodeEnum.getCode();
        this.message = httpCodeEnum.getMsg();
    }

    public SystemException(String msg){
        this.code = HttpCodeEnum.SYSTEM_ERROR.getCode();
        this.message = msg;
    }
    // 新增
    public SystemException(HttpCodeEnum httpCodeEnum, Throwable cause) {
        super(cause);
        this.code = httpCodeEnum.getCode();
        this.message = httpCodeEnum.getMsg();
    }

    // 新增
    public SystemException(Integer code, String message, Throwable cause) {
        super(cause);
        this.code = code;
        this.message = message;
    }
}
