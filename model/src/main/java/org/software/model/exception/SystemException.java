package org.software.model.exception;


import lombok.Getter;
import org.software.model.constants.HttpCodeEnum;

@Getter
public class SystemException extends Exception{
    Integer code = HttpCodeEnum.SYSTEM_ERROR.getCode();
    String message;

    public SystemException(String msg) {
        this.message = msg;
    }
}
