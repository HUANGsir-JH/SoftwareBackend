package org.software.model;


import lombok.Data;
import org.software.model.constants.HttpCodeEnum;

@Data
public class Response {

    private Integer code;
    private String msg;
    private Object data;

    public static Response success(){
        Response response = new Response();
        response.code = HttpCodeEnum.SUCCESS.getCode();
        response.msg = HttpCodeEnum.SUCCESS.getMsg();
        response.data = null;
        return response;
    }

    public static Response success(Object data) {
        Response response = new Response();
        response.code = HttpCodeEnum.SUCCESS.getCode();
        response.msg = HttpCodeEnum.SUCCESS.getMsg();
        response.data = data;
        return response;
    }

    public static Response error() {
        Response response = new Response();
        response.code = HttpCodeEnum.SYSTEM_ERROR.getCode();
        response.msg = HttpCodeEnum.SYSTEM_ERROR.getMsg();
        response.data = null;
        return response;
    }

    public static Response error(Integer code, String msg) {
        Response response = new Response();
        response.code = code;
        response.msg = msg;
        response.data = null;
        return response;
    }
}
